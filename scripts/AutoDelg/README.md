

Script waits for console input of magnet torrent links (one at a time) and adds them to download queue.

Enter 'quit' to exit (default).

Verify script commands and download path in prop.prop file which should be in the same folder.
----------------------
COMMANDS:

Compile:  groovyc AutoDelg.groovy

Make Jar: jar cfm AutoDelg.jar MANIFEST.MF AutoDelg.*

Run:      groovy jar:file:AutoDelg.jar'!'/AutoDelg.groovy | bash
