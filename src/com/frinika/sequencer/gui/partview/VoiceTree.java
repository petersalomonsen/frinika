package com.frinika.sequencer.gui.partview;

//import GM.javasound.JavaSoundSynth;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.patchname.MyPatch;
import com.frinika.sequencer.patchname.Node;
import com.frinika.sequencer.patchname.PatchNameMap;


/*
 * Created on Mar 14, 2006
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

/**
 * Used to select voices.
 */
public class VoiceTree extends JTree  {

	private static Icon instrument_icon =  new javax.swing.ImageIcon(ProjectFrame.class.getResource("/icons/instrument.gif"));
	
    public VoiceTree(PatchNameMap vl) {
        setModel( new DefaultTreeModel(myTreeRoot(vl)));
        setRootVisible(true);
        jbInit();
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(instrument_icon);
        setCellRenderer(renderer);
    }
    
    HashMap patchlist = new HashMap();
    public void select(MyPatch patch)
    {
    	DefaultMutableTreeNode treenode = (DefaultMutableTreeNode)patchlist.get(patch);
    	if(treenode == null) return;
    	
		TreePath path = new TreePath(treenode.getPath());
		setSelectionPath(path);
		Rectangle bounds =  getPathBounds(path);
		scrollRectToVisible(bounds);    	
    }


    public void jbInit() {
        this.setMinimumSize(new Dimension(0, 0));
    }

    /**
     * My stuff
     *
     */

    private void addNodes(DefaultMutableTreeNode tn,Vector<Node> list) {
    	
    	for (Node o : list) {
    		DefaultMutableTreeNode newNode=new DefaultMutableTreeNode(o);
    		if(o.getData() instanceof MyPatch)
    			patchlist.put(o.getData(), newNode);
    	    tn.add(newNode);
			if (o.getData() instanceof Vector) {
				addNodes(newNode,(Vector)(o.getData()));
			} 
    	}   	
    }
    
    private DefaultMutableTreeNode myTreeRoot(PatchNameMap vl) {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Instruments");
    	addNodes(root,vl.getList());
    	return root;
    }
    	
}
