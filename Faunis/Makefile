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

SRC_DIR=src/
BIN_DIR=bin/

MUX_DIR=./MuxInputStream/
FAUNIS_DIR=./Faunis/
TOOLS_DIR=./Tools/

JAVAC_MUX = javac -sourcepath $(MUX_DIR)$(SRC_DIR) -classpath $(MUX_DIR)$(BIN_DIR):junit.jar -d $(MUX_DIR)$(BIN_DIR)
JAVAC_FAUNIS = javac -sourcepath $(FAUNIS_DIR)$(SRC_DIR) -classpath $(FAUNIS_DIR)$(BIN_DIR):$(MUX_DIR)$(BIN_DIR):$(TOOLS_DIR)$(BIN_DIR) -d $(FAUNIS_DIR)$(BIN_DIR)
JAVAC_TOOLS = javac -sourcepath $(TOOLS_DIR)$(SRC_DIR) -classpath $(TOOLS_DIR)$(BIN_DIR):$(MUX_DIR)$(BIN_DIR) -d $(TOOLS_DIR)$(BIN_DIR)
JAVA = java -enableassertions -classpath $(FAUNIS_DIR)$(BIN_DIR):$(MUX_DIR)$(BIN_DIR):$(TOOLS_DIR)$(BIN_DIR)

compile:
	$(JAVAC_MUX) $(MUX_DIR)$(SRC_DIR)mux/MuxObjectInputStream.java
	$(JAVAC_TOOLS) $(TOOLS_DIR)$(SRC_DIR)tools/*.java
	$(JAVAC_FAUNIS) $(FAUNIS_DIR)$(SRC_DIR)clientSide/client/Client.java
	$(JAVAC_FAUNIS) $(FAUNIS_DIR)$(SRC_DIR)serverSide/MainServer.java
	$(JAVAC_FAUNIS) $(FAUNIS_DIR)$(SRC_DIR)serverSide/tests/RobotMaster.java

runClient:
	$(JAVA) clientSide/client/Client

runServer:
	$(JAVA) serverSide/MainServer

runRobots:
	$(JAVA) serverSide/tests/RobotMaster

clean:
	rm -rf $(FAUNIS_DIR)$(BIN_DIR)*
	rm -rf $(MUX_DIR)$(BIN_DIR)*
	rm -rf $(TOOLS_DIR)$(BIN_DIR)*
