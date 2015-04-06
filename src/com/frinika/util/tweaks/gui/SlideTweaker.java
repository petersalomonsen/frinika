/*
 * Created on 23-Feb-2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.util.tweaks.gui;
import com.frinika.util.tweaks.Tweakable;


import javax.swing.*;
import javax.swing.event.*;

class SlideTweaker implements ChangeListener {

    
    JSlider slider;
    Tweakable t;

    SlideTweaker(TweakerPanel p,Tweakable t) {
	this.t=t;
	slider = new JSlider(((Number)t.getMinimum()).intValue(),
			     ((Number)t.getMaximum()).intValue(),
			     t.getNumber().intValue());
	slider.addChangeListener(this);
	p.add(new JLabel(t.getLabel()),slider);
    }


    public void stateChanged(ChangeEvent e) {
	t.set(new Integer(slider.getValue()));
    }
}
