// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.multi;

import java.util.Observable;
import java.util.Observer;

import uk.org.toot.audio.system.AudioOutput;
import uk.org.toot.synth.BasicMidiSynth;
import uk.org.toot.synth.SynthChannel;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.SynthChannelServices;

/**
 * This class allows each SynthChannel to be externally set.
 * @author st
 *
 */
public class MultiMidiSynth extends BasicMidiSynth
{
	public MultiMidiSynth(final MultiSynthControls controls) {
		super(controls.getName());
		controls.addObserver(
			new Observer() {
				public void update(Observable obs, Object obj) {
					if ( obj instanceof Integer ) {
						int chan = ((Integer)obj).intValue();
						if ( chan < 0 || chan > 15 ) return;
						SynthChannelControls channelControls = controls.getChannelControls(chan);
						if ( channelControls != null ) {
							// SPI lookup plugin SynthChannel for these controls
							SynthChannel synthChannel = SynthChannelServices.createSynthChannel(channelControls);
							if ( synthChannel == null ) {
								System.err.println("No SynthChannel for SynthControls "+channelControls.getName());
							} else {
								synthChannel.setLocation(MultiMidiSynth.this.getLocation()+" Channel "+(1+chan));
								synthChannel.addObserver(channelControls);
							}
							setChannel(chan, synthChannel);
						} else {
							setChannel(chan, null);
						}
					}
				}					
			}
		);
	}

	protected void setChannel(int chan, SynthChannel synthChannel) {
		SynthChannel old = getChannel(chan);
		if ( old != null && old instanceof AudioOutput ) {
			removeAudioOutput((AudioOutput)old);
		}
		super.setChannel(chan, synthChannel);
		if ( synthChannel != null && synthChannel instanceof AudioOutput ) {
			addAudioOutput((AudioOutput)synthChannel);
		}
	}
}
