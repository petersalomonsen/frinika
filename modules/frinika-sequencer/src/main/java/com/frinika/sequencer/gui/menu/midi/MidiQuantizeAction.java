/*
 * Created on Feb 7, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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
package com.frinika.sequencer.gui.menu.midi;

import com.frinika.gui.OptionsDialog;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.Quantization;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;

/**
 * Menu-action for quantizing selected MIDI notes.
 *
 * @author Jens Gulden
 */
public class MidiQuantizeAction extends AbstractMidiAction {

    public Quantization q = new Quantization();

    public MidiQuantizeAction(AbstractProjectContainer project) {
        super(project, "sequencer.midi.quantize");
    }

    @Override
    protected OptionsDialog createDialog(Component parent) {
        OptionsDialog dialog = super.createDialog(parent);
        dialog.setRepackDelta(new Dimension(150, 20));
        return dialog;
    }

    @Override
    protected JComponent createGUI() {
        return new MidiQuantizeActionEditor(this);
    }

    @Override
    public void modifyNoteEvent(NoteEvent note) {
        q.quantize(note);
    }

    @Override
    public AbstractProjectContainer getProject() {
        return (AbstractProjectContainer) super.getProject();
    }
}
