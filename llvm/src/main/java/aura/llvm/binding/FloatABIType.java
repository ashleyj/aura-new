/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package aura.llvm.binding;

public enum FloatABIType {
  FloatABITypeDefault,
  FloatABITypeSoft,
  FloatABITypeHard;

  public final int swigValue() {
    return swigValue;
  }

  public static FloatABIType swigToEnum(int swigValue) {
    FloatABIType[] swigValues = FloatABIType.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (FloatABIType swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + FloatABIType.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private FloatABIType() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private FloatABIType(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private FloatABIType(FloatABIType swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

