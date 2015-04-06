/*
 * Created on Mar 20, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.audio.analysis.gui;

import com.frinika.audio.DynamicMixer;
import com.frinika.audio.io.AudioReaderFactory;
import com.frinika.audio.analysis.gui.*;
import com.frinika.audio.io.LimitedAudioReader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JPanel;

import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.SpectrumDataBuilder;
import com.frinika.audio.analysis.StaticSpectrogramSynth;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.server.IOAudioProcess;

import com.frinika.project.FrinikaAudioSystem;

@SuppressWarnings("serial")
public class AudioAnalysisTimePanel extends JPanel implements Observer {

	Vector<CursorObserver> cursorObservers = new Vector<CursorObserver>();


    AudioReaderFactory part;

	LimitedAudioReader input;

	WaveImage wavePanel;

	SpectrogramImage spectroImage;

	//SpectrumDataBuilder spectroData;

	boolean isPlaying;

	private Dimension size = new Dimension(400, 100);

	private int chunkCursor;

	private long framePtr = 0;

	AudioProcess myProcess;

	DynamicMixer mixer;

	private boolean staticSynthMode = false;

	StaticSpectrogramSynth synthPlayer;

	private KeyboardFocusManager kbd;

	static int count=0;
	String tag;

	private KeyEventDispatcher keyDispatcher;
	float  curBin;
	
	public AudioAnalysisTimePanel(AudioReaderFactory part, DynamicMixer mixer,Mapper mapper,
			SpectrumDataBuilder spectroData,KeyboardFocusManager kbd) {
	//	synthPlayer = spectroData.getSynth(); //new StaticSpectrogramSynth(spectroData);
		if (synthPlayer != null) addCursorObserver(synthPlayer); 
		this.part = part;
		this.kbd=kbd;
	//	this.client = new MyClient();
		this.mixer=mixer;
		myProcess=new MyAudioProcess();
		
		mixer.addMixerInput(myProcess, tag="XYZ"+count++);
	//	FrinikaAudioSystem.stealAudioServer(this, this.client);

		try {

			this.spectroImage = new SpectrogramImage(spectroData, mapper);
			this.wavePanel = new WaveImage(part.createAudioReader());

			spectroData.addSizeObserver(this.wavePanel);
			spectroData.addSizeObserver(this.spectroImage);
			// spectroData.addObserver(spectroImage);

			this.spectroImage.addObserver(this);
			this.wavePanel.addObserver(this);
			addMouseListener(new MyMouseListener());
			
			
			addMouseMotionListener(new  MouseMotionListener() {
				
				public void mouseMoved(MouseEvent e) {
					if (isPlaying) return;
					
					int curY=e.getY();
					curBin=spectroImage.pixToBin(curY);
					
					chunkCursor=e.getX();
					notifyCursorObservers();
					repaint();
				//	System.out.println(" MOUSE MOVED");
				}
				
				public void mouseDragged(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		setFocusable(true);
		overrideKeys();
	
	}

	StaticSpectrogramSynth getSynth() {
		return synthPlayer;
	}
	
	// public Vector<Tweakable> getTweaks() {
	// return spectroImage.getTweaks();
	// }
	//	
	public void dispose() {

		// TODO remove other types of observers
		this.wavePanel.deleteObservers();
		this.spectroImage.deleteObservers();
		
		mixer.removeStrip(tag);
		
		kbd.removeKeyEventDispatcher(keyDispatcher);
		keyDispatcher=null;
		  
	//	FrinikaAudioSystem.returnAudioServer(this);
	
	}


	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);
	//	System.out.println(" AnalysisPanel PAINT");
		Graphics2D g = (Graphics2D) gg;
		this.spectroImage.drawImage(g, 0, 0);
		this.wavePanel.drawImage(g, 0, this.spectroImage.getHeight());
		g.setColor(Color.RED);
		g.drawLine(this.chunkCursor, 0, this.chunkCursor, this.size.height);
	}

	public void update(Observable o, Object arg) {
	
		this.size = new Dimension(this.wavePanel.getWidth(), this.spectroImage.getHeight()
				+ this.wavePanel.getHeight());
		revalidate();
	//	getParent().validate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return this.size;
	}

	@Override
	public Dimension getMinimumSize() {
		return this.size;
	}

	@Override
	public Dimension getMaximumSize() {
		return this.size;
	}

	public void addCursorObserver(CursorObserver o) {
		this.cursorObservers.add(o);
	}

	private void notifyCursorObservers() {
		for (CursorObserver o : this.cursorObservers) {
			o.notifyCursorChange(this.chunkCursor,this.curBin);
		}
	}



	public void startStop() {
		//System.out.println(" START-STOP");
		this.isPlaying = !this.isPlaying;
	}

	public void nudge(int pixs) {
		this.chunkCursor += pixs;
		this.framePtr = this.wavePanel.screenToFrame(this.chunkCursor);
		try {
			this.input.seekFrameInEnvelope(this.framePtr,false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		notifyCursorObservers();
		repaint();
	}

	public int cursorChunkPos() {

		return this.chunkCursor;
	}

	void overrideKeys() {
	
		
		
		kbd.addKeyEventDispatcher(keyDispatcher=new KeyEventDispatcher() {

			public boolean dispatchKeyEvent(KeyEvent e) {

		//		System.out.println(" KEY HIT " + e);

				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					if ((e.getID() == KeyEvent.KEY_PRESSED)) {
						startStop();
					}
					return true;

				case KeyEvent.VK_LEFT:
					if ((e.getID() == KeyEvent.KEY_PRESSED)) {
						nudge(-1);
					}
					return true;

				case KeyEvent.VK_RIGHT:
					if ((e.getID() == KeyEvent.KEY_PRESSED)) {
						nudge(1);
					}
					return true;

					//
					// case KeyEvent.VK_HOME:
					// if ((e.getID() == KeyEvent.KEY_PRESSED)) {
					// rewind.actionPerformed(null);
					// }
					// return true;
					//
					// case KeyEvent.VK_MULTIPLY:
					// if ((e.getID() == KeyEvent.KEY_PRESSED)) {
					// record.actionPerformed(null);
					// }
					// return true;
					//
					// case KeyEvent.VK_NUMPAD1:
					// if ((e.getID() == KeyEvent.KEY_PRESSED)) {
					// warpToLeft.actionPerformed(null);
					// }
					// return true;
					//
					// case KeyEvent.VK_NUMPAD2:
					// if ((e.getID() == KeyEvent.KEY_PRESSED)) {
					// warpToRight.actionPerformed(null);
					// }
					// return true;
					//
					// case KeyEvent.VK_A:
					//
					// if ((e.getID() == KeyEvent.KEY_PRESSED)) {
					//
					// if (e.isControlDown()) {
					// return selectAllAction.selectAll(e);
					// }
					// }

				default:
					return false;
				}
			}
		});

	}

	class MyClient implements AudioClient {

		IOAudioProcess output;

		AudioBuffer buffer;

		private boolean enabled;

		MyClient() {
			this.buffer = FrinikaAudioSystem.getAudioServer().createAudioBuffer(
					"TiemAnalysis");
			this.output = FrinikaAudioSystem.getDefaultOutput(null);
			try {
				input = part.createAudioReader(); // TODO CachedAudio stuff
				input.seekFrameInEnvelope(framePtr,false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void work(int size) {

			buffer.makeSilence();
			if (staticSynthMode) {
				synthPlayer.processAudio(this.buffer);
			} else if (isPlaying) {
				input.processAudio(this.buffer);
				framePtr += buffer.getSampleCount();
				updateCursorFromFramePos();
			} else {
				buffer.makeSilence();
			}

		output.processAudio(buffer);

		}

		private void updateCursorFromFramePos() {
			int newCursor = wavePanel.frameToScreen(framePtr);
			if (newCursor != chunkCursor) {
				chunkCursor = newCursor;
				notifyCursorObservers();
				repaint();
			}
		}

		public void setEnabled(boolean b) {
			this.enabled = b;
		}

	}

	
	class MyAudioProcess implements AudioProcess {


		private boolean enabled;

		MyAudioProcess() {
//			this.buffer = FrinikaAudioSystem.getAudioServer().createAudioBuffer(
//					"TiemAnalysis");
//			this.output = FrinikaAudioSystem.getDefaultOutput(null);
			try {
				input = part.createAudioReader(); // TODO CachedAudio stuff
				input.seekFrameInEnvelope(framePtr,false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public int processAudio(AudioBuffer buffer) {

			buffer.makeSilence();
			if (staticSynthMode) {
				synthPlayer.processAudio(buffer);
			} else if (isPlaying) {
				input.processAudio(buffer);
				framePtr += buffer.getSampleCount();
				updateCursorFromFramePos();
			} else {
				buffer.makeSilence();
			}
			
			return AUDIO_OK;
		}

		private void updateCursorFromFramePos() {
			int newCursor = wavePanel.frameToScreen(framePtr);
			if (newCursor != chunkCursor) {
				chunkCursor = newCursor;
				notifyCursorObservers();
				repaint();
			}
		}

		public void setEnabled(boolean b) {
			this.enabled = b;
		}

		public void close() throws Exception {
			// TODO Auto-generated method stub
			
		}

		public void open() throws Exception {
			// TODO Auto-generated method stub
			
		}

	}
	class MyMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
//			if (chunkCursor == e.getX()) {
//				return;
//			}
			chunkCursor = e.getX();
			framePtr = wavePanel.screenToFrame(chunkCursor);
			try {
				input.seekFrameInEnvelope(framePtr,false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			notifyCursorObservers();
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			requestFocusInWindow();
		}

	}

	public void setSynthMode(boolean b) {
		this.staticSynthMode=b;
	}
}
