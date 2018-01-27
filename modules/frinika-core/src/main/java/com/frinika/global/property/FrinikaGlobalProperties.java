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

import java.awt.Font;
import java.io.File;

/**
 * Enumeration of configuration properties.
 *
 * @author hajdam
 */
public class FrinikaGlobalProperties {

    public static final ConfigurationProperty<Boolean> SETUP_DONE = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.SETUP_DONE, false);
    public static final ConfigurationProperty<String> THEME = new ConfigurationProperty<>(String.class, FrinikaGlobalProperty.THEME, null);
    public static final ConfigurationProperty<Integer> TICKS_PER_QUARTER = new ConfigurationProperty<>(Integer.class, FrinikaGlobalProperty.TICKS_PER_QUARTER, 128);
    public static final ConfigurationProperty<Integer> SEQUENCER_PRIORITY = new ConfigurationProperty<>(Integer.class, FrinikaGlobalProperty.SEQUENCER_PRIORITY, 0);
    // used by JavaSoundVoiceServer
    // TODO: IS THIS IN MILLISECONDS? OTHERWISE CONVERSION MILLISECONDS (as in gui) <-> AUDIO_BUFFER_LENGTH is needed 
    public static final ConfigurationProperty<Integer> AUDIO_BUFFER_LENGTH = new ConfigurationProperty<>(Integer.class, FrinikaGlobalProperty.AUDIO_BUFFER_LENGTH, 512);
    public static final PackedStringListProperty MIDIIN_DEVICES_LIST = new PackedStringListProperty(FrinikaGlobalProperty.MIDIIN_DEVICES_LIST, "");
    public static final ConfigurationProperty<Boolean> DIRECT_MONITORING = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.DIRECT_MONITORING, false);
    public static final ConfigurationProperty<Boolean> MULTIPLEXED_AUDIO = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.MULTIPLEXED_AUDIO, false);
    public static final ConfigurationProperty<Boolean> BIG_ENDIAN = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.BIG_ENDIAN, false);
    public static final ConfigurationProperty<Integer> SAMPLE_RATE = new ConfigurationProperty<>(Integer.class, FrinikaGlobalProperty.SAMPLE_RATE, 44100);
    public static final ConfigurationProperty<Boolean> JACK_AUTO_CONNECT = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.JACK_AUTO_CONNECT, false);
    public static final ConfigurationProperty<Boolean> AUTOMATIC_CHECK_FOR_NEW_VERSION = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.AUTOMATIC_CHECK_FOR_NEW_VERSION, true);
    // deprecated I believe PJL
    //	public static int OS_LATENCY_MILLIS = 0;
    //	public static Meta _OS_LATENCY_MILLIS;
    public static final ConfigurationProperty<Boolean> MAXIMIZE_WINDOW = new ConfigurationProperty<>(Boolean.class, FrinikaGlobalProperty.MAXIMIZE_WINDOW, false);
    public static final ConfigurationProperty<Float> MOUSE_NUMBER_DRAG_INTENSITY = new ConfigurationProperty<>(Float.class, FrinikaGlobalProperty.MOUSE_NUMBER_DRAG_INTENSITY, 2.0f);
    public static final ConfigurationProperty<Font> TEXT_LANE_FONT = new ConfigurationProperty<>(Font.class, FrinikaGlobalProperty.TEXT_LANE_FONT, new Font("Arial", Font.PLAIN, 8));
    // Directories
    public static final ConfigurationProperty<File> GROOVE_PATTERN_DIRECTORY = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.GROOVE_PATTERN_DIRECTORY, new File(System.getProperty("user.home"), "frinika/groovepatterns/"));
    public static final ConfigurationProperty<File> SCRIPTS_DIRECTORY = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.SCRIPTS_DIRECTORY, new File(System.getProperty("user.home"), "frinika/scripts/"));
    public static final ConfigurationProperty<File> PATCHNAME_DIRECTORY = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.PATCHNAME_DIRECTORY, new File(System.getProperty("user.home"), "frinika/patchname/"));
    public static final ConfigurationProperty<File> AUDIO_DIRECTORY = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.AUDIO_DIRECTORY, new File(System.getProperty("user.home"), "frinika/audio/"));
    public static final ConfigurationProperty<File> SOUNDFONT_DIRECTORY = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.SOUNDFONT_DIRECTORY, new File(System.getProperty("user.home"), "frinika/soundfonts/"));
    public static final ConfigurationProperty<File> DEFAULT_SOUNDFONT = new ConfigurationProperty<>(File.class, FrinikaGlobalProperty.DEFAULT_SOUNDFONT, new File(System.getProperty("user.home"), "frinika/soundfonts/8MBGMSFX.SF2"));
    // Recent files
    public static final ConfigurationProperty<String> LAST_PROJECT_FILENAME = new ConfigurationProperty<>(String.class, FrinikaGlobalProperty.LAST_PROJECT_FILENAME, null);
    public static final ConfigurationProperty<String> LAST_PROJECT_TYPE = new ConfigurationProperty<>(String.class, FrinikaGlobalProperty.LAST_PROJECT_TYPE, null);
    public static final ConfigurationProperty<String> LAST_PROJECT_NAME = new ConfigurationProperty<>(String.class, FrinikaGlobalProperty.LAST_PROJECT_NAME, null);
    public static final RecentProjectsProperty RECENT_PROJECTS = new RecentProjectsProperty(FrinikaGlobalProperty.RECENT_PROJECTS, null);

    public static void initialize() {
        getSampleRate();
    }

    public static int getSampleRate() {
        return SAMPLE_RATE.getValue();
    }
}
