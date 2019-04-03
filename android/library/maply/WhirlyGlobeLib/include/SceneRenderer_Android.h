/*
 *  SceneRenderer_Android.h
 *  WhirlyGlobeLib
 *
 *  Created by Steve Gifford on 3/7/19.
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

#import "Maply_jni.h"
#import <WhirlyGlobe.h>

namespace WhirlyKit {

// Android version keeps track of the context
class SceneRendererES2_Android : public SceneRendererES2 {
public:
    SceneRendererES2_Android();

    // Called when the window changes size (or on startup)
    bool resize(int width, int height);

    EGLContext context;
};

}