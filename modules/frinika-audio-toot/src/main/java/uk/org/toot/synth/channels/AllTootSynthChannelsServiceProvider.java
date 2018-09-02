package uk.org.toot.synth.channels;

import uk.org.toot.synth.SynthChannel;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.spi.TootSynthChannelServiceProvider;
import uk.org.toot.synth.channels.valor.*;
import uk.org.toot.synth.channels.pluck.*;
import uk.org.toot.synth.channels.copal.*;
import uk.org.toot.synth.channels.whirl.*;
import uk.org.toot.synth.channels.total.*;
import uk.org.toot.synth.channels.nine.*;

import static uk.org.toot.synth.id.TootSynthControlsId.*;

/**
 * This class provides the services of all builtin Toot synth channels.
 * @author st
 */
public class AllTootSynthChannelsServiceProvider extends TootSynthChannelServiceProvider
{
	public AllTootSynthChannelsServiceProvider() {
		super("Toot Synth Channels", "0.2");
		String name;
		name = ValorSynthControls.NAME;
		addControls(ValorSynthControls.class, VALOR_CHANNEL_ID, name, 
				"Virtual Analog Polyphonic", "0.2");
		add(ValorSynthChannel.class, name, "Valor", "0.2");
		name = PluckSynthControls.NAME;
		addControls(PluckSynthControls.class, PLUCK_CHANNEL_ID, name, 
				"Physically Modelled Plucked String", "0.1");
		add(PluckSynthChannel.class, name, "Pluck", "0.1");
		name = CopalSynthControls.NAME;
		addControls(CopalSynthControls.class, COPAL_CHANNEL_ID, name, 
				"Paraphonic Sring Ensemble", "0.2");
		add(CopalSynthChannel.class, name, "Cepal", "0.2");
		name = WhirlSynthControls.NAME;
		addControls(WhirlSynthControls.class, WHIRL_CHANNEL_ID, name, 
				"Virtual Analog Monophonic", "0.2");
		add(WhirlSynthChannel.class, name, "Whirl", "0.2");
		name = TotalSynthControls.NAME;
		addControls(TotalSynthControls.class, TOTAL_CHANNEL_ID, name, 
				"Digital Polyphonic", "0.1");
		add(TotalSynthChannel.class, name, "Total", "0.1");
		name = NineSynthControls.NAME;
		addControls(NineSynthControls.class, NINE_CHANNEL_ID, name, 
				"Hammond Drawbar Organ", "0.1");
		add(NineSynthChannel.class, name, "Nine", "0.1");
	}

	public SynthChannel createSynthChannel(SynthChannelControls c) {
		if ( c instanceof ValorSynthControls ) {
			return new ValorSynthChannel((ValorSynthControls)c);
		} else if ( c instanceof PluckSynthControls ) {
			return new PluckSynthChannel((PluckSynthControls)c);
		} else if ( c instanceof CopalSynthControls ) {
			return new CopalSynthChannel((CopalSynthControls)c);
		} else if ( c instanceof WhirlSynthControls ) {
			return new WhirlSynthChannel((WhirlSynthControls)c);
		} else if ( c instanceof TotalSynthControls ) {
			return new TotalSynthChannel((TotalSynthControls)c);
		} else if ( c instanceof NineSynthControls ) {
			return new NineSynthChannel((NineSynthControls)c);
		}
		return null;
	}
}
