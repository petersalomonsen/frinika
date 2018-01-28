// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.composition;

import uk.org.toot.music.tonality.*;
import uk.org.toot.music.MeterCoding;

/**
 * A BarContext is passed to BarComposer.composeBar
 * @author st
 *
 */
public class BarContext 
{
	// by definition only one meter per bar
	private int meter = MeterCoding.COMMON_TIME;
	
	// potentially multiple key changes per bar
	private Key[] keys;
	private int[] keyTimes;
	
	// the key immediately before this bar
	private Key previousKey;
	
	private int[] avoidNotes;
	
	// reset anything that can't persist into next bar
	public void reset() {
		avoidNotes = null;
	}
	
	public int getMeter() {
		return meter;
	}
	
	public void setMeter(int meter) {
		this.meter = meter;
	}
	
	public Key getPreviousKey() {
		return previousKey;
	}
	
	/**
	 * @return the key changes
	 */
	public Key[] getKeys() {
		return keys;
	}
	
	/**
	 * Set the key changes
	 * @param keys the keys to set
	 */
	public void setKeys(Key[] keys) {
		if ( this.keys == null ) {
			// there were no previous keys
			// so we use the next one, meaning no change effectively
			previousKey = keys[0];
		} else {
			// we remember the last of the keys from the previous bar
			previousKey = this.keys[this.keys.length-1];
		}
		this.keys = keys;
	}
	
	/**
	 * @return the keyTimes
	 */
	public int[] getKeyTimes() {
		return keyTimes;
	}
	
	/**
	 * @param keyTimes the keyTimes to set
	 */
	public void setKeyTimes(int[] keyTimes) {
		this.keyTimes = keyTimes;
	}
	
	public void setAvoidNotes(int[] avoidNotes) {
		this.avoidNotes = avoidNotes;
	}
	
	public int[] getAvoidNotes() {
		return avoidNotes;
	}
}
