/*
 *  QuadLoaderBase.java
 *  WhirlyGlobeLib
 *
 *  Created by Steve Gifford on 3/22/19.
 *  Copyright 2011-2019 mousebird consulting
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mousebird.maply;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;
import java.util.HashSet;

/**
 * Base class for the quad loaders.
 * <br>
 * The image, frame, and data paging loaders all share much of the same functionality.
 */
public class QuadLoaderBase implements QuadSamplingLayer.ClientInterface
{
    protected WeakReference<QuadSamplingLayer> samplingLayer;

    protected QuadLoaderBase(BaseController inControl,SamplingParams params,int numFrames,Mode mode)
    {
        control = new WeakReference<BaseController>(inControl);
        initialise(params,numFrames,mode.ordinal());
    }

    /**
     *  Control how tiles are indexed, either from the lower left or the upper left.
     *
     *  If set, we'll use the OSM approach (also Google Maps) to y indexing.  That's that default and it's normally what you're run into.
     *
     *  Strictly speaking, TMS addressing (the standard) is flipped the other way.  So if your tile source looks odd, try setting this to false.
     *
     *  Default value is true.
     */
    public native void setFlipY(boolean newVal);

    /**
     *  Control how tiles are indexed, either from the lower left or the upper left.
     *
     *  If set, we'll use the OSM approach (also Google Maps) to y indexing.  That's that default and it's normally what you're run into.
     *
     *  Strictly speaking, TMS addressing (the standard) is flipped the other way.  So if your tile source looks odd, try setting this to false.
     *
     *  Default value is true.
     */
    public native boolean getFlipY();

    /**
     * Set for a lot of debugging output.
     */
    public native void setDebugMode(boolean debugMode);

    private WeakReference<BaseController> control;

    /**
     * Controller associated with this quad loader.
     * This is where you send geometry and such.
     */
    BaseController getController()
    {
        return control.get();
    }

    /**
     *  Calculate the bounding box for a single tile in geographic.
     *  <br>
     *  This is a utility method for calculating the extents of a given tile in geographic (e.g. lon/lat).
     *
     *  @param tileID The ID for the tile we're interested in.
     *
     *  @return The lower left and upper right corner of the tile in geographic coordinates.
     *          Returns null in case of error.
     */
    Mbr geoBoundsForTile(TileID tileID)
    {
        Mbr mbr = new Mbr();
        geoBoundsForTileNative(tileID.x,tileID.y,tileID.level,mbr.ll,mbr.ur);

        return mbr;
    }

    protected native void geoBoundsForTileNative(int x,int y,int level,Point2d ll,Point2d ur);

    /**
     *  Calculate the bounding box for a single tile in the local coordinate system.
     *
     *  This utility method calculates the bounding box for a tile in the coordinate system used for the layer.
     *
     *  @param tileID The ID for the tile we're interested in.
     *
     *  @return The lower left and upper right corner of the tile in local coordinates.
     */
    Mbr boundsForTile(TileID tileID)
    {
        Mbr mbr = new Mbr();
        boundsForTileNative(tileID.x,tileID.y,tileID.level,mbr.ll,mbr.ur);

        return mbr;
    }

    protected native void boundsForTileNative(int x,int y,int level,Point2d ll,Point2d ur);

    /**
     *  Return the center of the tile in display coordinates.
     *
     *  @param tileID The ID for the tile we're interested in.
     *
     *  @return Return the center in display space for the given tile.
     */
    Point3d displayCenterForTile(TileID tileID)
    {
        Point3d pt = new Point3d();
        displayCenterForTileNative(tileID.x,tileID.y,tileID.level,pt);
        return pt;
    }

    protected native void displayCenterForTileNative(int x,int y,int level,Point3d pt);

    protected TileFetcher tileFetcher = null;

    /**
     * Use a specific tile fetcher rather than the one shared by everyone else.
     */
    public void setTileFetcher(TileFetcher newFetcher) {
        tileFetcher = newFetcher;
    }

    protected LoaderInterpreter loadInterp = null;

    /**
     * Set the interpreter for the data coming back.  If you're just getting images, don't set this.
     */
    public void setLoaderInterpreter(LoaderInterpreter newInterp) {
        loadInterp = newInterp;
    }

    /**
     * Tile Info objects for individual frames.
     */
    TileInfoNew[] tileInfos = null;

    /**
     * The subclasses fill this in with the loaderReturn that they need.
     */
    protected LoaderReturn makeLoaderReturn()
    {
        return null;
    }

    /**
     * Turn off the loader and shut things down.
     * <br>
     * This unregisters us with the sampling layer and shuts down the various objects we created.
     */
    public void shutdown()
    {
        tileFetcher = null;
        loadInterp = null;

        samplingLayer.get().removeClient(this);

        ChangeSet changes = new ChangeSet();
        cleanupNative(changes);

        if (control.get() != null && control.get().renderControl != null)
            control.get().renderControl.scene.addChanges(changes);

        control.get().releaseSamplingLayer(samplingLayer.get(),this);

        samplingLayer.clear();
        control.clear();
    }

