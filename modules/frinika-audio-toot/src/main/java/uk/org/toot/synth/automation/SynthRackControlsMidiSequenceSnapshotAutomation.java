// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.automation;

import javax.sound.midi.*;

import uk.org.toot.audio.system.AudioSystem;
import uk.org.toot.control.*;
import uk.org.toot.control.automation.MidiPersistence;
import uk.org.toot.control.automation.MidiSequenceSnapshotAutomation;
import uk.org.toot.synth.ChannelledSynthControls;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.SynthControls;
import uk.org.toot.synth.SynthRackControls;
import uk.org.toot.synth.SynthChannelServices;
import uk.org.toot.synth.SynthServices;
import uk.org.toot.synth.synths.multi.MultiSynthControls;

import static uk.org.toot.control.automation.ControlSysexMsg.*;
import static uk.org.toot.midi.message.MetaMsg.*;
import static uk.org.toot.midi.message.NoteMsg.*;
import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;
import static uk.org.toot.synth.id.TootSynthControlsId.VSTI_SYNTH_ID;

/**
 * Stores and recalls synth rack automation snaphots as Midi Sequences.
 * To concretise this class extend it and:
 *  Implement configure(String name) to call configureSequence(Sequence s)
 *  Implement recall(String name) to call recallSequence(Sequence s)
 *  Implement store(String name) to call storeSequence(String name)
 */
