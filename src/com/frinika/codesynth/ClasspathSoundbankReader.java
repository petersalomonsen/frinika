/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */
package com.frinika.codesynth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.spi.SoundbankReader;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class ClasspathSoundbankReader extends SoundbankReader {

    @Override
    public Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
        try {
            System.out.println("Loading soundbank");
            URLClassLoader ucl = new URLClassLoader(new URL[]{url});
            BufferedReader br = new BufferedReader(new InputStreamReader(ucl.findResource("codesynthsoundbank.properties").openStream()));
            String soundbankClassName = br.readLine();
            return (Soundbank) ucl.loadClass(soundbankClassName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(ClasspathSoundbankReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new InvalidMidiDataException(ex.getMessage());
        }
    }

    @Override
    public Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException {
        return getSoundbank(file.toURI().toURL());
    }

    
}
