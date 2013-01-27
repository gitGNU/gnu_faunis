Copyright 2012, 2013 Simon Ley alias "skarute"
This documentation is published under the GNU Free Documentation
License v1.3 or later. You can find a copy of this license in
"fdl-1.3.txt" or at <http://www.gnu.org/licenses/>.

########################################
Faunis 0.2 - a free furry MMORPG
########################################
 
All files of Faunis are copyrighted: Copyright 2012, 2013 Simon Ley alias "skarute"
All files of Faunis are licensed under the GNU AGPL v3 or later, except for
the documentation files "README.txt", "devdoc.txt" and "windows.txt",
which are licensed under the GNU FDL v1.3 or later.
Faunis is still under heavy development, so don't expect too much for now. :o]

1. Requirements
To execute: Java Runtime Environment (JRE) 6 or higher.
To compile: Java Development Kit (JDK) 6 or higher. Optionally eclipse as IDE.
Windows users may find some tips in the included file "windows.txt".

2. How to compile
Faunis is developed in eclipse. If you want to use this IDE, you can just import the whole Faunis directory as a project. If not, you may use the Makefile, the target "clean" removes all compiled data, the target "compile" rebuilds server and client.
Also note that Faunis is delivered precompiled, so you don't need to compile if you don't want to.

3. How to play
The command to run the client is "make runClient", whilst the command to start the server is "make runServer".

4. Quick introduction into the client usage
The client status is shown in the left upper corner. It can be "disconnected", "logged out", "no character loaded" or "exploring". Currently the only supported mouse commands is a left click for walking, so to actually do something, you primarily have to type commands into the field at the bottom.
There are the following commands:
To connect to the server, type: /c
Log in: /i username password
Load player: /l playername
Walk around: /m xcoordinate ycoordinate
Send private message: /w username message
Broadcast message: /b message
Request server source code: /s
Create new player: /n playername
Trigger emote: /e emote
Unload player: /u
Log out: /o
Disconnect: /x
Note that in the default configuration, the server is contacted at localhost, so you have to start a server instance on the same machine to be able to connect.

5. Server usage
Just start it and it should do the rest. Is also configured to use localhost. Close the window when you have enough.

6. Configurating client and server
Client and server settings are until now hardcoded. To change them, edit the ClientSettings and ServerSettings classes and recompile.

I am grateful for your feedback, contact me under simon7ley at googlemail dot com.
Enjoy!