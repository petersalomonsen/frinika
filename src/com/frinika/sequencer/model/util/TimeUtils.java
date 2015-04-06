package com.frinika.sequencer.model.util;

/*
 * Created on May 9, 2006
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

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.TimeSignatureEvent;

/**
 * Helper class to convert ticks into the display format and visa versa
 * 
 * tick <---> bar.beat:tick
 * 
 * Imcomplete strings are allowed
 * 
 * bar. beat: :tick bar.beat beat:tick .beat:tick bar.beat:
 * 
 * You can select NORMAL mode 0 ---> 1.1:000 OR CMODE 0 ---> 0.0.000
 * 
 * @author pjl
 * 
 */

public class TimeUtils {

	// ProjectContainer project;

	final static int NORMAL = 0;

	final static int CMODE = 1;

	static int defaultMode = CMODE;

	int ticksPerBeat; // = 420;

	TimeSignatureList timeSig;

	// TODO hash map allowing sig changes.
	// int beatsPerBar = 4;

	static int barOff = 0;

	static int beatOff = 0;

	
	/**
	 * 
	 * @param proj
	 */
	public TimeUtils(ProjectContainer proj) {   // could get rid of this ?
		this(proj.getSequence().getResolution(), proj.getTimeSignatureList());
	}

	public TimeUtils(int ticksPerBeat, TimeSignatureList timeSig) {
		this.ticksPerBeat = ticksPerBeat;
		;
		this.timeSig = timeSig;
		setMode(defaultMode);
		// TODO keep list of TImeUtils and set all with a static setMode
	}

