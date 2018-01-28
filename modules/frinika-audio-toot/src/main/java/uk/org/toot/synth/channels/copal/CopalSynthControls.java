package uk.org.toot.synth.channels.copal;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.id.TootSynthControlsId.COPAL_CHANNEL_ID;
import uk.org.toot.control.FloatControl;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.ASREnvelopeControls;
import uk.org.toot.synth.modules.envelope.ASREnvelopeVariables;
import uk.org.toot.synth.modules.filter.FormantFilterControls;
import uk.org.toot.synth.modules.filter.FormantFilterVariables;
import uk.org.toot.synth.modules.filter.LP1pHP1pControls;
import uk.org.toot.synth.modules.filter.LP1pHP1pVariables;
import uk.org.toot.synth.modules.mixer.MixerControls;
import uk.org.toot.synth.modules.mixer.MixerVariables;

public class CopalSynthControls extends SynthChannelControls
{
	public static String NAME = "Copal";

	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
	private final static int LPHP_OFFSET 	= 0x20;
	private final static int MIX_OFFSET 	= 0x28;
	private final static int AMP_OFFSET 	= 0x38;
	private final static int AMPENV_OFFSET 	= 0x40;
	private final static int FORMANT_OFFSET = 0x50;
	
	private MixerControls mixerControls;
	private LP1pHP1pControls lphpControls;
	private ASREnvelopeControls envelopeControls;
	private AmplifierControls amplifierControls;
	private FormantFilterControls formantControls;

	public CopalSynthControls() {
		super(COPAL_CHANNEL_ID, NAME);
		ControlRow row = new ControlRow();
		mixerControls = new MixerControls(0, getString("Oscillators"), MIX_OFFSET, 3) {
		    public boolean isAlwaysVertical() { return true; }
			protected FloatControl createLevelControl(int i) {
				FloatControl c = super.createLevelControl(i);
				String reg = "2";
				if ( i == 1 ) reg = "4";
				else if ( i == 2 ) reg = "8";
				c.setName(reg+"'");
				return c;
			}
		};
		row.add(mixerControls);
		lphpControls = new LP1pHP1pControls(0, 0, getString("Filter"), LPHP_OFFSET) {
		    public boolean isAlwaysVertical() { return true; }
		};
		row.add(lphpControls);
		envelopeControls = new ASREnvelopeControls(0, getString("Envelope"), AMPENV_OFFSET) {
		    public boolean isAlwaysVertical() { return true; }
		};
		row.add(envelopeControls);
		formantControls = new FormantFilterControls(0, getString("Formant")+" "+getString("Filter"), FORMANT_OFFSET);
		row.add(formantControls);
		amplifierControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET) {
		    public boolean isAlwaysVertical() { return true; }
		};
		row.add(amplifierControls);
		add(row);
	}
	
	public MixerVariables getMixerVariables() {
		return mixerControls;
	}
	
	public LP1pHP1pVariables getLPHPVariables() {
		return lphpControls;
	}
	
	public ASREnvelopeVariables getEnvelopeVariables() {
		return envelopeControls;
	}

	public AmplifierVariables getAmplifierVariables() {
		return amplifierControls;
	}
	
	public FormantFilterVariables getFormantFilterVariables() {
		return formantControls;
	}
}
