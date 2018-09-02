// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.tap;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/**
 * We defer to our controls to create our buffer so that Taps has its array
 * set at control creation time such that reference counting is ready in time.
 * We are created too late after snapshot recall to do it ourselves.
 * @author st
 */
public class TapProcess implements AudioProcess
{
	private TapControls controls;
	private AudioBuffer tap;
	
	public TapProcess(TapControls controls) {
		this.controls = controls;
	}
	
	public void open() throws Exception {
		tap = controls.getBuffer();
	}

	public int processAudio(AudioBuffer buffer) {
		if ( controls.isInactive() ) return AUDIO_OK;
		// copy to local buffer
		tap.setChannelFormat(buffer.getChannelFormat());
		int ns = buffer.getSampleCount();
		int nc = buffer.getChannelCount();
		for ( int i = 0; i < nc; i++ ) {
			System.arraycopy(buffer.getChannel(i), 0, tap.getChannel(i), 0, ns);
		}
		return AUDIO_OK;
	}

	public void close() throws Exception {
		controls.removeBuffer();
	}
}
