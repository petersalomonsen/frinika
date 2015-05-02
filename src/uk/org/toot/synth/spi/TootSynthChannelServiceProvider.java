// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.spi;

import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;

/**
 * The Toot Synth ServiceProvider is implemented so that the
 * provider id and name are only used once.
 */
abstract public class TootSynthChannelServiceProvider extends SynthChannelServiceProvider
{
    public TootSynthChannelServiceProvider(String description, String version) {
        super(TOOT_PROVIDER_ID, "Toot Software", description, version);
    }
}
