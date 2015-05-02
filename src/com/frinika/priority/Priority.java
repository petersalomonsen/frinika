package com.frinika.priority;

import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;

/**
 * A class to set java threads to real time priority.
 * 
 * @author pjl
 * 
 */
public class Priority {
	public static native void setPriorityRR(int prio);

	public static native void setPriorityFIFO(int prio);

	public static native void setPriorityOTHER(int prio);

	public static native void display();

	public static boolean priorityLibraryLoaded = false;
	 // ------------------------------------------------------------------------
    // --- static initializer                                               ---
    // ------------------------------------------------------------------------

    static {
    	String libJJackFileName=null;
    	try
    	{
            // try loading native library from system lib (library path)
            System.loadLibrary("priority");
            System.out.println("native priority library loaded using system library path");
            priorityLibraryLoaded = true;
    	} catch(Throwable e)
    	{
            try {
    		File file = new File("lib/"+System.getProperty("os.arch")+"/"+System.getProperty("os.name")+"/libpriority.so");
                libJJackFileName = file.getAbsolutePath();
                System.load(libJJackFileName);
       		System.out.println("loaded priority native library "+ libJJackFileName );
    		priorityLibraryLoaded = true;
            } catch (Throwable e2) {
                System.out.println("Problem loading priority library.");
                System.out.println("Tried system library path and " + libJJackFileName);
                e.printStackTrace();
                e2.printStackTrace();
            }
    	}
    }
//	static {
//		// load native library
//		// String strLibPath = System.getProperty("java.library.path");
//
//		/*c
//		 * Load frinika from system resource and write it to the system - If
//		 * we're loading from a JAR file this is the only way to load a library.
//		 * 
//		 * Modified by Peter Salomonsen 2005-05-05 PINCHED BY PJL
//		 */
//		String libFrinikaFileName = "";
//		try {
//			File file = new File("libpriority.so");
//			libFrinikaFileName = file.getAbsolutePath();
//			FileOutputStream fos = new FileOutputStream("libpriority.so");
//			String ldPath ="lib/"+System.getProperty("os.arch")+"/"+System.getProperty("os.name")+"/libpriority.so";
//			System.out.println("Loading Priority library for architecture: "
//					+ System.getProperty("os.arch"));
//			InputStream is = ClassLoader.getSystemResource(ldPath).openStream();
//			
//			while (is.available() > 0)
//				fos.write(is.read());
//			is.close();
//			fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		System.load(libFrinikaFileName);
//
//	}

	static public double sleepTest() throws Exception {
		double maxM = 0.0;
		int nanoSleep = 5000000;
		for (int i = 0; i < 500; i++) {
			long t1 = System.nanoTime();
			Thread.sleep(nanoSleep / 1000000, nanoSleep % 1000000);
			long t = System.nanoTime() - t1;
			double millis = ((t - nanoSleep) / 1e6);
			maxM = Math.max(maxM, Math.abs(millis));
		}
		return maxM;
	}

	public static void main(String[] args) throws Exception {
            System.out.println("Priority native lib loaded = "+Priority.priorityLibraryLoaded);
		display();
		System.out.println("error =" + sleepTest());
		setPriorityRR(80);
		display();
		System.out.println("error =" + sleepTest());
		setPriorityFIFO(80);
		display();
		System.out.println("error =" + sleepTest());
		setPriorityOTHER(80);
		display();
		System.out.println("error =" + sleepTest());

	}

}
