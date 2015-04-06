
/*
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

package com.frinika.tootX.midi;

import java.util.Hashtable;
import uk.org.toot.control.Control;

/**
 * 
 *  Associates controls with a string.
 * 
 *  Helps reconstructing midi to control mapping
 * 
 *
 * @author pjl
 */
public class ControlResolver {

    Hashtable<String,Control> map;
    
    public ControlResolver() {
        map=new Hashtable<String,Control>();  
    }
    
    public Control resolve(String key) {
      Control cntrl=map.get(key);
      return cntrl;
    }

    public void register(Control control) {
        map.put(generateKey(control),control);
    }
    
    String generateKey(Control control) {
        String str=control.getName() + "_" + control.getId();
 
        if (control.getParent() != null) {
            return generateKey(control.getParent())+":"+str;
        }
        
        return str;
    }
    
}
