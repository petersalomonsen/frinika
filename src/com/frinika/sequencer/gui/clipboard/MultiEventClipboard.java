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
package com.frinika.sequencer.gui.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;

import static com.frinika.clipboard.ClipboardAccess.getClipboard;
import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.sequencer.FrinikaSequencer;

import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;

/**
 * The MultiEventClip board uses the global clipboard with a MultiEvent
 * DataFlavor type.
 * 
 * @author Peter Johan Salomonsen
 */
public class MultiEventClipboard {

	double quantization = 1;

	private static final MultiEventClipboard defaultMultiEventClipboard = new MultiEventClipboard();

	/**
	 * @return Returns the defaultMultiEventClipboard.
	 */
	public static MultiEventClipboard getDefaultMultiEventClipboard() {
		return defaultMultiEventClipboard;
	}

	/**
	 * 
	 * @param multiEvents
	 * @param referenceTick
	 */
	public void copy(Collection<MultiEvent> multiEvents, long referenceTick) {
		final MultiEventClipboardData multiEventClipBoardData = new MultiEventClipboardData(
				referenceTick, multiEvents);

		System.out.println(" CLIPBOARD COPY ");
			getClipboard().setContents(new Transferable() {

			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { new MultiEventDataFlavor() };
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return true;
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				// TODO Auto-generated method stub
				return multiEventClipBoardData;
			}
		}, new ClipboardOwner() {

			public void lostOwnership(Clipboard clipboard, Transferable contents) {

			}
		});
	}

	/**
	 * 
	 * @param destinationGroup
	 *            the destination MultiEvent group
	 * @param destinationReferenceTick
	 *            the destination reference tick
	 */
	private void pasteOrig(MidiPart destinationGroup,
			long destinationReferenceTick) {
		System.out.println(" CLIPBOARD PASTE ORIG ");
		
		try {
			MultiEventClipboardData data = (MultiEventClipboardData) getClipboard()
					.getContents(null).getTransferData(
							new MultiEventDataFlavor());
			destinationGroup.getEditHistoryContainer().mark(
					getMessage("sequencer.menu.edit.lcase.paste"));
            
            destinationGroup.getLane().getProject().getMultiEventSelection().clearSelection();
            
            for (MultiEvent event : data.getClonedMultiEvents()) {
				event.setStartTick(event.getStartTick()
						+ destinationReferenceTick);
				destinationGroup.add(event);
				destinationGroup.getLane().getProject().getMultiEventSelection().addSelected(event);
				destinationGroup.getLane().getProject().getMultiEventSelection().notifyListeners();
			}
			destinationGroup.getEditHistoryContainer()
					.notifyEditHistoryListeners();

		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param destinationTrack
	 *            the destination track
	 * @param destinationReferenceTick
	 *            the destination reference tick
	 */
	public void paste(MidiPart destinationGroup,EditHistoryContainer history,
			long destinationReferenceTick, FrinikaSequencer sequencer) {
	
		if (sequencer == null || destinationGroup != null ) {
			pasteOrig(destinationGroup, destinationReferenceTick);
			return;
		}
	

		try {
			MultiEventClipboardData data = (MultiEventClipboardData) getClipboard()
					.getContents(null).getTransferData(
							new MultiEventDataFlavor());
	
			long startTick = Long.MAX_VALUE;

			for (MultiEvent event : data.getClonedMultiEvents()) {
				long tickOn = event.getStartTick();
				if (tickOn < startTick) {
					startTick = tickOn;

				}
			}

			long deltaTick = sequencer.getTickPosition() - startTick;

			destinationReferenceTick = (long)( Math.floor(deltaTick / quantization)
					* quantization);
			
			history.mark(
							getMessage("sequencer.menu.edit.lcase.paste"));
			
            // TODO: Wouldn't have to use iterator if the multiEvents was a list
            data.getClonedMultiEvents().iterator().next().getPart().getLane().getProject().getMultiEventSelection().clearSelection();

            for (MultiEvent event : data.getClonedMultiEvents()) {
				event.setStartTick(event.getStartTick()
						+ destinationReferenceTick);
				event.getPart().add(event);
                event.getPart().getLane().getProject().getMultiEventSelection().addSelected(event);
                assert(false); // FIXME ?
				//destinationGroup.add(event);
			}
			
			history.notifyEditHistoryListeners();
		    // project.getMultiEventSelection().notifyListeners();
			assert(false); // FIXME if no0t deprecated
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setQuantization(double quantization) {
		this.quantization = quantization;
	}
}
