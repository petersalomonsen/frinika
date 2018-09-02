// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.synthui;

import java.util.Vector;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.service.ServiceDescriptor;
import uk.org.toot.service.ServiceVisitor;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.SynthChannelServices;
import uk.org.toot.synth.synths.multi.MultiSynthControls;

public class MultiSynthPanel extends MultiControlPanel
{
	private static Vector<String> selectionNames = new Vector<String>();

	static {
		selectionNames.add(NONE);
		try {
			SynthChannelServices.accept(
				new ServiceVisitor() {
					public void visitDescriptor(ServiceDescriptor d) {
						selectionNames.add(d.getName());
					}
				}, SynthChannelControls.class
			);
		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

	public MultiSynthPanel(MultiSynthControls controls) {
		super(controls, 16, "Channels");
	}
	
	protected Vector<String> getSelectionNames() {
		return selectionNames;
	}
	
	protected String getAnnotation(int chan) {
		return String.valueOf(1+chan);
	}

	private MultiSynthControls getControls() {
		return (MultiSynthControls)multiControls;
	}
	
	protected CompoundControl getControls(int chan) {
		return getControls().getChannelControls(chan);
	}
	
	protected void setControls(int chan, CompoundControl controls) {
		try {
			getControls().setChannelControls(chan, (SynthChannelControls)controls);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	protected CompoundControl createControls(String name) {
		return SynthChannelServices.createControls(name);
	}
	
}
