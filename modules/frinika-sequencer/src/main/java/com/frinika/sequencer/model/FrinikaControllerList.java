/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
 * 
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
package com.frinika.sequencer.model;

import com.frinika.audio.model.ControllerListProvider;
import com.frinika.sequencer.gui.pianoroll.ControllerHandle;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.ShortMessage;

public class FrinikaControllerList implements ControllerListProvider {

    private static List<Object> controllers = null;

    @Override
    public List<Object> getList() {
        if (controllers == null) {
            controllers = new ArrayList<>();
            controllers.add(new ControllerHandle(Handles.VELOCITY.name, 0, 127, 0, ShortMessage.NOTE_ON));
            controllers.add(new ControllerHandle(Handles.VIBRATO.name, 0, 127, 1, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.VOLUME.name, 0, 127, 7, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.PAN.name, 0, 127, 10, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.DISTORTION.name, 0, 127, 20, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.ECHO.name, 0, 127, 22, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.ECHO_LENGTH.name, 0, 127, 23, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.SUSTAIN.name, 0, 127, 64, ShortMessage.CONTROL_CHANGE));
            controllers.add(new ControllerHandle(Handles.PITCH_BEND.name, -8192, 16383 - 8192, 64, ShortMessage.PITCH_BEND));
        }

        return controllers;
    }

    public enum Handles {
        VELOCITY("Velocity"),
        VIBRATO("Vibrato"),
        VOLUME("Volume"),
        PAN("Pan"),
        DISTORTION("Distortion"),
        ECHO("Echo"),
        ECHO_LENGTH("Echo length"),
        SUSTAIN("Sustain"),
        PITCH_BEND("Pitch Bend");

        private final String name;

        private Handles(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
