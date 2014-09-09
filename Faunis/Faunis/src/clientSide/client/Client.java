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

import java.awt.Image;
import java.awt.Point;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import tools.ConcurrentDupSortedMap;
import listeners.StreamExceptionListener;
import mux.MuxObjectInputStream;
import clientSide.ClientSettings;
import clientSide.MessageType;
import clientSide.animation.Animator;
import clientSide.archivist.ClientSettingsArchivist;
import clientSide.graphics.MapDrawable;
import clientSide.graphics.ZOrderManager;
import clientSide.gui.GameWindow;
import clientSide.gui.SwingMessageRunnable;
import clientSide.player.ClientPlayer;
import clientSide.userToClientOrders.UCOrder;
import common.Logger;
import common.Map;
import common.butlerToClientOrders.*;
import common.clientToButlerOrders.*;
import common.enums.ClientStatus;
import common.graphics.graphicsContentManager.GraphicsContentManager;
import common.graphics.osseous.path.ClearPath;
import common.modules.ModuleOwner;
import common.movement.Mover;


public class Client implements ZOrderManager, ModuleOwner {
	final ClientReceiver receiverPart;
	final ClientSender senderPart;
	final ClientMoverModule moverModule;
	final ClientAnimatorModule animatorModule;
	final ClientPlayerModule playerModule;
	final ClientWorker worker;

	ObjectInputStream serverInput;
	ObjectOutputStream serverOutput;
	final ArrayBlockingQueue<UCOrder> userInput;

	final MuxObjectInputStream mux;

	final ClientSettingsArchivist clientSettingsArchivist;
	final ClientSettings clientSettings;

	private ClientStatus clientStatus;
	GraphicsContentManager graphicsContentManager;
	Socket socket;
	GameWindow win;
	String activePlayerName;
	Map currentMap;

	final ConcurrentHashMap<String, ClientPlayer> currentPlayers;
	final ConcurrentHashMap<ClientPlayer, Mover<ClientPlayer, Client>> movingPlayers;
	final ConcurrentHashMap<ClientPlayer, Animator<ClientPlayer>> animatedPlayers;
	final ConcurrentDupSortedMap<Float, MapDrawable> zOrderedDrawables;

	public static void main(String[] args){
		Client client = new Client();
		GameWindow window = new GameWindow(client, 640, 480, "Faunis");
		client.init(window);
	}

