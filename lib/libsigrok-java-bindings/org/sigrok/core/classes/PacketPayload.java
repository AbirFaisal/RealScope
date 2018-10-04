/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

/** Abstract base class for datafeed packet payloads. */
public class PacketPayload {
  protected transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected PacketPayload(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PacketPayload obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        classesJNI.delete_PacketPayload(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

}
