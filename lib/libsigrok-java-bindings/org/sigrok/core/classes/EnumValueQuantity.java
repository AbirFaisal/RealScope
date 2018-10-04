/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

/** Base class for objects which wrap an enumeration value from libsigrok. */
public class EnumValueQuantity {
  protected transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected EnumValueQuantity(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(EnumValueQuantity obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        throw new UnsupportedOperationException("C++ destructor does not have public access");
      }
      swigCPtr = 0;
    }
  }

  /** The integer constant associated with this value. */
public int id() {
    return classesJNI.EnumValueQuantity_id(swigCPtr, this);
  }

  /** The name associated with this value. */
public String name() {
    return classesJNI.EnumValueQuantity_name(swigCPtr, this);
  }

  public static Quantity get(int id) {
    long cPtr = classesJNI.EnumValueQuantity_get(id);
    return (cPtr == 0) ? null : new Quantity(cPtr, false);
  }

  public static SWIGTYPE_p_std__vectorT_sigrok__Quantity_const_p_t values() {
    return new SWIGTYPE_p_std__vectorT_sigrok__Quantity_const_p_t(classesJNI.EnumValueQuantity_values(), true);
  }

}
