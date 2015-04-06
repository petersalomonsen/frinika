/*
 * Created on 3.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import javax.swing.JOptionPane;

public class ObjectInputStreamFixer extends ObjectInputStream {

	public ObjectInputStreamFixer(InputStream in) throws IOException {
		super(in);
	}

	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		ObjectStreamClass osc = super.readClassDescriptor();		
		ObjectStreamClass target_osc = ObjectStreamClass.lookup(Class.forName(osc.getName()));
		if(target_osc.getSerialVersionUID() != osc.getSerialVersionUID())
		{
			String[] options = {"Yes", "No"}; 
			int sel = JOptionPane.showOptionDialog(null, "SerializedVersionUID mismatch for class " + osc.getName() + "!\n"+
					osc.getSerialVersionUID() + " != " + target_osc.getSerialVersionUID() + "\n" +
					"Do you want to ignore this error?", "Read Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,null, options, options[0]);
			if(sel == 0) return target_osc; // ObjectStreamClass.lookup(Class.forName("com.frinika.sequencer.model.MidiPlayOptions"));
		}		
		return osc;
	}

	

}
