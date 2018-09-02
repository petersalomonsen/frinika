// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.audio.id.TootAudioControlsId.DYNAMICS_BASE_ID;

class DynamicsIds
{
    final static int LIMITER_ID = DYNAMICS_BASE_ID + 1;
    final static int COMPRESSOR_ID = DYNAMICS_BASE_ID + 2;
    final static int EXPANDER_ID = DYNAMICS_BASE_ID + 3;
    final static int GATE_ID = DYNAMICS_BASE_ID + 4;
    final static int DEESSER_ID = DYNAMICS_BASE_ID + 5; // !!!
    final static int MULTI_BAND_COMPRESSOR_ID = DYNAMICS_BASE_ID + 6;
    final static int TREMOLO_ID = DYNAMICS_BASE_ID + 7;
    final static int MID_SIDE_COMPRESSOR_ID = DYNAMICS_BASE_ID + 8;
    final static int VARI_MU_COMPRESSOR = DYNAMICS_BASE_ID + 9;
    final static int OPTO_COMPRESSOR = DYNAMICS_BASE_ID + 10;
    final static int BUS_COMPRESSOR = DYNAMICS_BASE_ID + 11;
}