    protected native void cleanupNative(ChangeSet changes);

    protected native void mergeLoaderReturn(LoaderReturn loadReturn,ChangeSet changes);

    /* --- QuadSamplingLayer interface --- */

    public void samplingLayerConnect(QuadSamplingLayer layer,ChangeSet changes)
    {
        samplingLayerConnectNative(layer,changes);
    }

    private native void samplingLayerConnectNative(QuadSamplingLayer layer,ChangeSet changes);

    public void samplingLayerDisconnect(QuadSamplingLayer layer,ChangeSet changes)
    {
        samplingLayerDisconnectNative(layer,changes);
    }

    private native void samplingLayerDisconnectNative(QuadSamplingLayer layer,ChangeSet changes);

    // Used to initialize the loader for certain types of data.
    enum Mode {SingleFrame,MultiFrame,Object};

    /* --- Callback from C++ side --- */

    // Process the cancels and starts we get from the C++ side
    public void processBatchOps(QIFBatchOps batchOps)
    {
        batchOps.process(tileFetcher);
    }

    // Frame assets are used C++ side, but we have to hold a reference to them
    //  or they disappear at inopportune times.  We don't look inside them here.
    HashSet<QIFFrameAsset> frameAssets = new HashSet<QIFFrameAsset>();

    // Stop tracking a frame asset
    public void clearFrameAsset(QIFFrameAsset frameAsset)
    {
        frameAssets.remove(frameAsset);
    }

    // Start off fetches for all the frames within a given tile
    // Return an array of corresponding frame assets
    public void startTileFetch(QIFBatchOps batchOps,QIFFrameAsset[] inFrameAssets, final int tileX, final int tileY, final int tileLevel, int priority, double importance)
    {
        if (tileInfos.length == 0 || tileInfos.length != inFrameAssets.length)
            return;

        TileID tileID = new TileID();
        tileID.x = tileX;  tileID.y = tileY;  tileID.level = tileLevel;

        QIFFrameAsset[] frames = new QIFFrameAsset[tileInfos.length];
        int frame = 0;
        final QuadLoaderBase loaderBase = this;
        for (TileInfoNew tileInfo : tileInfos) {
            final int fFrame = frame;
            final int dispFrame = tileInfos.length > 1 ? frame : -1;

            // Put together a fetch request for, you now, fetching
            TileFetchRequest fetchRequest = new TileFetchRequest();
            fetchRequest.fetchInfo = tileInfo.fetchInfoForTile(tileID,getFlipY());
            fetchRequest.tileSource = tileInfo.uniqueID;
            fetchRequest.priority = priority;
            fetchRequest.importance = (float)importance;
            fetchRequest.callback = new TileFetchRequest.Callback() {
                @Override
                public void success(TileFetchRequest fetchRequest, byte[] data) {
                    // Build a loader return object, fill in the data and then parse it
                    final LoaderReturn loadReturn = makeLoaderReturn();
                    loadReturn.setTileID(tileX, tileY, tileLevel);
                    loadReturn.setFrame(fFrame);
                    loadReturn.addTileData(data);

                    // We're on an AsyncTask in the background here, so do the loading
                    loadInterp.dataForTile(loadReturn,loaderBase);

                    // Merge the data back in on the sampling layer's thread
                    final QuadSamplingLayer layer = samplingLayer.get();
                    if (layer != null)
                        layer.layerThread.addTask(new Runnable() {
                            @Override
                            public void run() {
                                ChangeSet changes = new ChangeSet();
                                mergeLoaderReturn(loadReturn,changes);
                                layer.layerThread.addChanges(changes);
                            }
                        });
                }

                @Override
                public void failure(TileFetchRequest fetchRequest, String errorStr) {
                    final QuadSamplingLayer layer = samplingLayer.get();
                    if (layer != null) {
                        layer.layerThread.addTask(new Runnable() {
                            @Override
                            public void run() {
                                ChangeSet changes = new ChangeSet();
                                mergeLoaderReturn(null, changes);
                                layer.layerThread.addChanges(changes);
                            }
                        });
                    }
                }
            };

            // Update the frame asset and track it
            QIFFrameAsset frameAsset = inFrameAssets[frame];
            frameAsset.request = fetchRequest;
            frameAssets.add(frameAsset);

            // This will start the fetch request in a bit
            batchOps.addToStart(fetchRequest);

            frame++;
        }
    }

    public void finalize()
    {
        dispose();
    }
    static
    {
        nativeInit();
    }
    private static native void nativeInit();
    native void initialise(SamplingParams params,int numFrames,int mode);
    native void dispose();
    private long nativeHandle;
}