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
package client;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import communication.GraphicalPlayerStatus;
import communication.GraphicsContentManager;
import communication.MapInfo;
import communication.butlerToClientOrders.*;
import communication.clientToButlerOrders.*;
import communication.enums.AniEndType;
import communication.enums.ClientStatus;
import communication.movement.Moveable;
import communication.movement.Mover;
import communication.movement.MoverManager;
import communication.movement.MovingTask;
import communication.movement.SoftMovingTask;


public class Client implements MoverManager, AnimatorManager {
	private Object connectionModiMutexKey;
	private boolean connectionModiOccupied;
	private ClientStatus clientStatus;
	private ClientSettings clientSettings;
	private GraphicsContentManager graphicsContentManager;
	private Socket socket;
	private ObjectOutputStream output;
	protected ObjectInputStream input;
	private GameWindow win;
	private String activePlayerName;
	private String currentMapName;
	private HashMap<String, PlayerGraphics> currentPlayers;
	private HashMap<PlayerGraphics, Mover> movingPlayerGraphics;
	private HashMap<PlayerGraphics, Animator> animatedPlayerGraphics;
	private Thread serverThread; // steadily replies to requests from the server side
	private ServerRunnable serverRunnable; // the serverThread's job
	protected boolean stopServerThread;
	public final static FileFilter directoryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	
	public static void main(String[] args){
		new Client().init();
	}
	
	private void run(){
		while(true){
			GraphWin.delay(clientSettings.delayBetweenFrames());
	
			win.draw();
			win.repaint();
		}
	}
	
	public void init() {
		// setup (dis-)connection management stuff:
		this.connectionModiMutexKey = new Object();
		this.connectionModiOccupied = false;
		this.stopServerThread = false;
		
		// set initial clientStatus:
		clientStatus = ClientStatus.disconnected;
		// create game window:
		/* NOTE: Swing is single-threaded. And as I have learned, it
		 * causes problems even when only one thread is accessing it at all.
		 * So we introduce the "good" / complicated way to deal with Swing
		 * stuff: Runnables for the Event Dispatching Thread (EDT).
		 */
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					win = new GameWindow(Client.this, 800, 500, "Faunis");
					win.show();
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logSystemMessage("Welcome to Faunis!");
		logSystemMessage("Copyright 2012 Simon Ley alias \"skarute\"");
		logSystemMessage("Licensed under GNU AGPL v3 or later");
		
		// load client settings:
		clientSettings = new ClientSettings();
		// check if clientSettings paths exist:
		String pathErrorMsg = clientSettings.checkPaths();
		if (pathErrorMsg != null)
			logErrorMessage(pathErrorMsg);
		
		// load data:
		graphicsContentManager = new GraphicsContentManager(
			clientSettings.playerGraphicsPath(), clientSettings.imageFileEnding());
		graphicsContentManager.loadResourcesForClient();
		
		currentPlayers = new HashMap<String, PlayerGraphics>();
		
		this.movingPlayerGraphics = new HashMap<PlayerGraphics, Mover>();
		this.animatedPlayerGraphics = new HashMap<PlayerGraphics, Animator>();
		
		this.serverRunnable = new ServerRunnable(this);

		
		connect();
		this.run();
	}

	
	public PlayerGraphics getPlayerGraphics(String name) {
		synchronized(currentPlayers) {
			assert(currentPlayers.containsKey(name));
			return currentPlayers.get(name);
		}
	}
	
	public String getCurrentMapName() {
		return currentMapName;
	}
	
	public String getCurrentPlayerName() {
		return activePlayerName;
	}
	
	public ClientSettings getClientSettings() {
		return clientSettings;
	}

	@Override
	public Object[] getSynchroStuffForMoverStop() {
		return new Object[] {movingPlayerGraphics, animatedPlayerGraphics};
	}
	
	@Override
	public Object getSynchroStuffForAnimatorStop() {
		return new Object[] {animatedPlayerGraphics};
	}
	
