package uk.org.toot.synth.channels.whirl;

import static uk.org.toot.misc.Localisation.getString;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.modules.GlideControls;
import uk.org.toot.synth.modules.GlideVariables;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.*;
import uk.org.toot.synth.modules.filter.*;
import uk.org.toot.synth.modules.mixer.ModulationMixerControls;
import uk.org.toot.synth.modules.mixer.ModulationMixerVariables;
import uk.org.toot.synth.modules.oscillator.*;
import static uk.org.toot.synth.id.TootSynthControlsId.WHIRL_CHANNEL_ID;

/**
 * @author st
 *
 */
public class WhirlSynthControls extends SynthChannelControls
{
	public static String NAME = "Whirl";
	
	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
	private final static int OSC1_OFFSET 	= 0x00;
	private final static int OSC2_OFFSET 	= 0x08;
	private final static int LFOVIB_OFFSET 	= 0x18;
//	private final static int LPF_OFFSET 	= 0x20;
	private final static int SVF_OFFSET 	= 0x30;
	private final static int AMP_OFFSET 	= 0x38;
	private final static int AMPENV_OFFSET 	= 0x40;
	private final static int GLIDE_OFFSET 	= 0x48;
	private final static int MODENV_OFFSET 	= 0x50;
	private final static int OSC1_TUNING_MOD_OFFSET = 0x58;
	private final static int OSC1_PWM_MOD_OFFSET = 0x60;
	private final static int OSC2_PWM_MOD_OFFSET = 0x68;
	private final static int SVF_CUTOFF_MOD_OFFSET = 0x70;
	

	private DualMultiWaveOscillatorControls[] oscillatorControls;
	private DualStateVariableFilterControls[] filterControls;
	private EnvelopeControls[] envelopeControls;
	private AmplifierControls amplifierControls;
	private ModulationMixerControls[] modulationControls;
	private LFOControls[] lfoControls;
	private GlideControls glideControls;
	
	public WhirlSynthControls() {
		super(WHIRL_CHANNEL_ID, NAME);
		
		oscillatorControls = new DualMultiWaveOscillatorControls[2];
		filterControls = new DualStateVariableFilterControls[2];
		envelopeControls = new EnvelopeControls[2];
		modulationControls = new ModulationMixerControls[4];
		lfoControls = new LFOControls[1];
		
		LFOConfig lfoConfig = new LFOConfig();
		lfoConfig.rateMin = 0.1f;
		lfoConfig.rateMax = 10f;
		lfoConfig.rate = 1f;
		lfoConfig.deviationMax = 0f;

		ControlRow row1 = new ControlRow();
		oscillatorControls[0] = new DualMultiWaveOscillatorControls(0, getString("Main")+" "+getString("Osc"), OSC1_OFFSET, false);
		row1.add(oscillatorControls[0]);
		oscillatorControls[1] = new DualMultiWaveOscillatorControls(1, getString("Sub")+" "+getString("Osc"), OSC2_OFFSET, true);
		row1.add(oscillatorControls[1]);
		filterControls[0] = new DualStateVariableFilterControls(0, getString("Filter"), SVF_OFFSET);
		row1.add(filterControls[0]);
		amplifierControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET);
		row1.add(amplifierControls);
		add(row1);
		
		ControlRow row2 = new ControlRow();
		String[] labels = { getString("LFO"), getString("Env") };
		modulationControls[0] = new ModulationMixerControls(0, getString("Sync")+" "+getString("Mod"), OSC1_TUNING_MOD_OFFSET, labels, false);
		row2.add(modulationControls[0]);
		modulationControls[1] = new ModulationMixerControls(0, getString("Main")+" "+getString("PWM"), OSC1_PWM_MOD_OFFSET, labels, true);
		row2.add(modulationControls[1]);
		modulationControls[2] = new ModulationMixerControls(0, getString("Sub")+" "+getString("PWM"), OSC2_PWM_MOD_OFFSET, labels, true);
		row2.add(modulationControls[2]);
		String[] flabels = { getString("LFO"), getString("Env"), getString("Vel"), getString("AT"), getString("Wheel") };
		modulationControls[3] = new ModulationMixerControls(0, getString("Filter")+" "+getString("Cutoff")+" "+getString("Mod"), SVF_CUTOFF_MOD_OFFSET, 
				flabels, true, 64, getString("semitones"));
		row2.add(modulationControls[3]);
		add(row2);
		
		ControlRow row3 = new ControlRow();
		lfoControls[0] = new LFOControls(0, getString("LFO"), LFOVIB_OFFSET, lfoConfig);
		row3.add(lfoControls[0]);
		envelopeControls[1] = 
			new EnvelopeControls(1, getString("Modulation")+" "+getString("Envelope"), MODENV_OFFSET);
		row3.add(envelopeControls[1]);
		envelopeControls[0] = 
			new EnvelopeControls(0, getString("Amplifier")+" "+getString("Envelope"), AMPENV_OFFSET); 
		row3.add(envelopeControls[0]);
		glideControls = new GlideControls(GLIDE_OFFSET);
		row3.add(glideControls);
		add(row3);
		
	}

	public DualMultiWaveOscillatorVariables getOscillatorVariables(int instance) {
		return oscillatorControls[instance];
	}
	
	public DualStateVariableFilterVariables getFilterVariables(int instance) {
		return filterControls[instance];
	}
	
	public EnvelopeVariables getEnvelopeVariables(int instance) {
		return envelopeControls[instance];
	}
	
	public AmplifierVariables getAmplifierVariables() {
		return amplifierControls;
	}
	
	public LFOControls getLFOVariables(int instance) {
		return lfoControls[instance];
	}
	
	public ModulationMixerVariables getModulationMixerVariables(int instance) {
		return modulationControls[instance];
	}
	
	public GlideVariables getGlideVariables() {
		return glideControls;
	}
}
