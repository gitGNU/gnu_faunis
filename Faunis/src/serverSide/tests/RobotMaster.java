/* Copyright 2012, 2013 Simon Ley alias "skarute"
 * 
 * This file is part of Faunis.
 * 
 * Faunis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Faunis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General
 * Public License along with Faunis. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package serverSide.tests;

import java.util.ArrayList;
import java.util.Random;

import serverSide.ServerSettings;

public class RobotMaster {
	private static ServerSettings serverSettings = new ServerSettings();
	private static ArrayList<Robot> robots;
	public static void main(String[] args) {
		initialise();
		start();
	}
	
	private static void start() {
		for (Robot robot : robots) {
			new Thread(robot).start();
		}
	}
	
	private static void initialise() {
		robots = new ArrayList<Robot>();
		Random random = new Random(5l);
		int robotsCount = 5;
		int playersPerRobot = 3;
		createTestServerData(robotsCount, playersPerRobot);
		for (int robotIndex = 0; robotIndex < robotsCount; robotIndex++) {
			String username = "robot"+robotIndex;
			String[] playernames = new String[playersPerRobot];
			for (int playerIndex = 0; playerIndex < playersPerRobot; playerIndex++) {
				playernames[playerIndex] = username+"player"+playerIndex;
			}
			Robot robot = new Robot(username, playernames, random);
			robots.add(robot);
		}
	}
	
	private static void createTestServerData(int robotsCount, int playersPerRobot) {
		TestServerDataFactory.createTestServerData(serverSettings, robotsCount, playersPerRobot);
	}
}