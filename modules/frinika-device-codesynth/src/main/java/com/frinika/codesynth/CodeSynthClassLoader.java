/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.codesynth;

import java.net.URL;
import java.net.URLClassLoader;
import javax.sound.midi.Soundbank;

/**
 *
 * @author peter
 */
public class CodeSynthClassLoader extends URLClassLoader {
    
    public CodeSynthClassLoader(URL[] urls) {
	super(urls);
    }
      
    Class soundbankClass;
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {		    
	Class cls = super.loadClass(name, resolve); 
	
	if(Soundbank.class.isAssignableFrom(cls)) {
	    this.soundbankClass = cls;
	}
	return cls;
    }    

    public Class getSoundbankClass() {
	return soundbankClass;
    }        
}
