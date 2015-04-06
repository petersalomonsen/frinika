/*
 * Created on Jun 19, 2011
 *
 * Copyright (c) 2004-2011 Peter Johan Salomonsen
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
 * 
 */
package com.frinika.soundbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Since the URLClassLoader might cache loaded JAR file, this class will create
 * a temporary unique file for the jar
 * @author Peter Johan Salomonsen
 */
public class JARSoundbankLoader {

    public static File getTempSoundbankFile(File JARSoundBankFile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File outputFile = null;
        try {
            fis = new FileInputStream(JARSoundBankFile);
            outputFile = new File(System.getProperty("java.io.tmpdir")+"/"+JARSoundBankFile.getName().toLowerCase().replace(".jar", System.currentTimeMillis()+".jar"));
            fos = new FileOutputStream(outputFile);
            int b = fis.read();
            while(b!=-1)
            {
                fos.write(b);
                b = fis.read();
            }
            return outputFile;
        } catch (IOException ex) {
            Logger.getLogger(JARSoundbankLoader.class.getName()).log(Level.SEVERE, null, ex);
            return JARSoundBankFile;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(JARSoundbankLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fos.close();
                outputFile.deleteOnExit();
            } catch (IOException ex) {
                Logger.getLogger(JARSoundbankLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
}
