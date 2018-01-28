// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * An AudioProcess with a ChannelFormat and a name, suitable for hardware i/o.
 * @author st
 *
 */
public interface IOAudioProcess extends AudioProcess
{
    ChannelFormat getChannelFormat();

    String getName();
}