	public GraphicsContentManager getGraphicsContentManager() {
		return graphicsContentManager;
	}
	
	public ClientStatus getClientStatus() {
		return clientStatus;
	}
		
	/** locks connectionModiMutexKey; clientStatus */
	public boolean connect() {
		synchronized(connectionModiMutexKey) {
			if (connectionModiOccupied) {
				return false;
			} else {
				connectionModiOccupied = true;
			}
		}
		synchronized(clientStatus) {
			if (this.clientStatus != ClientStatus.disconnected) {
				logErrorMessage("Couldn't connect since clientStatus is not set to \"disconnected\"!");
				connectionModiOccupied = false;
				return false;
			}
			assert(socket == null || socket.isClosed());
			// create socket:
			try {
				this.socket = new Socket(clientSettings.host(), clientSettings.port());
			} catch (Exception e) {
				logErrorMessage("Socket to game server couldn't be created! Reason: "+e.getLocalizedMessage());
				connectionModiOccupied = false;
				return false;
			}
			System.out.println("Socket created.");
			
			// create input and output stream:
			System.out.println("Try to create input / output streams...");
			try {
				this.input = new ObjectInputStream(this.socket.getInputStream());
				this.output = new ObjectOutputStream(this.socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				logErrorMessage("Couldn't create input / output!");
				connectionModiOccupied = false;
				return false;
			}
			clientStatus = ClientStatus.loggedOut;
			System.out.println("Input / output have been created.");
			System.out.println("Start serverThread...");
			stopServerThread = false;
			assert(serverThread == null || !serverThread.isAlive());
			this.serverThread = new Thread(this.serverRunnable, "client_serverThread");
			this.serverThread.start();
			connectionModiOccupied = false;
			return true;
		}
	}
	
	/** locks connectionModiMutexKey; clientStatus, currentPlayers, movingPlayerGraphics */ 
	public boolean disconnect() {
		System.out.println("Client: called disconnect()");
		synchronized(connectionModiMutexKey) {
			if (connectionModiOccupied) {
				System.out.println("Client: disconnect(): Connection is already being modified?");
				return false;
			} else {
				connectionModiOccupied = true;
			}
		}
		synchronized(clientStatus) {
			if (this.clientStatus == ClientStatus.disconnected) {
				logErrorMessage("Client: Couldn't disconnect as clientStatus is already \"disconnected\"");
				connectionModiOccupied = false;
				return false;
			}
			// Cause serverThread to terminate
			assert(serverThread.isAlive());
			stopServerThread = true;
			
			try {
				this.input.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Couldn't choke serverThread!");
			}
			if (Thread.currentThread() != this.serverThread) {
				System.out.println("disconnect(): Caller isn't serverThread");
				while (this.serverThread.isAlive()) {}
				assert(!serverThread.isAlive());
				System.out.println("Client: serverThread terminated.");
			} else {
				System.out.println("disconnect(): Caller is serverThread");
			}
			
			stopServerThread = false;
			unloadMap();
			
			clientStatus = ClientStatus.disconnected;
			input = null;
			output = null;
			serverThread = null;
			connectionModiOccupied = false;
			System.out.println("Client: Connection to server terminated.");
			return true;
		}
	}
	
	
	
	// Server to client section: #################################################################
	
	class ServerRunnable implements Runnable {
		private Client parent;
		
		public ServerRunnable(Client parent) {
			this.parent = parent;
		}
		
		@Override
		public void run() {
			// handle Order from parent.input:
			while (!stopServerThread) {
				Object read = null;
				try {
					read = parent.input.readObject();
				} catch (IOException e) {
					logErrorMessage("Couldn't read anymore from server!");
					if (!stopServerThread) disconnect();
					System.out.println("serverRunnable ends.");
					return;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					System.out.println("ClassNotFoundException!");
				}
				if (read != null && read instanceof BCOrder)
					parent.handleServerOrder((BCOrder)read);
			}
			System.out.println("serverRunnable ends.");
		}
	}
	
	/** locks movingPlayerGraphics, animatedPlayerGraphics, playerGraphics */
	private void tryStopMovement(PlayerGraphics playerGraphics) {
		synchronized(movingPlayerGraphics) {
			synchronized(animatedPlayerGraphics) {
				synchronized(playerGraphics) {
					Mover mover = movingPlayerGraphics.get(playerGraphics);
					if (mover != null) {
						mover.stop();
						unregisterMover(playerGraphics);
						// TODO: I think we can sum the above to mover.stopAndUnregister();
						System.out.println("Movement stopped.");
					}
				}
			}
		}
	}
	
	/** locks animatedPlayerGraphics, playerGraphics <br/>
	 * Stops animation, removes emote from playerGraphics and resets frame counter. */
	private void tryStopAnimation(PlayerGraphics playerGraphics) {
		synchronized(animatedPlayerGraphics) {
			synchronized(playerGraphics) {
				Animator animator = animatedPlayerGraphics.get(playerGraphics);
				if (animator != null) {
					animator.stop();
					unregisterAnimator(playerGraphics);
					// TODO: I think we can sum the above to animator.stopAndUnregister();
					System.out.println("Animation stopped.");
				}
			}
		}
	}
	
	public void handleServerOrder(BCOrder order) {
		assert(order != null);
		if (order instanceof BCAddCharOrder) {
			addChar((BCAddCharOrder) order);
		} else if (order instanceof BCChangeCharOrder) {
			changeChar((BCChangeCharOrder) order);
		} else if (order instanceof BCRemoveCharOrder) {
			removeChar((BCRemoveCharOrder) order);
		} else if (order instanceof BCSetMapOrder) {
			setMap((BCSetMapOrder) order);
		} else if (order instanceof BCSetClientStatusOrder) {
			setClientStatus((BCSetClientStatusOrder) order);
		} else if (order instanceof BCErrorMessageOrder) {
			logErrorMessage((BCErrorMessageOrder) order);
		} else if (order instanceof BCSystemMessageOrder) {
			logSystemMessage((BCSystemMessageOrder) order);
		} else if (order instanceof BCChatMessageOrder) {
			showChatMessage((BCChatMessageOrder) order);
		} else {
			System.out.println("Received unknown server order!");
		}
		// TODO: Implement the handling of further server orders
	}
	
	public void showChatMessage(BCChatMessageOrder order) {
		if (order.isBroadcast()) {
			SwingUtilities.invokeLater(
				new SwingMessageRunnable(order.getFromName()+": "
							+order.getMessage(), MessageType.broadcast, win));
		} else {
			SwingUtilities.invokeLater(
					new SwingMessageRunnable(order.getFromName()+": "
								+order.getMessage(), MessageType.whisper, win));
		}
	}
	
	public void logErrorMessage(BCErrorMessageOrder order) {
		logErrorMessage(order.getMessage());
	}
	
	public void logErrorMessage(String errorMessage) {
		SwingUtilities.invokeLater(
			new SwingMessageRunnable("ERROR: "+errorMessage, MessageType.error, win));
	}
	
	public void logSystemMessage(BCSystemMessageOrder order) {
		logSystemMessage(order.getMessage());
	}
	
	public void logSystemMessage(String systemMessage) {
		SwingUtilities.invokeLater(
			new SwingMessageRunnable(systemMessage, MessageType.system, win));
	}
	
	/** locks currentPlayers, movingPlayerGraphics, playerGraphics */
	public void addChar(BCAddCharOrder order) {
		GraphicalPlayerStatus status = order.getGraphStatus();
		PlayerGraphics playerGraphics = new PlayerGraphics(status, this);
		String playerName = order.getPlayerName();
		
		// Add playerGraphics:
		synchronized(currentPlayers) {
			assert(! currentPlayers.containsKey(playerName));
			currentPlayers.put(playerName, playerGraphics);
		}
		// If the new playerGraphics is moving, create Mover and add it:
		if (playerGraphics.hasPath()) {
			tryStartMovement(playerGraphics);
		} else if (playerGraphics.hasEmote()) {
			tryStartAnimation(playerGraphics, playerGraphics.getEmote());
		}
	}
	
	/** locks currentPlayers; movingPlayerGraphics; currentPlayers */
	public void removeChar(BCRemoveCharOrder order) {
		String playerName = order.getPlayerName();
		PlayerGraphics playerGraphics;
		synchronized(currentPlayers) {
			assert(currentPlayers.containsKey(playerName));
			playerGraphics = currentPlayers.get(playerName);
		}
		// Stop possible earlier animation and movement:
		tryStopMovement(playerGraphics);
		tryStopAnimation(playerGraphics);
		
		synchronized(currentPlayers) {
			assert(currentPlayers.containsKey(playerName));
			currentPlayers.remove(playerName);
		}
	}
	
	/** locks currentPlayers; movingPlayerGraphics; currentPlayers */
	public void changeChar(BCChangeCharOrder order) {
		GraphicalPlayerStatus status = order.getGraphStatus();
		PlayerGraphics playerGraphics = new PlayerGraphics(status, this);
		String playerName = order.getPlayerName();
		PlayerGraphics oldGraphics = null;
		synchronized(currentPlayers) {
			assert(currentPlayers.containsKey(playerName));
			oldGraphics = currentPlayers.get(playerName);
		}
		System.out.println("old x="+oldGraphics.getX()+", y="+oldGraphics.getY());
		System.out.println("new x="+playerGraphics.getX()+", y="+playerGraphics.getY());
		// If playerGraphics was moving before, remove Mover:
		tryStopMovement(oldGraphics);
		tryStopAnimation(oldGraphics);
		// see NOTE above
		
		// Replace playerGraphics
		synchronized(currentPlayers) {
			assert(currentPlayers.containsKey(playerName));
			currentPlayers.put(playerName, playerGraphics);
		}
		// If the new playerGraphics is moving, create Mover and add it:
		if (playerGraphics.hasPath()) {
			tryStartMovement(playerGraphics);
		} else if (playerGraphics.hasEmote()) {
			tryStartAnimation(playerGraphics, playerGraphics.getEmote());
		}
	}

	/** locks movingPlayerGraphics, animatedPlayerGraphics, playerGraphics<br/>
	 * Also starts walking animation. */
	public void tryStartMovement(PlayerGraphics playerGraphics) {
		synchronized(movingPlayerGraphics) {
			synchronized(animatedPlayerGraphics) {
				synchronized(playerGraphics) {
					// TODO: assert that there doesn't already exist a Mover!
					if (movingPlayerGraphics.containsKey(playerGraphics)) {
						System.out.println("There already exists a Mover!");
						return;
					}
					if (playerGraphics.hasPath() && !playerGraphics.getPath().isEmpty()) {
						System.out.print("Start movement...");
						int numDeltaLevels = clientSettings.numberOfDeltaLevelStates();
						Mover mover = new Mover(this, playerGraphics, 500/numDeltaLevels);//TODO: Zeiteinheit
						MovingTask movingTask = new SoftMovingTask(mover, playerGraphics);
						mover.setMovingTask(movingTask);
						movingPlayerGraphics.put(playerGraphics, mover);
						mover.start();
						System.out.println(" movement started. Start animation too...");
						tryStartAnimation(playerGraphics, "walk");
					}
				}
			}
		}
	}
	
	/** locks animatedPlayerGraphics, playerGraphics<br/>
	 * Starts the given animation. */
	public void tryStartAnimation(PlayerGraphics playerGraphics, String animation) {
		synchronized(animatedPlayerGraphics) {
			synchronized(playerGraphics) {
				// assert that there doesn't already exist an Animator!
				if (animatedPlayerGraphics.containsKey(playerGraphics)) {
					System.out.println("There already exists a Animator!");
					return;
				}
				System.out.print("Start animation...");
				AnimationData animationData = graphicsContentManager.getAnimationData(playerGraphics.getType(),
						animation);
				AniEndType endType = animationData.endType;
				int maxFrameIndex = animationData.numberOfFrames-1;
				// TODO: Get animation interval
				Animator animator = new Animator(this, playerGraphics, 100, endType, maxFrameIndex);
				animatedPlayerGraphics.put(playerGraphics, animator);
				animator.start();
				System.out.println(" animation started.");
			}
		}
	}
	
	/** locks currentPlayers, movingPlayerGraphics */
	private void unloadMap() {
		synchronized(currentPlayers) {
			synchronized(movingPlayerGraphics) {
				for (String playerName : currentPlayers.keySet()) {
					PlayerGraphics playerGraphics = currentPlayers.get(playerName);
					tryStopMovement(playerGraphics);
				}
				currentPlayers.clear();
				activePlayerName = null;
				currentMapName = null;
			}
		}
	}
	
	/** locks currentPlayers, movingPlayerGraphics<br/>
	 * A new map will be loaded: Remove all movements and
	 * playerGraphics and register them anew from the MapInfo */
	public void setMap(BCSetMapOrder order) {
		MapInfo mapInfo = order.getMapInfo();
		synchronized(currentPlayers) {
			synchronized(movingPlayerGraphics) {
				unloadMap();
				// everything is unloaded, now load:
				currentMapName = mapInfo.mapName;
				activePlayerName = order.getActivePlayerName();
				for (String playerName2 : mapInfo.players.keySet()) {
					GraphicalPlayerStatus status = mapInfo.players.get(playerName2);
					PlayerGraphics playerGraphics = new PlayerGraphics(status, this);
					currentPlayers.put(playerName2, playerGraphics);
					tryStartMovement(playerGraphics);
				}
			}
		}
	}
	
	/** locks clientStatus, currentPlayers, movingPlayerGraphics */
	public void setClientStatus(BCSetClientStatusOrder order) {
		ClientStatus newStatus = order.getNewStatus();
		if (newStatus == ClientStatus.disconnected) {
			// Don't call disconnect() here as the serverRunnable cannot
			// force its termination at this point. Instead
			// we'll just close the input, causing this serverRunnable to
			// call disconnect() in its main loop where it can terminate.
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Couldn't choke serverThread!");
			}
			return;
		}

		synchronized(clientStatus) {
			if ((  clientStatus == ClientStatus.exploring
				|| clientStatus == ClientStatus.fighting)
				&& newStatus != ClientStatus.exploring
				&& newStatus != ClientStatus.fighting)
			{
				unloadMap();
			}
			this.clientStatus = newStatus;
		}
	}
	
