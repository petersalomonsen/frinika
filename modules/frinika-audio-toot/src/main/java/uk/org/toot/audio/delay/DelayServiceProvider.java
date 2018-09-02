// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.spi.TootAudioServiceProvider;

import static uk.org.toot.misc.Localisation.*;

public class DelayServiceProvider extends TootAudioServiceProvider
{
    public DelayServiceProvider() {
        super(getString("Delay"), "0.2");
		String family = description;
        addControls(
            ModulatedDelayControls.class,
            DelayIds.MODULATED_DELAY_ID,
            getString("Modulated.Delay"),
            family,
            "0.2");
        addControls(
            MultiTapDelayStereoControls.class,
            DelayIds.MULTI_TAP_DELAY_ID,
            getString("Stereo.Multi.Tap.Delay"),
            family,
            "0.1",
            ChannelFormat.STEREO,
            null);
        addControls(
        	TempoDelayControls.class,
        	DelayIds.TEMPO_DELAY_ID,
        	getString("BPM.Delay"),
        	family,
        	"0.2");
        addControls(
            PhaserControls.class,
            DelayIds.PHASER_ID,
            getString("Phaser"),
            family,
            "0.1",
            ChannelFormat.MONO,
            null);
        addControls(
            WowFlutterControls.class,
            DelayIds.WOW_FLUTTER_ID,
            getString("Wow & Flutter"),
            family,
            "0.1");
        addControls(
            CabMicingControls.class,
            DelayIds.CAB_MICING_ID,
            getString("Cab.Mic"),
            "EQ",
            "0.1");
        
        add(ModulatedDelayProcess.class, getString("Modulated.Delay"), family, "0.2");
        add(MultiTapDelayProcess.class, getString("Multi.Tap.Delay"), family, "0.1");
        add(TempoDelayProcess.class, getString("BPM.Delay"), family, "0.2");
        add(PhaserProcess.class, getString("Phaser"), family, "0.1");
        add(WowFlutterProcess.class, getString("Wow & Flutter"), family, "0.1");
        add(CabMicingProcess.class, getString("Cab.Mic"), "EQ", "0.1"); // note family!
    }

    public AudioProcess createProcessor(AudioControls c) {
        if ( c instanceof ModulatedDelayProcess.Variables ) {
            return new ModulatedDelayProcess((ModulatedDelayProcess.Variables)c);
        } else if ( c instanceof MultiTapDelayProcess.Variables ) {
            return new MultiTapDelayProcess((MultiTapDelayProcess.Variables)c);
        } else if ( c instanceof TempoDelayControls ) {
        	return new TempoDelayProcess((TempoDelayControls)c);
        } else if ( c instanceof PhaserControls ) {
        	return new PhaserProcess((PhaserControls)c);
        } else if ( c instanceof CabMicingProcess.Variables ) {
            return new CabMicingProcess((CabMicingProcess.Variables)c);
        } else if ( c instanceof WowFlutterProcess.Variables ) {
            return new WowFlutterProcess((WowFlutterProcess.Variables)c);
        }
        return null; // caller then tries another provider
    }
    
    /*
     * In place of the obsolete stero modulated delay, we return modulated delay
     * (non-Javadoc)
     * @see uk.org.toot.audio.spi.AudioServiceProvider#createControls(int)
     */
    @Override
    public AudioControls createControls(int moduleId) {
        if ( moduleId == DelayIds.STEREO_MODULATED_DELAY_ID ) {
            return super.createControls(DelayIds.MODULATED_DELAY_ID);
        }
        return super.createControls(moduleId);
    }
}
