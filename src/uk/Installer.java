/* ZipSelfExtractor.java */
/* Author: Z.S. Jin
   Updates: John D. Mitchell */

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;

public class Installer extends JFrame
{
    private String myClassName;
    static String MANIFEST = "META-INF/MANIFEST.MF";

    Installer() {
    }

    private String getJarFileName() {
		myClassName = this.getClass().getName()+".class";
/*        System.out.println(this.getClass().getCanonicalName());
        System.out.println(this.getClass().getSimpleName());
        System.out.println(myClassName); */
//		URL urlJar = this.getClass().getClassLoader().getSystemResource(myClassName);
		URL urlJar = ClassLoader.getSystemResource(myClassName);
		String urlStr = urlJar.toString();
		int from = "jar:file:".length();
		int to = urlStr.indexOf("!/");
		return urlStr.substring(from, to);
    }

    public void extract(String zipfile)
    {
		File currentArchive = new File(zipfile);

		JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File("."));
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Select installation directory for " + currentArchive.getName());
        fc.setMultiSelectionEnabled(false);
       
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if ( fc.showDialog(Installer.this, "Select") != JFileChooser.APPROVE_OPTION) {
            return;  //only when user select valid dir, it can return approve_option
        }

        File outputDir = fc.getSelectedFile();

        byte[] buf = new byte[1024];
        SimpleDateFormat formatter = new SimpleDateFormat ("MM/dd/yyyy hh:mma",Locale.getDefault());
        
        ProgressMonitor pm = null;
        
        boolean overwrite = false;

		ZipFile zf = null;
		FileOutputStream out = null;
		InputStream in = null;

        try
        {
	        zf = new ZipFile(currentArchive);

			int size = zf.size();
			int extracted = 0;
			pm = new ProgressMonitor(getParent(), "Extracting files...", "starting", 0, size-4);
			pm.setMillisToDecideToPopup(0);
			pm.setMillisToPopup(0);

			Enumeration entries = zf.entries();

			for (int i=0; i<size; i++) {
			    ZipEntry entry = (ZipEntry) entries.nextElement();
			    if(entry.isDirectory())
					continue;

			    String pathname = entry.getName();
			    if(myClassName.equals(pathname) || MANIFEST.equals(pathname.toUpperCase()))
					continue;

			    extracted ++;
                pm.setProgress(i);
                pm.setNote(pathname);
			    if(pm.isCanceled())
					return;

                in = zf.getInputStream(entry);

                File outFile = new File(outputDir, pathname);
		    	Date archiveTime = new Date(entry.getTime());

                if(overwrite==false)
                    {
                        if(outFile.exists())
                        {
                            Object[] options = {"Yes", "Yes To All", "No"};
                            Date existTime = new Date(outFile.lastModified());
                            Long archiveLen = new Long(entry.getSize());
                            
                            String msg = outFile.getName() +
				  " already exists."
				+ "\nExisting file: "
				+ formatter.format(existTime) + ",  "
				+ outFile.length() + " bytes"
                                + "\nFile to install:"
				+ formatter.format(archiveTime) + ",  " 
				+ archiveLen + " bytes"
				+"\n\nOverwrite this file?";

                            int result = JOptionPane.showOptionDialog(Installer.this,
				msg, "Warning", JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options,options[0]); 
                            
                            if(result == 2) // No
                            {
                                continue;
                            }
                            else if( result == 1) //YesToAll
                            {
                                overwrite = true;
                            }
                        }
                    }

                    File parent = new File(outFile.getParent());
                    if (parent != null && !parent.exists())
                    {
                        parent.mkdirs();
                    }

                    out = new FileOutputStream(outFile);                

                    while (true) 
                    {
                        int nRead = in.read(buf, 0, buf.length);
                        if (nRead <= 0)
                            break;
                        out.write(buf, 0, nRead);
                    }
                    
                    out.close();
		    		outFile.setLastModified(archiveTime.getTime());
                }
             
                pm.close();
                zf.close();
                getToolkit().beep();
                
                JOptionPane.showMessageDialog
		    (Installer.this,
		     "Installed " + extracted + " file" + ((extracted > 1) ? "s": "") +
                 " to " + outputDir.getPath(),
		     "Installer",
		     JOptionPane.INFORMATION_MESSAGE);

            }
            catch (Exception e)
            {
                System.out.println(e);
				if(zf!=null) { try { zf.close(); } catch(IOException ioe) {;} }
				if(out!=null) { try {out.close();} catch(IOException ioe) {;} }
				if(in!=null) { try { in.close(); } catch(IOException ioe) {;} }
	    	}
    }

    public static void main(String[] args) {
		try {
	     		String syslaf = UIManager.getSystemLookAndFeelClassName() ;
      		UIManager.setLookAndFeel(syslaf);
	    	} catch(Exception e) {
			System.err.println(e);
    		}
		Installer i = new Installer();
		String jarFileName = i.getJarFileName();
		i.extract(jarFileName);
		System.exit(0);
    }

}
