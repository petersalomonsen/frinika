/*
 * Created on Nov 18, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.voiceserver;

/**
 * The voice interrupt is used to alter voice parameters in real time. The
 * reason for why the parameters should be altered through a voice interrupt,
 * is because of latency compensation. An example of this is if you alter the 
 * parameters directly - let's say you trigger a release - the sound would end
 * too early. If you modify the parameter through a voice interrupt you're sure
 * that the note will be released exactly when it's supposed to be. The voice
 * server is aware of all scheduled interrupts, and will therefore make sure
 * that it is executed at the right time.
 * 
 * How to use? Use this class in your synth on all places where you perform a 
 * control change, note off, pitch bend - or other latency sensitive events.
 * Instead of placing the parameter-altering code directly under the method
 * receiving the events, rather create a voiceinterrupt class and place the code
 * under the doInterrupt() method. Then this code will be executed when it's
 * supposed to be.
 * 
 * @author Peter Johan Salomonsen
 */
public abstract class VoiceInterrupt {
	/**
     * Used by the voiceserver to set execution time point
	 */
    public long interruptFramePos = 0;

    /**
     * Place your interrupt code in this method. Any realtime midi event
     * handling (control changes, pitch-bend, note off etc), should go under 
     * here. It will then be executed at the right point of time.
     */
    public abstract void doInterrupt();
}
