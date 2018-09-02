// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import uk.org.toot.audio.core.ChannelFormat;

/**
 * MixVariables defines a contract required for mixing to a Mixable.
 */
public interface MixVariables {
// for asynchronous routing
    String getName();
    boolean isEnabled(); // from mute/solo
    boolean isMaster();
    float getGain();
    void getChannelGains(float[] dest);
    ChannelFormat getChannelFormat();
}