	private void run(){
		while(win != null){
			try {
				Thread.sleep(clientSettings.delayBetweenFrames());
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			win.draw();
			win.repaint();
		}
	}

	public void putUCOrder(UCOrder order) {
		try {
			userInput.put(order);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Client() {
		this.userInput = new ArrayBlockingQueue<UCOrder>(16);
		this.currentPlayers = new ConcurrentHashMap<String, ClientPlayer>();
		this.movingPlayers = new ConcurrentHashMap<ClientPlayer, Mover<ClientPlayer, Client>>();
		this.animatedPlayers = new ConcurrentHashMap<ClientPlayer, Animator<ClientPlayer>>();
		this.zOrderedDrawables = new ConcurrentDupSortedMap<Float, MapDrawable>();
		clientSettingsArchivist = new ClientSettingsArchivist();
		clientSettings = new ClientSettings(); // Settings that are read from hard disk are not
											   // yet initialised and therefore can't be read yet

		this.mux = new MuxObjectInputStream(
			"Client", 16, true, new ObjectInputStream[0], new Class[0],
			new BlockingQueue[] {this.userInput}, new Class[] {UCOrder.class}
		);
		mux.addStreamExceptionListener(new StreamExceptionListener() {
			@Override
			public void onEvent(
				MuxObjectInputStream _mux,
				ObjectInputStream stream, Exception exception
			) {
				Logger.log("Caught a stream exception: "+exception.toString());
				exception.printStackTrace();
				Client.this.senderPart.disconnect();
			}
		});

		receiverPart = new ClientReceiver();
		senderPart = new ClientSender();
		moverModule = new ClientMoverModule(movingPlayers);
		animatorModule = new ClientAnimatorModule(animatedPlayers);
		playerModule = new ClientPlayerModule(currentPlayers);
		worker = new ClientWorker(mux, "clientWorker");
	}

	public void init(GameWindow window) {
		receiverPart.init(this);
		senderPart.init(this);
		moverModule.init(this);
		animatorModule.init(this);
		playerModule.init(this);
		worker.init(this);

		// check if clientSettings paths exist:
		clientSettings.checkPaths();
		clientSettingsArchivist.readClientSettingsFromFile(clientSettings);

		// assign game window:
		win = window;
		
		// set initial clientStatus:
		clientStatus = ClientStatus.disconnected;
		setClientStatus(clientStatus);

		System.out.println(clientSettings.commandPrefix()+clientSettings.connectCommand());
		logSystemMessage("Welcome to Faunis!");
		logSystemMessage("Copyright 2012 - 2014 Simon Ley alias \"skarute\"");
		logSystemMessage("Licensed under GNU AGPL v3 or later");

		// if we have graphical output, set things up for it: 
		if (win != null) {
			graphicsContentManager = new GraphicsContentManager(clientSettings);
			graphicsContentManager.loadResourcesForClient();
			Image icon = null;
			try {
				icon = graphicsContentManager.guiGraphicsContentManager().image(new ClearPath("icon"));
			} catch (Exception e) {
			}
			if (icon != null) { win.setIcon(icon); }
			win.show();
		}
		worker.start();
		senderPart.connect();
		this.run();
	}

	public boolean hasGraphicalOutput() {
		return win != null;
	}

	public GameWindow getGameWindow() {
		return win;
	}

	public ClientPlayer getPlayers(String name) {
		return currentPlayers.get(name);
	}

	public Map getCurrentMap() {
		return currentMap;
	}

	public String getCurrentMapName() {
		if (currentMap != null) {
			return currentMap.getName();
		} else {
			return null;
		}
	}

	public String getCurrentPlayerName() {
		return activePlayerName;
	}

	public ClientSettings getClientSettings() {
		return clientSettings;
	}

	public GraphicsContentManager getGraphicsContentManager() {
		return graphicsContentManager;
	}

	public ClientStatus getClientStatus() {
		return clientStatus;
	}

	// ################################################################################
	// Client to server section: ######################################################
	// ################################################################################


	public void mouseClick(Point viewPoint) {
		Point field = clientSettings.pixelToMapField(viewPoint);
		senderPart.sendOrder(new CBMoveOrder(field.x, field.y));
	}


	// ###########################################################################################
	// Server to client section: #################################################################
	// ###########################################################################################

	public void showInventory(BCSendInventoryOrder order){
		HashMap<Integer, Integer> hashmap = order.getPlayerItems();
		if(hashmap.isEmpty()) {
			logSystemMessage("You don't have any items on your inventory");
		} else{
			//new InventoryWindow(win, hashmap);
		}
	}

	public void showChatMessage(BCChatMessageOrder order) {
		if (win == null) {
			return;
		}
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
		if (win == null) {
			return;
		}
		logErrorMessage(order.getMessage());
	}

	public void logErrorMessage(String errorMessage) {
		if (win == null) {
			return;
		}
		SwingUtilities.invokeLater(
			new SwingMessageRunnable("ERROR: "+errorMessage, MessageType.error, win));
	}

	public void logSystemMessage(BCSystemMessageOrder order) {
		if (win == null) {
			return;
		}
		logSystemMessage(order.getMessage());
	}

	public void logSystemMessage(String systemMessage) {
		if (win == null) {
			return;
		}
		SwingUtilities.invokeLater(
			new SwingMessageRunnable(systemMessage, MessageType.system, win));
	}


	// ################################################################################
	// ################################################################################
	// ################################################################################



	/** locks zOrderedDrawables */
	@Override
	public void notifyZOrderChange(MapDrawable drawable, float oldValue, float newValue) {
		Logger.log("notifyZOrderChange(): oldValue ="+oldValue+" newValue="+newValue);
			boolean success = zOrderedDrawables.remove(drawable, oldValue);
			assert(success);
			zOrderedDrawables.add(drawable, newValue);
	}

	/** locks zOrderedDrawables */
	public ArrayList<MapDrawable> getAllDrawablesToDraw() {
		ArrayList<MapDrawable> result = new ArrayList<MapDrawable>();
		if (zOrderedDrawables == null) {
			return result;
		}
		result = zOrderedDrawables.values();
		return result;
	}

	@Override
	public ConcurrentDupSortedMap<Float, MapDrawable> getZOrderedDrawables() {
		return zOrderedDrawables;
	}

	public void setClientStatus(ClientStatus newStatus) {
		clientStatus = newStatus;
		if (win != null) {
			win.notifyClientStatus(newStatus);
		}
	}

	public void shutdown() {
		senderPart.disconnect();
		System.exit(0);
	}

	public void logHelpInstructions() {
		if (win == null) {
			return;
		}
		ClientSettings s = clientSettings;
		String helpText =
			"   I n s t r u c t i o n s\n"+
			"Here are the available commands that you can type in the text box below:\n"+
			"Connect to server: "+s.commandPrefix()+s.connectCommand()+"\n"+
			"Disconnect from server: "+s.commandPrefix()+s.disconnectCommand()+"\n"+
			"Log in: "+s.commandPrefix()+s.loginCommand()+" username password\n"+
			"Log out: "+s.commandPrefix()+s.logoutCommand()+"\n"+
			"Get list of own players: "+s.commandPrefix()+s.queryOwnPlayersCommand()+"\n"+
			"Create new player: "+s.commandPrefix()+s.createPlayerCommand()+" playername\n"+
			"Load player: "+s.commandPrefix()+s.loadPlayerCommand()+" playername\n"+
			"Unload player: "+s.commandPrefix()+s.unloadPlayerCommand()+"\n"+
			"Get the server source (AGPL): "+s.commandPrefix()+s.serverSourceCommand()+"\n"+
			"Trigger animation: "+s.commandPrefix()+s.animationCommand()+" animationname\n"+
			"Move around: "+s.commandPrefix()+s.moveCommand()+" x y\n"+
			"Send private message (whisper): "+s.commandPrefix()+s.whisperCommand()+" playername message\n"+
			"Broadcast message: "+s.commandPrefix()+s.broadcastCommand()+" message";
		logSystemMessage(helpText);
	}
}
