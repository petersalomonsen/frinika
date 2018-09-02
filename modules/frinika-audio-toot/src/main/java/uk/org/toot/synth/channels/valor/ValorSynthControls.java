package uk.org.toot.synth.channels.valor;

import static uk.org.toot.misc.Localisation.getString;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.*;
import uk.org.toot.synth.modules.filter.*;
import uk.org.toot.synth.modules.mixer.MixerControls;
import uk.org.toot.synth.modules.mixer.MixerVariables;
import uk.org.toot.synth.modules.mixer.ModulationMixerControls;
import uk.org.toot.synth.modules.mixer.ModulationMixerVariables;
import uk.org.toot.synth.modules.oscillator.*;
import static uk.org.toot.synth.id.TootSynthControlsId.VALOR_CHANNEL_ID;

/**
 * @author st
 *
 */
public class ValorSynthControls extends SynthChannelControls
{
	public static String NAME = "Valor";
	
	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
	private final static int OSC1_OFFSET 	= 0x00; // 8, 5 used, 3 free
	private final static int OSC2_OFFSET 	= 0x08; // 8, 5 used, 3 free
	private final static int OSC3_OFFSET 	= 0x10; // 8, 5 used, 3 free
	private final static int LFOVIB_OFFSET 	= 0x18; // 4, 3 used, 1 free
	private final static int VIB_MOD_OFFSET = 0x1C; // 4, 3 used, 1 free
	private final static int LPF_OFFSET 	= 0x20; // 4, 3 used, 1 free
	private final static int OSC3_MOD_OFFSET= 0x24; // 4, 2 used, 2 free
	private final static int LPFMIX_OFFSET 	= 0x28; // 4, 3 used, 1 free
	private final static int SVFMIX_OFFSET 	= 0x2D; // 4, 3 used, 1 free
	private final static int SVF_OFFSET 	= 0x30; // 8, 7 used, 1 free
	private final static int AMP_OFFSET 	= 0x38; // 2, 2 used
	private static final int VIBENV_OFFSET	= 0x3A; // 6, 6 used
	private final static int AMPENV_OFFSET 	= 0x40; // 8, 6 used, 2 free
	private final static int ENV1_OFFSET 	= 0x48; // 8, 6 used, 2 free
	private final static int ENV2_OFFSET 	= 0x50; // 8, 6 used, 2 free
	private final static int LPF_MOD_OFFSET = 0x58; // 8, 8 used
	private final static int SVF_MOD_OFFSET = 0x60; // 8, 8 used
	private final static int LFO1_OFFSET	= 0x68; // 4, 3 used, 1 free
	private final static int OSC1_MOD_OFFSET= 0x6C; // 4, 2 used, 2 free
	private final static int LFO2_OFFSET	= 0x70; // 4, 3 used, 1 free
	private final static int OSC2_MOD_OFFSET= 0x74; // 4, 2 used, 2 free
										//    0x78; // 8, 0 used, 8 free

	private MultiWaveOscillatorControls[] oscillatorControls;
	private FilterControls[] filterControls;
	private EnvelopeControls[] envelopeControls;
	private AmplifierControls amplifierControls;
	private LFOControls[] lfoControls;
	private MixerControls[] mixerControls;
	private ModulationMixerControls[] modulationControls;
	
