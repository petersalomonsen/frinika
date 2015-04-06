/*
 * Created on Mar 6, 2006
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
package com.frinika.project;

import com.frinika.tootX.midi.MidiDeviceRouter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;

import uk.org.toot.audio.core.AudioControlsChain;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.mixer.AudioMixer;
import uk.org.toot.audio.mixer.AudioMixerStrip;
import uk.org.toot.audio.mixer.MixControls;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.audio.mixer.MixerControlsIds;
// import uk.org.toot.audio.mixer.MixerControlsDescriptor;
import uk.org.toot.audio.mixer.MixerControlsFactory;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.server.AudioServer;

import com.frinika.SplashDialog;
import com.frinika.audio.toot.AudioInjector;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.tootX.MidiHub;
import com.frinika.global.ConfigListener;
import com.frinika.global.FrinikaConfig;
import com.frinika.midi.DrumMapper;
import com.frinika.midi.MidiDebugDevice;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.scripting.FrinikaScriptingEngine;
import com.frinika.project.settings.ProjectSettings;
import com.frinika.renderer.FrinikaRenderer;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.MidiResource;
import com.frinika.sequencer.converter.MidiSequenceConverter;
import com.frinika.sequencer.gui.clipboard.MyClipboard;
import com.frinika.sequencer.gui.mixer.SynthWrapper;

import com.frinika.sequencer.gui.selection.DragList;
import com.frinika.sequencer.gui.selection.LaneSelection;
import com.frinika.sequencer.gui.selection.MidiSelection;
import com.frinika.sequencer.gui.selection.MultiEventSelection;
import com.frinika.sequencer.gui.selection.PartSelection;
import com.frinika.sequencer.gui.selection.SelectionFocusable;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.EditHistoryRecordableAction;
import com.frinika.sequencer.model.EditHistoryRecorder;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.ProjectLane;
import com.frinika.sequencer.model.RecordableLane;
import com.frinika.sequencer.model.SoloManager;
import com.frinika.sequencer.model.SynthLane;
import com.frinika.sequencer.model.TextLane;
import com.frinika.sequencer.model.ViewableLaneList;
import com.frinika.audio.io.BufferedRandomAccessFileManager;
import com.frinika.audio.DynamicMixer;
import com.frinika.sequencer.TempoChangeListener;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.TimeSignatureEvent;
import com.frinika.sequencer.model.util.TimeUtils;
import com.frinika.synth.SynthRack;
import com.frinika.synth.settings.SynthSettings;
import com.frinika.tools.ObjectInputStreamFixer;
import com.frinika.tools.ProgressBarInputStream;
import com.frinika.tootX.midi.ControlResolver;
import com.frinika.tootX.midi.MidiConsumer;
import com.frinika.tootX.midi.MidiRouterSerialization;
import uk.org.toot.audio.core.Taps;
import uk.org.toot.misc.Tempo;
import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * Use to load Frinika projects.
 * 
 * This class links together all components of a Frinika project, and provides
 * all operations and features - including a Frinika sequencer instance.
 * 
 * Information about Midi Devices - naming and how to reopen them is contained
 * using the MidiDeviceDescriptors.
 * 
 * Audio files are stored in a folder named audio which is created in the same
 * folder as where the project is. Thus a good convention is to have one folder
 * per project.
 * 
 * @author Peter Johan Salomonsen
 */
public class ProjectContainer implements EditHistoryRecorder<Lane>,MidiConsumer,
        Serializable,DynamicMixer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    TootMixerSerializer mixerSerializer;
    ProjectLane projectLane;
    transient MidiResource midiResource;
    transient SoloManager soloManager;
    transient FrinikaSequencer sequencer;
    transient FrinikaRenderer renderer;
    transient FrinikaSequence sequence = null;
    transient EditHistoryContainer editHistoryContainer;
    transient MultiEventSelection multiEventSelection;
    transient DragList dragList;
    transient PartSelection partSelection;
    transient LaneSelection laneSelection;
    transient MidiSelection midiSelection; // Jens
    transient SelectionFocusable selectionFocus;
    String title = null;
    File projectFile = null;
    File audioDir = null;
    transient SynthRack audioSynthRack;
    float tempo = 100;
    transient MidiEvent tempoEvent;
    transient MixerControls mixerControls;
    transient FrinikaAudioServer audioServer;
    transient AudioMixer mixer;
    transient AudioInjector outputProcess;
    transient BufferedRandomAccessFileManager audioFileManager;
    transient AudioClient audioClient;
    transient MidiDeviceRouter midiDeviceRouter; // maps mididevices onto receivers
    transient ControlResolver controlResolver;  // controls register with this if the want to use the midi mapping stuff.
    FrinikaScriptingEngine scriptingEngine; // Jens
    /**
     * These two are deprecated - but should not be deleted for backwards
     * compatibility. Replacement is now the midiDeviceDescriptors
     */
    @Deprecated
    List<SynthSettings> synthSettings;
    @Deprecated
    Vector<String> externalMidiDevices;
    /**
     * Information about the midi devices used in this project
     */
    List<MidiDeviceDescriptor> midiDeviceDescriptors = new ArrayList<MidiDeviceDescriptor>();
    /**
     * Used to map midiDevices when saving
     */
    transient HashMap<MidiDevice, Integer> midiDeviceIndex;
    /**
     * Used to map midiDevices to their descriptors
     */
    transient HashMap<MidiDevice, MidiDeviceDescriptor> midiDeviceDescriptorMap = new HashMap<MidiDevice, MidiDeviceDescriptor>();
    TimeSignatureList timeSignitureList; 
    /**
     * The resolution of the sequence. If you import a midi track they may have
     * a different resolution - and when saving to a project - the sequence
     * created when reloading will have the resolution stored here.
     */
    int ticksPerQuarterNote = FrinikaConfig.TICKS_PER_QUARTER;
    private TempoList tempoList;
    private double pianoRollSnapQuantization = 0;
    private double partViewSnapQuantization = 0;
    private boolean isPianoRollSnapQuantized = true;
    private boolean isPartViewSnapQuantized = true;
    /**
     * Whether to embed externally referenced data or not (e.g. soundfonts)
     */
    private boolean saveReferencedData;
    transient private MyClipboard myClipboard;
    long endTick = 100000;    // static boolean dynam = true;
    private transient int count = 1;
    private transient int pixelsPerRedraw;
    private long loopStartPoint;
    private long loopEndPoint;
    private int loopCount;
    private MidiRouterSerialization midiRouterSerialization;
    private String genres;
    private Long dataBaseID;

    public Long getDataBaseID() {
        return dataBaseID;
    }

    public void setDataBaseID(Long dataBaseID) {
        this.dataBaseID = dataBaseID;
    }

    public ProjectContainer(int ticksPerBeat) throws Exception {
        if (ticksPerBeat > 0) {
            ticksPerQuarterNote = ticksPerBeat;
        }
        defaultInit();
        sequencer = new FrinikaSequencer();
        sequencer.open();
        attachTootNotifications();
        renderer = new FrinikaRenderer(this);
        createSequencerPriorityListener();


        // This also creates a track for the Tempo Events.
        createSequence();
        System.out.println(sequence.getFrinikaTrackWrappers().size());
        projectLane = new ProjectLane(this);
        midiResource = new MidiResource(sequencer);
        tempoList = new TempoList(sequence.getResolution(), this);
        sequencer.setTempoList(tempoList);

        setTempoInBPM(100);

        /*
         * Tempo message ShortMessage msg = new ShortMessage();
         * msg.setMessage(ShortMessage.NOTE_ON, 0, 63, 0); // Default tempo =
         * 100 BPM int mpq = (int) (60000000.0 / 100.0); MetaMessage tempoMsg =
         * new MetaMessage(); tempoMsg.setMessage(0x51, new byte[] { (byte) (mpq >>
         * 16 & 0xff), (byte) (mpq >> 8 & 0xff), (byte) (mpq & 0xff) }, 3);
         * MidiEvent tempoEvent = new MidiEvent(tempoMsg, 0);
         * ((FrinikaSequence)sequencer.getSequence()).getFrinikaTrackWrappers().get(0).add(tempoEvent);
         */

        postInit();

    }
    //   transient Receiver midiReceiver;   
    //   transient MidiFilter midiPreFilter; // filter events before any processing

    public MidiDeviceRouter getMidiDeviceRouter() {
        return midiDeviceRouter;
    }

    public MidiRouterSerialization getMidiRouterSerialization() {
        return midiRouterSerialization;
    }

    /**
     * set song genres.
     *
     * @param string A colon seperate list of genre names.
     */
    public void setGenres(String string) {
        genres = string;
    }

    public String getGenres() {
        return genres;
    }

    /**
     * Set the title of the song.
     * 
     * @param t
     */
    public void setTitle(String t) {
        title = t;
    }

    /**
     * 
     * @return title of the song or file name if title is null
     */
    public String getTitle() {
        if (title != null) {
            return title;
        }
        if (projectFile != null) {
            String str = projectFile.getName();
            int ind = str.lastIndexOf(".frinika");
            if (ind > 0) {
                str = str.substring(0, ind);
            }
            return str;
        }
        return "Unnamed";
    }

