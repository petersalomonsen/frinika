package uk.org.toot.audio.distort;

import java.awt.Color;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.IntegerControl;
import uk.org.toot.control.IntegerLaw;

import static uk.org.toot.misc.Localisation.getString;

public class BitCrusherControls extends AudioControls implements BitCrusherProcess.Variables
{
	private static IntegerLaw BITS_LAW = new IntegerLaw(4, 16, "bits");

	private float precision = 1 << ((int)(BITS_LAW.getMaximum())-1);
	
	public BitCrusherControls() {
		super(DistortionIds.BIT_CRUSH, getString("BitCrush"));
		IntegerControl bitsControl = new IntegerControl(0, getString("Bits"), BITS_LAW, 1f, 8) {
			public void derive(Control c) {
				precision = 1 << (getUserValue()-1);
			}
		};
		bitsControl.setInsertColor(Color.DARK_GRAY);
		add(bitsControl);
	}
	
	public float getPrecision() { 
		return precision; 
	}
}
