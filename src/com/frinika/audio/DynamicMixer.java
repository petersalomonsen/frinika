/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.audio;

import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.mixer.MixControls;

/**
 *
 * @author pjl
 */
public interface DynamicMixer {

    public MixControls addMixerInput(AudioProcess myProcess, String string);

    public void removeStrip(String tag);

}
