/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sigrok.core.classes;

public final class SWIGTYPE_sr_datatype {
  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static SWIGTYPE_sr_datatype swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + SWIGTYPE_sr_datatype.class + " with value " + swigValue);
  }

  private SWIGTYPE_sr_datatype(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private SWIGTYPE_sr_datatype(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private SWIGTYPE_sr_datatype(String swigName, SWIGTYPE_sr_datatype swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static SWIGTYPE_sr_datatype[] swigValues = {  };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

