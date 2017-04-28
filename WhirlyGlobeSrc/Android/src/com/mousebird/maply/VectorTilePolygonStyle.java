/*
 *  MaplyVectorTilePolygonStyle.java
 *  WhirlyGlobeLib
 *
 *  Created by Ranen Ghosh on 3/27/17.
 *  Copyright 2011-2017 mousebird consulting
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


import java.util.HashMap;
import java.util.List;

import android.util.Log;

public class VectorTilePolygonStyle extends VectorTileStyle {

    private VectorInfo vectorInfo;


    public VectorTilePolygonStyle(VectorInfo vectorInfo, VectorStyleSettings settings, MaplyBaseController viewC) {
        super(viewC);
        this.vectorInfo = vectorInfo;
    }

    public ComponentObject[] buildObjects(List<VectorObject> objects, MaplyTileID tileID, MaplyBaseController controller) {
        ComponentObject compObj = controller.addVectors(objects, vectorInfo, MaplyBaseController.ThreadMode.ThreadCurrent);
        if (compObj != null) {
            return new ComponentObject[]{compObj};
        }
        return null;
    }

}