/*
 * Created on May 24, 2005
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
package com.frinika.tracker.filedialogs;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class OpenGearDialog extends JDialog {
private static final long serialVersionUID = 1L;

    JProgressBar progressBar;
    
    public OpenGearDialog(JFrame frame)
    {
        super(frame,true);
        
        this.setResizable(false);
        this.setUndecorated(true);
        try
        {
            
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
            setLayout(new GridLayout(0,1));
            
            JLabel lb = new JLabel("Please wait while loading");
            lb.setFont(new Font(lb.getFont().getName(),Font.BOLD,lb.getFont().getSize()*2));
            add(lb);
            add(progressBar);
           
            this.setSize(getPreferredSize());
            
            this.setLocationRelativeTo(frame);
        } catch(Exception e) {}
    }

    public void setProgress(int n,String name)
    {
        if(n==100)
            OpenGearDialog.this.dispose();
        else
        {
            progressBar.setValue(n);
            progressBar.setString(name);
        }
    }    
}
