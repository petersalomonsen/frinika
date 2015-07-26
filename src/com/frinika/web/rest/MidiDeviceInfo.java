/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.web.rest;

import java.io.Serializable;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author peter
 */
public class MidiDeviceInfo {
    MidiDevice.Info[] infos;

    public MidiDeviceInfo() {
	infos = MidiSystem.getMidiDeviceInfo();
    }
    

    public MidiDevice.Info[] getInfos() {
	return infos;
    }

    public void setInfos(MidiDevice.Info[] infos) {
	this.infos = infos;
    }                
}