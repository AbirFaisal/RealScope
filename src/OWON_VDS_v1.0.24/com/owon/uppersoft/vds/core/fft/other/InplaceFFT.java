package com.owon.uppersoft.vds.core.fft.other;

/*************************************************************************
 *  Compilation:  javac InplaceFFT.java
 *  Execution:    java InplaceFFT N
 *  Dependencies: Complex.java
 *
 *  Compute the FFT of a length N complex sequence in-place.
 *  Uses a non-recursive version of the Cooley-Tukey FFT.
 *  Runs in O(N log N) time.
 *
 *  Reference:  Algorithm 1.6.1 in Computational Frameworks for the
 *  Fast Fourier Transform by Charles Van Loan.
 *
 *
 *  Limitations
 *  -----------
 *   -  assumes N is a power of 2
 *
 *  
 *************************************************************************/

public class InplaceFFT {

    // compute the FFT of x[], assuming its length is a power of 2
    public static void fft(ComplexD[] x) {

        // check that length is a power of 2
        int N = x.length;
        if (Integer.highestOneBit(N) != N) {
            throw new RuntimeException("N is not a power of 2");
        }

        // bit reversal permutation
        int shift = 1 + Integer.numberOfLeadingZeros(N);
        for (int k = 0; k < N; k++) {
            int j = Integer.reverse(k) >>> shift;
            if (j > k) {
            	ComplexD temp = x[j];
                x[j] = x[k];
                x[k] = temp;
            }
        }

        // butterfly updates
        for (int L = 2; L <= N; L = L+L) {
            for (int k = 0; k < L/2; k++) {
                double kth = -2 * k * Math.PI / L;
                ComplexD w = new ComplexD(Math.cos(kth), Math.sin(kth));
                for (int j = 0; j < N/L; j++) {
                	ComplexD tao = w.times(x[j*L + k + L/2]);
                    x[j*L + k + L/2] = x[j*L + k].minus(tao); 
                    x[j*L + k]       = x[j*L + k].plus(tao); 
                }
            }
        }
    }


    // test client
    public static void main(String[] args) { 
        int N = 8;//Integer.parseInt(args[0]);
        ComplexD[] x = new ComplexD[N];

        // original data
        for (int i = 0; i < N; i++) {
            x[i] = new ComplexD(i+1, 0);
            // x[i] = new Complex(-2*Math.random() + 1, 0);
        }
        for (int i = 0; i < N; i++)
            System.out.println(x[i]);
        System.out.println();

        // FFT of original data
        fft(x);
        for (int i = 0; i < N; i++)
            System.out.println(x[i]);
        System.out.println();

    }

}