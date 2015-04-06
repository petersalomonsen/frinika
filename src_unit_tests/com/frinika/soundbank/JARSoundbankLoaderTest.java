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

import com.sun.media.sound.JARSoundbankReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.sound.midi.Soundbank;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class JARSoundbankLoaderTest {
    
    public JARSoundbankLoaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

        int soundbankRevision = 0;
    
    private void createSoundbank(String testSoundbankFileName) throws Exception {
        System.out.println("Create soundbank");
                
        File packageDir = new File("testsoundbank");
        if(packageDir.exists())
        {
            for(File file : packageDir.listFiles())
                assertTrue(file.delete());
            assertTrue(packageDir.delete());
        }
        packageDir.mkdir();        
        
        String sourceFileName = "testsoundbank/TestSoundBank.java";
        File sourceFile   = new File(sourceFileName);
        FileWriter writer = new FileWriter(sourceFile);

        writer.write(
                "package testsoundbank;\n"+
            "public class TestSoundBank extends com.sun.media.sound.ModelAbstractOscillator { \n" +
                "    @Override public int read(float[][] buffers, int offset, int len) throws java.io.IOException { \n" +
                "   return 0;\n" +
                " }\n"+
                "    @Override public String getVersion() {\n"+
                "   return \""+(soundbankRevision++)+"\";\n"+
                "    }\n"+
                "}\n"
        );
        writer.close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null, null, null);

        fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                            Arrays.asList(new File(".")));
        
        compiler.getTask(null,
               fileManager,
               null,
               null,
               null,
               fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile)))
            .call();
        
        
        
        ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(testSoundbankFileName));
        ZipEntry ze = new ZipEntry("META-INF/services/javax.sound.midi.Soundbank");
        zos.putNextEntry(ze);
        zos.write("testsoundbank.TestSoundBank".getBytes());  
        ze = new ZipEntry("testsoundbank/TestSoundBank.class");
        zos.putNextEntry(ze);
        FileInputStream fis = new FileInputStream("testsoundbank/TestSoundBank.class");
        int b = fis.read();
        while(b!=-1) {
            zos.write(b);
            b = fis.read();
        }
        zos.close();        
    }
        

    /**
     * Test of getTempSoundbankFile method, of class JARSoundbankLoader.
     */
    @Test
    public void testGetTempSoundbankFile() throws Exception {
        System.out.println("getTempSoundbankFile");
        String testSoundbankFilename = "testsoundbank.jar";
        
        createSoundbank(testSoundbankFilename);
                
        File JARSoundBankFile = new File(testSoundbankFilename);        
        File result = JARSoundbankLoader.getTempSoundbankFile(JARSoundBankFile);
        assertTrue(result.getAbsolutePath().startsWith(System.getProperty("java.io.tmpdir")+"/"+testSoundbankFilename.replace(".jar", "")));
        
    }
    
    @Test
    public void testJARSoundbankReload() throws Exception {
        System.out.println("Test reloading of JAR soundbank using Gervill");
        String testSoundbankFilename = "testsoundbank.jar";
        
        int expectedVersion = soundbankRevision;
        for(int n=0;n<10;n++) {
            System.out.println("Reload attempt "+n+" soundbankrevision is: "+soundbankRevision+", Expected version is: "+expectedVersion);
            
            createSoundbank(testSoundbankFilename);
            Soundbank sb = new JARSoundbankReader().getSoundbank(JARSoundbankLoader.getTempSoundbankFile(new File(testSoundbankFilename)));
            assertNotNull(sb);
            assertEquals((expectedVersion++)+"",sb.getVersion());            
        }
        
        
    }   

}
