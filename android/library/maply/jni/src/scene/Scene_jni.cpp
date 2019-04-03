/*
 *  Scene_jni.cpp
 *  WhirlyGlobeLib
 *
 *  Created by Steve Gifford on 3/17/15.
 *  Copyright 2011-2016 mousebird consulting
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

#import "Scene_jni.h"
#import "com_mousebird_maply_Scene.h"

using namespace WhirlyKit;

template<> SceneClassInfo *SceneClassInfo::classInfoObj = NULL;

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_nativeInit
  (JNIEnv *env, jclass cls)
{
	SceneClassInfo::getClassInfo(env,cls);
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_addChangesNative
  (JNIEnv *env, jobject obj, jobject changesObj)
{
	try
	{
		SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
		Scene *scene = classInfo->getObject(env,obj);
		ChangeSet *changes = ChangeSetClassInfo::getClassInfo()->getObject(env,changesObj);
		scene->addChangeRequests(*changes);
		changes->clear();
	}
	catch (...)
	{
		__android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::addChanges()");
	}
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_addShaderProgram
(JNIEnv *env, jobject obj, jobject shaderObj, jstring sceneNameStr)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);
        ShaderClassInfo *shaderClassInfo = ShaderClassInfo::getClassInfo();
        Shader_Android *shader = shaderClassInfo->getObject(env,shaderObj);
        
        if (!scene || !shader)
            return;
        
        const char *cName = env->GetStringUTFChars(sceneNameStr,0);
        std::string name = cName;
        
        scene->addProgram(shader->prog);

        env->ReleaseStringUTFChars(sceneNameStr, cName);
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::addShaderProgram()");
    }
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_removeShaderProgram
        (JNIEnv *env, jobject obj, jlong shaderID)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);

        if (!scene)
            return;

        scene->removeProgram(shaderID);
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::removeShaderProgram()");
    }
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_teardownGL
(JNIEnv *env, jobject obj)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);
        if (!scene)
            return;
        
        scene->teardownGL();
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::teardownGL()");
    }
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_addRenderTargetNative
  (JNIEnv *env, jobject obj, jlong renderTargetID, jint width, jint height, jlong texID, jboolean clearEveryFrame, jboolean blend, jfloat r, jfloat g, jfloat b, jfloat a)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);
        if (!scene)
            return;
        
        ChangeSet changes;
        RGBAColor color(r,g,b,a);
        changes.push_back(new AddRenderTargetReq(renderTargetID,width,height,texID,clearEveryFrame,blend,color));
        
        scene->addChangeRequests(changes);
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::addRenderTargetNative()");
    }
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_changeRenderTarget
        (JNIEnv *env, jobject obj, jlong renderTargetID, jlong texID)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);
        if (!scene)
            return;

        ChangeSet changes;
        changes.push_back(new ChangeRenderTargetReq(renderTargetID,texID));
        scene->addChangeRequests(changes);
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::changeRenderTarget()");
    }
}

JNIEXPORT void JNICALL Java_com_mousebird_maply_Scene_removeRenderTargetNative
(JNIEnv *env, jobject obj, jlong targetID)
{
    try
    {
        SceneClassInfo *classInfo = SceneClassInfo::getClassInfo();
        Scene *scene = classInfo->getObject(env,obj);
        if (!scene)
            return;
        
        ChangeSet changes;
        changes.push_back(new RemRenderTargetReq(targetID));

        scene->addChangeRequests(changes);
    }
    catch (...)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, "Maply", "Crash in Scene::removeRenderTargetNative()");
    }
}