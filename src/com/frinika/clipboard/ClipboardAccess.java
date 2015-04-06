/*
 * Created on Feb 26, 2006
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
package com.frinika.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Methods for getting access to a clipboard instance
 * 
 * Why shouldn't you call the getSystemClipboard() method 
 * directly? Because this method requires clipboardAccess priveleges, and in case
 * you want to make an applet of Frinika, you could modify this getClipboard method
 * to return a local clipboard rather than the system clipboard (In case you want to use
 * an unsigned applet)
 * 
 * @author Peter Johan Salomonsen
 */
public class ClipboardAccess {
    
    static Clipboard defaultClipboard = new Clipboard("Frinika");
    
    /**
     * Returns the currently active clipboard. 
     * @return
     */
    public static Clipboard getClipboard()
    {
        
// Problem using systemClipboard on windows?
//        return Toolkit.getDefaultToolkit().getSystemClipboard();
        return defaultClipboard;
    }
    
    /**
     * A simple test of putting an object on the clipboard and getting it back again.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        getClipboard().setContents(new Transferable() {

            public DataFlavor[] getTransferDataFlavors() {
                
                return new DataFlavor[] { DataFlavor.stringFlavor };
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return true;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

                return "Hello world";
            }},new ClipboardOwner() {

                public void lostOwnership(Clipboard clipboard, Transferable contents) {
                    System.out.println("Lost ownership");
                    
                }});
        System.out.println(getClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor));
    }
}
