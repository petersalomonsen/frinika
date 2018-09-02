// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import java.util.List;

import uk.org.toot.audio.basic.tap.TapControls;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.control.Control;
import uk.org.toot.control.EnumControl;

/**
 * @author st
 */
public class Taps
{
	private static AudioServer server;
	static List<TapControls> taps = 
		new java.util.ArrayList<TapControls>();
	
	public static void setAudioServer(AudioServer aserver) {
		server = aserver;
	}
	
	public static AudioBuffer create(TapControls controls) {
		check();
		String name = tapName(controls);
		AudioBuffer buffer = server.createAudioBuffer(name);
		taps.add(controls);
		return buffer;
	}

	public static void remove(AudioBuffer buffer) {
		check();
		for ( TapControls c : taps ) {
			if ( c.getBuffer() == buffer ) {
				taps.remove(c);
				server.removeAudioBuffer(buffer);
				return;
			}
		}
		System.err.println("Taps failed to remove buffer");
	}
	
	public static TapControls getControls(String name) {
		for ( TapControls c : taps ) {
			String tapname = tapName(c);
			if ( name.equals(tapname) ) {
				return c;
			}
		}
		return null;
	}
	
	public static AudioBuffer getBuffer(String name) {
		TapControls c = getControls(name);
		if ( c == null ) return null;
		return c.getBuffer();
	}
	
	private static void check() {
		if ( server == null ) throw new IllegalStateException("null AudioServer");
	}
	
	static String tapName(TapControls controls) {
		String[] parts = controls.getName().split("\\s");
		String name = controls.getParent().getName();
		if ( parts.length > 1 ) name += parts[1];
		return name;
	}
	
	public static class TapControl extends EnumControl
	{
		private final static int SELF_VAL = 16000;
		private final static String SELF_STR = "Self";
		private String prevTapName = SELF_STR;
		private AudioBuffer buffer;

		public TapControl(int id, String name) {
			super(id, name, SELF_STR);
		}

		public AudioBuffer getBuffer() {
			return buffer;
		}

        public void remove() {
            reference((String)getValue(), -1);
        }
        
		@Override
		protected void derive(Control obj) {
			String tapName = (String)getValue();
			reference(prevTapName, -1);
			reference(tapName, +1);
			prevTapName = tapName;
			if ( tapName.equals(SELF_STR) ) {
				buffer = null;
				return;
			}
//			System.out.println("TC: tap "+tapName);
			buffer = Taps.getBuffer(tapName);
			if ( buffer == null ) {
				System.out.println("Taps returned null buffer for Tap "+tapName);
			}
		}

		protected void reference(String name, int ref) {
			if ( name.equals(SELF_STR) ) return;
			TapControls c = Taps.getControls(name);
			if ( c != null ) {
				c.reference(ref);
			} else {
				System.err.println("Taps couldn't find Tap "+name);
			}
		}

		@Override
		public List getValues() {
			List<String> values = new java.util.ArrayList<String>();
			values.add(SELF_STR);
			for ( TapControls c : Taps.taps ) {
				values.add(Taps.tapName(c));
			}
			return values;
		}

		@Override
		public void setIntValue(int value) {
			if ( value == SELF_VAL ) {
				setValue(SELF_STR);
				return;
			}
			int instance = value & 0x07;
			int strip = value >> 3;
			String name = String.valueOf(strip+1);
			if ( instance > 0 ) name += "#"+(instance+1);
			setValue(name);
		}

		@Override
		public int getIntValue() {
			String name = getValueString();
			if ( name.equals(SELF_STR) ) return SELF_VAL;
			int instance = 0;
			int strip;
			if ( name.contains("#") ) {
				String[] parts = name.split("#");
				instance = parts.length > 0 ? Integer.parseInt(parts[1])-1 : 0;
				strip = Integer.parseInt(parts[0])-1;
			} else {
				strip = Integer.parseInt(name)-1;
			}
			return strip * 8 + instance;
		}

		@Override
		public boolean hasLabel() { return true; }
	}
}
