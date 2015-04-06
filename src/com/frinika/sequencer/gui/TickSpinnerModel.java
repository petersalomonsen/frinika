/*
 * Created on Mar 9, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.gui;

import java.text.ParseException;

import javax.swing.SpinnerNumberModel;

import com.frinika.sequencer.model.util.TimeUtils;

/**
 * Model for TickSpinner.
 * 
 * @see TickSpinner
 * @author Jens Gulden
 */
public class TickSpinnerModel extends SpinnerNumberModel {
	
	protected TimeFormat format;
	protected TimeUtils timeUtils;
	
	public TickSpinnerModel(TimeUtils timeUtils) {
		this(TimeFormat.BAR_BEAT_TICK, 0L, timeUtils);
	}

	public TickSpinnerModel(TimeFormat format, TimeUtils timeUtils) {
		this(format, 0L, timeUtils);
	}
	
	public TickSpinnerModel(long value, TimeUtils timeUtils) {
		this(timeUtils);
		setValue(value);
	}
	
	public TickSpinnerModel(TimeFormat format, long value, TimeUtils timeUtils) {
		this(format, value, false, timeUtils);
	}
	
	public TickSpinnerModel(TimeFormat format, long value, boolean allowNegative, TimeUtils timeUtils) {
		super(value, allowNegative ? null : 0L, null, 1L);
		this.format = format;
		this.timeUtils = timeUtils;
	}
	
	public TimeFormat getFormat() {
		return format;
	}
	
	public long stringToTicks(String s) throws ParseException {
		try {
			switch (format) {
			case BEAT: return Math.round( Float.parseFloat(s) * timeUtils.ticksPerBeat() );
			case NOTE_LENGTH: return -1; // TODO (but currently not used with Spinner)
			default:
				if (s.indexOf(':') == -1) {
					s += ":000";
				}
				if (s.indexOf('.') == -1) {
					//s = "0." + s;
					return timeUtils.beatTickToTick(s);
				}
				return timeUtils.barBeatTickToTick(s);
		}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(),0);
		}
	}

	public String ticksToString(long tick) {
		switch (format) {
			case BAR_BEAT_TICK: return timeUtils.tickToBarBeatTick(tick); 
			case BEAT_TICK: return timeUtils.tickToBeatTick(tick);
			case BAR_BEAT: return timeUtils.tickToBarBeat(tick);
			case BEAT: return String.valueOf((float)tick / timeUtils.ticksPerBeat());
			case NOTE_LENGTH: return null; // TODO (but currently not used with Spinner)
			default: assert false; return null;
		}
		
	}

	public void updateStepSize(String currentValue, int caretPos) {
		final int s = getStepSize(currentValue, caretPos);
		//(new Thread() {
		//	public void run() {
				setStepSizeNoStateChange( s ); // must happen in different thread - no longer since firing event is disabled
		//	}
		//}).start();
	}
	
	private boolean suppressFireStateChange = false;
	
	private synchronized void setStepSizeNoStateChange(Number stepSize) { // synchronized!
		suppressFireStateChange = true; // cheap hack to avoid firing a state-changed event which would confuse cursor-position etc., and also would lead to an error due to chnging state in reaction to a state change event
		super.setStepSize(stepSize);
		suppressFireStateChange = false;
	}
	
	@Override
	protected void fireStateChanged() {
		if ( ! suppressFireStateChange ) { // hack
			super.fireStateChanged();
		}
	}
	
	public int getStepSize(String currentValue, int caretPos) {
		if (format == TimeFormat.BEAT) {
			return (int)timeUtils.ticksPerBeat();
		} else {
			int colonPos = currentValue.lastIndexOf(':');
			if (caretPos > colonPos) { // editing ticks (also if ':' is missing)
				return 1;
			} else {
				int dotPos = currentValue.lastIndexOf('.');
				if (caretPos > dotPos) { // editing beats (also if '.' is missing)
					return (int)timeUtils.ticksPerBeat();
				} else { // editing bars
					return (int)(timeUtils.ticksPerBeat() * timeUtils.beatsPerBar((Long)this.getValue()));
				}
			}
		}
	}
	
}
