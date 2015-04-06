import java.util.Random;

import com.frinika.sequencer.model.tempo.TempoList;

import junit.framework.TestCase;

/*
 * Created on 14 Jan 2008
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

/**
 * 
 * 
 * 
 * Check that the tempo list mapping of tick to time  (and then back) is consistant.
 * 
 */
public class TempoListTest extends TestCase {

	public void testTempoList() {

		int ticksPerBeat = 100;
		TempoList list = new TempoList(ticksPerBeat, null);

		int N = 1000;

		for (long i = 0; i < N; i += 10) {
			list.add(i, 60.0);

		}

		list.add(0, 60);

		Random rand = new Random();
		double ticks[] = new double[N];
		double bpm[] = new double[N];
		for (int i = 0; i < 0; i++) {
			list.add((long) (rand.nextDouble() * N),
					40 + rand.nextDouble() * 200);
		}

		for (int i = 0; i < N; i++) {
			double tick = (long) (rand.nextDouble() * N);
			double t = list.getTimeAtTick(tick);
			double t2 = list.getTickAtTime(t);
			// System.out.println(tick + " " + t + " " + t2);
			assertTrue (Math.abs(tick - t2) < 1e-7);
		}

	}
	
	public  void testTempo2() {
		int ticksPerBeat = 128;
		TempoList list = new TempoList(ticksPerBeat, null);

		list.add(0, 60);
		list.add(4*ticksPerBeat, 120);
		for (int i=0;i<8;i++){
			System.out.println(list.getTimeAtTick(ticksPerBeat*i));		
		}
		assertTrue (Math.abs(list.getTimeAtTick(0))  < 1e-7);
		assertTrue (Math.abs(list.getTimeAtTick(ticksPerBeat)-1.0)  < 1e-7);
		assertTrue (Math.abs(list.getTimeAtTick(ticksPerBeat*4)-4.0)  < 1e-7);
		assertTrue (Math.abs(list.getTimeAtTick(ticksPerBeat*5)-4.5)  < 1e-7);
		assertTrue (Math.abs(list.getTimeAtTick(ticksPerBeat*6)-5.0)  < 1e-7);

		
	}
	
	
	public  void testTempo3() {
		int ticksPerBeat = 128;
		TempoList list = new TempoList(ticksPerBeat, null);

		list.add(0, 60);
		list.add(4*ticksPerBeat, 120);
		list.getTickAtTime(4.0);
		for (int i=0;i<8;i++){
			System.out.println(list.getTickAtTime(i));		
		}
		assertTrue (Math.abs(list.getTickAtTime(0))  < 1e-7);
		assertTrue (Math.abs(list.getTickAtTime(1.0)-ticksPerBeat)  < 1e-7);
		assertTrue (Math.abs(list.getTickAtTime(2.0)-2*ticksPerBeat)  < 1e-7);
		assertTrue (Math.abs(list.getTickAtTime(3.0)-3*ticksPerBeat)  < 1e-7);
		assertTrue (Math.abs(list.getTickAtTime(4.0)-4*ticksPerBeat)  < 1e-7);
		assertTrue (Math.abs(list.getTickAtTime(5.0)-6.0*ticksPerBeat)  < 1e-7);

		
	}

	public static void main(String args[]) {
		new TempoListTest().testTempoList();
		new TempoListTest().testTempo2();
		new TempoListTest().testTempo3();
	}

}
