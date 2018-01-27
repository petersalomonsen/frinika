
package com.frinika.benchmark;

public class TestDenormal {

	final static int N = 10;

	final static int M = 500000;

	static float buf[] = new float[N];

	public static void work(float buf[]) {
		for (int i = 1; i < N; i++) {
			buf[i] = buf[i - 1] * 0.5f;
		}
	}

	public static void main(String args[]) {

		buf[0] = 1.0e-3f;

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
