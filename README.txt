Quick instructions for install & run (any operating system):
-------------------------------------------------------------

Make sure that you have Java 1.5 or greater installed (http://www.java.com)

Windows users: just launch frinika.exe

alternatively (for all operating systems (also windows))

Doubleclick frinika.jar (if your system support it), or run Frinika from command line by typing: java -jar frinika.jar 

Remember to check out example projects to see/hear how things can be done with Frinika:

http://sourceforge.net/project/showfiles.php?group_id=131823&package_id=148904

(both 0.2.0 example projects and the earlier example projects will work.)

If you want to run Frinika with support for more memory than 64MB, try this:

java -Xmx256m -jar frinika.jar

(Or replace 256 with the amount of megabytes that suits you).

If you get problems with glitches then try turning on incremental garbage collection.

java -Xmx256m -Xincgc -jar frinika.jar 


I have found with my dual processor machine this helps. 

java -Xmx512m -XX:+UseParNewGC frinika.jar

(please report if you find this helps).

For jack using java 1.6 you need to add -Xss20m to increase the stack size for the native thread.  


SuSE linux users - get packages here:
http://sourceforge.net/project/showfiles.php?group_id=131823&package_id=181208

MAC users: If you want to connect a MIDI keyboard - you should download Plumstone. (http://www.mandolane.co.uk)

