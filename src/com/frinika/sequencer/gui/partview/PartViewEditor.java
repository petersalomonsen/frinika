/*
 * Created on Jan 18, 2006
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

package com.frinika.sequencer.gui.partview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;


import com.frinika.gui.ToolbarSeperator;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;

import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.ProjectLane;
import static com.frinika.localization.CurrentLocale.getMessage;
/**
 * Top level panel for the piano roll.
 * 
 * @author pjl
 * 
 */
public class PartViewEditor extends ItemScrollPane {

	private static final long serialVersionUID = 1L;

	PartView partView;

	// VoiceView voiceView;

	LaneHeaderPanel laneHeaderPanel;
	
	public void showNewMenu(Component caller)
	{
		partView.getProjectFrame().newLaneMenu.show(caller, 0, 0);
	}

	public PartViewEditor(final ProjectFrame frame) {

		final ProjectContainer project=frame.getProjectContainer();
		
		partView = new PartView(frame, this);
				
		Vector<ItemPanel> clients=new Vector<ItemPanel>();
		clients.add(partView);
		ItemRollToolBar toolBar = new ItemRollToolBar(clients,frame.getProjectContainer());
		JPanel new_panel = new JPanel();
		new_panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		new_panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		new_panel.setOpaque(false);
		
		Insets insets = new Insets(0, 0, 0, 0);
		final JButton new_button = new JButton(ProjectFrame.getIconResource("new_track.gif"));
		new_button.setText(getMessage("new_track")) ;
		new_button.setMargin(insets);
		new_button.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						showNewMenu(new_button);
					}
				});
		new_panel.add(new_button);
		
		JPanel sep = new JPanel();
		sep.setOpaque(false);
		sep.setMinimumSize(new Dimension(5,5));
		new_panel.add(sep);
		
		final JButton up_button = new JButton(ProjectFrame.getIconResource("uparrow.gif"));
		up_button.setMargin(insets);
		up_button.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						Collection<Lane> selected_lane = partView.getProjectContainer().getLaneSelection().getSelected();
						if(selected_lane.size() == 1)
						{
							List<Lane> c = partView.getProjectContainer().getProjectLane().getChildren();							
							Lane lane = selected_lane.iterator().next();
							if(lane instanceof ProjectLane) return;
							int li = c.indexOf(lane);
							if(li == 0) return;
							li--;							
							partView.getProjectContainer().getEditHistoryContainer().mark("Move Lane Up");							
							partView.getProjectContainer().remove(lane);
							partView.getProjectContainer().add(li, lane);
							partView.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();
						}						
					}
				});
		new_panel.add(up_button);
		
		final JButton down_button = new JButton(ProjectFrame.getIconResource("downarrow.gif"));
		down_button.setMargin(insets);
		down_button.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						Collection<Lane> selected_lane = partView.getProjectContainer().getLaneSelection().getSelected();
						if(selected_lane.size() == 1)
						{							
							List<Lane> c = partView.getProjectContainer().getProjectLane().getChildren();							
							Lane lane = selected_lane.iterator().next();
							if(lane instanceof ProjectLane) return;
							int li = c.indexOf(lane);
							if(li == c.size() - 1) return;
							li++;							
							partView.getProjectContainer().getEditHistoryContainer().mark("Move Lane Down");							
							partView.getProjectContainer().remove(lane);
							partView.getProjectContainer().add(li, lane);
							partView.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();
						}						
					}
				});
		new_panel.add(down_button);		
		
		
		toolBar.add(new_panel, 0);
		toolBar.add(new ToolbarSeperator(),1);
		
		partView.setToolBar(toolBar);
		setView(partView);
		toolBar.addButtonToTools("scissors16", "split", getMessage("sequencer.partview.split_part_tool_tip"));
		toolBar.addButtonToTools("glue", "glue", getMessage("sequencer.partview.glue_part_tool_tip"));

		
	//	toolBar.add(new ToolbarSeperator(),1);
		
	//	toolBar.add(new ToolbarSeperator());

		JButton palette = new JButton(ProjectFrame
				.getIconResource("pallete.png"));
		palette.setMargin(insets);
		palette.setToolTipText(getMessage("sequencer.partview.colour_palette_tip"));
		toolBar.add(palette);
		palette.addActionListener(new ActionListener() {

			JColorChooser colorChooser;

			public void actionPerformed(ActionEvent e) {

				if (colorChooser == null)
					colorChooser = new JColorChooser();
				colorChooser.setPreviewPanel(new JPanel());

				JDialog dialog = JColorChooser.createDialog(frame,
						"Part Colour", true, colorChooser,
						(ActionListener) null, (ActionListener) null);

				dialog.setVisible(true);

				Color col = colorChooser.getColor();
				if (col != null)
					for (Part part : project.getPartSelection().getSelected()) {
						part.setColor(col);
					}
				project.getEditHistoryContainer().notifyEditHistoryListeners();
			}

		});
		
		
		
		JSplitPane split = new JSplitPane();
		split.setRightComponent(partView);
		add(split, BorderLayout.CENTER);
		setToolBar(toolBar);
	
	
	//	int maxY = 10 * Layout.getLaneHeight;
	//	partView.yRangeModel.setRangeProperties(0, getHeight(), 0, maxY,false);
		
		// vertScroll.setValues(0, getHeight(), 0, maxY);
		vertScroll.setModel(partView.getYRangeModel());

		// horizScroll.setValues(0, 10, 0, x);
		// horizScroll.setUnitIncrement(20);
		horizScroll.setModel(partView.getXRangeModel());
		
		
		
		laneHeaderPanel = new LaneHeaderPanel(partView,frame);
		split.setLeftComponent(laneHeaderPanel);
		vertScroll.addAdjustmentListener(laneHeaderPanel);

		partView.setLaneHeader(laneHeaderPanel);
		rebuild();
	}

	
	public PartView getPartView() {
		return partView;
	}

	protected void rebuild() {
		// TODO this is naff
		laneHeaderPanel.visibleLanes.rebuild();
		int maxY = 50 * Layout.getLaneHeightScale();
		partView.getYRangeModel().setRangeProperties(0, getHeight(), 0, maxY,false);
		partView.setDirty();
		partView.repaint();
		laneHeaderPanel.rePositionItems();
	    laneHeaderPanel.repaint();	
	}


	@Override
	protected void vertZoom(int inc) {
		Layout.laneHeightIndex += inc;
		Layout.laneHeightIndex = Math.min(Layout.laneHeightIndex,Layout.laneItemHeights.length-1);
		Layout.laneHeightIndex = Math.max(Layout.laneHeightIndex,0);	// TODO Auto-generated method stub
		
	}

}
