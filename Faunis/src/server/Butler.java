/* Copyright 2012 Simon Ley alias "skarute"
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
package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import server.butlerToInvmanOrders.BIAccessInventoryOrder;
import server.butlerToInvmanOrders.BIOrder;
import server.butlerToInvmanOrders.BISetPlayerItemFileOrder;
import server.butlerToInvmanOrders.BIUnsetPlayerItemFileOrder;
import server.butlerToMapmanOrders.*;
import server.invmanToButlerOrders.IBOrder;
import server.invmanToButlerOrders.IBSendErrorMessageOrder;
import server.invmanToButlerOrders.IBSendInventoryOrder;
import server.mapmanToButlerOrders.*;
import communication.butlerToClientOrders.*;
import communication.clientToButlerOrders.*;
import communication.enums.CharacterClass;
import communication.enums.ClientStatus;
import communication.enums.InventoryType;


/** The butler looks after the needs of a client.
 * He receives the socket that the reception has already created.
 * He represents his client on the server side. */
public class Butler {
	private Object shutdownMutexKey;
	private boolean shutdownOccupied;
	protected MainServer parent;
	private Socket clientSocket; // the socket through which to communicate with the client
	protected ObjectInputStream clientInput; // to read from the socket
	protected ObjectOutputStream clientOutput; // to write to the socket; synchronised access only
	private Thread clientThread; // steadily observes and handles the requests from the client
	private ClientRunnable clientRunnable; // the job of clientThread
	protected boolean stopRunning = false; // indicates termination to clientThread and serverThread
	protected Account loggedAccount;		// Account, unter dem der Client gerade eingeloggt ist (kann auch null sein)
	protected Player activePlayer;
	protected MapManager activeMapman;
	protected InventoryManager invMan;
	
	//MapMan's Variables
	protected BlockingQueue<MBOrder> serverOrders;
	private Thread serverThread; // steadily observes and handles the requests from server side
	private ServerRunnable serverRunnable; // the job of serverThread
	
	//InvMan's Variables
	protected BlockingQueue<IBOrder> invmanOrders;
	private Thread invmanThread;
	private InvManRunnable invManRunnable;
	