public class SynthRackControlsMidiSequenceSnapshotAutomation
	implements MidiSequenceSnapshotAutomation
{
	private SynthRackControls rackControls;
	private AudioSystem audioSystem;
	
    public SynthRackControlsMidiSequenceSnapshotAutomation(SynthRackControls controls) {
    	rackControls = controls;
    }

    public void setAudioSystem(AudioSystem system) {
    	audioSystem = system;
    }
    
    public void configureSequence(Sequence snapshot) {
    	if ( audioSystem != null ) audioSystem.setAutoConnect(false);
    	rackControls.removeAll();
        Track[] tracks = snapshot.getTracks();
        Track track;
        SynthControls synthControls;
        SynthChannelControls channelControls;
        int instanceIndex = -1;
        String trackName;
        
        for ( int t = 0; t < tracks.length; t++ ) {
            track = tracks[t];
            trackName = "";
            MidiMessage msg = track.get(0).getMessage();
            if ( isMeta(msg) && getType(msg) == TRACK_NAME ) {
            	trackName = getString(msg);
            } else continue;
            msg = track.get(1).getMessage();
            if ( !isNote(msg) ) continue;
            int providerId = getData1(msg);
            int synthId = getData2(msg);
            // VSTis don't have unique toot ids, but do have uniqueish names
            boolean useTrackName = providerId == TOOT_PROVIDER_ID && 
            						  synthId == VSTI_SYNTH_ID;
            String sname = useTrackName ? trackName
            							: SynthServices.lookupModuleName(providerId, synthId);
            if ( sname == null ) {
            	System.err.println("Synth configure: failed to lookup service "+providerId+"/"+synthId+" for "+trackName);
            	continue;
            }
            synthControls = SynthServices.createControls(sname);
            rackControls.setSynthControls(t, synthControls);
            
            if ( synthControls instanceof MultiSynthControls) {
            	for ( int m = 2; m < track.size(); m++ ) {
            		msg = track.get(m).getMessage();
            		if ( !isControl(msg) ) continue;
            		if ( instanceIndex == getInstanceIndex(msg) ) continue;
            		instanceIndex = getInstanceIndex(msg);
            		int chan = instanceIndex - 1;
            		if ( chan < 0 ) continue;
            		String name = SynthChannelServices.lookupModuleName(
            				getProviderId(msg), getModuleId(msg));
            		if ( name == null ) continue;
            		channelControls = SynthChannelServices.createControls(name);
            		if ( channelControls == null ) continue;
            		((MultiSynthControls)synthControls).setChannelControls(chan, channelControls);
//          		System.out.println("Synth configure: channel "+t+"/"+chan+" "+channelControls.getName());
            	}
            }
        }
        if ( audioSystem != null ) audioSystem.setAutoConnect(true);
    }

    public void recallSequence(Sequence snapshot) {
//        rackControls.setAutoConnect(false);
        Track[] tracks = snapshot.getTracks();
        Track track;
        SynthControls synthControls;
        int instanceIndex = -1;
        
        for ( int t = 0; t < tracks.length; t++ ) {
            track = tracks[t];
            MidiMessage msg = track.get(0).getMessage();
            if ( isMeta(msg) && getType(msg) == TRACK_NAME ) {
            } else continue;
           	synthControls = rackControls.getSynthControls(t);
           	if ( synthControls == null ) {
           		System.err.println("Synth recall: failed to get synth controls "+t);
           		continue;
           	}

            int providerId = -1;
            int moduleId = -1;

    		NativeSupport ns = synthControls.getNativeSupport();
            if ( ns != null && ns.canPersistMidi() ) {
            	ns.recall(track, 2);
            } else if ( synthControls instanceof ChannelledSynthControls ) {
               	SynthChannelControls channelControls = null;
            	for ( int i = 2; i < track.size(); i++ ) {
            		msg = track.get(i).getMessage();
            		// if Note recall to SynthChannelControls Controller/Control map
            		if ( isNote(msg) ) {
        				int controller = getData1(msg);
        				int cid = getData2(msg);
            			if ( channelControls != null ) {
            				channelControls.setMappedControlId(controller, cid);
            			} else {
            				System.err.println(
            						"Synth recall: no channelControl to map controller "+
            						controller+" to "+cid);
            			}
            		}
            		if ( !isControl(msg) ) continue;
            		if ( instanceIndex != getInstanceIndex(msg) ) {
            			instanceIndex = getInstanceIndex(msg);
            			int chan = instanceIndex - 1;
            			channelControls = ((ChannelledSynthControls)synthControls).getChannelControls(chan);
            			if ( channelControls == null ) {
            				System.err.println("Synth recall: failed to get channel controls "+t+"/"+chan);        		
            				break;
            			}
            			providerId = channelControls.getProviderId();
            			moduleId = channelControls.getId();
            		}
            		if ( getProviderId(msg) != providerId || 
            				getModuleId(msg) != moduleId ) continue;
            		int cid = getControlId(msg);
            		Control control = channelControls.deepFind(cid);
            		if ( control == null ) {
            			continue;
            		}
            		int newValue = getValue(msg);
            		if ( newValue == control.getIntValue() ) continue;
//          		System.out.println("recall: "+control.getControlPath());
            		control.setIntValue(newValue);
            	}
            } else { // normal controls
    			providerId = synthControls.getProviderId();
    			moduleId = synthControls.getId();
            	for ( int i = 2; i < track.size(); i++ ) {
            		msg = track.get(i).getMessage();
            		// TODO if Note recall to SynthControls Controller/Control map
            		if ( !isControl(msg) ) continue;
            		if ( getProviderId(msg) != providerId || 
            				getModuleId(msg) != moduleId ) continue;
            		int cid = getControlId(msg);
            		Control control = synthControls.deepFind(cid);
            		if ( control == null ) {
            			continue;
            		}
            		int newValue = getValue(msg);
            		if ( newValue == control.getIntValue() ) continue;
//          		System.out.println("recall: "+control.getControlPath());
            		control.setIntValue(newValue);
            	}
            }
        }
//        rackControls.setAutoConnect(true);
    }

    public Sequence storeSequence(String name) {
        // all events are at zero tick so sequence resolution is pointless
        // also, events waste space because tick is always zero !!!
        Sequence snapshot;
        try {
        	snapshot = new Sequence(Sequence.PPQ, 1);
        } catch ( InvalidMidiDataException imde ) {
            return null;
        }
        int providerId = -1;
        int moduleId = -1;
        int instanceIndex = -1;
        for ( int synth = 0; synth < rackControls.size(); synth++ ) {
        	SynthControls synthControls = rackControls.getSynthControls(synth);
    		Track t = snapshot.createTrack();
        	if ( synthControls == null ) continue;
    		try {
    			MidiMessage msg = createMeta(TRACK_NAME, synthControls.getName());
    			t.add(new MidiEvent(msg, 0L));
                // note off msg misused to allow configure to create synths
                msg = off(0, synthControls.getProviderId(), synthControls.getId());
                t.add(new MidiEvent(msg, 0L));
    		} catch ( InvalidMidiDataException imde ) {
    			System.err.println("Synth store: failed to store synth "+synthControls.getName());
    		}
    		NativeSupport ns = synthControls.getNativeSupport();
    		if ( ns != null && ns.canPersistMidi() ) {
    			ns.store(t);
    		} else if ( synthControls instanceof ChannelledSynthControls ) {
    			ChannelledSynthControls channelledControls = 
    				(ChannelledSynthControls)synthControls;
    			CompoundControl gc = channelledControls.getGlobalControls();
    			if ( gc != null ) {
    				providerId = gc.getProviderId();
    				moduleId = gc.getId();
    				instanceIndex = 0;
    				MidiPersistence.store(providerId, moduleId, instanceIndex, gc, t);
    			}

    			SynthChannelControls cc;
    			for ( int chan = 0; chan < 16; chan++ ) {
    				cc = channelledControls.getChannelControls(chan);
    				if ( cc == null ) continue;
    				providerId = cc.getProviderId();
    				moduleId = cc.getId();
    				instanceIndex = 1+chan; //cc.getInstanceIndex();
    				MidiPersistence.store(providerId, moduleId, instanceIndex, cc, t);
    				// store SynthChannelControls Controller/Control map as Notes
    				for ( int i = 0; i < 128; i++ ) {
    					try {
    						int cid = cc.getMappedControlId(i);
    						if ( cid < 0 ) continue;
    						MidiMessage msg = on(chan, i, cid);
    		    			t.add(new MidiEvent(msg, 0L));
    					} catch ( InvalidMidiDataException imde ) {
    						System.err.println("Synth store: failed on mapping controller"+i);
    					}
    				}
    			}
    		} else { // just an unchannelled synth controls
				providerId = synthControls.getProviderId();
				moduleId = synthControls.getId();
				instanceIndex = 0;
				MidiPersistence.store(providerId, moduleId, instanceIndex, synthControls, t);
				// TODO store Controller/Control map as Notes
    			
    		}
        }
        return snapshot;
    }
}
