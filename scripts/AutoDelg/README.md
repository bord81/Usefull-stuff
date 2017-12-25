/*Tested with deluge-console: 1.3.13*/

Tired of manually entering commands for adding each torrent link in deluge-console?

This script makes the most used actions easier.

 - To add a magnet link just paste it to the terminal prompt and press enter.
 - To view current downloads press 'd'.
 - Paused and queued torrents 'p' and 'q' resp.
 - If you put space and torrent id after 'p' or 'r' it will be paused or removed resp.
 - 'e' for exit

You can change the default keys and commands by editing prop.prop file which should be in the same folder as script.


COMMANDS:

Compile:  groovyc AutoDelg.groovy

Make Jar: jar cfm AutoDelg.jar MANIFEST.MF AutoDelg.*

Run:      groovy jar:file:AutoDelg.jar'!'/AutoDelg.groovy | bash
