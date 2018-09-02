// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import java.util.List;

public interface ConnectedAudioSystem extends AudioSystem
{
//    void createAudioConnection(String fromPortName, String toPortName, int flags);
 
//    void closeAudioConnection(String fromPortName, String toPortName);

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     */
    /*#AudioConnection lnkAudioConnection;*/
    List<AudioConnection> getConnections();
}
