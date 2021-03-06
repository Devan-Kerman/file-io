#include <stdio.h>
#include <jni.h>
#include "net_devtech_filepipeline_impl_util_flushzipfs_NativeZipFsFlush.h"

JNIEXPORT
void JNICALL Java_net_devtech_filepipeline_impl_util_flushzipfs_NativeZipFsFlush_flushZipFs(JNIEnv * env, jclass zipfsInternal, jobject zipfs) {
  jclass cls_foo = (*env)->GetObjectClass(env, zipfs);
  jmethodID mid_callback = (*env)->GetMethodID(env, cls_foo, "sync", "()V");
  (*env)->CallVoidMethod(env, zipfs, mid_callback);
  return;
}