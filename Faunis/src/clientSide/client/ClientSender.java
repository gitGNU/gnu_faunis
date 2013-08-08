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
package clientSide.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import clientSide.ClientSettings;

import common.HelperMethods;
import common.Logger;
import common.clientToButlerOrders.CBAccessInventoryOrder;
import common.clientToButlerOrders.CBChatOrder;
import common.clientToButlerOrders.CBCreatePlayerOrder;
import common.clientToButlerOrders.CBDisconnectOrder;
import common.clientToButlerOrders.CBLoadPlayerOrder;
import common.clientToButlerOrders.CBLoginOrder;
import common.clientToButlerOrders.CBLogoutOrder;
import common.clientToButlerOrders.CBMoveOrder;
import common.clientToButlerOrders.CBOrder;
import common.clientToButlerOrders.CBQueryOwnPlayersOrder;
import common.clientToButlerOrders.CBServerSourceOrder;
import common.clientToButlerOrders.CBTriggerAnimationOrder;
import common.clientToButlerOrders.CBUnloadPlayerOrder;
import common.enums.ClientStatus;
import common.enums.InventoryType;

public class ClientSender {
	private Client parent;
	ObjectOutputStream output;
	private ReentrantLock connectionModiMutexKey;
	
	
	
	public void init(Client _parent) {
		this.parent = _parent;
		this.connectionModiMutexKey = new ReentrantLock();
	}
	
	
	
