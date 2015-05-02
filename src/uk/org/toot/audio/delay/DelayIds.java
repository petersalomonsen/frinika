// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import static uk.org.toot.audio.id.TootAudioControlsId.DELAY_BASE_ID;

class DelayIds
{
    // STEREO_MODULATED_DELAY is for backwards compatibility
    final static int STEREO_MODULATED_DELAY_ID = DELAY_BASE_ID + 1;
    final static int MULTI_TAP_DELAY_ID = DELAY_BASE_ID + 2;
    final static int ROOM_SIMULATOR = DELAY_BASE_ID + 3;
    final static int MODULATED_DELAY_ID = DELAY_BASE_ID + 4; // mono modulation
    final static int TEMPO_DELAY_ID = DELAY_BASE_ID + 5;
    final static int PHASER_ID = DELAY_BASE_ID + 6;
    final static int CAB_MICING_ID = DELAY_BASE_ID + 7;
    final static int WOW_FLUTTER_ID = DELAY_BASE_ID + 8;
}
