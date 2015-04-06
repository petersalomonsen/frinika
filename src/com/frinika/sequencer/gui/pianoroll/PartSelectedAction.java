/*
 * Created on 28-May-2006
 *
 * Copyright (c) 2006 P.J.Leonard
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
package com.frinika.sequencer.gui.pianoroll;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;

/**
 *
 *  What to do when we select a part. e.g. position it for editing.
 *
 * @author pjl
 */
public class PartSelectedAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	ProjectContainer project;

	static boolean ignoreWarp = false;

	ItemPanel panel;

	public PartSelectedAction(ProjectContainer project, ItemPanel panel) {

		this.panel = panel;
		this.project = project;
	}

	public void actionPerformed(ActionEvent e) {

		// System.out.println("Warp to part left");
		if (ignoreWarp)
			return;
		Part focus = project.getPartSelection().getFocus();
		if (focus == null)
			return;
		// Rectangle rect = focus.getEventBounds();
		long left = focus.getStartTick();
		long tick1 = left - project.getSequence().getResolution(); // TODO this
																	// is naff
		if (tick1 < 0)
			tick1 = 0;
		double ticksToScreen = panel.userToScreen;

		int newVal = (int) (tick1 * ticksToScreen);
		if (newVal < panel.getXRangeModel().getValue()
				|| newVal > panel.getXRangeModel().getValue()
						+ panel.getXRangeModel().getExtent()) {
			panel.getXRangeModel().setValue((int) (tick1 * ticksToScreen));
		}

		// setX((int)(tick1*ticksToScreen));
		// setY((int)(pitchToScreen(rect.y+rect.height)));

		if (focus instanceof MidiPart) {
			int p[] = ((MidiPart) focus).getPitchRange();

			double midY = ((PianoRoll) panel).pitchToScreen((p[0] + p[1]) / 2);
			int val = (int) (midY - panel.getYRangeModel().getExtent() * 0.5);

			panel.getYRangeModel().setValue(val);
			// panel.yRangeModel.setValue((int) ((PianoRoll) panel)
			// .pitchToScreen(rect.y + rect.height));

		}
		if (!project.getSequencer().isRunning()) {
			project.getSequencer().setTickPosition(left);
		}
	}

}
