/*
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.global.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Enumeration of configuration properties.
 *
 * @author hajdam
 */
public enum FrinikaGlobalProperty {

    SETUP_DONE,
    THEME,
    TICKS_PER_QUARTER,
    SEQUENCER_PRIORITY,
    AUDIO_BUFFER_LENGTH,
    MIDIIN_DEVICES_LIST,
    DIRECT_MONITORING,
    MULTIPLEXED_AUDIO,
    BIG_ENDIAN,
    // lowercase spelling for 'historic' reasons
    SAMPLE_RATE("sampleRate"),
    JACK_AUTO_CONNECT,
    AUTOMATIC_CHECK_FOR_NEW_VERSION,
    MAXIMIZE_WINDOW,
    MOUSE_NUMBER_DRAG_INTENSITY,
    TEXT_LANE_FONT,
    GROOVE_PATTERN_DIRECTORY,
    SCRIPTS_DIRECTORY,
    PATCHNAME_DIRECTORY,
    AUDIO_DIRECTORY,
    SOUNDFONT_DIRECTORY,
    DEFAULT_SOUNDFONT,
    LAST_PROJECT_FILENAME,
    LAST_PROJECT_TYPE,
    LAST_PROJECT_NAME,
    RECENT_PROJECTS;

    private final String propertyName;

    private FrinikaGlobalProperty() {
        propertyName = name();
    }

    private <T> FrinikaGlobalProperty(@Nullable String name) {
        propertyName = name == null ? name() : name;
    }

    @Nonnull
    public String getName() {
        return propertyName;
    }
}
