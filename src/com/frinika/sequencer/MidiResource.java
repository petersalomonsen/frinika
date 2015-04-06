package com.frinika.sequencer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Synthesizer;

import com.frinika.midi.DrumMapper;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.ControllerListProvider;
import com.frinika.sequencer.model.GMControllerList;
import com.frinika.sequencer.patchname.PatchNameMap;
import com.frinika.sequencer.ChannelListProvider;

/**
 * 
 * Provides midi information such a voice lists and channel name. This stuff may
 * find a better home someday.
 * 
 * @author Paul
 * 
 */
public class MidiResource {

	static Integer[] chanList = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16 };

	FrinikaSequencer sequencer;

	HashMap<String, PatchNameMap> voiceTreeMap;

	String patchFiles[] = { "SW1000", "default" };

	static ControllerListProvider defaultControllerList = new GMControllerList();

	public MidiResource(FrinikaSequencer sequencer) {
		this.sequencer = sequencer;
		voiceTreeMap = new HashMap<String, PatchNameMap>();

		for (String name : patchFiles) {
			PatchNameMap sw = loadSerial(name); // new
			if (sw != null)
				voiceTreeMap.put(name, sw);

		}
	}

	private PatchNameMap loadSerial(String name) {
		
		PatchNameMap pn=null;
		try {

			InputStream is = ClassLoader.getSystemResource(
					"patchnames/" + name + ".pat").openStream();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while (is.available() > 0) {
				bos.write(is.read());
			}

			ByteArrayInputStream str = new ByteArrayInputStream(bos
					.toByteArray());
			
		
			
			try {
			
				ObjectInputStream ois = new ObjectInputStream(str);
				pn = (PatchNameMap) ois.readObject();
				ois.close();
				str.close();
				return pn;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		return null;
	}
	
	
	private PatchNameMap loadTxt(String name) {
		try {

			InputStream is = ClassLoader.getSystemResource(
					"patchnames/" + name + ".txt").openStream();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while (is.available() > 0) {
				bos.write(is.read());
			}

			ByteArrayInputStream str = new ByteArrayInputStream(bos
					.toByteArray());
			PatchNameMap sw = new PatchNameMap(str); // new
			return sw;
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}
//
//	/**
//	 * 
//	 * @return list of MidiOut devices registered with the sequencer
//	 */
//	public Object[] getMidiOutList() {
//		return sequencer.listMidiOutDevices().toArray();
//	}

	/**
	 * Return the channels provided by a given device. If device does not
	 * provide a list . . . return 0,1 2, ... 15
	 * 
	 * @param dev
	 * @return
	 */
	public Object[] getOutChannelList(MidiDevice dev) {
		if (dev instanceof ChannelListProvider) {
			return ((ChannelListProvider) dev).getList();
		}
		return chanList;
	}

	/**
	 * Return a list of voices for a device. if device does not provide a list
	 * return null.
	 * 
	 * @param dev
	 * @param channel
	 * @return
	 */
	public PatchNameMap getVoiceList(MidiDevice dev, int channel) {
		if (! (dev instanceof SynthWrapper) ) return null;
		assert (dev instanceof SynthWrapper);
		if (channel < 0 ) return null;  // Make channel -1 someone elses problem since we will get an array exception
		// use first part of the device string.
		// TODO think ??? Still thinking PJL ?? :) /PJS
		// Still think this is all a bit ugly PJL
		
		if(((SynthWrapper)dev).getRealDevice() instanceof Synthesizer)	{
			return new PatchNameMap((Synthesizer)dev, channel);
		} else if (((SynthWrapper)dev).getRealDevice() instanceof DrumMapper ){
			return null;
		} else	{
			String lookup = dev.getDeviceInfo().toString().split("\\s")[0];
	
			PatchNameMap vl = voiceTreeMap.get(lookup);
			if (vl == null) {
				System.out.println(" Could not find voice map for \"" + lookup
						+ "\"");
				return voiceTreeMap.get("default");
			}
			return vl;
		}
	}

	public static ControllerListProvider getDefaultControllerList() {
		return defaultControllerList;
	}

	public PatchNameMap getVoiceList(File patchMapName) {
		
		try {
			return new PatchNameMap(new FileInputStream(patchMapName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}

}
