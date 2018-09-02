package uk.org.toot.synth.channels.pluck;

//import static uk.org.toot.localisation.Localisation.getString;
import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;
import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.id.TootSynthControlsId.PLUCK_CHANNEL_ID;

/**
 * @author st
 *
 */
public class PluckSynthControls extends SynthChannelControls
{
	public static String NAME = "Pluck";
	
	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
//	private final static int LFOVIB_OFFSET 	= 0x18;
	private final static int STRING_OFFSET  = 0x00;
	private final static int AMP_OFFSET 	= 0x38;

	private StringControls stringControls;
	private AmplifierControls amplifierControls;

//	private DelayedLFOControls[] lfoControls;
	
	public PluckSynthControls() {
		super(PLUCK_CHANNEL_ID, NAME);
		
/*		lfoControls = new DelayedLFOControls[1];
		
		LFOConfig vibratoConfig = new LFOConfig();
		vibratoConfig.rateMin = 4f;
		vibratoConfig.rateMax = 7f;
		vibratoConfig.rate = 5.5f;
		vibratoConfig.deviationMax = 2f;
		vibratoConfig.deviation = 1.5f;		
		vibratoConfig.hasLevel = true;

		ControlRow osc0row = new ControlRow();
		lfoControls[0] = new DelayedLFOControls(0, "Vibrato", LFOVIB_OFFSET, vibratoConfig);
		osc0row.add(lfoControls[0]);
		add(osc0row); */
		ControlRow row = new ControlRow();
		stringControls = new StringControls();
		row.add(stringControls);		
		amplifierControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET);
		row.add(amplifierControls);
		add(row);
	}

	public float getPickup() {
		return stringControls.getPickup();
	}
	
	public float getPick() {
		return stringControls.getPick();
	}
	
	public float getVelocityTrack() {
		return amplifierControls.getVelocityTrack();
	}
	
	public float getLevel() {
		return amplifierControls.getLevel();
	}
	
/*	public DelayedLFOControls getLFOVariables(int instance) {
		return lfoControls[instance];
	} */
	
	protected class StringControls extends CompoundControl
	{
		private FloatControl pickupControl;
		private FloatControl pickControl;
		
		public StringControls() {
			super(STRING_OFFSET, "String");
			// TODO Auto-generated constructor stub
			pickupControl = createControl(0, "P/up");
			pickControl = createControl(1, "Pick");
			add(pickupControl);
			add(pickControl);
		}

		private FloatControl createControl(int id, String name) {
			FloatControl control = new FloatControl(id, name, LinearLaw.UNITY, 0.001f, 0.2f);
			control.setInsertColor(Color.WHITE);
			return control;
		}
		
		public float getPickup() {
			return pickupControl.getValue();
		}
		
		public float getPick() {
			return pickControl.getValue();
		}
	}
}
