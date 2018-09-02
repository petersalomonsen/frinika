// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import uk.org.toot.control.*;
import uk.org.toot.control.automation.SnapshotAutomation;
import uk.org.toot.audio.core.AudioControlsChain;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.id.ProviderId;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;
import static uk.org.toot.misc.Localisation.*;

/**
 * MixerControls defines the various types of strip that available and
 * represents mixer controls as a two-dimensional 'crossbar' of strips
 * and busses.
 */
public class MixerControls extends CompoundControl //AudioControls
{
    private boolean canAddBusses = true; // until strip added

    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     * @label Main
     */
    private BusControls mainBusControls;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     * @label Fx
     */
    /*#private BusControls lnkFxBusControls;*/
    private List<BusControls> fxBusControls = new ArrayList<BusControls>();
    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     * @label Aux
     */
    /*#private BusControls lnkAuxBusControls;*/
    private List<BusControls> auxBusControls = new ArrayList<BusControls>();

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    private SnapshotAutomation snapshotAutomation;

    public MixerControls(String name) {
        this(name, getString("Main"), ChannelFormat.STEREO);
    }

    public MixerControls(String name, String mainBusName, ChannelFormat channelFormat) {
        super(1, name);
        mainBusControls = new BusControls(MAIN_BUS, mainBusName, channelFormat);
    }

    public int getProviderId() {
        return ProviderId.TOOT_PROVIDER_ID;
    }

    public BusControls createFxBusControls(String name, ChannelFormat format) {
        if ( !canAddBusses ) {
            throw new IllegalStateException(
                "Can't add busses after adding strips");
        }
        ChannelFormat mainFormat = mainBusControls.getChannelFormat();
        if ( format == null ) {
            format = mainFormat;
        } else if ( format.getCount() > mainFormat.getCount() ) {
            // required because all mixes are upmixes, can't reduce channel count
            format = mainFormat;
            System.err.println(name+" Bus limited to Main Bus channel format");
        }
		BusControls busControls =
            new BusControls(FX_BUS, name, format);
        fxBusControls.add(busControls);
        return busControls;
    }

    public BusControls createAuxBusControls(String name, ChannelFormat format) {
        if ( !canAddBusses ) {
            throw new IllegalStateException(
                "Can't add busses after adding strips");
        }
		BusControls busControls =  new BusControls(AUX_BUS, name, format);
        auxBusControls.add(busControls);
        return busControls;
    }

    public BusControls getBusControls(String name) {
        if ( name.startsWith(mainBusControls.getName()) ) {
            return mainBusControls;
        }
        for ( BusControls busControls : fxBusControls ) {
            if ( name.startsWith(busControls.getName()) ) {
                return busControls;
            }
        }
        for ( BusControls busControls : auxBusControls ) {
            if ( name.startsWith(busControls.getName()) ) {
                return busControls;
            }
        }
        System.err.println(name+" not found");
        return null;
    }

    public BusControls getMainBusControls() {
        return mainBusControls;
    }

    public List<BusControls> getFxBusControls() {
        return Collections.<BusControls>unmodifiableList(fxBusControls);
    }

    public List<BusControls> getAuxBusControls() {
        return Collections.<BusControls>unmodifiableList(auxBusControls);
    }

    public AudioControlsChain createStripControls(int id, int index, String name) {
        return createStripControls(id, index, name, true, ChannelFormat.STEREO); // has bus mix controls
    }

    public AudioControlsChain createStripControls(int id, int index, String name, ChannelFormat constraintFormat) {
        return createStripControls(id, index, name, true, constraintFormat); // has bus mix controls
    }

    public AudioControlsChain createStripControls(int id, int index, String name,
        	boolean hasMixControls, ChannelFormat constraintFormat) {
        if ( name == null ) {
            throw new IllegalArgumentException("null name");
        }
        if ( getStripControls(name) != null ) {
            throw new IllegalArgumentException(name+" already exists");
        }
        AudioControlsChain chain = new AudioControlsChain(id, index, name, constraintFormat);
       	MixerControlsFactory.addMixControls(this, chain, hasMixControls);
        addStripControls(chain);
        return chain;
    }

    public void addStripControls(CompoundControl cc) {
        canAddBusses = false;
        add(cc);
        setChanged();
        notifyObservers(new Mutation(Mutation.ADD, cc));
    }

    public void removeStripControls(CompoundControl cc) {
        remove(cc);
        setChanged();
        notifyObservers(new Mutation(Mutation.REMOVE, cc));
    }

    public void removeStripControls(String name) {
        AudioControlsChain cc = getStripControls(name);
        if ( cc != null ) {
	        removeStripControls(cc);
        }
    }

    public void moveStripControls(String name, String beforeName) {
        AudioControlsChain cc = getStripControls(name);
        AudioControlsChain bc = getStripControls(beforeName);
        if ( cc != null && bc != null ) {
	        remove(cc);
   	        int bi = controls.indexOf(bc);
           	controls.add(bi, cc);
        	setChanged();
   	    	notifyObservers(null);
        }
    }

    public AudioControlsChain getStripControls(String name) {
        for ( Control control : getControls() ) {
            if ( control.getName().equals(name) ) {
                 return (AudioControlsChain)control;
            }
        }
        return null;
    }

    public AudioControlsChain getStripControls(int id, int index) {
        for ( Control c : getControls() ) {
            if ( c.getId() == id &&
                ((AudioControlsChain)c).getInstanceIndex() == index ) {
                 return (AudioControlsChain)c;
            }
        }
        return null;
    }

    public SnapshotAutomation getSnapshotAutomation() {
        return snapshotAutomation;
    }

    public void setSnapshotAutomation(SnapshotAutomation automation) {
        snapshotAutomation = automation;
    }

    private static String[] opNames = { "n/a", "Add", "Remove" };

    static public class Mutation
    {
        public static final int ADD = 1;
        public static final int REMOVE = 2;


        private int operation;
        private CompoundControl control;

        public Mutation(int operation, CompoundControl control) {
            this.operation = operation;
            this.control = control;
        }

        public int getOperation() { return operation; }

        public CompoundControl getControl() { return control; }

        public String toString() {
            return opNames[operation]+" "+control.getName();
        }
    }
}
