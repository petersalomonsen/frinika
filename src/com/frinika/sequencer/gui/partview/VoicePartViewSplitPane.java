package com.frinika.sequencer.gui.partview;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Lane;

public class VoicePartViewSplitPane extends JPanel implements
SelectionListener<Lane>  {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	LaneView laneView;
	PartViewEditor partViewEditor;
	ProjectFrame project;
	JSplitPane splitPane=new JSplitPane();
	LaneView nullView=new LaneView(null);
	
	boolean showVoiceView=true;
	
	public JComponent getLaneView()
	{
		return this;
	}
	
	public JComponent getPartViewEditor()
	{
		return partViewEditor;
	}			
	
	boolean dockmode;
	
	public VoicePartViewSplitPane(ProjectFrame project, boolean dockmode) {
		this.project=project;
		this.dockmode = dockmode;
		setLayout(new BorderLayout());
		partViewEditor = new PartViewEditor(project);
		Lane lane=project.getProjectContainer().getProjectLane();
		laneView = new LaneView(lane);
		laneView.setEnabled(false);
		
		if(dockmode)
		{
			add(laneView);			
		}
		else
		{
			add(splitPane,BorderLayout.CENTER);
			splitPane.add(partViewEditor,JSplitPane.RIGHT);
			splitPane.setResizeWeight(0.0);
			splitPane.add(laneView, JSplitPane.LEFT);
		}
		project.getProjectContainer().getLaneSelection().addSelectionListener(this);
	//	toggleVoiceView();
	}
	
	public void toggleVoiceView() {
		
		if(dockmode) return;
		showVoiceView = !showVoiceView;
		
		if (showVoiceView ) {
			remove(partViewEditor);			
			add(splitPane,BorderLayout.CENTER);
			splitPane.add(partViewEditor,JSplitPane.RIGHT);
			
		} else {
			splitPane.remove(partViewEditor);
			remove(splitPane);
			add(partViewEditor,BorderLayout.CENTER);
		}
		validate();
	}


	void dispose() {
		project.getProjectContainer().getLaneSelection().removeSelectionListener(this);
	}



	public void selectionChanged(SelectionContainer<? extends Lane> src) {

		Lane lane = src.getFocus();
	
		if (lane == null) return;
		
		LaneHeaderItem header = null;
		for (Component c : partViewEditor.laneHeaderPanel.getComponents()) {
			if (c instanceof LaneHeaderItem) {
				LaneHeaderItem h = (LaneHeaderItem) c;
				Lane il = h.lane;
				if (il == lane ) {
					header = h;
					break;
				}
			}
		}			

		LaneView newVoiceView = nullView;
		
		if (header != null) newVoiceView=header.voiceView;
		
		if (laneView != newVoiceView) {
			
			if(dockmode)
			{
				// PJL defensive programming to get rounf null pointers TODO 
				if (laneView !=null) remove(laneView);
					laneView = newVoiceView; 
				if (laneView != null) {
					add(laneView);
					laneView.setEnabled(true);
				}
				validate();
				repaint();
				partViewEditor.repaint();
			}
			else
			{
				remove(laneView);
				splitPane.setTopComponent(laneView = newVoiceView); //, BorderLayout.WEST);
				laneView.setEnabled(true);
				repaint();
			}
		}
		
	}
	public PartView getPartview() {
		return partViewEditor.getPartView();
	}
}
