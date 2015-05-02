// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import java.util.List;
import uk.org.toot.misc.IObservable;

/**
 * A composition of AudioDevices
 */
public interface AudioSystem extends IObservable
{
	void addAudioDevice(AudioDevice device);

	void removeAudioDevice(AudioDevice device);
	
    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     */
    /*#AudioDevice lnkAudioDevice;*/
    List<AudioDevice> getAudioDevices();
    
    List<AudioInput> getAudioInputs();

    List<AudioOutput> getAudioOutputs();
    
    void setAutoConnect(boolean autoConnect);
    
    /**
     * Close all AudioDevices
     */
    void close();
}
