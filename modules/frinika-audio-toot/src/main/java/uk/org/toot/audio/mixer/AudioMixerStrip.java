// Copyright (C) 2006,2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import uk.org.toot.audio.core.*;
import static uk.org.toot.audio.mixer.MixerControlsIds.*;

/**
 * An AudioMixerStrip is an AudioProcessChain which can be connected to by
 * means of setInputProcess() and setDirectOutputProcess() and allows arbitrary
 * insertion and ordering of plugin modules.
 */
public class AudioMixerStrip extends AudioProcessChain {
    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
    protected AudioMixer mixer;

    /**
     * @link aggregation
     * @supplierCardinality 0..1
     */
    private AudioBuffer buffer = null;

    private AudioBuffer.MetaInfo metaInfo;

    /**
     * @supplierCardinality 0..1
     * @link aggregation 
     * @label input
     */
    private AudioProcess input = null;

    private AudioProcess directOutput = null;

    private boolean isChannel = false;

    private ChannelFormat channelFormat;

    private int nmixed = 1;

    public AudioMixerStrip(AudioMixer mixer, AudioControlsChain controlsChain) {
        super(controlsChain);
        this.mixer = mixer;
        buffer = createBuffer(); // side effects !
		channelFormat = buffer.getChannelFormat();
    }

    public AudioProcess getInputProcess() {
    	return input;
    }
    
    public void setInputProcess(AudioProcess input) throws Exception {
        if ( controlChain.getId() != CHANNEL_STRIP ) {
            throw new IllegalArgumentException("No external input to this Strip type");
        }
        AudioProcess oldInput = this.input;
        if ( input != null ) input.open();
        this.input = input;
        if ( input == null ) {
        	metaInfo = null;
            controlChain.setMetaInfo(null);        	
        }
        // don't close old until new is properly set
        if ( oldInput != null ) oldInput.close();
    }

    public AudioProcess getDirectOutputProcess() {
    	return directOutput;
    }
    
    public void setDirectOutputProcess(AudioProcess output) throws Exception {
    	AudioProcess oldOutput = directOutput;
        if ( output != null ) output.open();
        this.directOutput = output;
        if ( oldOutput != null ) oldOutput.close();
    }

    public void silence() {
        if ( nmixed > 0 ) {
	        buffer.makeSilence();
            nmixed = 0;
        }
    }

    protected AudioBuffer createBuffer() {
        int id = controlChain.getId();
        if ( id == CHANNEL_STRIP ) {
            isChannel = true;
	        return mixer.getSharedBuffer();
        } else if ( id == GROUP_STRIP ) {
            AudioBuffer buf = mixer.createBuffer(getName());
            buf.setChannelFormat(mixer.getMainBus().getBuffer().getChannelFormat());
            return buf;
        } else if ( id == MAIN_STRIP ) {
	        return mixer.getMainBus().getBuffer();
        }
        // must be a bus strip, so bus called the same as this
        return mixer.getBus(getName()).getBuffer();
    }

    private final static int silenceCount = 1000; // at least 1000ms worth
    private int silenceCountdown = silenceCount;
    
	protected boolean processBuffer() {
		int ret = AUDIO_OK;
        if ( isChannel ) {
        	if ( input != null ) {
        	    ret = input.processAudio(buffer);
                checkMetaInfo(buffer.getMetaInfo());
                if ( ret == AUDIO_DISCONNECT ) {
                    processMutations();
                    return false; // fast exit if input disconnected 
                } else if ( ret == AUDIO_SILENCE && silenceCountdown == 0 ) {                     
                    return false; // fast exit if convolution effects finished 
                }
            } else {
                processMutations();
                return false; // fast exit if no input
            }
        }
        processAudio(buffer);
        if ( isChannel ) {
        	if ( ret == AUDIO_SILENCE ) {
        		if ( buffer.square() > 0.00000001f ) silenceCountdown = silenceCount;
        		else silenceCountdown--;
        	} else {
        		silenceCountdown = silenceCount;
        	}
        }
        if ( directOutput != null ) {
            directOutput.processAudio(buffer);
        }
        return true;
    }

    protected void checkMetaInfo(AudioBuffer.MetaInfo info) {
        if ( metaInfo == info ) return; // no change
        metaInfo = info;
        controlChain.setMetaInfo(metaInfo);
    }

    // intercept the SPI mechanism to handle bus mix controls
	protected AudioProcess createProcess(AudioControls controls) {
        if ( controls instanceof MixVariables ) {
            MixVariables vars = (MixVariables)controls;
            AudioMixerStrip routedStrip;
            if ( vars.getName().equals(mixer.getMainBus().getName()) ) {
                routedStrip = mixer.getMainStrip();
                return new MainMixProcess(routedStrip, (MainMixVariables)vars, mixer);
            } else {
				routedStrip = mixer.getStripImpl(vars.getName());
	            return new MixProcess(routedStrip, vars);
            }
        }
        return super.createProcess(controls);
	}

	public int mix(AudioBuffer bufferToMix, float[] gain) {
        if ( bufferToMix == null ) return 0;
        int ret = channelFormat.mix(buffer, bufferToMix, gain);
        if ( ret != 0 ) nmixed += 1;
        return ret;
    }
}