	// ################################################################################
	
	/** locks clientStatus, output<br/>
	 *  Tries to send given ClientOrder to the own Butler, and returns 
	 *  the success thereof as a boolean.*/
	public boolean sendOrder(CBOrder c){
		synchronized(clientStatus) {
			if (clientStatus == ClientStatus.disconnected) {
				logErrorMessage("Client: Couldn't send order since there's no connection!");
				return false;
			}
			assert(output != null);
			synchronized(output) {
				try {
					output.writeObject(c);
				} catch (IOException e) {
					logErrorMessage("Error while sending ClientOrder");
					return false;
				}
				return true;
			}
		}
	}


	public boolean parseCommand(String commandPrefix, String[] commandSplitDetails) {
		if (commandPrefix.equals("/c")) {
			connect();
			return true;
		} else if (commandPrefix.equals("/i") && commandSplitDetails.length >= 2) {
			String loginName = commandSplitDetails[0];
			String loginPassword = commandSplitDetails[1];
			sendOrder(new CBLoginOrder(loginName, loginPassword));
			return true;
		} else if (commandPrefix.equals("/o")) {
			sendOrder(new CBLogoutOrder());
			return true;
		} else if (commandPrefix.equals("/x")) {
			sendOrder(new CBDisconnectOrder());
			return true;
		} else if (commandPrefix.equals("/w") && commandSplitDetails.length >= 2) {
			String receiver = commandSplitDetails[0];
			String message = concatenateHelper(commandSplitDetails, 1);
			sendOrder(new CBChatOrder(message, receiver));
			return true;
		} else if (commandPrefix.equals("/b") && commandSplitDetails.length >= 1) {
			String message = concatenateHelper(commandSplitDetails, 0);
			sendOrder(new CBChatOrder(message, null));
			return true;
		} else if (commandPrefix.equals("/l")) {
			String playerName = commandSplitDetails[0];
			sendOrder(new CBLoadPlayerOrder(playerName));
			return true;
		} else if (commandPrefix.equals("/u")) {
			sendOrder(new CBUnloadPlayerOrder());
			return true;
		} else if (commandPrefix.equals("/n") && commandSplitDetails.length >= 1) {
			String playerName = commandSplitDetails[0];
			sendOrder(new CBCreatePlayerOrder(playerName));
			return true;
		} else if (commandPrefix.equals("/m") && commandSplitDetails.length >= 2) {
			int walkX, walkY;
			try {
				walkX = Integer.parseInt(commandSplitDetails[0]);
				walkY = Integer.parseInt(commandSplitDetails[1]);
			} catch(NumberFormatException e) {
				logErrorMessage("Error while parsing the numbers.");
				return false;
			}
			sendOrder(new CBMoveOrder(walkX, walkY));
			return true;
		} else if (commandPrefix.equals("/e")) {
			String emoteName = null;
			if (commandSplitDetails.length >= 1) {
				emoteName = commandSplitDetails[0];
			}
			sendOrder(new CBTriggerEmoteOrder(emoteName));
			return true;
		} else if (commandPrefix.equals("/s")) {
			sendOrder(new CBServerSourceOrder());
			return true;
		} else {
			logErrorMessage("Command couldn't be interpreted.");
			return false;
		}
		// TODO: handle further commands
	}

