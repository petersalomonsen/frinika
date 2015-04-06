/*
 * Created on Feb 22, 2005
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
package com.frinika.synth.synths.analogika;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frinika.synth.InstrumentNameListener;
import com.frinika.synth.Synth;
import com.frinika.synth.synths.Analogika;

/**
 * GUI for the Analogika synth.
 * @author Peter Johan Salomonsen
 *
 */
public class AnalogikaGUI extends JFrame implements InstrumentNameListener  {
 
	private static final long serialVersionUID = 1L;
	
	Analogika analogika;
    
    public AnalogikaGUI(Analogika analogika)
    {
        this.analogika = analogika;
        
        initComponents();
        
        setTitle(analogika.getInstrumentName());
        analogika.addInstrumentNameListener(this);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosed(WindowEvent e) {
                AnalogikaGUI.this.analogika.removeInstrumentNameListener(AnalogikaGUI.this);
            }
        });

        setVisible(true);        
        setSize(getPreferredSize());
    }
    
    void initComponents()
    {        
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        
        JMenuBar menuBar = new JMenuBar();
        add(menuBar,gc);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
                
        final JMenuItem renameMenuItem = new JMenuItem("Rename");
        renameMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                analogika.setInstrumentName(
                        JOptionPane.showInputDialog("Enter new name",
                        analogika.getInstrumentName()));
            }});
        fileMenu.add(renameMenuItem);

        gc.fill = GridBagConstraints.NONE;
        gc.ipadx = 5;
        gc.ipady = 5;
        gc.anchor = GridBagConstraints.CENTER;

        JComboBox waveformCombo = new JComboBox();
        waveformCombo.addItem("saw");
        waveformCombo.addItem("sine");
        waveformCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                float[] waveform = new float[(int)(2 * Math.PI * 10000)];
                
                if(e.getStateChange()==ItemEvent.SELECTED)
                {
                    for(float n=0;n<waveform.length;n++)
                    {               
                        if(e.getItem().equals("sine"))
                            waveform[(int)n] = (float)Math.sin((n / (float)waveform.length) * Math.PI * 2.0);
                        else if(e.getItem().equals("saw"))
                            waveform[(int)n] = (float)(n / (float)waveform.length)-0.5f;           
                    }
                }
                analogika.getAnalogikaSettings().setWaveform(waveform);                
            }});
        add(waveformCombo,gc);
        
        final JComboBox layerComboBox = new JComboBox();
        for(int n=1;n<=8;n++)
        {
            layerComboBox.addItem(new Integer(n));
        }
        layerComboBox.setSelectedIndex(analogika.getAnalogikaSettings().getLayers()-1);
        layerComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                analogika.getAnalogikaSettings().setLayers(layerComboBox.getSelectedIndex()+1);
            }});
        add(new JLabel("Layers"),gc);
        add(layerComboBox,gc);

        final JSlider freqSpreadSlider = new JSlider(JSlider.HORIZONTAL,0,1000,
                (int)(analogika.getAnalogikaSettings().getFreqSpread() * 50000));
        
        freqSpreadSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setFreqSpread((freqSpreadSlider.getValue() / 50000f));
            }});
        add(new JLabel("Frequency spread"),gc);
        add(freqSpreadSlider,gc);

        final JSlider volAttackSlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getVolAttack());
        volAttackSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setVolAttack(volAttackSlider.getValue());
            }});
        add(new JLabel("Attack"),gc);
        add(volAttackSlider,gc);
        
        final JSlider volDecaySlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getVolDecay());
        volDecaySlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setVolDecay(volDecaySlider.getValue());
            }});
        add(new JLabel("Decay"),gc);
        add(volDecaySlider,gc);

        final JSlider volSustainSlider = new JSlider(JSlider.HORIZONTAL,0,400,
                analogika.getAnalogikaSettings().getVolSustain());
        volSustainSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setVolSustain(volSustainSlider.getValue());
            }});
        add(new JLabel("Sustain"),gc);
        add(volSustainSlider,gc);

        final JSlider volReleaseSlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getVolRelease());
        volReleaseSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setVolRelease(volReleaseSlider.getValue());
            }});
        add(new JLabel("Release"),gc);
        add(volReleaseSlider,gc);

        final JSlider loPassMaxSlider = new JSlider(JSlider.HORIZONTAL,0,1000,
                analogika.getAnalogikaSettings().getLoPassMax());
        loPassMaxSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setLoPassMax(loPassMaxSlider.getValue());
            }});
        add(new JLabel("LoPass Peak"),gc);
        add(loPassMaxSlider,gc);

        final JSlider loPassAttackSlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getLoPassAttack());
        loPassAttackSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setLoPassAttack(loPassAttackSlider.getValue());
            }});
        add(new JLabel("LoPass Attack"),gc);
        add(loPassAttackSlider,gc);
        
        final JSlider loPassDecaySlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getLoPassDecay());
        loPassDecaySlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setLoPassDecay(loPassDecaySlider.getValue());
            }});
        add(new JLabel("LoPass Decay"),gc);
        add(loPassDecaySlider,gc);

        final JSlider loPassSustainSlider = new JSlider(JSlider.HORIZONTAL,0,1000,
                analogika.getAnalogikaSettings().getLoPassSustain());
        loPassSustainSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setLoPassSustain(loPassSustainSlider.getValue());
            }});
        add(new JLabel("LoPass Sustain"),gc);
        add(loPassSustainSlider,gc);

        final JSlider loPassReleaseSlider = new JSlider(JSlider.HORIZONTAL,-10000,5000,
                analogika.getAnalogikaSettings().getLoPassRelease());
        loPassReleaseSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                analogika.getAnalogikaSettings().setLoPassRelease(loPassReleaseSlider.getValue());
            }});
        add(new JLabel("LoPass Release"),gc);
        add(loPassReleaseSlider,gc);
    }

    public void instrumentNameChange(Synth synth, String instrumentName) {
        setTitle(instrumentName);
    }
}
