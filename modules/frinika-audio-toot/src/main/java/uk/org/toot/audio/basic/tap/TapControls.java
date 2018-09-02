// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.tap;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.Taps;
import uk.org.toot.control.CompoundControl;

import static uk.org.toot.audio.basic.BasicIds.TAP;

/**
 * We do buffer creation because we can do it early enough for reference counting
 * to work with snapshot recall. Our process is too late to do this.
 * @author st
 */
public class TapControls extends AudioControls
{
	private int refCount = 0;
	private AudioBuffer buffer;
	
	public TapControls() {
		super(TAP, "Tap");
		// force bypass off and disable ui
		getBypassControl().setValue(false);
		getBypassControl().setEnabled(false);
	}

	@Override
	protected void setParent(CompoundControl parent) {
		super.setParent(parent);
        if ( parent != null ) {
            buffer = Taps.create(this); // needs parent so done here
        } else {
            removeBuffer();
        }
	}
	
	public void removeBuffer() {
        if ( buffer == null ) return;
		Taps.remove(buffer);
        buffer = null;
	}
	
	public AudioBuffer getBuffer() {
		return buffer;
	}
	
	public void reference(int ref) {
		if ( ref != 1 && ref != -1 ) {
			throw new IllegalArgumentException("argument must be +/- 1");
		}
		refCount += ref;
		if ( refCount < 0 ) {
			refCount = 0;
			System.err.println("refCount < 0");
		}
	}
	
	/*
	 * Only allow deletion if not in use to avoid clients being left
	 * with a detached and useless AudioBuffer
	 * (non-Javadoc)
	 * @see uk.org.toot.control.CompoundControl#canBeDeleted()
	 */
	@Override
    public boolean canBeDeleted() { return isInactive(); }
	
	boolean isInactive() { return refCount == 0; }
}
