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

import java.awt.Point;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import clientSide.ClientSettings;
import clientSide.DupSortedMap;
import clientSide.MessageType;
import clientSide.animation.Animator;
import clientSide.archivist.ClientSettingsArchivist;
import clientSide.graphics.Drawable;
import clientSide.graphics.ZOrderManager;
import clientSide.gui.GameWindow;
import clientSide.gui.GraphWin;
import clientSide.gui.SwingMessageRunnable;
import clientSide.player.PlayerGraphics;

import common.Logger;
import common.Map;
import common.Sync;
import common.butlerToClientOrders.*;
import common.clientToButlerOrders.*;
import common.enums.ClientStatus;
import common.graphics.GraphicsContentManager;
import common.modules.ModuleOwner;
import common.movement.Mover;

public class Client implements ZOrderManager, ModuleOwner {
	final ClientReceiver receiverPart;
	final ClientSender senderPart;
	final ClientMoverModule moverModule;
	final ClientAnimatorModule animatorModule;
	final ClientPlayerModule playerModule;
	
	final ClientSettingsArchivist clientSettingsArchivist;
	final ClientSettings clientSettings;
	
	final Object clientStatusMutexLock;
	private ClientStatus clientStatus;
	GraphicsContentManager graphicsContentManager;
	Socket socket;
	GameWindow win;
	String activePlayerName;
	Map currentMap;
	
	final HashMap<String, PlayerGraphics> currentPlayerGraphics;
	final HashMap<PlayerGraphics, Mover<PlayerGraphics, Client>> movingPlayerGraphics;
	final HashMap<PlayerGraphics, Animator<PlayerGraphics>> animatedPlayerGraphics;
	final DupSortedMap<Float, Drawable> zOrderedDrawables;
	final Sync sync;
	
	public static void main(String[] args){
		Client client = new Client();
		GameWindow window = new GameWindow(client, 800, 500, "Faunis");
		client.init(window);
	}
	
	private void run(){
		while(win != null){
			GraphWin.delay(clientSettings.delayBetweenFrames());
			win.draw();
			win.repaint();
		}
	}
	
	public Client() {
		clientStatusMutexLock = new Object();
		this.currentPlayerGraphics = new HashMap<String, PlayerGraphics>();
		this.movingPlayerGraphics = new HashMap<PlayerGraphics, Mover<PlayerGraphics, Client>>();
		this.animatedPlayerGraphics = new HashMap<PlayerGraphics, Animator<PlayerGraphics>>();
		this.zOrderedDrawables = new DupSortedMap<Float, Drawable>();
		this.sync = new Sync(new Object[] {currentPlayerGraphics, movingPlayerGraphics,
										   animatedPlayerGraphics, zOrderedDrawables});
		
		clientSettingsArchivist = new ClientSettingsArchivist();
		clientSettings = new ClientSettings();
		
		receiverPart = new ClientReceiver();
		senderPart = new ClientSender();
		moverModule = new ClientMoverModule(movingPlayerGraphics);
		animatorModule = new ClientAnimatorModule(animatedPlayerGraphics);
		playerModule = new ClientPlayerModule(currentPlayerGraphics);
	}
	
	public void init(GameWindow window) {
		receiverPart.init(this);
		senderPart.init(this);
		moverModule.init(this);
		animatorModule.init(this);
		playerModule.init(this);

		// check if clientSettings paths exist:
		String pathErrorMsg = clientSettings.checkPaths();
		if (pathErrorMsg != null)
			logErrorMessage(pathErrorMsg);
		clientSettingsArchivist.readClientSettingsFromFile(clientSettings);
		
		// assign game window:
		win = window;
		// set initial clientStatus:
		clientStatus = ClientStatus.disconnected;
		setClientStatus(clientStatus);
		
		if (win != null)
			win.show();
		
		System.out.println(clientSettings.commandPrefix()+clientSettings.connectCommand());
		logSystemMessage("Welcome to Faunis!");
		logSystemMessage("Copyright 2012, 2013 Simon Ley alias \"skarute\"");
		logSystemMessage("Licensed under GNU AGPL v3 or later");
		
		// load data:
		if (win != null) {
			graphicsContentManager = new GraphicsContentManager(
				clientSettings.playerGraphicsPath(),
				clientSettings.decoGraphicsPath(),
				clientSettings.graphicsPath(),
				clientSettings.imageFileEnding());
			graphicsContentManager.loadResourcesForClient();
		}
		
		senderPart.connect();
		this.run();
	}

