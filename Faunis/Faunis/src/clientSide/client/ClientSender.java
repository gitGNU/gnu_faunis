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
package clientSide.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import clientSide.ClientSettings;
import clientSide.userToClientOrders.UCConnectOrder;
import clientSide.userToClientOrders.UCCreatePlayerOrder;
import clientSide.userToClientOrders.UCDisconnectOrder;
import clientSide.userToClientOrders.UCHelpOrder;
import clientSide.userToClientOrders.UCLoadPlayerOrder;
import clientSide.userToClientOrders.UCLoginOrder;
import clientSide.userToClientOrders.UCLogoutOrder;
import clientSide.userToClientOrders.UCOrder;
import clientSide.userToClientOrders.UCParseCommandOrder;
import clientSide.userToClientOrders.UCQueryOwnPlayersOrder;
import clientSide.userToClientOrders.UCServerSourceOrder;
import clientSide.userToClientOrders.UCUnloadPlayerOrder;

import common.HelperMethods;
import common.Logger;
import common.butlerToClientOrders.BCOrder;
import common.clientToButlerOrders.CBAccessInventoryOrder;
import common.clientToButlerOrders.CBChatOrder;
import common.clientToButlerOrders.CBCreatePlayerOrder;
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
	private ReentrantLock connectionModiMutexKey;
	
	
	
	public void init(Client _parent) {
		this.parent = _parent;
		this.connectionModiMutexKey = new ReentrantLock();
	}
	
	
	public void handleMessage(UCOrder order) {
		if (order instanceof UCConnectOrder) {
			connect();
		} else if (order instanceof UCDisconnectOrder) {
			disconnect();
		} else if (order instanceof UCLoginOrder) {
			UCLoginOrder loginOrder = (UCLoginOrder) order;
			sendOrder(new CBLoginOrder(loginOrder.getUsername(), loginOrder.getPassword()));
		} else if (order instanceof UCLogoutOrder) {
			sendOrder(new CBLogoutOrder());
		} else if (order instanceof UCLoadPlayerOrder) {
			UCLoadPlayerOrder loadPlayerOrder = (UCLoadPlayerOrder) order;
			sendOrder(new CBLoadPlayerOrder(loadPlayerOrder.getPlayername()));
		} else if (order instanceof UCUnloadPlayerOrder) {
			sendOrder(new CBUnloadPlayerOrder());
		} else if (order instanceof UCParseCommandOrder) {
			UCParseCommandOrder parseCommandOrder = (UCParseCommandOrder) order;
			boolean response = parseCommand(parseCommandOrder.getCommand());
			parseCommandOrder.setResponse(response);
		} else if (order instanceof UCQueryOwnPlayersOrder) {
			sendOrder(new CBQueryOwnPlayersOrder());
		} else if (order instanceof UCServerSourceOrder) {
			sendOrder(new CBServerSourceOrder());
		} else if (order instanceof UCCreatePlayerOrder) {
			UCCreatePlayerOrder createPlayerOrder = (UCCreatePlayerOrder) order;
			sendOrder(new CBCreatePlayerOrder(createPlayerOrder.getPlayername()));
		} else if (order instanceof UCHelpOrder) {
			parent.logHelpInstructions();
		}
	}
	
	
	
	/** locks connectionModiMutexKey, clientStatusMutexKey */
	public boolean connect() {
		if (connectionModiMutexKey.tryLock()) {
			try {
				ClientStatus clientStatus = parent.getClientStatus();
				if (clientStatus != ClientStatus.disconnected) {
					parent.logErrorMessage("Couldn't connect since clientStatus is not set to \"disconnected\"!");
					return false;
				}
				assert(parent.mux.getNumberOfStreams() == 0);
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
					parent.serverOutput = new ObjectOutputStream(parent.socket.getOutputStream());
					parent.serverInput = new ObjectInputStream(parent.socket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
					parent.logErrorMessage("Couldn't create input / output!");
					return false;
				}
				parent.mux.addStream(parent.serverInput, BCOrder.class);
				parent.setClientStatus(ClientStatus.loggedOut);
				Logger.log("Input / output have been created.");
				Logger.log("Start serverThread...");
				return true;
			} finally {
				connectionModiMutexKey.unlock();
			}
		} else {
			Logger.log("Client: connect(): Connection is already being modified?");
			return false;
		}
	}
	/**
	 * Unfriendly disconnect: Contains client termination logic,
	 * doesn't inform the server about it */
	public boolean disconnect() {
		// all the actions must unload the client connection completely,
		// but should also work if parts are already unloaded
		Logger.log("Client: called disconnect()");
		if (connectionModiMutexKey.tryLock()) {
			try {
				ClientStatus clientStatus = parent.getClientStatus();
				if (clientStatus == ClientStatus.disconnected) {
					parent.logErrorMessage("Client: Couldn't disconnect as clientStatus is already \"disconnected\"");
					return false;
				}
				parent.mux.closeStream(parent.serverInput, true);
				// Close the socket
				try {
					parent.socket.close();
				} catch (IOException e) {
					
				}
				
				parent.receiverPart.unloadMap();

				parent.setClientStatus(ClientStatus.disconnected);
				parent.serverInput = null;
				parent.serverOutput = null;
				Logger.log("Client: Connection to server terminated.");
				return true;
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
		ClientStatus clientStatus = parent.getClientStatus();
		if (clientStatus == ClientStatus.disconnected) {
			parent.logErrorMessage("Client: Couldn't send order since there's no connection!");
			return false;
		}
		assert(parent.serverOutput != null);
		try {
			parent.serverOutput.writeObject(c);
		} catch (IOException e) {
			parent.logErrorMessage("Error while sending ClientOrder");
			return false;
		}
		return true;
	}
	
	
	
	boolean parseCommand(String command) {
		String[] commandSplit = command.split(" ");
		assert(commandSplit.length > 0);
		String commandPrefix = commandSplit[0];
		String[] commandSplitDetails = new String[commandSplit.length-1];
		for (int i = 1; i < commandSplit.length; i++) {
			commandSplitDetails[i-1] = commandSplit[i];
		}
		
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
			disconnect();
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
