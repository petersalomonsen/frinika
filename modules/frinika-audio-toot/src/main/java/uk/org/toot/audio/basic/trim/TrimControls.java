// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.trim;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.audio.basic.BasicIds.TRIM;
import static uk.org.toot.misc.Localisation.*;

/**
 * @author st
 */
public class TrimControls extends AudioControls
{
	private static LinearLaw TRIM_LAW = new LinearLaw(-20f, 20f, "dB");
	private float trim = 1f;
	
	public TrimControls() {
		super(TRIM, getString("Trim"));
		FloatControl trimControl = new FloatControl(0, getString("Trim"), TRIM_LAW, 0.01f, 0f) {
		    protected void derive(Control obj) {
		    	trim = (float)TVolumeUtils.log2lin(getValue());
		    }
		};
		add(trimControl);
	}
	
	public float getTrim() {
		return trim;
	}
}
