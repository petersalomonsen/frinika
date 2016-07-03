/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://www.petersalomonsen.com) - Licensed under GNU GPL
 */

package com.frinika.jvstsynth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class NativeLibLoader {
    static boolean loadedNativeLib = false;
    public static void loadNativeLibs() throws Exception {
        if(loadedNativeLib)
            return;

        try
        {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            String osname = System.getProperty("os.name").toLowerCase();
            String nativeLibName = null;
            if(osname.startsWith("win") && "x86".equals(System.getProperty("os.arch")))
            {
                nativeLibName = "jvsthost2.dll";
            } else if(osname.startsWith("linux")) {
                nativeLibName = "libjvsthost2.so";
            } else if(osname.startsWith("mac")) {
                nativeLibName = "libjvsthost2.jnilib";
            } else {
                System.out.println("Found no JVSTHost binary for "+osname+" / "+System.getProperty("os.arch"));
                return;
            }
            InputStream is = NativeLibLoader.class.getResourceAsStream(nativeLibName);
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