	/** locks connectionModiMutexKey, clientStatusMutexKey */
	public boolean connect() {
		if (connectionModiMutexKey.tryLock()) {
			try {
				synchronized(parent.clientStatusMutexLock) {
					ClientStatus clientStatus = parent.getClientStatus();
					if (clientStatus != ClientStatus.disconnected) {
						parent.logErrorMessage("Couldn't connect since clientStatus is not set to \"disconnected\"!");
						return false;
					}
					parent.receiverPart.stop(true, false);
					assert(parent.socket == null || parent.socket.isClosed());
					// create socket:
					try {
						parent.socket = new Socket(parent.clientSettings.host(), parent.clientSettings.port());
					} catch (Exception e) {
						parent.logErrorMessage("Socket to game server couldn't be created! Reason: "+
											   e.getLocalizedMessage());
						return false;
					}
					Logger.log("Socket created.");
					
					// create input and output stream:
					Logger.log("Try to create input / output streams...");
					try {
						parent.receiverPart.input = new ObjectInputStream(parent.socket.getInputStream());
						output = new ObjectOutputStream(parent.socket.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
						parent.logErrorMessage("Couldn't create input / output!");
						return false;
					}
					parent.setClientStatus(ClientStatus.loggedOut);
					Logger.log("Input / output have been created.");
					Logger.log("Start serverThread...");
					parent.receiverPart.start();
					return true;
				}
			} finally {
				connectionModiMutexKey.unlock();
			}
		} else {
			Logger.log("Client: connect(): Connection is already being modified?");
			return false;
		}
	}
	
	/** locks connectionModiMutexKey, clientStatusMutexKey, currentPlayerGraphics, movingPlayerGraphics */
	public boolean disconnect() {
		Logger.log("Client: called disconnect()");
		if (connectionModiMutexKey.tryLock()) {
			try {
				synchronized(parent.clientStatusMutexLock) {
					ClientStatus clientStatus = parent.getClientStatus();
					if (clientStatus == ClientStatus.disconnected) {
						parent.logErrorMessage("Client: Couldn't disconnect as clientStatus is already \"disconnected\"");
						return false;
					}
					parent.receiverPart.stop(true, false);
					assert(parent.socket.isClosed());
					
					parent.receiverPart.unloadMap();
					
					parent.setClientStatus(ClientStatus.disconnected);
					parent.receiverPart.input = null;
					parent.senderPart.output = null;
					Logger.log("Client: Connection to server terminated.");
					return true;
				}
			} finally {
				connectionModiMutexKey.unlock();
			}
		} else {
			Logger.log("Client: disconnect(): Connection is already being modified?");
			return false;
		}
	}
	
	
	
	/** locks clientStatusMutexKey, output<br/>
	 *  Tries to send given ClientOrder to the own Butler, and returns
	 *  the success thereof as a boolean.*/
	public boolean sendOrder(CBOrder c){
		synchronized(parent.clientStatusMutexLock) {
			ClientStatus clientStatus = parent.getClientStatus();
			if (clientStatus == ClientStatus.disconnected) {
				parent.logErrorMessage("Client: Couldn't send order since there's no connection!");
				return false;
			}
			assert(output != null);
			synchronized(output) {
				try {
					output.writeObject(c);
				} catch (IOException e) {
					parent.logErrorMessage("Error while sending ClientOrder");
					return false;
				}
				return true;
			}
		}
	}
	
	
	
	boolean parseCommand(String commandPrefix, String[] commandSplitDetails) {
		if (commandSplitDetails == null)
			commandSplitDetails = new String[0];
		ClientSettings s = parent.clientSettings;
		if (commandPrefix.equals(s.commandPrefix()+s.connectCommand())) {
			connect();
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.loginCommand())
				   && commandSplitDetails.length >= 2) {
			String loginName = commandSplitDetails[0];
			String loginPassword = commandSplitDetails[1];
			sendOrder(new CBLoginOrder(loginName, loginPassword));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.logoutCommand())) {
			sendOrder(new CBLogoutOrder());
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.disconnectCommand())) {
			sendOrder(new CBDisconnectOrder());
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.whisperCommand())
				   && commandSplitDetails.length >= 2) {
			String receiver = commandSplitDetails[0];
			String message = HelperMethods.concatenateHelper(commandSplitDetails, 1);
			sendOrder(new CBChatOrder(message, receiver));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.broadcastCommand())
				   && commandSplitDetails.length >= 1) {
			String message = HelperMethods.concatenateHelper(commandSplitDetails, 0);
			sendOrder(new CBChatOrder(message, null));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.loadPlayerCommand())) {
			String playerName = commandSplitDetails[0];
			sendOrder(new CBLoadPlayerOrder(playerName));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.unloadPlayerCommand())) {
			sendOrder(new CBUnloadPlayerOrder());
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.createPlayerCommand())
				   && commandSplitDetails.length >= 1) {
			String playerName = commandSplitDetails[0];
			sendOrder(new CBCreatePlayerOrder(playerName));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.moveCommand())
				   && commandSplitDetails.length >= 2) {
			int walkX, walkY;
			try {
				walkX = Integer.parseInt(commandSplitDetails[0]);
				walkY = Integer.parseInt(commandSplitDetails[1]);
			} catch(NumberFormatException e) {
				parent.logErrorMessage("Error while parsing the coordinates.");
				return false;
			}
			sendOrder(new CBMoveOrder(walkX, walkY));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.emoteCommand())) {
			String animationName = null;
			if (commandSplitDetails.length >= 1) {
				animationName = commandSplitDetails[0];
			}
			sendOrder(new CBTriggerAnimationOrder(animationName));
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.serverSourceCommand())) {
			sendOrder(new CBServerSourceOrder());
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.queryOwnPlayersCommand())) {
			sendOrder(new CBQueryOwnPlayersOrder());
			return true;
		} else if (commandPrefix.equals(s.commandPrefix()+s.helpCommand())) {
			parent.logHelpInstructions();
			return true;
		}
		
		else if(commandPrefix.equals("/inv") && (commandSplitDetails.length == 4 || commandSplitDetails.length == 3 || commandSplitDetails.length == 1)){
			if(commandSplitDetails[0].toUpperCase().equals("VIEW")){
				sendOrder(new CBAccessInventoryOrder(InventoryType.VIEW, parent.activePlayerName));
			}else if(commandSplitDetails[0].toUpperCase().equals("ADD") && (commandSplitDetails.length == 3)){
				try{
					int itemID = Integer.parseInt(commandSplitDetails[1]);
					int qnt = Integer.parseInt(commandSplitDetails[2]);
					sendOrder(new CBAccessInventoryOrder(InventoryType.ADD, parent.activePlayerName, itemID, qnt));
				}catch(NumberFormatException e){
					parent.logErrorMessage("Error while parsing the numbers.");
					return false;
				}
			}else if(commandSplitDetails[0].toUpperCase().equals("THROW") && (commandSplitDetails.length == 3)){
				try{
					int itemID = Integer.parseInt(commandSplitDetails[1]);
					int qnt = Integer.parseInt(commandSplitDetails[2]);
					sendOrder(new CBAccessInventoryOrder(InventoryType.THROW, parent.activePlayerName, itemID, qnt));
				}catch(NumberFormatException e){
					parent.logErrorMessage("Error while parsing the numbers.");
					return false;
				}
			}else if(commandSplitDetails[0].toUpperCase().equals("GIVE") && (commandSplitDetails.length == 4)){
				try{
					int itemID = Integer.parseInt(commandSplitDetails[2]);
					int qnt = Integer.parseInt(commandSplitDetails[3]);
					sendOrder(new CBAccessInventoryOrder(InventoryType.GIVE, parent.activePlayerName, commandSplitDetails[1], itemID, qnt));
				}catch(NumberFormatException e){
					parent.logErrorMessage("Error while parsing the numbers.");
					return false;
				}
			}
			return true;
		}
		
		else {
			parent.logErrorMessage("Command couldn't be interpreted.");
			return false;
		}
		// TODO: handle further commands
	}

}
