// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.id;

public interface TootAudioControlsId
{
    // allow for 16 in each category
    // THESE CONSTANTS MUST NEVER BE CHANGED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	final static int EQ_BASE_ID = 0x00;
    final static int DELAY_BASE_ID = 0x10;
    final static int DYNAMICS_BASE_ID = 0x20;
    final static int BASIC_BASE_ID = 0x30;

    final static int REVERB_BASE_ID = 0x50;
    final static int TOOL_BASE_ID = 0x60; // !!!
    final static int DISTORT_BASE_ID = 0x70;
    // AUTOMATION_ID = 119
    final static int MIXER_BASE_ID = 120; // actually 125 is lowest used for busses
    // because strip ids aren't used for modules in a strip!
}
