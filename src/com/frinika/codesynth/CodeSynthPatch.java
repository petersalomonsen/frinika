/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */

package com.frinika.codesynth;

import javax.sound.midi.Patch;

/**
 *
 * @author Peter Johan Salomonsen
 */
class CodeSynthPatch extends Patch {

    public CodeSynthPatch(Patch patch) {
        super(patch.getBank(),patch.getProgram());
    }


    public CodeSynthPatch(int bank,int program) {
        super(bank, program);
    }

    @Override
    public boolean equals(Object obj) {
        if(Patch.class.isAssignableFrom(obj.getClass()) &&
                ((Patch)obj).getBank()==getBank() &&
                ((Patch)obj).getProgram()==getProgram())
                        return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return getProgram() + getBank() * 128;
    }    
}
