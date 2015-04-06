package com.frinika.midi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JPanel;

import com.frinika.sequencer.gui.pianoroll.VirtualPianoVert;

public class DrumMapperPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	VirtualPianoVert inPiano;

	VirtualPianoVert outPiano;


	OverLay overlay;

	DrumMapper dm;

	DrumMapperPanel(VirtualPianoVert inPiano, VirtualPianoVert outPiano,
			DrumMapper dm) {
		this.dm = dm;
		this.inPiano = inPiano;
		this.outPiano = outPiano;
		overlay = new OverLay();
		setLayout(null);// new OverlayLayout(this));

		add(inPiano);
		add(outPiano);
	
		add(overlay);
		inPiano.setLocation(0,0);
		inPiano.setSize(inPiano.getPreferredSize());
	//	setMap.setLocation(50,0);
	//	setMap.setSize(setMap.getPreferredSize());
		outPiano.setLocation(100,0);
		outPiano.setSize(outPiano.getPreferredSize());
		
		overlay.setLocation(0,0);
		overlay.setSize(new Dimension(200,1000)); //TODO work pout the real size
		setComponentZOrder(overlay,0);
//		setComponentZOrder(setMap,1);
		setComponentZOrder(inPiano,1);
		setComponentZOrder(outPiano,2);
		
			
	//	addComponentListener(this);
	}

	public Dimension getPreferredSize() {
		return new Dimension(inPiano.getPreferredSize());
		
	}

	
	class OverLay extends JPanel {
		OverLay() {
			setOpaque(false);

		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Point inO = inPiano.getLocation();
			Point outO = outPiano.getLocation();

			g.setColor(Color.GREEN);

			for (int i = 0; i < 128; i++) {
				DrumMapper.NoteMap nm = dm.getNoteMap(i);
				int j=nm.note;
				 if ( j== i) continue;
				VirtualPianoVert.Key inKey = inPiano.getKey(i);
				VirtualPianoVert.Key outKey = outPiano.getKey(j);
				int x1 = inO.x + inKey.x + inKey.width / 2;
				int y1 = inO.y + inKey.y + inKey.height / 2;
				int x2 = outO.x + outKey.x + outKey.width / 2;
				int y2 = outO.y + outKey.y + outKey.height / 2;

				g.drawLine(x1, y1, x2, y2);

			}

		}
	}

}
