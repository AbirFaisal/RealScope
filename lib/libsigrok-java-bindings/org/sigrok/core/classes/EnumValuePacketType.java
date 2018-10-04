/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

/** Base class for objects which wrap an enumeration value from libsigrok. */
public class EnumValuePacketType {
  protected transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected EnumValuePacketType(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(EnumValuePacketType obj) {
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
    return classesJNI.EnumValuePacketType_id(swigCPtr, this);
  }

  /** The name associated with this value. */
public String name() {
    return classesJNI.EnumValuePacketType_name(swigCPtr, this);
  }

  public static PacketType get(int id) {
    long cPtr = classesJNI.EnumValuePacketType_get(id);
    return (cPtr == 0) ? null : new PacketType(cPtr, false);
  }

  public static SWIGTYPE_p_std__vectorT_sigrok__PacketType_const_p_t values() {
    return new SWIGTYPE_p_std__vectorT_sigrok__PacketType_const_p_t(classesJNI.EnumValuePacketType_values(), true);
  }

}