	public Butler(MainServer parent, Socket clientSocket){
		this.shutdownMutexKey = new Object();
		this.shutdownOccupied = false;
		this.parent = parent;
		this.clientSocket = clientSocket;
		this.serverOrders = new ArrayBlockingQueue<MBOrder>(50);
		this.invmanOrders = new ArrayBlockingQueue<IBOrder>(50); //Added
		this.invMan = parent.getInvMan();
		
		// create input and output streams from clientSocket
		System.out.println("Butler: Try to create input / output streams...");
		try {
			this.clientOutput = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.clientInput = new ObjectInputStream(this.clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Couldn't create input / output stream!");
		}
		System.out.println("Input and output streams have been created.");
		
		// create and start clientThread
		this.clientRunnable = new ClientRunnable(this);
		this.clientThread = new Thread(this.clientRunnable);
		this.clientThread.start();
		// create and start serverThread
		this.serverRunnable = new ServerRunnable(this);
		this.serverThread = new Thread(this.serverRunnable);
		this.serverThread.start();
		//create and start invManThread
		this.invManRunnable = new InvManRunnable(this);
		this.invmanThread = new Thread(this.invManRunnable);
		this.invmanThread.start();
	}
	
	public boolean assertActivePlayer() {
		if (activePlayer == null) {
			sendErrorMessage("Command requires loaded player!");
			return false;
		}
		return true;
	}
	public boolean assertLoggedAccount() {
		if (loggedAccount == null) {
			sendErrorMessage("Command requires logged account!");
			return false;
		}
		return true;
	}
	
	
	/** Shuts this butler down. Only one thread is allowed
	 * to call this simultaneously. All others are rejected (returns false),
	 * thus not blocked by calling this.<br/>
	 * This concurrency problem is solved by shutdownMutexKey
	 * (the lock to synchronise on before shutdownOccupied is read),
	 * and shutdownOccupied which indicates if the method is already
	 * being called.<br/>
	 * Asserts that clientThread and serverThread terminate,
	 * unless it isn't one of them that calls this method, in
	 * which case only the termination of the other is asserted
	 * and it is the responsibility of the caller to terminate afterwards
	 * immediately. Thus, wherever clientThread / serverThread calls shutdown(),
	 * it must terminate thereafter. */
	public boolean shutdown(){
		synchronized(shutdownMutexKey) {
			if (shutdownOccupied == true)
				return false;
			else
				shutdownOccupied = true;
		}
		System.out.println("Butler: Initiate shutdown...");
		
		// Save data, log out, unregister butler etc.
		if (loggedAccount != null)
			logoutAccount();
		parent.deleteButler(this);
		
		// Ask both threads of this butler to terminate:
		stopRunning = true;
		// Since both threads may be stuck waiting / listening,
		// we have to enforce termination! (:<
		// Enforce termination of serverThread by a special order:
		serverOrders.clear();
		serverOrders.add(new MBStopThreadOrder(null));
		
		// Enforce termination of clientThread by closing clientInput:
		try {
			clientInput.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't choke clientThread!");
		}
		if (Thread.currentThread() == this.clientThread) {
			// Only wait for serverThread to terminate:
			while (serverThread.isAlive()) {}
		} else if (Thread.currentThread() == this.serverThread) {
			// Only wait for clientThread to terminate:
			while (clientThread.isAlive()) {}
		} else {
			// wait for both threads to terminate:
			while (serverThread.isAlive() || clientThread.isAlive()) {}
		}
		shutdownOccupied = false;
		return true;
	}
	
	// #################################################################################
	// Client -> Butler -> MapMan, Server side:
	// #################################################################################	

	
	public void loadActivePlayer(CBLoadPlayerOrder order) {
		if (!assertLoggedAccount())
			return;
		else if (activePlayer != null) {
			sendErrorMessage("Butler: Couldn't load player: There's already one loaded.");
			return;
		}
		String playerName = order.getPlayerName();
		Result<Player> query = parent.loadAndActivatePlayer(loggedAccount, playerName);
		if (!query.successful()) {
			sendErrorMessage(query.getErrorMessage());
			return;
		}
		activePlayer = query.getResult();
		// Assign player to mapman:
		String mapname = activePlayer.getMapName();
		assert(mapname != null);
		assert(activeMapman == null);
		activeMapman = parent.getMapman(mapname);
		addPlayerToMapman(activeMapman, false);
		//Add player to InvMan
		String itemFile = new String(parent.getAccountLocation() 
				+ loggedAccount.getName() + File.separator
				+ "players" + File.separator
				+ playerName + File.separator
				+ "items");
		sendOrderToInvMan(new BISetPlayerItemFileOrder(playerName, itemFile, this));
		// => the mapman will send a MBMapInfoOrder
		// to the butler who passes it on to the client
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.exploring));
		System.out.println("Butler: Player "+playerName+" successfully loaded.");
	}
	
	/** Registers the active player at given mapman.
	 * By doing so, the mapman will consequently send a MBMapInfoOrder
	 * to the butler, who passes it on to the client.*/
	private void addPlayerToMapman(MapManager mapman, boolean addPlayerMapEntry) {
		assert(activePlayer != null);
		assert(mapman != null);
		mapman.put(new BMRegisterOrder(this, activePlayer, addPlayerMapEntry));
	}
	
	private void removePlayerFromMapman(MapManager mapman, boolean removePlayerMapEntry) {
		assert(activePlayer != null);
		assert(mapman != null);
		mapman.put(new BMUnregisterOrder(this, activePlayer, removePlayerMapEntry));
	}
	
	public void unloadActivePlayer() {
		sendOrderToInvMan(new BIUnsetPlayerItemFileOrder(activePlayer.getName(), this)); //Called first before removing any of the info below
		if (!assertLoggedAccount()) return;
		if (!assertActivePlayer()) return;
		assert(activeMapman != null);
		removePlayerFromMapman(activeMapman, false);
		parent.unloadPlayer(activePlayer);
		activePlayer = null;
		activeMapman = null;
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.noCharLoaded));
		System.out.println("Butler: Unloaded player.");
	}
	
	public void loginAccount(CBLoginOrder order) {
		if (loggedAccount == null) {
			Result<Account> query = parent.loginAccount(this, order.getName(),
															order.getPassword());
			if (query.successful()) {
				loggedAccount = query.getResult();
				sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.noCharLoaded));
			} else {
				sendErrorMessage(query.getErrorMessage());
			}
		} else {
			sendErrorMessage("Butler: Couldn't log in since account seems to be already logged in.");
		}
	}
	
	public void logoutAccount() {
		if (loggedAccount != null) {
			if (activePlayer != null) {
				unloadActivePlayer();
			}
			Result<Boolean> result = parent.logoutAccount(loggedAccount.getName());
			if (!result.successful()) {
				sendErrorMessage(result.getErrorMessage());
				return;
			}
			loggedAccount = null;
			sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.loggedOut));
		} else {
			sendErrorMessage("Butler: Couldn't log out since there's no account logged in!");
		}
	}
	
	public void createNewPlayer(CBCreatePlayerOrder order) {
		if (!assertLoggedAccount()) return;
		Result<Boolean> result = parent.createNewPlayer(loggedAccount,
									order.getPlayerName(), CharacterClass.arctos);
		if (!result.successful())
			sendErrorMessage(result.getErrorMessage());
	}
	
	public void moveChar(CBMoveOrder order) {
		if (!assertLoggedAccount()) return;
		if (!assertActivePlayer()) return;
		activeMapman.put(new BMMoveOrder(activePlayer, this, order));
	}
	
	/** friendly shutdown: Tells client that he's now disconnected */
	public void disconnect() {
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.disconnected));
		if (!stopRunning) shutdown();
	}
	
	public void forwardChatOrder(CBChatOrder order) {
		if (!assertActivePlayer()) return;
		activeMapman.put(new BMChatMessageOrder(this, order, activePlayer.getName()));
	}
	
	public void forwardEmoteOrder(CBTriggerEmoteOrder order) {
		if (!assertActivePlayer()) return;
		activeMapman.put(new BMTriggerEmoteOrder(this, activePlayer,
				order.getEmote()));
	}
	
	public Socket getClientSocket(){
		return this.clientSocket;
	}
		
	// job of clientThread
	private class ClientRunnable implements Runnable {
		private Butler myButler;
		public ClientRunnable(Butler parent){
			this.myButler = parent;
		}
		@Override
		public void run() {
			// Interpret client's requests:
			while(!stopRunning){
				Object read = null;
				try {
					read = clientInput.readObject();
				} catch(IOException e) {
					// Connection broken -> delete butler
					System.out.println("connection reset!!");
					if (!stopRunning) //nobody ordered a shutdown,
						shutdown();   //so we will cause that on ourselves
					return;
				} catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (read != null && read instanceof CBOrder){
					// a clientOrder has been read:
					myButler.handleClientOrder((CBOrder) read);
				}
			}
		}
	}
	
	protected void handleClientOrder(CBOrder read) {
		if (read instanceof CBDisconnectOrder){
			System.out.println("CBDisconnectOrder");
			disconnect();
		} else if (read instanceof CBCreatePlayerOrder) {
			System.out.println("CBCreatePlayerOrder");
			createNewPlayer((CBCreatePlayerOrder) read);
		} else if (read instanceof CBChatOrder){
			System.out.println("CBChatOrder");
			forwardChatOrder((CBChatOrder) read);
		} else if (read instanceof CBLoginOrder){
			System.out.println("CBLoginOrder");
			loginAccount((CBLoginOrder) read);
		} else if (read instanceof CBLogoutOrder){
			System.out.println("CBLogoutOrder");
			logoutAccount();
		} else if (read instanceof CBLoadPlayerOrder) {
			System.out.println("CBLoadPlayerOrder");
			loadActivePlayer((CBLoadPlayerOrder) read);
		} else if (read instanceof CBUnloadPlayerOrder) {
			System.out.println("CBUnloadPlayerOrder");
			unloadActivePlayer();
		} else if (read instanceof CBMoveOrder) {
			System.out.println("CBMoveOrder");
			moveChar((CBMoveOrder) read);
		} else if (read instanceof CBTriggerEmoteOrder) {
			System.out.println("CBTriggerEmoteOrder");
			forwardEmoteOrder((CBTriggerEmoteOrder) read);
		} else if (read instanceof CBServerSourceOrder) {
			System.out.println("CBServerSourceOrder");
			sendOrderToClient(new BCSystemMessageOrder(
				"Server source code at "+parent.getServerSettings().serverSourceAt()));
		} else if (read instanceof CBAccessInventoryOrder){
			//Strips order to convert to BIOrder
			InventoryType invType = ((CBAccessInventoryOrder) read).getInvType();
			String playerName =  ((CBAccessInventoryOrder) read).getPlayerName();
			String otherPlayerName = ((CBAccessInventoryOrder) read).getOtherPlayerName();
			int itemID = ((CBAccessInventoryOrder) read).getItemID();
			int qnt = ((CBAccessInventoryOrder) read).getQnt();
			BIAccessInventoryOrder newOrder = new BIAccessInventoryOrder(invType, playerName, otherPlayerName, itemID, qnt, this);
			sendOrderToInvMan(newOrder);
		}
	}
	
	// #################################################################################
	// Mapman -> Butler -> Client side:
	// #################################################################################	
	public void put(MBOrder order) {
		try {
			serverOrders.put(order);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't pass server order to the butler!");
		}
	}
	
	
	// job of serverThread
	private class ServerRunnable implements Runnable {
		private Butler myButler;
		public ServerRunnable(Butler parent) {
			this.myButler = parent;
		}
		@Override
		public void run() {
			// Handle queries lying in serverOrders
			MBOrder order;
			while (!stopRunning) {
				order = null;
				try {
					order = myButler.serverOrders.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (order != null)
					myButler.handleServerOrder(order);
			}
		}
	}
	
	protected void handleServerOrder(MBOrder order) {
		// Assert that given order comes from our active mapman!
		// the only exception is if it's set to null (needed for chat orders, f.ex.)
		if (order.getSource() != null && order.getSource() != this.activeMapman) {
			System.out.println("Butler: Received order from foreign mapman!");
			return;
		}
		if (order instanceof MBAddCharOrder) {
			clientAddChar((MBAddCharOrder) order);
		} else if (order instanceof MBChangeCharOrder) {
			clientChangeChar((MBChangeCharOrder) order);
		} else if (order instanceof MBRemoveCharOrder) {
			clientRemoveChar((MBRemoveCharOrder) order);
		} else if (order instanceof MBChatMessageOrder) {
			clientChatMessage((MBChatMessageOrder) order);
		} else if (order instanceof MBMapInfoOrder) {
			clientMapInfo((MBMapInfoOrder) order);
		} else if (order instanceof MBCharAtOtherMapmanOrder) {
			changeMapman((MBCharAtOtherMapmanOrder) order);
		} else if (order instanceof MBStopThreadOrder) {
			// If nobody else caused a shutdown, we will do
			if (!stopRunning) shutdown();
		} else if (order instanceof MBErrorMessageOrder) {
			sendErrorMessage(((MBErrorMessageOrder) order).getErrorMessage());
		}
		// TODO: Handle further serverside orders
	}
	
	/** locks clientOutput */
	private void sendOrderToClient(BCOrder order) {
		assert(clientOutput != null);
		synchronized(clientOutput) {
			if (order == null) {
				System.out.println("Order is null!");
			}
			try {
				clientOutput.writeObject(order);
			} catch (IOException e) {
				System.out.println("Butler: Couldn't pass order to client!");
			}
		}
	}
	
	private void sendOrderToInvMan(BIOrder order){
		invMan.orders.add(order);
	}
	
	private void clientChatMessage(MBChatMessageOrder order) {
		String toName = order.getToPlayername();
		boolean isBroadcast = (toName == null || toName.equals(""));
		sendOrderToClient(new BCChatMessageOrder(order.getMessage(),
													order.getFromPlayername(),
													isBroadcast));
	}
	
	private void clientAddChar(MBAddCharOrder order) {
		sendOrderToClient(new BCAddCharOrder(order));
	}
	
	private void clientChangeChar(MBChangeCharOrder order) {
		sendOrderToClient(new BCChangeCharOrder(order));
	}
	
	private void clientRemoveChar(MBRemoveCharOrder order) {
		sendOrderToClient(new BCRemoveCharOrder(order));
	}
	
	private void clientMapInfo(MBMapInfoOrder order) {
		sendOrderToClient(new BCSetMapOrder(order, activePlayer.getName()));
	}
	
	private void changeMapman(MBCharAtOtherMapmanOrder order) {
		MapManager oldMapman = order.getSource();
		assert(this.activeMapman == oldMapman);
		MapManager newMapman = order.getNewMapman();
		this.activeMapman = null;
		removePlayerFromMapman(oldMapman, true);
		addPlayerToMapman(newMapman, true);
		this.activeMapman = newMapman;
	}
	
	private void sendErrorMessage(String errorMessage) {
		sendOrderToClient(new BCErrorMessageOrder(errorMessage));
	}
	
	// #################################################################################
	// Invman -> Butler -> Client side:
	// #################################################################################	
	public void put(IBOrder order) {
		try {
			invmanOrders.put(order);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't pass server order to the butler!");
		}
	}
	
	
	// job of serverThread
	private class InvManRunnable implements Runnable {
		private Butler myButler;
		public InvManRunnable(Butler parent) {
			this.myButler = parent;
		}
		@Override
		public void run() {
			// Handle queries lying in serverOrders
			IBOrder order;
			while (!stopRunning) {
				order = null;
				try {
					order = myButler.invmanOrders.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (order != null)
					myButler.handleInvManOrder(order);
			}
		}
	}
	
	protected void handleInvManOrder(IBOrder order) {
		if(order instanceof IBSendInventoryOrder){
			//Strips IBSendInventoryOrder and rebuild as BCSendInventoryOrder
			HashMap<Integer, Integer> playerItems = ((IBSendInventoryOrder) order).getPlayerItems();
			BCSendInventoryOrder newOrder = new BCSendInventoryOrder(playerItems);
			sendOrderToClient(newOrder);
		}else if(order instanceof IBSendErrorMessageOrder){
			IBSendErrorMessageOrder newOrder = (IBSendErrorMessageOrder) order;
			sendErrorMessage(newOrder.getMessage());
		}
	}
}
