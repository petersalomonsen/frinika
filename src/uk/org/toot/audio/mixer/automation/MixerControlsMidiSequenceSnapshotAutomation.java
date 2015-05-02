// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import javax.sound.midi.*;
import uk.org.toot.control.*;
import uk.org.toot.control.automation.MidiPersistence;
import uk.org.toot.control.automation.MidiSequenceSnapshotAutomation;
import uk.org.toot.audio.core.*;
import java.util.List;
import uk.org.toot.audio.mixer.MixerControls;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;
import static uk.org.toot.control.automation.ControlSysexMsg.*;
import static uk.org.toot.midi.message.MetaMsg.*;
import static uk.org.toot.midi.message.NoteMsg.*;
import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;

/**
 * Stores and recalls mixer automations snaphots as Midi Sequences.
 * To concretise this class extend it and:
 *  Implement configure(String name) to call configureSequence(Sequence s)
 *  Implement recall(String name) to call recallSequence(Sequence s)
 *  Implement store(String name) to call storeSequence(String name)
 */
public class MixerControlsMidiSequenceSnapshotAutomation 
	extends BasicSnapshotAutomation
	implements MidiSequenceSnapshotAutomation
{
    public MixerControlsMidiSequenceSnapshotAutomation(MixerControls controls) {
        super(controls);
    }

    public void configureSequence(Sequence snapshot) {
        Track[] tracks = snapshot.getTracks();
        Track track;
        String stripName;
        AudioControlsChain stripControls;
        for ( int t = 0; t < tracks.length; t++ ) {
            track = tracks[t];
            MidiMessage msg = track.get(0).getMessage();
            if ( !isMeta(msg) ) continue;
           	stripName = getString(msg);
            stripControls = mixerControls.getStripControls(stripName);
            // to create strips we need to know the strip id and instanceIndex
            // only dynamically create channel and group strips
            // not fx or aux or main strips at this stage
            if ( stripControls == null ) {
                msg = track.get(1).getMessage();
                if ( !isNote(msg) ) continue;
                int stripId = getData1(msg);
                if ( stripId != CHANNEL_STRIP &&
                     stripId != GROUP_STRIP ) continue;
                int stripInstanceIndex = getData2(msg);
                stripControls = mixerControls.createStripControls(
                    stripId, stripInstanceIndex, stripName);
            }
            AutomationControls autoc = stripControls.find(AutomationControls.class);
            if ( autoc != null && !autoc.canRecall() ) continue;
            stripControls.setMutating(true);
	        // reconstruct control hierarchy !!!
            // make list of needed modules
            List<AutomationIndices> needed = new java.util.ArrayList<AutomationIndices>();
            for ( int i = 2; i < track.size(); i++ ) {
                msg = track.get(i).getMessage();
                if ( isControl(msg) ) {
                    AutomationIndices triple = new AutomationIndices(getProviderId(msg),
                        getModuleId(msg), getInstanceIndex(msg));
                	// only store if needed doesn't already contain this triple
                    if ( !needed.contains(triple) ) {
                        needed.add(triple);
                    }
                }
            }

            // deletes first to reduce move and insert costs
            // don't delete automation controls !!!
            List<String> deletions = new java.util.ArrayList<String>();
            for ( Control c : stripControls.getControls() ) {
                if ( c instanceof CompoundControl ) {
                    CompoundControl cc = (CompoundControl)c;
                    AutomationIndices triple = new AutomationIndices(cc.getProviderId(),
                        cc.getId(), cc.getInstanceIndex());
                    if ( !needed.contains(triple) ) {
                        deletions.add(cc.getName());
                    }
                }
            }
            List<String> deletions2 = new java.util.ArrayList<String>();
            for ( String s : deletions ) {
                CompoundControl d = (CompoundControl)stripControls.find(s);
                if ( d.canBeDeleted() ) {
                    stripControls.delete(s);
                } else {
                    deletions2.add(s); // e.g. Taps may be deletable after client deleted
                }
            }
            for ( String s : deletions2 ) {
                CompoundControl d = (CompoundControl)stripControls.find(s);
                if ( d.canBeDeleted() ) {
                    stripControls.delete(s);
                }                
            }

            // moves, as a sort
            int size = stripControls.getControls().size();
            CompoundControl cc1, cc2;
            AutomationIndices ti1, ti2;
            for ( int i = 1; i < size; i++ ) {
                cc1 = (CompoundControl)stripControls.getControls().get(i-1);
                if ( !cc1.canBeMovedBefore() ) continue;
                cc2 = (CompoundControl)stripControls.getControls().get(i);
                if ( !cc2.canBeMoved() ) continue;
                ti1 = new AutomationIndices(cc1);
                ti2 = new AutomationIndices(cc2);
                int ni1 = needed.indexOf(ti1);
                int ni2 = needed.indexOf(ti2);
                if ( ni2 > 0 && ni1 > ni2 ) {
	                stripControls.move(cc2.getName(), cc1.getName());
/*                    System.out.println(stripName+
                        ": move "+cc2.getName()+" before "+cc1.getName()+
                        " because "+needed.indexOf(ti1)+" > "+needed.indexOf(ti2)); */
                }
            }

            // inserts
            // assumes deletes and moves have been done
            // !!! but they may not have been, so cope! !!! !!!
            AutomationIndices tin;
            AutomationIndices tia;
            CompoundControl cc;
            for ( int n = 0, a = 0; n < needed.size(); ) {
            	cc = a >= stripControls.getControls().size() ? null :
            		(CompoundControl)stripControls.getControls().get(a);
            	tin = needed.get(n);
            	if ( cc != null ) {
            		tia = new AutomationIndices(cc);
            		if ( tia.equals(tin) ) { // already matched
            			a += 1;
            			n += 1;
            			continue;
            		} else if ( !cc.canBeInsertedBefore() ) {
            			a += 1;
            			continue;
            		}
            	}
            	// to be inserted now
            	Control cInsert = AudioServices.createControls(
            			tin.getProviderId(), tin.getModuleId(), tin.getInstanceIndex());
            	if ( cInsert == null ) {
                    // don't fret about missing mixer busses
                    if ( isPluggable(tin.getProviderId(), tin.getModuleId()) ) {
                        System.err.println("configure: no service for "
            				+tin.getProviderId()+"/"+tin.getModuleId()+"/"+tin.getInstanceIndex()+
            				" in "+stripName); 
                    }
            		n += 1;
            		continue;
            	}
            	stripControls.insert(cInsert, cc == null ? null : cc.getName());
//            	System.out.println(stripName+": inserted "+cInsert.getName()+" before "+cc.getName());
            	n += 1;
            	a += 1;
            }
            if ( stripControls.getControls().size() < needed.size() ) {
            	System.err.println(stripName+": only configured "+stripControls.getControls().size()+" of "+needed.size()+" needed modules");
            }
            stripControls.setMutating(false);
        }
    }

    public void recallSequence(Sequence snapshot) {
        Track[] tracks = snapshot.getTracks();
        Track track;
        int providerId = 0;
        int moduleId = 0;
        int moduleIdReplacement = 0;
        int instanceIndex = -1;
        CompoundControl module = null;
        for ( int t = 0; t < tracks.length; t++ ) {
            track = tracks[t];
            MidiMessage msg = track.get(0).getMessage();
            if ( !isMeta(msg) ) {
                System.out.println("recall: no name in track "+t);
                continue;
            }
            String stripName = getString(msg);
	        CompoundControl cc = mixerControls.getStripControls(stripName);
        	if ( cc == null || !(cc instanceof AudioControlsChain) ) {
            	System.out.println("recall: no strip named "+stripName);
            	continue;
        	}
            AudioControlsChain strip = (AudioControlsChain)cc;
            AutomationControls autoc = strip.find(AutomationControls.class);
            if ( autoc != null && !autoc.canRecall() ) continue;
            module = null;
            for ( int i = 1; i < track.size(); i++ ) {
                msg = track.get(i).getMessage();
                if ( !isControl(msg) ) {
                	if ( isSysex(msg) ) {
                		if ( module == null ) continue; // should be preceded by Bypass
                		NativeSupport ns = module.getNativeSupport();
                		if ( ns != null && ns.canPersistMidi() ) {
                			ns.recall(track, i);
                		}
                	}
                	continue;
                }
                int pid = getProviderId(msg);
                int mid = getModuleId(msg);
                int ii = getInstanceIndex(msg);
                int cid = getControlId(msg);
                // only find module if id's changed
                if ( pid != providerId || mid != moduleId || ii != instanceIndex ) {
                    module = strip.find(pid, mid, ii);
                    moduleIdReplacement = mid;
                    if ( module == null ) {
                        // check for module replacements
                        CompoundControl mod2 = AudioServices.createControls(pid, mid, ii);
                        if ( mod2 != null ) {
                            moduleIdReplacement = mod2.getId();
                            module = strip.find(pid, moduleIdReplacement, ii);
                        }
                        if ( module == null ) {
                            System.err.println("recall: no module "+pid+"/"+mid+"/"+ii+" in "+stripName);
                        }
                    }
                    providerId = pid;
                    moduleId = mid; // not accurate for module replacement!
                    instanceIndex = ii;
                }
                if ( module == null ) {
                    continue;
                }
                Control control = module.deepFind(cid);
                if ( control == null ) {
                    // don't fret about missing mixer busses
                    if ( isPluggable(providerId, moduleId) ) {
                        String extra = moduleId == moduleIdReplacement ? "" :
                            " after mapping from"+" ("+providerId+"/"+moduleId+"/"+instanceIndex+")";
                        System.err.println("recall: no control "+cid+" in "+module.getControlPath()+" in "+stripName+" ("+providerId+"/"+moduleIdReplacement+"/"+instanceIndex+")"+extra);
                    }
	                continue;
                }
//                System.out.println("recall: "+control.getControlPath());
                int newValue = getValue(msg);
		        if ( newValue == control.getIntValue() ) continue;
                control.setIntValue(newValue);
            }
        }
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
	    // must separate strips
        for ( Control c : mixerControls.getControls() ) {
            if ( c.getId() < 0 ) continue;
            AudioControlsChain strip = (AudioControlsChain)c;
            AutomationControls autoc = strip.find(AutomationControls.class);
            if ( autoc != null && !autoc.canStore() ) continue;
            Track t = snapshot.createTrack();
	        try {
    		    MidiMessage msg = createMeta(TRACK_NAME, strip.getName());
                t.add(new MidiEvent(msg, 0L));
                // note off msg misused to allow configure to create strips
                msg = off(0, strip.getId(), strip.getInstanceIndex());
                t.add(new MidiEvent(msg, 0L));
	        } catch ( InvalidMidiDataException imde ) {
                System.out.println("store: error storing strip "+strip.getName());
       		}
            // store all modules in this strip
            for ( Control m : ((CompoundControl)c).getControls() ) {
                CompoundControl cc = (CompoundControl)m;
//                System.out.println("store: storing module "+cc.getName());
                providerId = cc.getProviderId();
                moduleId = cc.getId();
                instanceIndex = cc.getInstanceIndex();
            	MidiPersistence.store(providerId, moduleId, instanceIndex, cc, t);
            	// VST sysex
            	NativeSupport ns = cc.getNativeSupport();
            	if ( ns != null && ns.canPersistMidi() ) {
            		ns.store(t);
            	}
            }
        }
        return snapshot;
    }

    /**
     * Is this a module that should be able to be plugged in?
     * @param pid
     * @param mid
     * @return true if can be plugged in
     */
    protected boolean isPluggable(int pid, int mid) {
        return pid != TOOT_PROVIDER_ID || mid < CHANNEL_STRIP; 
        
    }
    
    static public class AutomationIndices
    {
        private int providerId;
        private int moduleId;
        private int instanceIndex;

        public AutomationIndices(int vId, int mId, int iIndex) {
            providerId = vId;
            moduleId = mId;
            instanceIndex = iIndex;
        }

        public AutomationIndices(CompoundControl cc) {
            this(cc.getProviderId(), cc.getId(), cc.getInstanceIndex());
        }

        public int getProviderId() { return providerId; }

        public int getModuleId() { return moduleId; }

        public int getInstanceIndex() { return instanceIndex; }

        public boolean equals(Object obj) {
            if ( obj == null ) return false;
            if ( !(obj instanceof AutomationIndices) ) return false;
            AutomationIndices ti = (AutomationIndices)obj;
            return ( ti.getProviderId() == getProviderId() &&
                	 ti.getModuleId() == getModuleId() &&
                     ti.getInstanceIndex() == getInstanceIndex() );
        }

        public int hashCode() {
            return providerId ^ moduleId ^ instanceIndex;
        }
    }
}
