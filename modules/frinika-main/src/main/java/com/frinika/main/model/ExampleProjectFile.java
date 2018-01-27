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
package com.frinika.main.model;

import javax.annotation.Nonnull;

/**
 *
 * @author hajdam
 */
public enum ExampleProjectFile {
    PASGRAVE("C'est pas grave", "pasgrave.frinika"),
    FRINIKATION("Frinikation", "frinikation.frinika"),
    KARL_FRINIKA_SONG("Karl - Frinika Song", "karl-0_4_0_beta2_thefrinikasong.frinika"),
    KARL_SLOW_STRINGS("Karl - Slow Strings", "karl-0_4_0_slowstrings.frinika"),
    KARL_FM_DREAMS("Karl - FM Dream", "karl-0_4_0_fmdream.frinika"),
    TEA_PARTY("Tea Party", "peter_salomonsen-teaparty-0_4_0_compressed.frinika"),
    TRACKER_SLAVE("Tracker Slave", "PeterSalomonsen_TrackerSlave.frinika.bz2");

    @Nonnull
    private final String name;
    @Nonnull
    private final String fileName;

    private ExampleProjectFile(@Nonnull String name, @Nonnull String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
    }
}
