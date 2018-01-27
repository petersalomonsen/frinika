Frinika
=======

Note: This is fork of Frinika under development, see
https://sourceforge.net/p/frinika/discussion/447356/thread/bf64b5f5/


Frinika is a free music workstation software for Linux, Windows, Mac OSX and other operating systems running Java. 
It features sequencer, soft-synths, realtime effects, MIDI and audio recording. 

See http://frinika.com for more 

Screenshot
----------

![Frinika Screenshot](images/screenshot.png?raw=true)

Features
--------

<ul>
<li>Sequencer</li>
<li>Piano roll</li>
<li>Amiga-style tracker</li>
<li>Notation</li>
<li>Audio recording</li>
<li>Soft synths</li>
<li>Mixer</li>
<li>Effects</li>
<li>Mastering</li>
<li>VST/VSTi support</li>
</ul>

License
-------

GNU General Public License version 2.0 or later (GPLv2+)

Structure
---------

 * doc - Documentation + related presentations
 * gradle - Gradle wrapper
 * src - Sources related to building distribution packages
 * modules - Application parts and library modules
 * plugins - Optional plugins
 * tools - Distributable applictions or runnable subprojects and utilities
 * lib - External libraries
 * resources - Related resource files, like sample files, images, etc.

Compiling
---------

Java Development Kit (JDK) version 8 or later is required to build this project.

For project compiling Gradle 4.0 build system is used. You can either download and install gradle and run "gradle distZip" command in project folder or gradlew or gradlew.bat scripts to download separate copy of gradle to perform the project build.

Build system website: http://gradle.org

Development
-----------

The Gradle build system provides support for various IDEs. See gradle website for more information.

 * Eclipse 3.7 or later

   Install Gradle integration plugin: http://marketplace.eclipse.org/content/gradle-integration-eclipse-0

 * NetBeans 8.0 or later

   Install Gradle support plugin: http://plugins.netbeans.org/plugin/44510/gradle-support
