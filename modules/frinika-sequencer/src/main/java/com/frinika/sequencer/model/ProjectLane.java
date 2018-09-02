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

import com.frinika.model.EditHistoryRecordable;
import com.frinika.sequencer.project.AbstractProjectContainer;
import com.frinika.sequencer.project.SequencerProjectSerializer;
import java.util.Vector;
import javax.swing.Icon;

/*
 * Top level lane container for a project.
 * A project has one of these and it is the root for all the other lanes.
 * 
 * The intention is that the part for this lane will provide an view and interface move editing complete vertical sections of the project.
 */
public class ProjectLane extends Lane {

    private static final long serialVersionUID = 1L;

    /**
     * This field is used for backward compatibility / serialization purposes
     * only.
     */
    @Deprecated
    public SequencerProjectSerializer project = null;

    Vector<LaneTreeListener> laneTreeListeners;

    {
        parts = new Vector<>();
    }

    /**
     * Public constructor for de-externalization.
     */
    public ProjectLane() {
        super();
    }

    public ProjectLane(AbstractProjectContainer project) {
        super("project", project);
        setHidden(true);
    }

    @Override
    public void restoreFromClone(EditHistoryRecordable object) {
        // TODO Auto-generated method stub	
    }

    public void notifyViewChanged() {
        for (LaneTreeListener l : laneTreeListeners) {
            l.fireLaneTreeChanged();
        }
    }

    @Override
    public Selectable deepCopy(Selectable parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deepMove(long tick) {
        // TODO Auto-generated method stub

    }

    @Override
    public long rightTickForMove() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Part createPart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }
}
