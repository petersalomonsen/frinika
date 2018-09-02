/*
 * Created on Mar 6, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.base;

import javax.annotation.Nonnull;
import javax.sound.midi.Sequence;

/**
 * Base project container.
 *
 */
public class FrinikaControl {

    private static final FrinikaControl INSTANCE = new FrinikaControl();
    private ProjectHandler projectHandler = null;

    private FrinikaControl() {
    }

    public static FrinikaControl getInstance() {
        return INSTANCE;
    }

    public void registerProjectHandler(@Nonnull ProjectHandler projectHandler) {
        this.projectHandler = projectHandler;
    }

    public void openProject(@Nonnull Sequence sequence) {
        projectHandler.openProject(sequence);
    }

    public interface ProjectHandler {

        void openProject(@Nonnull Sequence sequence);
    }
}