	public boolean hasGraphicalOutput() {
		return win != null;
	}
	
	public GameWindow getGameWindow() {
		return win;
	}
	
	public PlayerGraphics getPlayerGraphics(String name) {
		synchronized(currentPlayerGraphics) {
			assert(currentPlayerGraphics.containsKey(name));
			return currentPlayerGraphics.get(name);
		}
	}
	
	public Map getCurrentMap() {
		return currentMap;
	}
	
	public String getCurrentMapName() {
		if (currentMap != null)
			return currentMap.getName();
		else
			return null;
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
	
	@Override
	public Sync sync() {
		return sync;
	}
	
	// ################################################################################
	// Client to server section: ######################################################
	// ################################################################################

	
	public void mouseClick(Point point) {
		Point field = clientSettings.pixelToMapField(point);
		senderPart.sendOrder(new CBMoveOrder(field.x, field.y));
	}

	public boolean parseCommand(String commandPrefix, String[] commandSplitDetails) {
		return senderPart.parseCommand(commandPrefix, commandSplitDetails);
	}
	
	
	// ###########################################################################################
	// Server to client section: #################################################################
	// ###########################################################################################
		
	public void showInventory(BCSendInventoryOrder order){
		HashMap<Integer, Integer> hashmap = order.getPlayerItems();
		if(hashmap.isEmpty()) logSystemMessage("You don't have any items on your inventory");
		else{
			//new InventoryWindow(win, hashmap);
		}
	}
	
	public void showChatMessage(BCChatMessageOrder order) {
		if (win == null)
			return;
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
		if (win == null)
			return;
		logErrorMessage(order.getMessage());
	}
	
	public void logErrorMessage(String errorMessage) {
		if (win == null)
			return;
		SwingUtilities.invokeLater(
			new SwingMessageRunnable("ERROR: "+errorMessage, MessageType.error, win));
	}
	
	public void logSystemMessage(BCSystemMessageOrder order) {
		if (win == null)
			return;
		logSystemMessage(order.getMessage());
	}
	
	public void logSystemMessage(String systemMessage) {
		if (win == null)
			return;
		SwingUtilities.invokeLater(
			new SwingMessageRunnable(systemMessage, MessageType.system, win));
	}
	
	
	// ################################################################################
	// ################################################################################
	// ################################################################################
	


	/** locks zOrderedDrawables */
	@Override
	public void notifyZOrderChange(Drawable drawable, float oldValue, float newValue) {
		Logger.log("notifyZOrderChange(): oldValue ="+oldValue+" newValue="+newValue);
		synchronized(zOrderedDrawables) {
			assert(zOrderedDrawables.contains(drawable, oldValue));
			zOrderedDrawables.remove(drawable, oldValue);
			zOrderedDrawables.add(drawable, newValue);
		}
	}
	
	/** locks zOrderedDrawables */
	public ArrayList<Drawable> getAllDrawablesToDraw() {
		ArrayList<Drawable> result = new ArrayList<Drawable>();
		if (zOrderedDrawables == null) return result;
		synchronized(zOrderedDrawables) {
			result = zOrderedDrawables.values();
		}
		return result;
	}

	@Override
	public DupSortedMap<Float, Drawable> getZOrderedDrawables() {
		return zOrderedDrawables;
	}
	
	/** locks clientStatusMutexLock */
	public void setClientStatus(ClientStatus newStatus) {
		synchronized(clientStatusMutexLock) {
			clientStatus = newStatus;
			if (win != null)
				win.notifyClientStatus(newStatus);
		}
	}
	
	/** locks connectionModiMutexKey, clientStatusMutexKey, currentPlayerGraphics, movingPlayerGraphics */
	public void shutdown() {
		senderPart.disconnect();
		System.exit(0);
	}
	
	public void logHelpInstructions() {
		if (win == null)
			return;
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
			"Trigger emote: "+s.commandPrefix()+s.emoteCommand()+" emotename\n"+
			"Move around: "+s.commandPrefix()+s.moveCommand()+" x y\n"+
			"Send private message (whisper): "+s.commandPrefix()+s.whisperCommand()+" playername message\n"+
			"Broadcast message: "+s.commandPrefix()+s.broadcastCommand()+" message";
		logSystemMessage(helpText);
	}
}
