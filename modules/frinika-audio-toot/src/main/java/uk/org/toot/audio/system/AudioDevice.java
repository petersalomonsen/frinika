// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import java.util.List;
import uk.org.toot.misc.IObservable;

/**
 * An arbitrary composition of AudioInput and AudioOutput instances.
 * AudioInputs and AudioOutputs may not be added by a public API, they
 * are expected to be added by an instance.
 * 
 * @author Steve Taylor
 */
public interface AudioDevice extends IObservable
{
	/**
	 * Return a unique name for the device.
	 * @return String - the unique name
	 */
    String getName();

   /**
     * Get the list of AudioInputs for this AudioDevice.
     * @supplierCardinality 0..*
     * @link aggregationByValue
     */
    /*#AudioInput lnkAudioInput;*/
    List<AudioInput> getAudioInputs();

    /**
     * Get the list of AudioOutputs for this AudioDevice.
     * @supplierCardinality 0..*
     * @link aggregationByValue
     */
    /*#AudioOutput lnkAudioOutput;*/
    List<AudioOutput> getAudioOutputs();
    
    void closeAudio();
}
