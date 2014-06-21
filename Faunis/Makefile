# Copyright 2012 - 2014 Simon Ley alias "skarute"
#
# This file is part of Faunis.
# 
# Faunis is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of
# the License, or (at your option) any later version.
# 
# Faunis is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General
# Public License along with Faunis. If not, see
# <http://www.gnu.org/licenses/>.

MUX_DIR=./MuxInputStream/
MUX_SRC=$(MUX_DIR)src/
MUX_BIN=$(MUX_DIR)bin/

FAUNIS_DIR=./Faunis/
FAUNIS_SRC=$(FAUNIS_DIR)src/
FAUNIS_BIN=$(FAUNIS_DIR)bin/

JAVAC_MUX = javac -sourcepath $(MUX_SRC) -classpath $(MUX_BIN):junit.jar -d $(MUX_BIN)
JAVAC_FAUNIS = javac -sourcepath $(FAUNIS_SRC) -classpath $(FAUNIS_BIN):$(MUX_BIN) -d $(FAUNIS_BIN)
JAVA = java -enableassertions -classpath $(FAUNIS_BIN):$(MUX_BIN)

compile:
	$(JAVAC_MUX) $(MUX_SRC)mux/MuxObjectInputStream.java
	$(JAVAC_FAUNIS) $(FAUNIS_SRC)clientSide/client/Client.java
	$(JAVAC_FAUNIS) $(FAUNIS_SRC)serverSide/MainServer.java
	$(JAVAC_FAUNIS) $(FAUNIS_SRC)serverSide/tests/RobotMaster.java

runClient:
	$(JAVA) clientSide/client/Client

runServer:
	$(JAVA) serverSide/MainServer

runRobots:
	$(JAVA) serverSide/tests/RobotMaster

clean:
	rm -rf $(FAUNIS_BIN)*
	rm -rf $(MUX_BIN)*