	public static void setMode(int key) {

		switch (key) {
		case NORMAL:
			barOff = 1;
			beatOff = 1;
			break;
		case CMODE:
			barOff = 0;
			beatOff = 0;
			break;
		default:
			try {
				throw new Exception("Unknown display mode");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * 
	 * @param tick
	 * @return formated bar.beat:tick
	 */
	public String tickToBarBeatTick(long tick) {
		boolean minus = (tick < 0);
		if (minus) {
			tick = -tick;
		}
		// this one is easy
		long beats = tick / ticksPerBeat;
		long tickBit = tick % ticksPerBeat;
		// long bar = beats / beatsPerBar;
		// beats = beats % beatsPerBar;
		TimeSignatureEvent ev = timeSig.getEventAtBeat((int) beats);

		int bar = (int) (ev.bar + (beats - ev.beat) / ev.beatsPerBar);
		beats = (beats - ev.beat) % ev.beatsPerBar;

		String s = (bar + barOff) + "." + (beats + beatOff) + ":"
				+ String.format("%03d", tickBit);

		if (minus) {
			s = "-" + s;
		}
		return s;
	}

	/**
	 * 
	 * 
	 * @param tick
	 *            must be an multiple of ticksPerBeat
	 * @return formated bar.beat
	 */
	public String tickToBarBeat(long tick) {
		boolean minus = false;
		if (tick < 0) {
			minus = true;
			tick = -tick;
		}
		// this one is easy
		long beats = tick / ticksPerBeat;
		long tickBit = tick % ticksPerBeat;
		assert (tickBit == 0);
		// long bar = beats / beatsPerBar;
		// beats = beats % beatsPerBar;

		TimeSignatureEvent ev = timeSig.getEventAtBeat((int) beats);

		String s = ((ev.bar + barOff) + "." + (beats - ev.beat + beatOff));
		if (minus) {
			return "-" + s;
		} else {
			return s;
		}
	}

	/**
	 * Do not attempt to convert beats into bars.
	 * 
	 * @param tick
	 * @return beat:tick
	 */
	public String tickToBeatTick(long tick) {
		boolean minus = false;
		if (tick < 0) {
			minus = true;
			tick = -tick;
		}
		long beats = tick / ticksPerBeat;
		long tickBit = tick % ticksPerBeat;
		String s = (beats + beatOff) + ":" + String.format("%03d", tickBit);
		if (minus) {
			return "-" + s;
		} else {
			return s;
		}
	}

	/**
	 * 
	 * @param tick
	 * @return bar at or just before tick
	 */
	public int barAtTick(long tick) {
		long beats = tick / ticksPerBeat;
		long tickBit = tick % ticksPerBeat;

		if (tickBit != 0)
			return -1;

		TimeSignatureEvent ev = timeSig.getEventAtBeat((int) beats);
		if (ev == null) return -1;
		int bb = (int) ((beats - ev.beat) % ev.beatsPerBar);
		if (bb != 0)
			return -1;
		return (int) (ev.bar + barOff + (beats - ev.beat) / ev.beatsPerBar);
	}

	/**
	 * 
	 * @param str
	 *            bar.beat:tick string
	 * @return ticks
	 */
	public long barBeatTickToTick(String str) {

		// This is a lot of if statements
		try {

			str = str.trim();

			if (str.length() == 0)
				return 0;
			long sign = (str.charAt(0) == '-') ? -1 : 1;
			if (sign == -1) {
				str = str.substring(1).trim();
				if (str.length() == 0)
					return 0;
			}


			String toks[] = str.split("[:|.]");

			switch (toks.length) {
			case 1:

				if (str.charAt(0) == ':')
					return sign * Long.parseLong(toks[0]);
				else if (str.indexOf('.') > 0) { // bar to tick
					if (sign < 0) {
						throw new Exception(" negative times are broken ");
					}

					int beat = timeSig.getBeatAtBar(Integer.parseInt(toks[0])
							- barOff);
					return sign * beat * ticksPerBeat;

					// return 0; // sign * (Long.parseLong(toks[0]) - barOff) *
					// ticksPerBeat * beatsPerBar;

				} else
					return sign * (Long.parseLong(toks[0]) - beatOff)
							* ticksPerBeat;

			case 2:

				if (str.charAt(0) == '.')
					return sign * (Long.parseLong(toks[1]) - beatOff)
							* ticksPerBeat;
				else if (str.charAt(0) == ':')
					return sign * Long.parseLong(toks[1]);
				else if (str.indexOf('.') > 0) {
					if (sign < 0) {
						throw new Exception(" negative times are broken ");
					}

					int beat = timeSig.getBeatAtBar(Integer.parseInt(toks[0])
							- barOff);
					return sign
							* (beat + (Integer.parseInt(toks[1]) - beatOff))
							* ticksPerBeat;

					// ticksPerBeat * beatsPerBar + (Long.parseLong(toks[1]) -
					// beatOff) * ticksPerBeat);

					// return sign * ((Long.parseLong(toks[0]) - barOff) *
					// ticksPerBeat * beatsPerBar + (Long.parseLong(toks[1]) -
					// beatOff) * ticksPerBeat);
				} else {
					return sign
							* ((Long.parseLong(toks[0]) - beatOff)
									* ticksPerBeat + Long.parseLong(toks[1]));
				}

			case 3:
				if (!toks[0].equals("")) {
					if (sign < 0) {
						throw new Exception(" negative times are broken ");
					}

					int beat = timeSig.getBeatAtBar(Integer.parseInt(toks[0])
							- barOff);

					return sign
							* (beat + (Integer.parseInt(toks[1]) - beatOff))
							* ticksPerBeat + +Long.parseLong(toks[2]);
					// return sign * (((Long.parseLong(toks[0]) - barOff) *
					// beatsPerBar + Long.parseLong(toks[1]) - beatOff) *
					// ticksPerBeat + Long.parseLong(toks[2]));
				} else
					return sign
							* ((Long.parseLong(toks[1]) - beatOff)
									* ticksPerBeat + Long.parseLong(toks[2]));

			default:
				throw new Exception("error in time format >" + str + "<");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}
	
	public long beatTickToTick(String s) {
		int sgn = 1;
		s = s.trim();
		if (s.length() == 0) return 0;
		char sgnch = s.charAt(0); 
		if (sgnch=='-') {
			sgn = -1;
			s = s.substring(1);
		} else if (sgnch=='+') {
			s = s.substring(1);
		}
		int pos = s.indexOf(':');
		if (pos == -1) { // assume ticks only
			try {
				return sgn * Long.parseLong(s);
			} catch (NumberFormatException nfe) {
				return 0;
			}
		}
		String beatsStr  = s.substring(0, pos);
		String ticksStr  = s.substring(pos+1);
		int beats;
		try {
			beats = Integer.parseInt(beatsStr);
		} catch (NumberFormatException nfe1) {
			beats = 0;
		}
		int ticks;
		try {
			ticks = Integer.parseInt(ticksStr);
		} catch (NumberFormatException nfe1) {
			ticks = 0;
		}
		return sgn * ((beats * (long)ticksPerBeat) + ticks);
	}

	// public BarIterator barIterator(long tick1, long tick2) {
	// return new BarIterator(tick1, tick2);
	// }

	public double tickToFloatBeat(long tick) {
		return tick / ticksPerBeat; // project.getSequence().getResolution();
	}

	// public class BarTick {
	// int bar;
	//
	// int tick;
	//
	// BarTick(int bar) {
	// this.bar = bar;
	// this.tick = (bar - barOff) * ticksPerBeat * beatsPerBar;
	// }
	//
	// public long getTick() {
	// return tick;
	// }
	//
	// public long getBar() {
	// return bar;
	// }
	// }
	//
	// class BarIterator implements Iterator {
	//
	// int barNext = 0;
	//
	// int barLast = 0;
	//
	// BarIterator(long tick1, long tick2) {
	// barNext = barOff + (int) (tick1 / ticksPerBeat / beatsPerBar);
	// barLast = barOff + (int) (tick2 / ticksPerBeat / beatsPerBar);
	// }
	//
	// public boolean hasNext() {
	// return barNext <= barLast;
	// }
	//
	// public Object next() {
	// // TODO Auto-generated method stub
	// return new BarTick(barNext++);
	// }
	//
	// public void remove() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }

	public long ticksPerBeat() { // Jens
		// return project.getSequence().getResolution();
		return ticksPerBeat;
	}

	public int beatsPerBar(long tick) { // Jens
		long beats = tick / ticksPerBeat;
	    TimeSignatureEvent ev = timeSig.getEventAtBeat((int) beats);
	    //int bar = (int) (ev.bar + (beats - ev.beat) / ev.beatsPerBar);
	    //beats = (beats - ev.beat) % ev.beatsPerBar;
	    return ev.beatsPerBar;
	}

	
	public long startTickOfBar(int bar) {  // PJL
		return timeSig.getBeatAtBar(bar)*ticksPerBeat;	
	}
	
	public long beatToTick(double beat) {
		return (long)Math.round(ticksPerBeat*beat);
	}
	
	static public void main(String args[]) {

		TimeSignatureList list = new TimeSignatureList();
		list.add(0, 4);
		TimeUtils time = new TimeUtils(60, list);

		//              
		String tests[] = { "1.", "234", // beats
				"2:234", // beat.ticks
				":234", // ticks
				"17.3:031", // bar.beat.ticks
				"14.", // bar.
				".3", // beat
				".3:004", // .beat:tick
				"2:", // beat:
				"15.1:", // bar.beat
		};

		setMode(CMODE);
		for (String str : tests)
			System.out.println(str + " = "
					+ time.tickToBarBeatTick(time.barBeatTickToTick(str)));

		setMode(NORMAL);
		for (String str : tests)
			System.out.println(str + " = "
					+ time.tickToBarBeatTick(time.barBeatTickToTick(str)));

		/*
		 * for (int i = 0; i < 10000; i += 5) {
		 * System.out.println(time.tickToBarBeatTick(i)); }
		 */
	}

}