package com.frinika.sequencer.gui.pianoroll;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemScrollPane;

import com.frinika.sequencer.model.EditHistoryListener;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.util.EventFilter;

public abstract class PianoRollPanelAdapter  extends ItemPanel implements EditHistoryListener,EventFilter {


	
	public PianoRollPanelAdapter(final ProjectContainer project, ItemScrollPane scroller,
			boolean hasTimeLine, boolean canScrollY) {
		super(project,scroller, hasTimeLine, canScrollY,.5,false);
	
	}
	
	@Override
	public double getSnapQuantization() {
		return this.project.getPianoRollSnapQuantization();
	}

	@Override
	public void setSnapQuantization(double quant) {
		this.project.setPianoRollSnapQuantization(quant);
		repaintItems();
	}

	@Override
	public boolean isSnapQuantized() {
		return this.project.isPianoRollSnapQuantized();
	}
	@Override
	public void setSnapQuantized(boolean b) {
		this.project.setPianoRollSnapQuantized(b);
/*		
		
		quantize = b;
		if (b)
			MultiEventClipboard.getDefaultMultiEventClipboard()
					.setQuantization(snaptoQuantization);
		else
			MultiEventClipboard.getDefaultMultiEventClipboard()
					.setQuantization(1);
*/
	}
	
	@Override
	public void setFocus(Item item) {
		this.project.getMultiEventSelection().setFocus((MultiEvent) item);
		this.project.getPartSelection().notifyListeners();
		
	}
	
	@Override
	public void clientNotifySelectionChange() {
		this.project.getPartSelection().notifyListeners();	
	}
	
	@Override
	public void setTimeAtX(int x) {
		long tick = screenToTickAbs(x, this.project.isPianoRollSnapQuantized());
		this.sequencer.setTickPosition(tick);
	}

}
