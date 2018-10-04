/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

public class DriverMap {
  protected transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected DriverMap(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(DriverMap obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        classesJNI.delete_DriverMap(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public DriverMap() {
    this(classesJNI.new_DriverMap__SWIG_0(), true);
  }

  public DriverMap(DriverMap arg0) {
    this(classesJNI.new_DriverMap__SWIG_1(DriverMap.getCPtr(arg0), arg0), true);
  }

  public long size() {
    return classesJNI.DriverMap_size(swigCPtr, this);
  }

  public boolean empty() {
    return classesJNI.DriverMap_empty(swigCPtr, this);
  }

  public void clear() {
    classesJNI.DriverMap_clear(swigCPtr, this);
  }

  public SWIGTYPE_p_std__shared_ptrT_sigrok__Driver_t get(String key) {
    return new SWIGTYPE_p_std__shared_ptrT_sigrok__Driver_t(classesJNI.DriverMap_get(swigCPtr, this, key), false);
  }

  public void set(String key, SWIGTYPE_p_std__shared_ptrT_sigrok__Driver_t x) {
    classesJNI.DriverMap_set(swigCPtr, this, key, SWIGTYPE_p_std__shared_ptrT_sigrok__Driver_t.getCPtr(x));
  }

  public void del(String key) {
    classesJNI.DriverMap_del(swigCPtr, this, key);
  }

  public boolean has_key(String key) {
    return classesJNI.DriverMap_has_key(swigCPtr, this, key);
  }

}
