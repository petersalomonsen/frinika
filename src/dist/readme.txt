Frinika
=======

Frinika is a free music workstation software for Linux, Windows, Mac OSX and other operating systems running Java.

It features sequencer, soft-synths, realtime effects, MIDI and audio recording.

See http://frinika.com for more.

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

GNU General Public License version 2.0 (GPLv2)


Quick instructions for install & run (any operating system):
-------------------------------------------------------------

Remember that this is free software with absolutely NO WARRANTY and no responsibility
for damaging your system or speakers or hearing or anything else. 

Use at own risk and keep volume control down before launching or starting to 
play anything. Otherwise you may experience loud sound or noise that may 
damage your speakers or even your hearing.

Before proceeding you should read and accept the GNU General Public License which is 
in the license.txt file and you can also read more here: 

https://www.gnu.org/licenses/gpl-2.0.html

Make sure that you have Java 8 or greater installed (http://www.java.com)

If you've downloaded this as a zip file, remember to extract all the files.

From the extracted folder either launch frinika.jar from your desktop, or enter
a command prompt terminal and type:

java -jar frinika.jar

If you want lowest latency possible you can try the following:

Windows users:
java -DuseASIOAudioServer=true -jar frinika.jar 

Linux users:
Simply start Jack before launching Frinika

Mac OSX (warning: Sometimes using this option causes a terrible noise,
so turn the volume down before launching. If it happens, try restarting.)

java -DuseOSXAudioServer=true -jar frinika.jar
