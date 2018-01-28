// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.List;

/**
 * A simple-mided AudioClient using the Composite design pattern.
 * May have concurrency issues !!! !!!
 * But work() shouldn't block on synchronisation so code lock-free.
 * 
 * Don't extend this for other functionality such as enablement, clients
 * form a tree but aren't currently aware of their parents so any added
 * functionality is currently not available to child clients.
 * API needs to be defined in AudioCient for best effect.
 * Perhaps we should be final to enforce this?
 */
public class CompoundAudioClient implements AudioClient
{
    private List<AudioClient> clients;
    private boolean enabled = true;

    public CompoundAudioClient() {
        clients = new java.util.ArrayList<AudioClient>();
    }

    public void work(int nFrames) {
        if ( !enabled ) return;
        for ( AudioClient client : clients ) {
            client.work(nFrames);
        }
    }

    public void setEnabled(boolean enable) {
        enabled = enable;
        for ( AudioClient client : clients ) {
            client.setEnabled(enable);
        }
    }

    public void add(AudioClient client) {
        if ( client == null ) return;
        clients.add(client);
    }

    public void remove(AudioClient client) {
        if ( client == null ) return;
        clients.remove(client);
    }
}
