package com.frinika.sequencer.gui.selection;

import java.util.Collection;

import com.frinika.sequencer.model.Selectable;
/**
 * Generic interface for SelectionContainers for Cut/Copy/Paste operations.
 * @author Paul
 *
 */
public interface SelectionFocusable {
	public Collection<Selectable> getObjects();
	public void clearSelection();
}
