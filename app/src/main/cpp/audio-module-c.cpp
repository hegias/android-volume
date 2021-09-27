//
// Created by Max on 20/09/2021.
//

#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_audiotracktest_MainActivity_myFun(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from My other lib C";
    return env->NewStringUTF(hello.c_str());
}