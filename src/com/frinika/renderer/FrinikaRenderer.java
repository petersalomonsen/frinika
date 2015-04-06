/*
 * Created on 5.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika.renderer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.sound.midi.MidiDevice;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.SequencerListener;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPlayOptions;

public class FrinikaRenderer implements SequencerListener {

	ProjectContainer project;
	FrinikaSequencer seqr;
	
	ArrayList<FrinikaDeviceRenderer> deviceRenderers = new ArrayList<FrinikaDeviceRenderer>();
	
	public FrinikaDeviceRenderer getDeviceRenderer(MidiDevice mididevice)
	{
		for(FrinikaDeviceRenderer render : deviceRenderers)
			if(render.getDevice() == mididevice) return render;
		
		FrinikaDeviceRenderer render = new FrinikaDeviceRenderer(this, mididevice);
		deviceRenderers.add(render);
		return render;
	}
	
	public FrinikaRenderer(ProjectContainer project)
	{	
		this.project = project;
		seqr = project.getSequencer();
		seqr.addSequencerListener(this);
	}

	boolean started = false;
	
	public MidiLane findLane(FrinikaTrackWrapper track)
	{
		for(Lane lane : project.getLanes())
		{					
			if(lane instanceof MidiLane)
			{
				MidiLane midilane = (MidiLane)lane;
				if(midilane.getTrack() == track)
				{
					return midilane;
				}
			}
		}		
		return null;
	}
	
	public boolean anyLaneWithPreRendering()
	{
		for(Lane lane : project.getLanes())
		{					
			if(lane instanceof MidiLane)
			{
				MidiLane midilane = (MidiLane)lane;				
				if(midilane.getPlayOptions() != null)
				{
					if(midilane.getPlayOptions().preRendered) return true;
				}
			}
		}		
		return false;
		
	}
	
	public void beforeStart() {
		
		if(started) return;
		started = true;
		
		FrinikaSequence seq = project.getSequence();
		
		for(FrinikaTrackWrapper track : seq.getFrinikaTrackWrappers())
		{
			MidiPlayOptions opt = seqr.getPlayOptions(track);
			if(opt != null)
			{
				if(rendermode > 0)
					opt.preRenderedUsed = true;
				else
					opt.preRenderedUsed = false;
			}
		}
		
		
		Collection<FrinikaTrackWrapper> tracks;
        if(seqr.getSoloFrinikaTrackWrappers().size()>0)
        	tracks = seqr.getSoloFrinikaTrackWrappers(); 
        else
        	tracks = seq.getFrinikaTrackWrappers();
        
        HashSet<MidiDevice> supress_candidates = new HashSet<MidiDevice>();
		
		for(FrinikaTrackWrapper track : tracks)
		if(track.getMidiDevice() != null)
		{
			MidiPlayOptions opt = seqr.getPlayOptions(track);
			if(opt != null)
			if(!opt.muted)
			{
				if(opt.preRendered)
				{
					MidiDevice dev = track.getMidiDevice();
					if(dev instanceof SynthWrapper)
					{
						SynthWrapper synth = (SynthWrapper)dev;
						if(synth.isRenderable())
						{	
							if(suppress_realtime)
								supress_candidates.add(synth);
							opt.preRenderedUsed = true;
							getDeviceRenderer(synth).addTrack(track);
						}
					}
				}
			}
		}
		
		for(FrinikaTrackWrapper track : tracks)
		{
			MidiDevice dev = track.getMidiDevice();
			if(supress_candidates.contains(dev));
			{
				MidiPlayOptions opt = seqr.getPlayOptions(track);
				if(opt != null)
				if(!opt.preRendered)
				{
					supress_candidates.remove(dev);
					continue;
				}

				MidiLane lane = findLane(track);
				if(lane != null)
				{
					if(lane.isRecording())
					{
						supress_candidates.remove(dev);
						continue;
					}
				}
			}
		}
		
		for(MidiDevice synth : supress_candidates)
		{
			if(synth instanceof SynthWrapper)
				((SynthWrapper)synth).setSupressAudio(true);
		}
		
		for(FrinikaDeviceRenderer render : deviceRenderers)
			render.beforeStart();
						
	}

	public void start() {		
		for(FrinikaDeviceRenderer render : deviceRenderers)
			render.start();
	}

	public void stop() {		
		if(!started) return;
		started = false;

		for(FrinikaDeviceRenderer render : deviceRenderers)
		{
			render.stop();
			if(render.dev instanceof SynthWrapper)
			{
				((SynthWrapper)render.dev).setSupressAudio(false);
			}
		}
		
		deviceRenderers.clear();		
	}
	
	public void close()
	{
		stop();
		seqr.removeSequencerListener(this);
		
		
	}
	
	public void purgeRenderCache()
	{
		File projectfile = project.getFile();		
		File parentdir = projectfile.getParentFile();
		
		String name = projectfile.getName();
		if(name.toLowerCase().endsWith(".frinika")) name = name.substring(0, name.length() - 8);
		name += ".";
		String suffix = ".rendercache";
		File[] filelist = parentdir.listFiles();
		if(filelist != null)
		{
			for(File f : filelist)
			{
				if(f.getName().startsWith(name))
				if(f.getName().endsWith(suffix))
				{
					f.delete();
				}
			}
		}
	}
	
	int rendermode = 0;
	
	public void render(JFrame frame, long tickfrom, long tickto)
	{		
		if(!anyLaneWithPreRendering())
		{
			JOptionPane.showMessageDialog(frame, "No lane with prerender flag was found.", "Render Selected Timeline", JOptionPane.WARNING_MESSAGE);
			return;
		}
		rendermode = 1;
		new RenderDialog(frame, project, tickfrom, tickto);
		project.getSequencer().stop();
		rendermode = 0;
	}
	
	public void rerender(JFrame frame, long tickfrom, long tickto)
	{		
		if(!anyLaneWithPreRendering())
		{
			JOptionPane.showMessageDialog(frame, "No lane with prerender flag was found.", "Rerender Selected Timeline", JOptionPane.WARNING_MESSAGE);
			return;
		}		
		rendermode = 2;
		new RenderDialog(frame, project, tickfrom, tickto);
		project.getSequencer().stop();
		rendermode = 0;
	}
	
	boolean suppress_realtime = true;
	public void setSupressRealTime(boolean suppress_realtime)
	{
		
	}
	
}
