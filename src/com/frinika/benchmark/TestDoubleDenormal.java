
package com.frinika.benchmark;

public class TestDoubleDenormal {

	final static int N = 10;

	final static int M = 500000;

	static double buf[] = new double[N];

	public static void work(double buf[]) {
		for (int i = 1; i < N; i++) {
			buf[i] = buf[i - 1] * 0.5;
		}
	}

	public static void main(String args[]) {

		buf[0] = 1.0e-300;

		do {
			
			long t1 = System.nanoTime();

			buf[0] = buf[0] * 0.5f;
			for (int i = 0; i < M; i++) {
				work(buf);
			}

			long t2 = System.nanoTime();
			System.out.println((t2 - t1) + "    " + buf[0] + "   " + buf[N - 1] + "   " );
		} while (buf[N - 1] > 0.0f);
	}

}
