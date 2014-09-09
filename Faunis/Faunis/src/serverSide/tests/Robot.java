/* Copyright 2012 - 2014 Simon Ley alias "skarute"
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

import common.Map;
import common.enums.ClientStatus;
import clientSide.client.Client;
import clientSide.userToClientOrders.UCParseCommandOrder;

public class Robot implements Runnable {
	private Client client;
	private String username;
	private String[] playernames;
	private Random random;
	private int delayMediumMs = 300;
	private int delayDeviationMs = 100;
	private int doNothingPart = 40; // number of cases where robot just waits a small delay
	private int movePart = 4; // number of cases where a move command is given
	private static String[] chatMessages = new String[] {"Hi folks!", "How are you?",
		"Where's the chocolate?", "I'm beginning to feel a bit hungry...",
		"Isn't it time for hibernation yet?", "Eurofurence rocks", "^^", "lol", "XD",
		"Admin, I found a bug", "May I hug you?", "Hmm, I smell the fresh forest air...",
		"Please support this game by drawing graphics for it - it can only get better :o]",
		"Born to be furry", "This game is free and open-source!", "Home sweet home!",
		"Who wants to trade for some honey?", "I think I got lost!", "*pant, pant*",
		"(They're watching us, everybody behave!)", "Burr needs some fooood!", "Hey!",
		"*sniff* hmmm, smells good...", "Somebody in there?", "Where the fur am I going?",
		"Do not tempt me!", "My tail is itching.", "I'm back", "Have to go - bye"};

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
			if (random.nextBoolean()) {
				increaseStatus(true);
			} else {
				decreaseStatus(true);
			}
		} else {
			int rndInt = random.nextInt(doNothingPart+movePart+3)+1;
			if (rndInt <= doNothingPart) {
				delay(); // do nothing
			}
			else if (rndInt > doNothingPart && rndInt <= doNothingPart+movePart) {
				Map map = client.getCurrentMap();
				int x = random.nextInt(map.getWidth());
				int y = random.nextInt(map.getHeight());
				client.putUCOrder(new UCParseCommandOrder("/m "+x+" "+y));
			}
			else if (rndInt == doNothingPart+movePart+1) {
				delay();
			} else if (rndInt == doNothingPart+movePart+2) {
				client.putUCOrder(
					new UCParseCommandOrder("/b "+chatMessages[random.nextInt(chatMessages.length)])
				);
			} else if (rndInt == doNothingPart+movePart+3) {
				decreaseStatus(true);
			}
		}
	}

	private void getToStatus(ClientStatus status) {
		assert(status != ClientStatus.fighting);
		while(status != clientStatus()) {
			if (status.compareTo(clientStatus()) > 0) {
				increaseStatus(true);
			} else if (status.compareTo(clientStatus()) < 0) {
				decreaseStatus(true);
			}
		}
	}

	private void decreaseStatus(boolean wait) {
		switch(clientStatus()) {
			case loggedOut:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/x"));
				} else {
					while (clientStatus() != ClientStatus.disconnected) {
						client.putUCOrder(new UCParseCommandOrder("/x"));
						delay();
					}
				}
				break;
			case noCharLoaded:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/o"));
				} else {
					while (clientStatus() != ClientStatus.loggedOut) {
						client.putUCOrder(new UCParseCommandOrder("/o"));
						delay();
					}
				}
				break;
			case fighting:
			case exploring:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/u"));
				} else {
					while (clientStatus() != ClientStatus.noCharLoaded) {
						client.putUCOrder(new UCParseCommandOrder("/u"));
						delay();
					}
				}
				break;
			case disconnected:
				break;
		}
	}

	private void increaseStatus(boolean wait) {
		switch(clientStatus()) {
			case disconnected:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/c"));
				} else {
					while (clientStatus() != ClientStatus.loggedOut) {
						client.putUCOrder(new UCParseCommandOrder("/c"));
						delay();
					}
				}
				break;
			case loggedOut:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/i "+username+" "+username));
				} else {
					while (clientStatus() != ClientStatus.noCharLoaded) {
						client.putUCOrder(new UCParseCommandOrder("/i "+username+" "+username));
						delay();
					}
				}
				break;
			case noCharLoaded:
				if (!wait) {
					client.putUCOrder(new UCParseCommandOrder("/l "+playernames[0]));
				} else {
					while (clientStatus() != ClientStatus.exploring) {
						client.putUCOrder(new UCParseCommandOrder("/l "+playernames[0]));
						delay();
					}
				}
				break;
			case fighting:
			case exploring:
				break;
		}
	}

	private void delay() {
		try {
			Thread.sleep(delayMediumMs + (long) ((2*random.nextFloat()-1)*delayDeviationMs));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ClientStatus clientStatus() {
		return client.getClientStatus();
	}
}