	/** locks movingPlayerGraphics, animatedPlayerGraphics <br/>
	 * Unregisters the Mover for given playerGraphics after movement has stopped.
	 * Also unregisters the Animator. <br/>
	 * Do not call this to stop movement, but call tryStopMovement() instead! */
	@Override
	public void unregisterMover(Moveable moveable) {
		System.out.println("Unregistering Mover...");
		assert(moveable instanceof PlayerGraphics);
		PlayerGraphics playerGraphics = (PlayerGraphics) moveable;
		synchronized(movingPlayerGraphics) {
			tryStopAnimation(playerGraphics);
			movingPlayerGraphics.remove(playerGraphics);
		}
	}
	
	/** locks animatedPlayerGraphics <br/>
	 * Unregisters the Animator for given playerGraphics after animation has
	 * stopped.<br/> Do not call this to stop animation, but call tryStopAnimation() instead! */
	@Override
	public void unregisterAnimator(Animateable animateable) {
		assert(animateable instanceof PlayerGraphics);
		PlayerGraphics playerGraphics = (PlayerGraphics) animateable;
		synchronized(animatedPlayerGraphics) {
			animatedPlayerGraphics.remove(playerGraphics);
		}
	}
	
	/** locks currentPlayers */
	public ArrayList<PlayerGraphics> getAllGraphicsToDraw() {
		ArrayList<PlayerGraphics> result = new ArrayList<PlayerGraphics>();
		if (currentPlayers == null) return result;
		synchronized(currentPlayers) {
			for (PlayerGraphics playerGraphics : currentPlayers.values()) {
				result.add(playerGraphics);
			}
		}
		return result;
	}
	
	private static String concatenateHelper(String[] array, int startIndex) {
		assert(startIndex < array.length);
		StringBuilder message = new StringBuilder();
		message.append(array[startIndex]);
		for (int i = startIndex+1; i < array.length; i++) {
			message.append(" ");
			message.append(array[i]);
		}
		return message.toString();
	}
}