//    public MidiDeviceRouter getMidiDeviceRouter() {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    /**
     * Redirects midi events used for controls
     * 
     * If not consumed event is sent to midiReciever.
     *  
     * 
     * @param devInfo
     * @param arg0
     * @param arg1
     */
    public void processMidiMessageFromDevice(Info devInfo, MidiMessage arg0, long arg1) {
        if (midiDeviceRouter != null && midiDeviceRouter.consume(devInfo, arg0, arg1)) {
            return;
        }
        try {
            sequencer.getReceiver().send(arg0, arg1);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(ProjectContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//        /**
//         * allows one to capture midi events before they are forwarded to the 
//         *  sequencer.getReceiver();
//         * 
//         * 
//         * @param filter 
//         */
//        public void setMidiPreFilter(MidiFilter filter) {
//            midiPreFilter=filter;
//            
//        }
//     public Receiver getMidiReceiver() {
//
//        if (midiReceiver != null) {
//            return midiReceiver;
//        }
//
//        try {
//
//            final Receiver activeReceiver = sequencer.getReceiver();
//
//            // main recipient of midi events (after filtering)
//            midiReceiver = new Receiver() {
//
//                public void send(MidiMessage message, long timeStamp) {
//
//                    /**
//                     * //PJL
//                     * frinika is not interested in these inputs ?
//                     */
//                    if (message.getStatus() >= ShortMessage.MIDI_TIME_CODE) {
//                        return;
//                    }
//
//
//                    if (!(message instanceof ShortMessage)) {
//                        return;
//                    }
//
//                    // PJL ... Allow filter to grab events before anyone else
//                    if (midiPreFilter != null) {
//                        if (midiPreFilter.consume(message, timeStamp)) {
//                            return;
//                        }
//                    }
//
//                    if (activeReceiver != null) {
//                        activeReceiver.send(message, timeStamp);
//                    }
//                }
//
//                public void close() {
//                }
//            };
//        } catch (MidiUnavailableException ex) {
//            Logger.getLogger(ProjectContainer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return midiReceiver;
//    }
    // can be used to remove channels and groups

    public void removeStrip(String name) {
        mixerControls.removeStripControls(name);
    }

    public MixControls addMixerInput(AudioProcess audioProcess, String string) {

        AudioMixerStrip strip = null;
        MixControls x = null;
        try {

            strip = getMixer().getStrip(string);
            AudioControlsChain controls;

            // If it exists we have loaded a mixer with the strip so no need to
            // create it
            if (strip == null) {
                controls = mixerControls.createStripControls(
                        MixerControlsIds.CHANNEL_STRIP, count++, string);
                strip = getMixer().getStrip(string);
            } else {
                controls = mixerControls.getStripControls(string);
            }
            strip.setInputProcess(audioProcess);
            x = (MixControls) controls.find(mixerControls.getMainBusControls().getName());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return x;

    }

    void createMixer() {

        if (mixerControls != null) {
            return;
        }
        try {

            mixerControls = new MixerControls("Mixer");
            // defaults to having one main bus
            // create 2 aux send busses for effects and 1 aux monitor bus
            // because if you don't have any aux busses you can't insert
            // effects :(
            MixerControlsFactory.createBusses(mixerControls, 2, 1);
            MixerControlsFactory.createBusStrips(mixerControls);

            audioServer = FrinikaAudioSystem.getAudioServer();
            List<String> list = audioServer.getAvailableOutputNames();

            mixer = new AudioMixer(mixerControls, audioServer);
            audioClient = new ProjectAudioClient();
            FrinikaAudioSystem.installClient(audioClient);

            String outDev = FrinikaAudioSystem.configureServerOutput();

            if (outDev != null) {

                outputProcess = new AudioInjector(audioServer.openAudioOutput(
                        outDev, "output"));
                System.out.println("Using " + outDev + " as audio out device");

                mixer.getMainBus().setOutputProcess(outputProcess);

            } else {
                message(" No output devices found ");
            }

        // This should only be done once.
        // audioServer.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.err.println(" \n Sorry but I do not want to go on without an audio output device. \n Bye bye . . .  ");
            System.exit(1);
        }

    }

    /**
     * Mixer the process output with the main mix
     * 
     * @param process
     */
    public void injectIntoOutput(AudioProcess process) {
        if (outputProcess != null) {
            outputProcess.add(process);
        }
    }

    /**
     * 
     * set up after objects have been created
     */
    void postInit() {
        new RecordingManager(this, 10000);

    }

    /**
     * 
     * set up default stuff before reading or construction of objects
     */
    void defaultInit() {
        pixelsPerRedraw = 1;
        audioFileManager = new BufferedRandomAccessFileManager();

        /**
         * This means the clipboard is really a singleton for pasting between
         * projects.
         */
        myClipboard = MyClipboard.the(); // new MyClipboard(this);

        multiEventSelection = new MultiEventSelection(this);
        partSelection = new PartSelection(this);
        laneSelection = new LaneSelection(this);
        midiSelection = new MidiSelection(this); // Jens
        scriptingEngine = new FrinikaScriptingEngine(this); // Jens
        editHistoryContainer = new EditHistoryContainer();
        dragList = new DragList(this);
        soloManager = new SoloManager(this);


        createMixer();

        midiDeviceRouter = new MidiDeviceRouter();
        controlResolver = new ControlResolver();
    }

    /**
     * Create empty project
     * 
     */
    public ProjectContainer() throws Exception {
        this(0);
    }

    private void createSequencerPriorityListener() {

        sequencer.setPlayerPriority(FrinikaConfig.SEQUENCER_PRIORITY);
        FrinikaConfig.addConfigListener(new ConfigListener() {

            public void configurationChanged(ChangeEvent event) {
                if (event.getSource() == FrinikaConfig._SEQUENCER_PRIORITY) {
                    sequencer.setPlayerPriority(FrinikaConfig.SEQUENCER_PRIORITY);

                }
            }
        });
    }

    /**
     * This will load an old Frinika project (pre 0.2.0) based on the
     * projectSettings interface
     * 
     * @param projectSettings
     * @throws Exception
     */
    private ProjectContainer(ProjectSettings project) throws Exception {
        defaultInit();
        System.out.println(" LOADING PROJECT ");
        ByteArrayInputStream sequenceInputStream = new ByteArrayInputStream(
                project.getSequence());

        sequencer = new FrinikaSequencer();
        sequencer.open();
        attachTootNotifications();
        createSequencerPriorityListener();

        renderer = new FrinikaRenderer(this);

        sequence = new FrinikaSequence(MidiSequenceConverter.splitChannelsToMultiTrack(MidiSystem.getSequence(sequenceInputStream)));

        sequencer.setSequence(sequence);

        tempoList = new TempoList(sequence.getResolution(), this);
        tempoList.add(0, tempo);
        sequencer.setTempoList(tempoList);

        try { // TODO find all tempo changes
            setTempoInBPM(MidiSequenceConverter.findFirstTempo(sequence));
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        SynthRack synthRack = new SynthRack(null);

        SynthWrapper midiDev = new SynthWrapper(this, synthRack);
        synthRack.loadSynthSetup(project.getSynthSettings());

        // create a copy
        Vector<FrinikaTrackWrapper> origTracks = sequence.getFrinikaTrackWrappers();
        Vector<FrinikaTrackWrapper> tracks = new Vector<FrinikaTrackWrapper>(
                origTracks);

        projectLane = new ProjectLane(this);

        // we are going to rebuild this
        origTracks.removeAllElements();

        for (FrinikaTrackWrapper ftw : tracks) {
            // Use the first MidiEvent in ftw to detect the channel used

            MidiMessage msg = ftw.get(0).getMessage();
            if (msg instanceof ShortMessage) {
                ftw.setMidiDevice(midiDev);
                ftw.setMidiChannel(((ShortMessage) msg).getChannel());
                System.out.println(((ShortMessage) msg).getChannel() + " channel");
            }

            // PJS: The resolving of channel and device has to be done before
            // the ftw is attached to a lane (cause the following uperation can
            // change the first midi event)
            MidiLane lane = new MidiLane(ftw, this);
            if (ftw.getMidiChannel() > -1) {
                lane.setProgram(ftw.getMidiChannel(), 0, 0);
            }
            projectLane.addChildLane(lane);

            MidiPart part = new MidiPart(lane);
            long startTick = 0;
            long endTick = Long.MAX_VALUE;
            part.importFromMidiTrack(startTick, endTick);
        }

        int ticks = (int) getSequence().getTickLength();
        endTick = Math.max(endTick, ticks);

        addMidiOutDevice(midiDev);
        postInit();
        rebuildGUI();
    }

    /**
     * Import a Sequence (e.g. obtained from a MidiFile) into a new project
     * 
     * @param seq
     * @throws Exception
     */
    public ProjectContainer(Sequence seq) throws Exception {
        this(seq, null,false);
    }

    public ProjectContainer(Sequence seq, MidiDevice midiDevice)
            throws Exception {
        this(seq,midiDevice,false);
    }
    /**
     *
     * @param seq           sequence
     * @param midiDevice    assign tracks to mididevice.
     * @param adjustPPQ     recalculate the ticks if sequence  PPQ is not the defualt.
     *
     * @throws java.lang.Exception
     */
    public ProjectContainer(Sequence seq, MidiDevice midiDevice,boolean adjustPPQ)
           throws Exception {
    
        defaultInit();
        System.out.println(" LOADING MIDI SEQUENCE ");

        sequencer = new FrinikaSequencer();
        sequencer.open();
        attachTootNotifications();
        createSequencerPriorityListener();

        renderer = new FrinikaRenderer(this);

        if (seq.getDivisionType() == Sequence.PPQ) {
            ticksPerQuarterNote = seq.getResolution();
            System.out.println(" Ticks per quater is " + ticksPerQuarterNote);
        } else {
            System.out.println("WARNING: The resolution type of the imported Sequence is not supported by Frinika");
        }

        FrinikaSequence seq1 = new FrinikaSequence(MidiSequenceConverter.splitChannelsToMultiTrack(seq));

        if (adjustPPQ) { // TODO }
            try {
                // TODO }
                throw new Exception(" adjust PPQ not implemented yet");
            } catch (Exception ex) {
                Logger.getLogger(ProjectContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            sequence=seq1;
        }

//
//        int cnt = 0;
//        for (Track track : seq.getTracks()) {
//            System.out.println("TRACK " + cnt++ + "  ===================================================================");
//            for (int i = 0; i < track.size(); i++) {
//                MidiEvent e = track.get(i);
//                if (e.getMessage() instanceof ShortMessage) {
//                    ShortMessage shm=(ShortMessage)e.getMessage();
//                            
//                
//                if (e.getCommand() == ShortMessage.PROGRAM_CHANGE)) 
//                        (e.getMessage().getStatus() == ShortMessage.CONTROL_CHANGE)) {
//                    String tt = MidiDebugDevice.eventToString(e.getMessage());
//                    System.out.println(e.getTick() + " : " + tt);
//                }
//            }
//        }



        sequencer.setSequence(sequence);

        // create a copy
        Vector<FrinikaTrackWrapper> origTracks = sequence.getFrinikaTrackWrappers();
        Vector<FrinikaTrackWrapper> tracks = new Vector<FrinikaTrackWrapper>(
                origTracks);


        projectLane = new ProjectLane(this);

        // we are going to rebuild this but leave in the first track 
        int n=origTracks.size();
        for (int i=n-1;i>0;i--) {
            origTracks.remove(i);
        }

      //  int count=0;

        for (FrinikaTrackWrapper ftw : tracks) {

            // Use the first MidiEvent in ftw to detect the channel used
            // Detect by first ShortMessage find (FIX by KH)
            for (int i = 0; i < ftw.size(); i++) {
                MidiMessage msg = ftw.get(i).getMessage();
                if (msg instanceof ShortMessage) {
                    ftw.setMidiChannel(((ShortMessage) msg).getChannel());
                    System.out.println(((ShortMessage) msg).getChannel() + " channel");
                    break;
                }
            }

            int progChange = -1;
            for (int i = 0; i < ftw.size(); i++) {
                MidiMessage msg = ftw.get(i).getMessage();
                if (msg instanceof ShortMessage) {
                    ShortMessage shm = (ShortMessage) msg;
                    if (shm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        if (progChange == -1) {
                            String tt = MidiDebugDevice.eventToString(msg);
                            progChange = ((ShortMessage) msg).getData1();
                            System.out.println(" PROG CHANGE =" + tt + "    " + progChange);
                        } else {
                            System.out.println(" MULTIPLE PROG CHANGES !!!!!!");
                        }
                    }
                // break;
                }
            }

            // only create a lane if it has a program change (PJL)
            if (progChange >= 0) {
                // PJS: The resolving of channel and device has to be done before
                // the ftw is attached to a lane (cause the following uperation can
                // change the first midi event)
                MidiLane lane = new MidiLane(ftw, this);
                System.out.println(" Creting MidiLane with track " + count);

                lane.setProgram(progChange, 0, 0);

                projectLane.addChildLane(lane);

                MidiPart part = new MidiPart(lane);
                long startTick = 0;
                long endTick1 = Long.MAX_VALUE;
                part.importFromMidiTrack(startTick, endTick1);

                // Find name of the track from the sequence file
                for (int i = 0; i < ftw.size(); i++) {
                    MidiEvent event = ftw.get(i);
                    if (event.getTick() > 0) {
                        break;
                    }
                    MidiMessage msg = event.getMessage();
                    if (msg instanceof MetaMessage) {
                        MetaMessage meta = (MetaMessage) msg;
                        if (meta.getType() == 3) // Track text
                        {
                            if (meta.getLength() > 0) {
                                try {
                                    String txt = new String(meta.getData());
                                    lane.setName(txt);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }
                    }
                }

//                // added these 2 because it did not play!!
//                lane.attachFTW();
//                lane.onLoad();
//                   //
            } else if (title == null) {
                // Find name of the track from the sequence file
                for (int i = 0; i < ftw.size(); i++) {
                    MidiEvent event = ftw.get(i);
                    if (event.getTick() > 0) {
                        break;
                    }
                    MidiMessage msg = event.getMessage();
                    if (msg instanceof MetaMessage) {
                        MetaMessage meta = (MetaMessage) msg;
                        if (meta.getType() == 3) // Track text
                        {
                            if (meta.getLength() > 0) {
                                try {
                                    String txt = new String(meta.getData());
                                    title = txt;
                                    System.out.println("setTing title \"" + txt + "\"");
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }
                    }
                }


            }
            count=count+1;

        }

        try { // TODO find all tempos
            setTempoInBPM(MidiSequenceConverter.findFirstTempo(seq));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rebuildGUI();

        if (midiDevice != null) {
            try {
                // midiDevice.open();
                midiDevice = new SynthWrapper(this, midiDevice);

                addMidiOutDevice(midiDevice);

            } catch (Exception e2) {
                e2.printStackTrace();
                midiDevice = null;
            }
        }
        for (FrinikaTrackWrapper ftw : tracks) {
            if (midiDevice != null) {
                ftw.setMidiDevice(midiDevice);
            }
        }
        postInit();

    }

    public static ProjectContainer loadProject(File file) throws Exception {
        ProjectContainer proj;
        if (file.exists()) {
            if (file.getName().toLowerCase().contains(".mid")) {
                proj = new ProjectContainer(MidiSystem.getSequence(file));
            } else {
                proj = loadCompressedProject(file);
            }
        } else {
            proj = new ProjectContainer();
        }
        proj.projectFile = file;
        return proj;
    }

    public static ProjectContainer loadCompressedProject(File file)
            throws Exception {
        ProjectContainer project = null;

        // Check if InputStream is compressed
        InputStream inputStream = new FileInputStream(file);
        byte[] magic = new byte[4];
        inputStream.read(magic);
        inputStream.close();

        // Check if magic is: 0x50,0x4b,0x03,0x04 (ZIP)
        // If stream is uncompressed then magic is: 0xac,0xed (objectstream)
        if (magic[3] == (byte) 0x04 && magic[2] == (byte) 0x03 && magic[1] == (byte) 0x4b && magic[0] == (byte) 0x50) {
            // Use Zip Decoding
            FileInputStream fileinputStream = new FileInputStream(file);
            inputStream = fileinputStream;
            try {
                ZipInputStream zipi = new ZipInputStream(inputStream);
                zipi.getNextEntry();
                inputStream = zipi;
                inputStream = new BufferedInputStream(inputStream);
                project = loadProject(inputStream);
                project.compression_level = 1;
            } finally {
                fileinputStream.close();
            }
        }
        if (magic[0] == (byte) 0x4c && magic[1] == (byte) 0x5a && magic[2] == (byte) 0x4d && magic[3] == (byte) 0x61) {
            // User Lzma Decoding
            FileInputStream fileinputStream = new FileInputStream(file);
            inputStream = fileinputStream;
            try {
                inputStream.read(magic);
                InputStream inStream = inputStream;

                int propertiesSize = 5;
                byte[] properties = new byte[propertiesSize];
                if (inStream.read(properties, 0, propertiesSize) != propertiesSize) {
                    throw new Exception("input .lzma file is too short");
                }
                final SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
                if (!decoder.SetDecoderProperties(properties)) {
                    throw new Exception("Incorrect stream properties");
                }
                long outSize = 0;
                for (int i = 0; i < 8; i++) {
                    int v = inStream.read();
                    if (v < 0) {
                        throw new Exception("Can't read stream size");
                    }
                    outSize |= ((long) v) << (8 * i);
                }

                File tempfile = File.createTempFile("lzma", "temp");
                FileOutputStream fos = new FileOutputStream(tempfile);
                try {
                    try {
                        if (!decoder.Code(inStream, fos, outSize)) {
                            throw new Exception("Can't decode stream");
                        }

                    } finally {
                        fos.close();
                    }

                    inputStream = new FileInputStream(tempfile);
                    try {
                        project = loadProject(inputStream);
                        project.compression_level = 2;
                    } finally {
                        inputStream.close();
                    }

                } finally {
                    tempfile.delete();
                }

            } finally {
                fileinputStream.close();
            }

        }

        if (project == null) {
            FileInputStream fileinputStream = new FileInputStream(file);
            try {
                project = loadProject(fileinputStream);
            } finally {
                fileinputStream.close();
            }

        }

        return project;
    }

    public static ProjectContainer loadProject(InputStream inputStream)
            throws Exception {

        if (SplashDialog.isSplashVisible()) {
            SplashDialog splash = SplashDialog.getInstance();
            JProgressBar bar = splash.getProgressBar();
            bar.setMaximum(inputStream.available());
            inputStream = new ProgressBarInputStream(bar, inputStream);
        }

        ObjectInputStream in = new ObjectInputStreamFixer(inputStream);
        Object obj = in.readObject();

        if (SplashDialog.isSplashVisible()) {
            SplashDialog splash = SplashDialog.getInstance();
            JProgressBar bar = splash.getProgressBar();
            bar.setValue(bar.getMaximum());
        }
        if (obj instanceof ProjectSettings) {
            return new ProjectContainer((ProjectSettings) obj);
        } else {
            return (ProjectContainer) obj;
        }
    }
    /**
     * Save project to a file
     * 
     * @param file
     */
    public transient int compression_level = 0;
    transient private TimeUtils timeUtils;    // Keep a note of all open midi out devices
    private static Vector<MidiDevice> midiOutList = new Vector<MidiDevice>();

    public void saveProject(File file) throws IOException { // throw exception
        // so ProjectFrame
        // can show error
        // message, user
        // should know if
        // saving went wrong

        if (mixerSerializer == null) {
            System.out.println(" Creating serialization for the mixer ");
            mixerSerializer = new TootMixerSerializer(this);
        }
        // try {

        // Added compression to frinika projects
        int usecompression = compression_level; // 0=no compression, 1=zip,
        // 2=Lzma

        if (usecompression == 2) // Use Lzma Compression
        {

            File tempfile = File.createTempFile("lzma", "temp");
            FileInputStream fis = null;
            try {
                FileOutputStream fos = new FileOutputStream(tempfile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                try {
                    oos.writeObject(this);
                } finally {
                    oos.close();
                    fos.close();
                }
                fis = new FileInputStream(tempfile);

                byte[] magic = new byte[4];
                magic[0] = (byte) 0x4c;
                magic[1] = (byte) 0x5a;
                magic[2] = (byte) 0x4d;
                magic[3] = (byte) 0x61;

                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(magic);
                InputStream inStream = fis;

                boolean eos = false;
                SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();
                encoder.SetAlgorithm(2);
                encoder.SetDictionarySize(1 << 23);
                encoder.SeNumFastBytes(128);
                encoder.SetMatchFinder(1);
                encoder.SetLcLpPb(3, 0, 2);
                encoder.SetEndMarkerMode(eos);
                encoder.WriteCoderProperties(outStream);
                long fileSize = tempfile.length();
                for (int i = 0; i < 8; i++) {
                    outStream.write((int) (fileSize >>> (8 * i)) & 0xFF);
                }
                encoder.Code(inStream, outStream, -1, -1, null);

                outStream.close();

            } finally {
                if (fis != null) {
                    fis.close();
                }
                tempfile.delete();
            }

        } else if (usecompression == 1) // Use ZIP Compression
        {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry(file.getName()));
            BufferedOutputStream bos = new BufferedOutputStream(zos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            bos.flush();
            zos.closeEntry();
            zos.finish();
            fos.close();
        } else // Use no compression
        {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        }
        projectFile = file;
        editHistoryContainer.updateSavedPosition();
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    /***********************************************************************
     * // Saving in 0.1 series format try { ByteArrayOutputStream
     * sequenceOutputStream = new ByteArrayOutputStream();
     * MidiSystem.write(sequence, 1, sequenceOutputStream);
     * 
     * Project20050227 project = new Project20050227();
     * project.setSequence(sequenceOutputStream.toByteArray()); MidiDevice
     * firstDevice = sequencer.listMidiOutDevices().iterator() .next(); if
     * (firstDevice instanceof SynthRack)
     * project.setSynthSettings(((SynthRack) firstDevice) .getSynthSetup());
     * ObjectOutputStream out = new ObjectOutputStream( new
     * FileOutputStream(file)); out.writeObject(project); projectFile =
     * file; } catch (Exception e) { e.printStackTrace(); }
     **********************************************************************/
    }

    /**
     * @return Returns the lanes.
     */
    public List<Lane> getLanes() {
        return projectLane.getFamilyLanes();
    }

//    /**
//     * Creates a XAudioLane and adds it to the Lane collection
//     * 
//     * @return
//     */
//    public XAudioLane createXAudioLane() {
//        XAudioLane lane = new XAudioLane(this);
//        add(lane);
//        return lane;
//    }
    /**
     * Creates a AudioLane and adds it to the Lane collection
     * 
     * @return
     */
    public AudioLane createAudioLane() {
        AudioLane lane = new AudioLane(this);
        add(lane);
        return lane;
    }

    /**
     * Creates a TextLane and adds it to the Lane collection
     */
    public TextLane createTextLane() { // Jens
        TextLane lane = new TextLane(this);
        add(lane);
        return lane;
    }

    /**
     * @return Returns the sequencer.
     */
    public FrinikaSequencer getSequencer() {
        return sequencer;
    }

    /**
     * Creates a sequence based on the resolution defined in ticksPerQuarterNote
     * 
     */
    public void createSequence() {
        if (sequence == null) {
            try {
                if (ticksPerQuarterNote == 0) {
                    ticksPerQuarterNote = FrinikaConfig.TICKS_PER_QUARTER;
                }
                sequence = new FrinikaSequence(Sequence.PPQ,
                        ticksPerQuarterNote, 1);
                sequencer.setSequence(sequence);
            } catch (InvalidMidiDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Returns the sequence.
     */
    public FrinikaSequence getSequence() {
        return sequence;
    }

    /**
     * @return Returns the renderer.
     */
    public FrinikaRenderer getRenderer() {
        return renderer;
    }

    /**
     * If this projectContainer was initialized by a project file -the return
     * the file
     * 
     * @return
     */
    public File getProjectFile() {
        return projectFile;
    }

    /**
     * @return Returns the multiEventSelection.
     */
    public MultiEventSelection getMultiEventSelection() {
        return multiEventSelection;
    }

    /**
     * 
     * @return the Part selection container for this project.
     */
    public PartSelection getPartSelection() {
        return partSelection;
    }

    /**
     * 
     * @return the Lane selection container for this project.
     */
    public LaneSelection getLaneSelection() {
        return laneSelection;
    }

    /**
     * 
     * @return the Midi selection container for this project.
     */
    public MidiSelection getMidiSelection() { // Jens
        return midiSelection;
    }

    public FrinikaScriptingEngine getScriptingEngine() { // Jens
        return scriptingEngine;
    }

    /**
     * 
     * @return the Edit history container for this project.
     */
    public EditHistoryContainer getEditHistoryContainer() {
        return editHistoryContainer;
    }

    /**
     * Close the project
     * 
     */
    public void close() {
        if (renderer != null) {
            renderer.close();
        }
        sequencer.close();
        audioFileManager.stop();
    }

    /**
     * adds a lane to the project and updates the history
     */
    public void add(Lane lane) {
        projectLane.addChildLane(lane);
        // System. out.println(" about to lane.onload");f
        lane.onLoad();
        editHistoryContainer.push(this,
                EditHistoryRecordableAction.EDIT_HISTORY_TYPE_ADD, lane);
    }

    public void add(int index, Lane lane) {
        projectLane.addChildLane(index, lane);
        lane.onLoad();
        editHistoryContainer.push(this,
                EditHistoryRecordableAction.EDIT_HISTORY_TYPE_ADD, lane);
    }

    public void remove(Lane lane) {
        projectLane.removeChildLane(lane);
        getEditHistoryContainer().push(this,
                EditHistoryRecordableAction.EDIT_HISTORY_TYPE_REMOVE, lane);
    }

    /**
     * 
     * Lanes can contain other lanes. A project is contained within a project
     * lane.
     * 
     * @return top level Lane that containes all others.
     */
    public ProjectLane getProjectLane() {
        return projectLane;
    }

    public SynthRack getSynthRack() {
        return audioSynthRack;
    }

    public MidiResource getMidiResource() {
        return midiResource;
    }

    public FrinikaTrackWrapper getTempoTrack() {
        return sequence.getFrinikaTrackWrappers().get(0);
    }

    /**
     * 
     * Set the tempo of the first event in the tempo list
     * 
     * @param tempo
     */
    public void setTempoInBPM(float tempo) {
        getTempoList().add(0, tempo);

    }

    public void buildMidiIndex() {
        int mdIndex = 0;
        midiDeviceIndex = new HashMap<MidiDevice, Integer>();

        for (MidiDevice midiDev : sequencer.listMidiOutDevices()) {
            if (midiDev instanceof SynthRack) {
                ((SynthRack) midiDev).setSaveReferencedData(saveReferencedData);
            }
            if (midiDev instanceof SynthWrapper) {
                ((SynthWrapper) midiDev).setSaveReferencedData(saveReferencedData);
            }
            System.out.println(midiDeviceDescriptorMap.get(midiDev).getProjectName() + "(" + midiDeviceDescriptorMap.get(midiDev).getMidiDeviceName() + ") has index " + mdIndex);
            midiDeviceIndex.put(midiDev, mdIndex++);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Store the used midi devices

        //        int mdIndex = 0;
 //        midiDeviceIndex = new HashMap<MidiDevice, Integer>();

        /**
         * Use 0.3.0 format instead
         */
        synthSettings = null;
        externalMidiDevices = null;

        buildMidiIndex();

//        for (MidiDevice midiDev : sequencer.listMidiOutDevices()) {
//            if (midiDev instanceof SynthRack) {
//                ((SynthRack) midiDev).setSaveReferencedData(saveReferencedData);
//            }
//            if (midiDev instanceof SynthWrapper) {
//                ((SynthWrapper) midiDev).setSaveReferencedData(saveReferencedData);
//            }
//            System.out.println(midiDeviceDescriptorMap.get(midiDev).getProjectName() + "(" + midiDeviceDescriptorMap.get(midiDev).getMidiDeviceName() + ") has index " + mdIndex);
//            midiDeviceIndex.put(midiDev, mdIndex++);
//        }

        /**
         * Deprecated from 0.3.0
         * 
         * synthSettings = new ArrayList<SynthSettings>(); externalMidiDevices =
         * new Vector<String>(); // Since SynthRack and external midi are
         * separate arrays - they have to be saved groupwise so that // the
         * midiDeviceIndexes are correct on reload (This is a fix of bugid
         * 1506823)
         * 
         * for (MidiDevice midiDev : sequencer.listMidiOutDevices()) { if
         * (midiDev instanceof SynthRack) { ((SynthRack)
         * midiDev).setSaveReferencedData(saveReferencedData);
         * synthSettings.add(((SynthRack) midiDev).getSynthSetup());
         * midiDeviceIndex.put(midiDev, mdIndex++); } }
         * 
         * for (MidiDevice midiDev : sequencer.listMidiOutDevices()) { if
         * (!(midiDev instanceof SynthRack)) {
         * externalMidiDevices.add(midiDev.getDeviceInfo().toString());
         * midiDeviceIndex.put(midiDev, mdIndex++); } }
         */
        loopStartPoint = sequencer.getLoopStartPoint();
        loopEndPoint = sequencer.getLoopEndPoint();
        loopCount = sequencer.getLoopCount();
        midiRouterSerialization = new MidiRouterSerialization();
        midiRouterSerialization.buildSerialization(controlResolver, midiDeviceRouter);
        out.defaultWriteObject();

    }

    /**
     * 
     * Note we don't rebuild the midirouter here because we want to wait untill all the controllers have been created by the GUI.
     * 
     * 
     * 
     * @param in
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        defaultInit();

        // --------------- Create a sequencer that we can load data into

        sequencer = new FrinikaSequencer();
        try {
            sequencer.open();
            attachTootNotifications();

            midiResource = new MidiResource(sequencer);

        } catch (Exception e) {
            e.printStackTrace();
        }
        renderer = new FrinikaRenderer(this);

        // ------------ Now read the object. This will also generate
        // FrinikaTrackWrappers in the lanes

        in.defaultReadObject();

        createSequence();
        /**
         * This is for backwards compatibility. The serialized form of
         * a Frinika SynthRack is for older projects stored directly
         * in the ProjectContainer in the synthSettings property.
         * 
         * As you can see below, when the "old" project is loaded
         * the Frinka Synthrack is put into a MidiDeviceDescriptor,
         * so that when saved next time it follows the new format.
         * 
         * In the future we might remove this code - and instruct users
         * to convert old projects using an older version of Frinika
         */
        if (synthSettings != null || externalMidiDevices != null) {
            // Make sure that the project doesn't contain both
            if (midiDeviceDescriptors != null) {
                this.midiDeviceDescriptors.clear();
            } else // Or convert this into a new project version
            {
                this.midiDeviceDescriptors = new ArrayList<MidiDeviceDescriptor>();
            }
            this.midiDeviceDescriptorMap = new HashMap<MidiDevice, MidiDeviceDescriptor>();

            // ------------ Initialize soft synths

            for (SynthSettings synthSetup : synthSettings) {
                SynthRack synthRack = new SynthRack(null);                
                try {
                    MidiDevice midiDevice = new SynthWrapper(this, synthRack);
                    synthRack.loadSynthSetup(synthSetup);
                    addMidiOutDevice(midiDevice);

                    /**
                     * Fix the lane program change event for older projects
                     */
                    if (!synthSetup.hasProgramChangeEvent()) {
                        FrinikaSynthRackDescriptor.fixLaneProgramChange(this,
                                midiDevice);
                    }
                } catch (MidiUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // ---------- and the other ones

            if (externalMidiDevices != null) { // allow loading of previous
                // versions
                for (String name : externalMidiDevices) {
                    MidiDevice dev = MidiHub.getMidiOutDeviceByName(name);
                    if (dev == null) {
                        System.out.println(" Failed to find MidiDevice " + name);
                    } else {
                        SynthWrapper externMidi = new SynthWrapper(this, dev);

                        try {
                            externMidi.open();
                        } catch (MidiUnavailableException e) {
                            System.out.println(" Failed to open MidiDevice " + name);
                            e.printStackTrace();
                        }

                        try {
                            addMidiOutDevice(externMidi);
                        } catch (MidiUnavailableException e) {
                            System.out.println(" Failed to add MidiDevice " + name);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            installMidiDevices();
        }
        /**
         * Regenerate MIDI events - This has to be done after the
         * deserialization cause one cannot be sure of the ordering of parts,
         * lanes and multievents
         */
        validate(); /// DEBUG REMOVE IF IT ANNOYS

        projectLane.onLoad();

        // PJL tempo stuff for old files (before tempo list)
        if (tempoList == null) {
            tempoList = new TempoList(sequence.getResolution(), this);
            tempoList.add(0, tempo); // use the tempo from the old project to
        // set first event
        }
        tempoList.reco();
        sequencer.setTempoList(tempoList);
        sequencer.setLoopStartPoint(loopStartPoint);
        sequencer.setLoopEndPoint(loopEndPoint);
        sequencer.setLoopCount(loopCount);
        postInit();
        rebuildGUI();
    }

    /**
     * Go through the mididevice descriptor map and install mididevices
     */
    public void installMidiDevices() {
        this.midiDeviceDescriptorMap = new HashMap<MidiDevice, MidiDeviceDescriptor>();
        for (MidiDeviceDescriptor midiDeviceDescriptor : midiDeviceDescriptors) {
            System.out.println("Installing Midi device: " + midiDeviceDescriptor.getMidiDeviceName() + " as " + midiDeviceDescriptor.getProjectName());
            midiDeviceDescriptor.install(this);
        }
    }

    public void setSaveReferencedData(boolean saveReferencedData) {
        this.saveReferencedData = saveReferencedData;

    }

    public Integer getMidiDeviceIndex(MidiDevice midiDevice) {
        return midiDeviceIndex.get(midiDevice);
    }

    public SelectionFocusable getSelectionFocus() {
        return selectionFocus;
    }

    public void setSelectionFocus(SelectionFocusable focus) {
        // System.out.println(" Set project focus " + focus);
        selectionFocus = focus;

    }

    public MyClipboard clipBoard() {
        // TODO Auto-generated method stub
        return myClipboard;
    }

    public void rebuildGUI() {
        ViewableLaneList list = new ViewableLaneList(this);
        list.rebuild();
        /*
         * for (Lane lane : list) {
         * 
         * if (lane.getParts() != null) { for (Part p : lane.getParts()) {
         * System.out.println(p); } } }
         */

        // projectLane.setHidden(true);
        // projectLane.getChildren().get(0).setHidden(true);
        // TODO should this part of the loadProject ?
        // Myabe not. Resources vary from machine to machine.
        // Need to discuss this ?
        midiResource = new MidiResource(sequencer);
    }

    /**
     * 
     * @return piano roll quantization in ticks
     */
    public double getPianoRollSnapQuantization() {
        return pianoRollSnapQuantization;
    }

    public double getPartViewSnapQuantization() {
        return partViewSnapQuantization;

    }

    public void setPianoRollSnapQuantization(double val) {
        pianoRollSnapQuantization = val;
    }

    public void setPartViewSnapQuantization(double val) {
        partViewSnapQuantization = val;
    }

    public boolean isPianoRollSnapQuantized() {
        return isPianoRollSnapQuantized;
    }

    public boolean isPartViewSnapQuantized() {
        return isPartViewSnapQuantized;

    }

    public void setPianoRollSnapQuantized(boolean val) {
        isPianoRollSnapQuantized = val;
    }

    public void setPartViewSnapQuantized(boolean val) {
        isPartViewSnapQuantized = val;
    }

    public long eventQuantize(long tick) {
        if (isPianoRollSnapQuantized) {
            tick = (long) (Math.rint(tick / pianoRollSnapQuantization) * pianoRollSnapQuantization);
        }
        return tick;
    }

    public long partQuantize(long tick) {
        double tt = tick;
        if (isPartViewSnapQuantized) {


            double quant = partViewSnapQuantization;
            if (quant > 0.0) {
                tt = (long) (tt / quant) * quant;

            } else {
                double beat = tt / getTicksPerBeat();
                TimeSignatureEvent ev = getTimeSignatureList().getEventAtBeat((int) beat);
                int nBar = (int) ((beat - ev.beat + ev.beatsPerBar / 2.0) / ev.beatsPerBar);
                tt = (ev.beat + nBar * ev.beatsPerBar) * getTicksPerBeat();
            // System.out.println(" STT -ve quant " + tt);
            }
        }
        return (long) tt;
    }

    public long getEndTick() {
        return endTick;
    }

    public void setEndTick(long tick) {
        if (tick == endTick) {
            return;
        }
        endTick = tick;
        sequencer.notifySongPositionListeners();
    }

    /**
     * 
     * DEBUGING --- NOT FOR PUBLIC USE
     * 
     */
    public void validate() {
        validate(projectLane);
    }

    /**
     * 
     * DEBUGING --- NOT FOR PUBLIC USE
     * 
     * @param parent
     */
    public void validate(Lane parent) {
        for (Part part : parent.getParts()) {
            assert (part.getLane() == parent);
            if (part instanceof MidiPart) {
                MidiPart midiPart = (MidiPart) part;
                if (part.getStartTick() > part.getEndTick()) {
                    System.out.println("Correcting invalid data " + part.getStartTick() + "-->" + part.getEndTick());

                    part.setStartTick(part.getEndTick());

                }
                for (MultiEvent ev : midiPart.getMultiEvents()) {
                    assert (ev.getPart() == part);
                }
            }
        }

        for (Lane lane : parent.getChildren()) {
            validate(lane);
        }

    }

    public void resetEndTick() {
        setEndTick(projectLane);
    }

    private void setEndTick(Lane parent) {
        for (Part part : parent.getParts()) {
            assert (part.getLane() == parent);
            //	if (part instanceof MidiPart) {
            //  		MidiPart midiPart = (MidiPart) part;
            endTick = Math.max(endTick, part.getEndTick());
        //	}
        }

        for (Lane lane : parent.getChildren()) {
            setEndTick(lane);
        }

    }

    public Vector<Lane> recordableLaneList() {
        Vector<Lane> list = new Vector<Lane>();

        addRecordableLanes(list, projectLane);

        return list;
    }

    private void addRecordableLanes(Vector<Lane> list, Lane parent) {
        for (Lane lane : parent.getChildren()) {
            if (lane instanceof RecordableLane) {
                list.add(lane);
            }
            addRecordableLanes(list, lane);
        }
    }

    public File getFile() {
        return projectFile;
    }

    /**
     * 
     * @return
     */
    public File getAudioDirectory() {
        if (audioDir != null) {
            return audioDir;
        } else {
            newAudioDirectory();
        }
        return audioDir;
    }

    private void newAudioDirectory() {
        File file = null;
        int count = 0;
        String base = "New";

        if (projectFile != null) {
            base = projectFile.getName();
            int index = base.indexOf('.');
            if (index > 0) {
                base = base.substring(0, index);
            }
        }

        file = new File(FrinikaConfig.AUDIO_DIRECTORY, base);

        while (file.exists()) {
            file = new File(FrinikaConfig.AUDIO_DIRECTORY, base + "_" + count++);
        }

        file.mkdirs();
        audioDir = file;
    //		
    // File audioDir = new File(.getParentFile(), "audio");
    // if (!audioDir.exists())
    // audioDir.mkdir();
    // return audioDir.toString();

    }

    /**
     * Add a midi device to the project. The midi device will be added to the
     * sequencer, and a descriptor of how to reopen the mididevice from a saved
     * instance will be added. Extra information such as custom name and
     * soundbank info is also part of the descriptor.
     * 
     * @param midiDev
     * @throws MidiUnavailableException
     */
    public MidiDeviceDescriptor addMidiOutDevice(MidiDevice midiDev)
            throws MidiUnavailableException {
        // First create the MidiDeviceDescriptor

        MidiDeviceDescriptor descriptor = null;

        /**
         * Check if this is an external Javasound plugin or mididevice
         */
        if (midiDev instanceof SynthWrapper) {
            SynthWrapper synthWrapper = (SynthWrapper) midiDev;

            if (synthWrapper.getRealDevice().getClass().isAnnotationPresent(
                    MidiDeviceDescriptorClass.class)) {
                try {
                    descriptor = (MidiDeviceDescriptor) synthWrapper.getRealDevice().getClass().getAnnotation(
                            MidiDeviceDescriptorClass.class).value().newInstance();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } // Check for SynthRack first since this also implements the
            // Synthesizer if
            else if (synthWrapper.getRealDevice() instanceof SynthRack) {
                descriptor = new FrinikaSynthRackDescriptor(synthWrapper);
            } else if (synthWrapper.getRealDevice() instanceof DrumMapper) {
                descriptor = new DrumMapperDescriptor(synthWrapper, this);
            } else if (synthWrapper.getRealDevice() instanceof Synthesizer) {
                descriptor = new SynthesizerDescriptor((Synthesizer) midiDev);
            } else {
                descriptor = new MidiDeviceDescriptor(midiDev);
            }
            if (synthWrapper.soundBankFile != null) {
                if (descriptor instanceof SoundBankNameHolder) {
                    ((SoundBankNameHolder) descriptor).setSoundBankFileName(synthWrapper.soundBankFile);
                }
            }
        }

        // Set a default name
        descriptor.setProjectName(midiDev.getDeviceInfo().getName());

        // Insert the new descriptor
        this.midiDeviceDescriptors.add(descriptor);

        // PJL
        // OK for rasmusDSP but not a very neat way to find out
        SynthLane lane = createSynthLane(descriptor); // The correct way is to
        // check for Mixer
        // interface
        if (midiDev instanceof SynthWrapper && ((SynthWrapper) midiDev).getAudioProcess() != null) {
            lane.attachAudioProcessToMixer();
        }
        // *************

        // Finally load the new descriptor
        loadMidiOutDevice(descriptor);
        return descriptor;
    }

    /**
     * Package private method used by descriptors to install MidiOutdevices.
     * Will create the neccesary mappings, and add the device to the sequencer
     * 
     * You should not use this to add a new Midi device - use the public method
     * addMidiOutDevice for that
     * 
     * @param descriptor
     * @throws MidiUnavailableException
     */
    void loadMidiOutDevice(MidiDeviceDescriptor descriptor)
            throws MidiUnavailableException {

        this.midiDeviceDescriptorMap.put(descriptor.getMidiDevice(), descriptor);
        sequencer.addMidiOutDevice(descriptor.getMidiDevice());
        midiOutList.add(descriptor.getMidiDevice()); // PJL keep note of open
    // devices

    }

    /**
     * 
     */
    static public void closeAllMidiOutDevices() {
        for (MidiDevice dev : midiOutList) {
            System.out.println(" Closing " + dev);
            dev.close();
        }
    }

    /**
     * Get the midi device descriptor for the given midi device
     * 
     * @param midiDevice
     * @return
     */
    public MidiDeviceDescriptor getMidiDeviceDescriptor(MidiDevice midiDevice) {
        return midiDeviceDescriptorMap.get(midiDevice);
    }

    /**
     * Remove a midiOutDevice from the project
     * 
     * @param midiDevice
     */
    public void removeMidiOutDevice(MidiDevice midiDevice) {

        midiDeviceDescriptors.remove(midiDeviceDescriptorMap.get(midiDevice));
        midiDeviceDescriptorMap.remove(midiDevice);
        sequencer.removeMidiOutDevice(midiDevice);
    }

    public List<MidiDeviceDescriptor> getMidiDeviceDescriptors() {
        return midiDeviceDescriptors;
    }

    public MixerControls getMixerControls() {
        return mixerControls;
    }

    public FrinikaAudioServer getAudioServer() {
        return audioServer;
    }

    public AudioMixer getMixer() {
        return mixer;
    }

    public AudioInjector getOutputProcess() {
        return outputProcess;
    }

    public void message(String string) {
        ProjectFrame.staticMessage(this, string);

    }

    public SynthLane createSynthLane(MidiDeviceDescriptor desc) {
        SynthLane lane = new SynthLane(this, desc);
        add(lane);
        return lane;
    }

    public BufferedRandomAccessFileManager getAudioFileManager() {
        // TODO Auto-generated method stub
        return audioFileManager;
    }

    // PJL SequencerListener not used ???? I delete to avoid confusion
    class ProjectAudioClient implements AudioClient { //, SequencerListener {

        double tick = 0;
        long framePtr = 0;
        double sampleRate;
        double ticksPerBuffer;

        ProjectAudioClient() {
            sampleRate = FrinikaConfig.sampleRate;

        }

        public void work(int bufsize) {
            // if (sequencer != null && sequence != null) {
            // if (sequencer.isRunning()) {
            // double ticksPerBuffer = FrinikaAudioSystem
            // .getAudioBufferSize()
            // / samplesPerTick();
            // sequencer.setRealtime(false);
            // while (sequencer.getTickPosition() < tick){
            // sequencer.nonRealtimeNextTick();
            // }
            // tick += ticksPerBuffer;
            // }
            // }
            mixer.work(bufsize);
        }

        public void setEnabled(boolean b) {
            mixer.setEnabled(b);
        }

//		public void beforeStart() {
//			tick = sequencer.getTickPosition();
//			framePtr = (long) (tick * samplesPerTick());
//		}
        public void start() {
        }

        public void stop() {
        }//		double samplesPerTick() {
//
//			// TODO check if OK for changing tempos ticksPerBeat beatPerMin
//			double ticksPerSecond = (sequence.getResolution() * sequencer
//					.getTempoInBPM()) / 60.0;
//
//			return (sampleRate / ticksPerSecond);
//		}
    }

    public AudioClient getAudioClient() {
        return audioClient;
    }

    /**
     * Creates a MidiLane and adds it to the Lane collection
     * 
     * @return
     */
    public MidiLane createMidiLane() {
        sequence.createTrack();
        FrinikaTrackWrapper ftw = sequence.getFrinikaTrackWrappers().lastElement();
        ftw.setMidiChannel(0);
        MidiLane lane = new MidiLane(ftw, this);
    // Set channel 1 (0) as default MIDI channel
        // MidiLane lane = new MidiLane(ftw,this);

        add(lane);
        return lane;
    }

    public void createMidiLanesFromSequence(Sequence seq, MidiDevice midiDevice) {

        // Vector<MidiLane> lanesToLoad = new Vector<MidiLane>();

        // FrinikaSequence fSeq = sequence;

        if (seq.getDivisionType() == Sequence.PPQ) {
            int ticksPerQuarterNote1 = seq.getResolution();
            System.out.println(" Project PPQ = " + ticksPerQuarterNote);
            System.out.println(" Midi    PPQ = " + ticksPerQuarterNote1);
        } else {
            System.out.println("WARNING: The resolution type of the imported Sequence is not supported by Frinika");
        }

        // Vector<FrinikaTrackWrapper> origTracks = new
        // Vector<FrinikaTrackWrapper>(
        // sequence.getFrinikaTrackWrappers());
        //
        // Vector<FrinikaTrackWrapper> midiTracks = sequence
        // .addSequence(

        Sequence splitSeq = MidiSequenceConverter.splitChannelsToMultiTrack(seq);



        int nTrack = splitSeq.getTracks().length;
        System.out.println(" Adding " + (nTrack) + " tracks ");

        // sequencer.setSequence(sequence);

        // create a copy

        // we are going to rebuild this
        // origTracks.removeAllElements();

        getEditHistoryContainer().mark(
                getMessage("sequencer.project.add_midi_lane"));

        for (int iTrack = 0; iTrack < nTrack; iTrack++) {

            int chan = 0;
            Track track = splitSeq.getTracks()[iTrack];
            // if (origTracks.contains(ftw)) continue;
            // Use the first MidiEvent in ftw to detect the channel used
            // Detect by first ShortMessage find (FIX by KH)
            for (int i = 0; i < track.size(); i++) {
                MidiMessage msg = track.get(i).getMessage();
                if (msg instanceof ShortMessage) {
                    chan = ((ShortMessage) msg).getChannel();
                    break;
                }
            }

            // PJS: The resolving of channel and device has to be done before
            // the ftw is attached to a lane (cause the following uperation can
            // change the first midi event)
            // FrinikaTrackWrapper ftw=sequence.createFrinikaTrack();
            MidiLane lane = createMidiLane(); // new MidiLane(ftw, this);

            // lanesToLoad.add(lane);

            lane.setMidiChannel(chan);

            MidiPart part = new MidiPart(lane);
            long startTick = 0;
            long endTick = Long.MAX_VALUE;
            part.importFromMidiTrack(track, startTick, endTick);

            // Find name of the track from the sequence file
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getTick() > 0) {
                    break;
                }
                MidiMessage msg = event.getMessage();
                if (msg instanceof MetaMessage) {
                    MetaMessage meta = (MetaMessage) msg;
                    if (meta.getType() == 3) // Track text
                    {
                        if (meta.getLength() > 0) {
                            try {
                                String txt = new String(meta.getData());
                                lane.setName(txt);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                }
            }
            part.commitEventsAdd();
        // seq.(ftw);
        // add(lane);
        // part.onLoad();
        }

        rebuildGUI();

        if (midiDevice != null) {
            try {
                // midiDevice.open();
                midiDevice = new SynthWrapper(this, midiDevice);

                addMidiOutDevice(midiDevice);

            } catch (Exception e2) {
                e2.printStackTrace();
                midiDevice = null;
            }
        }

        // for (FrinikaTrackWrapper ftw : midiTracks) {
        // // if (origTracks.contains(ftw)) continue;
        // if (midiDevice != null)
        // ftw.setMidiDevice(midiDevice);
        // }

        // for (MidiLane lane : lanesToLoad) {
        // add(lane);
        // }

        getEditHistoryContainer().notifyEditHistoryListeners();
    }

    public DragList getDragList() {
        return dragList;
    }

    public int getPixelsPerRedraw() {
        return pixelsPerRedraw;
    }

    public void setPixelsPerRedraw(int i) {
        pixelsPerRedraw = i;
    }

    // TODO tempo stuff
    /**
     * translate microsecond time to ticks
     */
    public double tickAtMicros(double micros) {


        return tempoList.getTickAtTime(micros / 1000000.0);

    // TODO Broken (move part)
    //	return micros * (sequence.getResolution() * sequencer.getTempoInBPM())
    //			/ (60.0 * 1000000.0);
    }

    /**
     * translate ticks to microseconds
     */
    public double microsAtTick(double tick) {

        return 1000000.0 * tempoList.getTimeAtTick(tick);

    // TODO Broken (move part)
//		return (60.0 * 1000000.0 * tick)
//				/ (sequence.getResolution() * sequencer.getTempoInBPM());
    }

    /**
     * Getter for the tempoList
     * 
     * @return tempoList
     */
    public TempoList getTempoList() {
        if (tempoList == null) { // I don't think this ever happens
            System.out.println(" Creating a new tempo list ");
            tempoList = new TempoList(sequence.getResolution(), this);
            tempoList.add(0, 100.0);
            sequencer.setTempoList(tempoList);
        }
        return tempoList;
    }

    public TimeSignatureList getTimeSignatureList() {
        if (timeSignitureList == null) {    // if we have a legacy project timesig will be null.
            timeSignitureList = new TimeSignatureList();
            timeSignitureList.add(0, 4);
        }
        return timeSignitureList;
    }

    public int getTicksPerBeat() {
        return sequence.getResolution();
    }

    /**
     * 
     * @return a TimeUtils for this project
     */
    public TimeUtils getTimeUtils() {
        if (timeUtils == null) {
            timeUtils = new TimeUtils(this);
        }
        return timeUtils;
    }

    public double tickToSample(long tick) {
        double tt = tempoList.getTimeAtTick(tick);
        return tt * audioServer.getSampleRate();
    }

    public SoloManager getSoloManager() {
        return soloManager;
    }

    public ControlResolver getControlResolver() {
        return controlResolver;
    }

    private void attachTootNotifications() {
        Taps.setAudioServer(audioServer);
        sequencer.addTempoChangeListener(new TempoChangeListener() {

            public void notifyTempoChange(float bpm) {
                Tempo.setTempo(bpm);
            }
        });
    }
}