	public ValorSynthControls() {
		super(VALOR_CHANNEL_ID, NAME);
		
		oscillatorControls = new MultiWaveOscillatorControls[4];
		filterControls = new FilterControls[2];
		envelopeControls = new EnvelopeControls[4];
		lfoControls = new LFOControls[3];
		mixerControls = new MixerControls[2];
		modulationControls = new ModulationMixerControls[6];
		
		LFOConfig widthLFOConfig = new LFOConfig();
		LFOConfig vibratoConfig = new LFOConfig();
		vibratoConfig.rateMin = 4f;
		vibratoConfig.rateMax = 7f;
		vibratoConfig.rate = 5.5f;
		vibratoConfig.deviationMax = 2f;
		vibratoConfig.deviation = 1.5f;		

		String lfoString = getString("LFO");
		String[] widthLabels = { lfoString+" 1", lfoString+" 2" };

		String widthModString = getString("Width")+" "+getString("Mod");
		String oscillatorString = getString("Oscillator");
		ControlRow oscRow = new ControlRow();
		oscillatorControls[0] = new MultiWaveOscillatorControls(0, oscillatorString+" 1", OSC1_OFFSET, false);
		oscRow.add(oscillatorControls[0]);
		modulationControls[0] = new ModulationMixerControls(0, widthModString, OSC1_MOD_OFFSET, widthLabels, true);
		oscRow.add(modulationControls[0]);
		oscillatorControls[1] = new MultiWaveOscillatorControls(1, oscillatorString+" 2", OSC2_OFFSET, false);
		oscRow.add(oscillatorControls[1]);
		modulationControls[1] = new ModulationMixerControls(1, widthModString, OSC2_MOD_OFFSET, widthLabels, true);
		oscRow.add(modulationControls[1]);
		oscillatorControls[2] = new MultiWaveOscillatorControls(2, oscillatorString+" 3", OSC3_OFFSET, false);
		oscRow.add(oscillatorControls[2]);
		modulationControls[2] = new ModulationMixerControls(2, widthModString, OSC3_MOD_OFFSET, widthLabels, true);
		oscRow.add(modulationControls[2]);
		add(oscRow);
		
		String envString = getString("Env");
		String[] cutoffLabels = { lfoString+" 1", lfoString+" 2", 
									envString+" 1", envString+" 2", 
									getString("Vel"), getString("AT"), getString("Wheel") };

		ControlRow lpfRow = new ControlRow();		
		mixerControls[0] = new MixerControls(0, getString("LPF")+" "+oscillatorString+" "+getString("Mix"), LPFMIX_OFFSET, 3);
		lpfRow.add(mixerControls[0]);
		filterControls[0] = new MoogFilterControls(0, getString("Low.Pass")+" "+getString("Filter"), LPF_OFFSET);
		lpfRow.add(filterControls[0]);
		modulationControls[3] = new ModulationMixerControls(3, getString("Cutoff")+" "+getString("Mod"), LPF_MOD_OFFSET, cutoffLabels, true, 64, "semitones");
		lpfRow.add(modulationControls[3]);
		add(lpfRow);

		ControlRow svfRow = new ControlRow();		
		mixerControls[1] = new MixerControls(1, getString("SVF")+" "+oscillatorString+" "+getString("Mix"), SVFMIX_OFFSET, 3);
		svfRow.add(mixerControls[1]);
		filterControls[1] = new StateVariableFilterControls(0, getString("State.Variable")+" "+getString("Filter"), SVF_OFFSET);
		svfRow.add(filterControls[1]);
		modulationControls[4] = new ModulationMixerControls(4, getString("Cutoff")+" "+getString("Mod"), SVF_MOD_OFFSET, cutoffLabels, true, 64, "semitones");
		svfRow.add(modulationControls[4]);
		add(svfRow);

		ControlRow envRow = new ControlRow();
		envelopeControls[1] = new EnvelopeControls(1, getString("Envelope")+" 1", ENV1_OFFSET);
		envRow.add(envelopeControls[1]);
		envelopeControls[2] = new EnvelopeControls(2, getString("Envelope")+" 2", ENV2_OFFSET);
		envRow.add(envelopeControls[2]);
		envelopeControls[0] = 
			new EnvelopeControls(0, getString("Amplifier")+" "+getString("Envelope"), AMPENV_OFFSET); 
		envRow.add(envelopeControls[0]);
		amplifierControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET);
		envRow.add(amplifierControls);
		add(envRow);
		
		String[] vibLabels = { "Env", "AT", "Wheel" };

		ControlRow lfoRow = new ControlRow();
		lfoControls[1] = new LFOControls(1, lfoString+" 1", LFO1_OFFSET, widthLFOConfig);
		lfoRow.add(lfoControls[1]);
		lfoControls[2] = new LFOControls(2, lfoString+" 2", LFO2_OFFSET, widthLFOConfig);
		lfoRow.add(lfoControls[2]);
		lfoControls[0] = new LFOControls(0, getString("Vibrato"), LFOVIB_OFFSET, vibratoConfig);
		lfoRow.add(lfoControls[0]);
		envelopeControls[3] = 
			new EnvelopeControls(3, getString("Vibrato")+" "+getString("Envelope"), VIBENV_OFFSET, "D", 5f); 
		lfoRow.add(envelopeControls[3]);
		modulationControls[5] = new ModulationMixerControls(5, getString("Vibrato")+" "+getString("Mod"), VIB_MOD_OFFSET, vibLabels, false);
		lfoRow.add(modulationControls[5]);
		add(lfoRow);
		
	}

	public MultiWaveOscillatorVariables getOscillatorVariables(int instance) {
		return oscillatorControls[instance];
	}
	
	public FilterVariables getFilterVariables(int instance) {
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
	
	public MixerVariables getMixerVariables(int instance) {
		return mixerControls[instance];
	}

	public ModulationMixerVariables getModulationMixerVariables(int instance) {
		return modulationControls[instance];
	}
}
