package com.owon.uppersoft.vds.core.fft.other;


/*************************************************************************
 *  Compilation:  javac Complex.java
 *  Execution:    java Complex
 *
 *  Data type for complex numbers.
 *
 *  The data type is "immutable" so once you create and initialize
 *  a Complex object, you cannot change it. The "final" keyword
 *  when declaring real and imaginary enforces this rule, making it a
 *  compile-time error to change the .real or .imaginary fields after
 *  they've been initialized.
 *
 *  % java Complex
 *  a            = 5.0 + 6.0i
 *  b            = -3.0 + 4.0i
 *  Re(a)        = 5.0
 *  Im(a)        = 6.0
 *  b + a        = 2.0 + 10.0i
 *  a - b        = 8.0 + 2.0i
 *  a * b        = -39.0 + 2.0i
 *  b * a        = -39.0 + 2.0i
 *  a / b        = 0.36 - 1.52i
 *  (a / b) * b  = 5.0 + 6.0i
 *  conj(a)      = 5.0 - 6.0i
 *  |a|          = 7.810249675906654
 *  tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
 *
 *************************************************************************/

public class ComplexD implements IComplex{
	public double real;   // the real part
    public double imaginary;   // the imaginary part
    
    public ComplexD() {
    }
    
    // create a new object with the given real and imaginary parts
    public ComplexD(double real, double imag) {
        this.real = real;
        imaginary = imag;
    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (imaginary == 0) return real + "";
        if (real == 0) return imaginary + "i";
        if (imaginary <  0) return real + " - " + (-imaginary) + 'i';
        return real + " + " + imaginary + 'i';
    }

    // return abs/modulus/magnitude and angle/phase/argument
    public double abs()   { return Math.hypot(real, imaginary); }  // Math.sqrt(real*real + imaginary*imaginary)
    public double phase() { return Math.atan2(imaginary, real); }  // between -pi and pi

    // return a new Complex object whose value is (this + b)
    public ComplexD plus(ComplexD b) {
        ComplexD a = this;             // invoking object
        double real = a.real + b.real;
        double imag = a.imaginary + b.imaginary;
        return new ComplexD(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public ComplexD minus(ComplexD b) {
        ComplexD a = this;
        double real = a.real - b.real;
        double imag = a.imaginary - b.imaginary;
        return new ComplexD(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public ComplexD times(ComplexD b) {
        ComplexD a = this;
        double real = a.real * b.real - a.imaginary * b.imaginary;
        double imag = a.real * b.imaginary + a.imaginary * b.real;
        return new ComplexD(real, imag);
    }

    // scalar multiplication
    // return a new object whose value is (this * alpha)
    public ComplexD times(double alpha) {
        return new ComplexD(alpha * real, alpha * imaginary);
    }

    // return a new Complex object whose value is the conjugate of this
    public ComplexD conjugate() {  return new ComplexD(real, -imaginary); }

    // return a new Complex object whose value is the reciprocal of this
    public ComplexD reciprocal() {
        double scale = real * real + imaginary * imaginary;
        return new ComplexD(real / scale, -imaginary / scale);
    }

    // return the real or imaginary part
    public double re() { return real; }
    public double im() { return imaginary; }

    // return a / b
    public ComplexD divides(ComplexD b) {
        ComplexD a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public ComplexD exp() {
        return new ComplexD(Math.exp(real) * Math.cos(imaginary), Math.exp(real) * Math.sin(imaginary));
    }

    // return a new Complex object whose value is the complex sine of this
    public ComplexD sin() {
        return new ComplexD(Math.sin(real) * Math.cosh(imaginary), Math.cos(real) * Math.sinh(imaginary));
    }

    // return a new Complex object whose value is the complex cosine of this
    public ComplexD cos() {
        return new ComplexD(Math.cos(real) * Math.cosh(imaginary), -Math.sin(real) * Math.sinh(imaginary));
    }

    // return a new Complex object whose value is the complex tangent of this
    public ComplexD tan() {
        return sin().divides(cos());
    }
    


    // a static version of plus
    public static ComplexD plus(ComplexD a, ComplexD b) {
        double real = a.real + b.real;
        double imag = a.imaginary + b.imaginary;
        ComplexD sum = new ComplexD(real, imag);
        return sum;
    }



    // sample client for testing
    public static void main(String[] args) {
        ComplexD a = new ComplexD();
        ComplexD b = new ComplexD(-3.0, 4.0);

        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Re(a)        = " + a.re());
        System.out.println("Im(a)        = " + a.im());
        System.out.println("b + a        = " + b.plus(a));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("a / b        = " + a.divides(b));
        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
        System.out.println("conj(a)      = " + a.conjugate());
        System.out.println("|a|          = " + a.abs());
        System.out.println("tan(a)       = " + a.tan());
    }

}