/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.project;

import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;
import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author pjl
 */
public class MultiPart implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Vector<Part> parts=new Vector<Part>();

    void add(MidiPart lastPart) {
        getParts().add(lastPart);
       assert(lastPart.getMultiPart() == null);
       lastPart.setMultiPart(this);
    }

    public Vector<Part> getParts() {
        return parts;
    }
    

}
