Building frinika.jar
====================

For building the frinika.jar file in order to run frinika - type: ant jar

Frinika is most easily built and set up for development in the Eclipse IDE (www.eclipse.org).
Just import it into the Eclipse workspace and you should be up and running.

An alternative build method that should work across IDE's (and without IDE) is the ant build script.
Check out the various targets of build.xml.

Note that the Ant builds also works in Eclipse. If you need alternative build setups - the Ant file
is the way to go. 

An example is the JavaDoc build, update javadocs by running the javadocs build of the Ant file.

How to use ant?

In eclipse - very simple:

From the menu: window -> show view -> ant
dragndrop the build.xml file into the ant pane
expand "frinika" in the ant view, and right click on release or source 
release, and select run as..

This produces either frinika.zip or frinika-src.zip.. Also works with java 1.5 
(even though you've set up eclipse for 1.6)


The only thing to keep in mind is when changing the lib versions - remember to 
update this line in build.xml:

<property name="classpath" value="lib/js.jar lib/flexdock-0.5.1.jar 
lib/looks-2.1.3.jar lib/toot-r2.jar lib/jmod_0_9k.jar 
lib/rasmusdsp.jar"/>

This is also responsible for creating the manifest of frinika.jar. Also remove 
the old version when upgrading to a new one...

Java 1.6 packages
=================

To use Java 1.6 and still compile with java 1.5 you use reflection. e.g.


		ClassLoader loader=ClassLoader.getSystemClassLoader();
		
		try {
			Class clazz =loader.loadClass("uk.co.simphoney.music.menu.SimphoneyMenu");
			Class params[]={ProjectContainer.class};
			Constructor con=clazz.getConstructor(params);
			Object args[]={project};
			JMenu x=(JMenu)con.newInstance(args);
	     } catch (Exception e) {
	        // do nothing here . Or a message that the user might want to use 1.6 
	        // to get a cool feature	
		 }	
			
			
The following packages require java 1.6 and can be removed from the build path if making a 1.5
build

uk.co.simphoney.music
