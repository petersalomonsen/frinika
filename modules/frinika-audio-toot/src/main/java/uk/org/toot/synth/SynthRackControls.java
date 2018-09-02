// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import uk.org.toot.control.CompoundControl;
//import uk.org.toot.control.Control;

import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;;

/**
 * A List of SynthControls.
 * The root of the tree of all synth controls.
 * This class contains SynthControls, they contain CompoundControls for each channel.
 * These channel CompoundControls typically contain other CompoundControls for different
 * modules but ultimately the leaves of the tree are Controls.
 * @author st
 */
public class SynthRackControls extends CompoundControl
{
	private SynthControls[] synthControls;
	private int nsynths;
	
	public SynthRackControls(int nsynths) {
		super(2, "Synth Rack"); // 2, audio.mixer.MixerControls uses 1
		synthControls = new SynthControls[nsynths];
		this.nsynths = nsynths;
	}
	
	public int size() {
		return nsynths;
	}

	public SynthControls getSynthControls(int synth) {
		return synthControls[synth];
	}
	
	public void setSynthControls(int synth, SynthControls controls) {
		CompoundControl old = synthControls[synth];
		if ( old != null ) {
			remove(old);
		}
		if ( controls != null ) {
			String name = controls.getName();
	        if ( find(name) != null ) {
	        	disambiguate(controls);
	        	controls.setAnnotation(name); // annotation isn't disambiguated
	        }
			add(controls);			
		}
		synthControls[synth] = controls;
		setChanged();
		notifyObservers(synth);
	}
	
	public void removeAll() {
		for ( int synth = 0; synth < nsynths; synth++ ) {
			setSynthControls(synth, null);
		}
	}
	
	public int getProviderId() {
		return TOOT_PROVIDER_ID;
	}
	
	// causes plugins to show Preset menu
	public boolean isPluginParent() { 
		return true; 
	}
	
    // return a domain specific string for preset organisation
	// to avoid id collisions from different domains
    // i.e. audio, synth
    public String getPersistenceDomain() {
    	return "synth";
    }
}
