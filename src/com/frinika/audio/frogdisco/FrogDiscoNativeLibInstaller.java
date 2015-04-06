/*
 * Copyright (c) 2004-2014 Peter Johan Salomonsen (http://www.petersalomonsen.com) - Licensed under GNU GPL
 */

package com.frinika.audio.frogdisco;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FrogDiscoNativeLibInstaller {
    static boolean loadedNativeLib = false;
    public static void loadNativeLibs() throws Exception {
        if(loadedNativeLib)
            return;

        try
        {
            
            String osname = System.getProperty("os.name").toLowerCase();
            String nativeLibName = null;
            if(osname.startsWith("mac")) {
                nativeLibName = "libFrogDisco.dylib";
            } else {
                return;
            }
            InputStream is = FrogDiscoNativeLibInstaller.class.getResourceAsStream(nativeLibName);
            File tmpDir = new File("/Library/Java/Extensions/");
            File tmpLibFile = new File(tmpDir,nativeLibName);
            System.out.println("Extracting native library to "+tmpLibFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(tmpLibFile);
            byte[] buf = new byte[1024];
            int len = is.read(buf);
            while(len>-1)
            {
                fos.write(buf,0,len);
                len = is.read(buf);
            }
            fos.close();
            System.load(tmpLibFile.getAbsolutePath());
            System.out.println("Loaded native library: "+tmpLibFile.getAbsolutePath());
            loadedNativeLib = true;
        } catch(Throwable t) {
            throw(new Exception(t));
        }
    }

    public static boolean isLoadedNativeLib() {
        return loadedNativeLib;
    }
}
