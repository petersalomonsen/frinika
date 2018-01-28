// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.spi;

import static uk.org.toot.audio.id.ProviderId.TOOT_PROVIDER_ID;

/**
 * The Toot Audio ServiceProvider is implemented so that the
 * provider id and name are only used once.
 */
abstract public class TootAudioServiceProvider extends AudioServiceProvider
{
    public TootAudioServiceProvider(String description, String version) {
        super(TOOT_PROVIDER_ID, "Toot Software", description, version);
    }
}
