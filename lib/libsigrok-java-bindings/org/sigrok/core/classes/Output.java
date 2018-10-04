/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

/** An output instance (an output format applied to a device) */
public class Output {
  protected transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Output(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Output obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        classesJNI.delete_Output(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  /** Update output with data from the given packet.
   * @param packet Packet to handle. */
public String receive(Packet packet) {
    return classesJNI.Output_receive(swigCPtr, this, Packet.getCPtr(packet), packet);
  }

}
