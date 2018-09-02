/*
 * Created on Jun 27, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.benchmark;

public class SchedulerTest {

	public static void main(String arg[]) {
		Thread t = new MyThread();
		t.start();
	}

}

class MyThread extends Thread {

	public void run() {
		boolean isRunning = true;
		double deltaTimeSec = 2e-3; // attempt to visit at this interval
		boolean ultraLowLatency = false;
		long deltaTimeNanos = (long) (deltaTimeSec * 1e9);
		long errMax = 0;
		long N = (long) (20.0 / deltaTimeSec);
		try {
			for (int i = 0; i < N; i++) {
				long time = System.nanoTime();

				long expireNanos = time + deltaTimeNanos;

				if (ultraLowLatency) {
					// Frinika Estimated frame position
					while (System.nanoTime() < expireNanos)
						Thread.yield(); // Keeps your CPU as busy as possible
				} else {

					Thread.sleep(deltaTimeNanos / 1000000,
							(int) (deltaTimeNanos % 1000000));
				}
				long expireReal = System.nanoTime();
				long err = expireReal - expireNanos;
				if (err > errMax) {
					errMax = err;
					System.out.println(String.format(" Max err is  %6.3f mS",
							(errMax * 1e-6)));
				}
				
				if (err > 2e6)
					System.out.println(String.format(" err is  %6.3f mS",
							(err * 1e-6)));
		
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(String.format(" Max err is  %6.3f mS",
				(errMax * 1e-6)));

		// System.out.println(" Max err is " + (errMax*1e-9));
	}
}