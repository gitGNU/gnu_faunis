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

import java.util.Random;
import common.enums.ClientStatus;
import clientSide.client.Client;

public class Robot implements Runnable {
	private Client client;
	private String username;
	private String[] playernames;
	private Random random;
	
	public Robot(String username, String[] playernames, Random random) {
		this.username = username;
		this.playernames = playernames;
		this.random = random;
	}
	
	@Override
	public void run() {
		client = new Client();
		client.init(null);
		getToStatus(ClientStatus.exploring);
		while (true) {
//			try {
//			Thread.sleep(600);
//			} catch(InterruptedException e) {
//				e.printStackTrace();
//				return;
//			}
			randomAction();
		}
	}
	
	private void randomAction() {
		if (clientStatus() != ClientStatus.exploring) {
			if (random.nextBoolean())
				increaseStatus(true);
			else
				decreaseStatus(true);
		} else {
			int rndInt = random.nextInt(18);
			if (rndInt <= 9) {
				// do nothing
			}
			else if (rndInt >= 10 && rndInt <= 14) {
				int x = random.nextInt(30)+1;
				int y = random.nextInt(20)+1;
				client.parseCommand("/m", new String[]{String.valueOf(x), String.valueOf(y)});
			}
			else if (rndInt == 15)
				client.parseCommand("/e", new String[]{"walk"});
			else if (rndInt == 16)
				client.parseCommand("/b", new String[]{"Hallo", "zusammen!"});
			else if (rndInt == 17)
				decreaseStatus(true);
		}
	}
	
	private void getToStatus(ClientStatus status) {
		assert(status != ClientStatus.fighting);
		while(status != clientStatus()) {
			if (status.compareTo(clientStatus()) > 0)
				increaseStatus(true);
			else if (status.compareTo(clientStatus()) < 0)
				decreaseStatus(true);
		}
	}
	
	private void decreaseStatus(boolean wait) {
		switch(clientStatus()) {
			case loggedOut:
				client.parseCommand("/x", new String[0]);
				if (wait) waitForStatus(ClientStatus.disconnected);
				break;
			case noCharLoaded:
				client.parseCommand("/o", new String[0]);
				if (wait) waitForStatus(ClientStatus.loggedOut);
				break;
			case fighting:
			case exploring:
				client.parseCommand("/u", new String[0]);
				if (wait) waitForStatus(ClientStatus.noCharLoaded);
				break;
			case disconnected:
				break;
		}
	}
	
	private void increaseStatus(boolean wait) {
		switch(clientStatus()) {
			case disconnected:
				client.parseCommand("/c", new String[0]);
				if (wait) waitForStatus(ClientStatus.loggedOut);
				break;
			case loggedOut:
				client.parseCommand("/i", new String[]{username, ""});
				if (wait) waitForStatus(ClientStatus.noCharLoaded);
				break;
			case noCharLoaded:
				client.parseCommand("/l", new String[]{playernames[0]});
				if (wait) waitForStatus(ClientStatus.exploring);
				break;
			case fighting:
			case exploring:
				break;
		}
	}
	
	private void waitForStatus(ClientStatus status) {
		while (clientStatus() != status) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private ClientStatus clientStatus() {
		return client.getClientStatus();
	}
}
