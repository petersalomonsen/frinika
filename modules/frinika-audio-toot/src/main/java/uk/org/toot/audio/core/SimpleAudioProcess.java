// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

/**
 * A simple AudioProcess with empty open and close implementations.
 * Many AudioProcesses don't need open and close implementations.
 */
abstract public class SimpleAudioProcess implements AudioProcess
{
    public void open() {}

    public void close() {}
}
