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
package serverSide;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.JFrame;

import serverSide.archivist.CoreArchivist;
import serverSide.archivist.fileSystemArchivist.FileSystemArchivist;
import serverSide.butler.Butler;
import serverSide.mapManager.MapManager;
import serverSide.mapmanToButlerOrders.MBPoisonPillOrder;
import serverSide.player.Player;

import common.Logger;
import common.Map;
import common.enums.CharacterClass;
import common.graphics.GraphicsContentManager;


/** The main program; Holds all references and manages file access. */
public class MainServer {
	private ServerSettings serverSettings;
	private Reception reception;
	private CoreArchivist archivist;
	
	private final List<Butler> butlers;	// To avoid deadlocks, these resources have to
									// be always locked from top to bottom
	private final HashMap<String, Butler> accnameToButler;
	private final HashMap<String, MapManager> mapnameToMapman;
	private final HashMap<String, Player> activePlayernameToPlayer;
	private final HashMap<String, Account> loggedAccnameToAccount;
	private GraphicsContentManager graphicsContentManager;
	private java.util.Map<String, Map> maps;
	
	public static void main(String[] args) {
		MainServer server = new MainServer();
		server.initAndRun();
	}
	
	public MainServer() {
		butlers = new ArrayList<Butler>();
		accnameToButler = new HashMap<String, Butler>();
		mapnameToMapman = new HashMap<String, MapManager>();
		activePlayernameToPlayer = new HashMap<String, Player>();
		loggedAccnameToAccount = new HashMap<String, Account>();
	}
	
	public void initAndRun() {
		Logger.log("Welcome to the Faunis server!");
		Logger.log("Copyright 2012, 2013 Simon Ley alias \"skarute\"");
		Logger.log("Licensed under GNU AGPL v3 or later");
		serverSettings = new ServerSettings();
		Logger.log("account storage in "+serverSettings.accountPath());
		// Check if serverSettings paths exist:
		serverSettings.checkPaths();
		archivist = new FileSystemArchivist(this);
		archivist.init();
		
		reception = new Reception(this, serverSettings.receptionPort());
		Logger.log("Reception has been created.");
		
		JFrame control = new JFrame("MainServer");
		control.setSize(200, 200);
		control.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		control.setVisible(true);
		
		// load content:
		graphicsContentManager = new GraphicsContentManager(
				serverSettings.playerGraphicsPath(),
				serverSettings.decoGraphicsPath(),
				serverSettings.graphicsPath(),
				serverSettings.imageFileEnding());
		graphicsContentManager.loadResourcesForServer();
		
		maps = archivist.mapArchivist().loadAllMaps(serverSettings.mapPath());
		synchronized(mapnameToMapman) {
			for (Entry<String, Map> mapEntry : maps.entrySet()) {
				MapManager mapman = new MapManager(this, mapEntry.getValue());
				mapman.init();
				mapnameToMapman.put(mapEntry.getKey(), mapman);
			}
		}
		//invMan = new InventoryManager(this);
		reception.startListening();
	}
	
	public ServerSettings getServerSettings() {
		return serverSettings;
	}
	
	public String getAccountPath(){
		return serverSettings.accountPath();
	}
	
	public GraphicsContentManager getGraphicsContentManager() {
		return graphicsContentManager;
	}
	
	
	public Result<Boolean> createNewPlayer(Account account, String playerName, CharacterClass type) {
		return archivist.accountsArchivist().createNewPlayer(account, playerName, type);
		
	}
	
	public Result<Player> loadAndActivatePlayer(Account account, String playerName) {
		Result<Player> result = archivist.accountsArchivist().loadAndActivatePlayer(account, playerName);
		if (result.successful()) {
			synchronized(activePlayernameToPlayer) {
				activePlayernameToPlayer.put(playerName, result.getResult());
			}
		}
		return result;
			
	}
	
	
	public Map getMap(String mapName) {
		return maps.get(mapName);
	}
	
	public Set<String> getMapNames() {
		return maps.keySet();
	}
	
	
	/** locks mapnameToMapman */
	public MapManager getMapman(String mapName) {
		synchronized(mapnameToMapman) {
			return mapnameToMapman.get(mapName);
		}
	}
	
	/** locks activePlayernameToPlayer<br/>
	 * returns the active Player instance with the given name */
	public Player getActivePlayerByName(String playerName) {
		synchronized(activePlayernameToPlayer) {
			return activePlayernameToPlayer.get(playerName);
		}
	}
	
	/** locks activePlayerNameToPlayer; accNameToButler */
	public Result<Butler> getButlerByPlayerName(String playerName) {
		Player player = getActivePlayerByName(playerName);
		if (player == null) {
			String error = "getButlerByPlayerName(): Player with given name isn't active!";
			return new Result<Butler>(null, error);
		}
		String accountName = player.getAccountName();
		synchronized(accnameToButler) {
			return new Result<Butler>(accnameToButler.get(accountName), null);
		}
	}
	
	/** locks activePlayerNameToPlayer; mapnameToMapman */
	public Result<MapManager> getMapmanByPlayerName(String playerName) {
		Player player = getActivePlayerByName(playerName);
		if (player == null) {
			String error = "getMapmanByPlayerName(): Player with given name isn't active!";
			return new Result<MapManager>(null, error);
		}
		String mapName = player.getMapName();
		synchronized(mapnameToMapman) {
			return new Result<MapManager>(mapnameToMapman.get(mapName), null);
		}
	}
	
	
	/** locks butlers<br/>
	 * Creates a butler who looks after the querying client at given socket.
	 * That doesn't mean that the client is logged in already! */
	public Butler createButler(Socket c){
		Butler b = new Butler(this, c);
		b.init();
		synchronized(butlers) {
			butlers.add(b);
		}
		Logger.log("new butler for client at port "+c.getPort());
		return b;
	}
	
	
	/** locks butlers */
	public void deleteButler(Butler b){
		synchronized(butlers) {
			assert(butlers.contains(b));
			butlers.remove(b);
		}
		Logger.log("Butler at port " + b.getClientSocket().getPort()+" destroyed!");
	}
	
	
	/** locks accnameToButler, loggedAccnameToAccount<br/>
	 * logs out the account with the given name
	 * Requirement: No more active players */
	public Result<Boolean> logoutAccount(String accountName){
		if (!loggedIn(accountName)){
			String error = "This account isn't even logged in!";
			return new Result<Boolean>(null, error);
		}
		// Check if there's still an active player:
		Account account;
		synchronized (loggedAccnameToAccount) {
			account = this.loggedAccnameToAccount.get(accountName);
		}
		Player activePlayer = account.getActivePlayer();
		if (activePlayer != null) {
			String error = "Can't log out since there's still an active player!";
			return new Result<Boolean>(null, error);
		}
		
		// log out:
		//saveAccount(acc); TODO
		synchronized(accnameToButler) {
			synchronized (loggedAccnameToAccount) {
				assert(loggedAccnameToAccount.containsKey(accountName));
				assert(accnameToButler.containsKey(accountName));
				loggedAccnameToAccount.remove(accountName);
				accnameToButler.remove(accountName);
			}
		}
		Logger.log("Account "+accountName+" successfully logged out.");
		return new Result<Boolean>(true, null);
	}
	
	
	/** locks loggedAccounts, accnameToButler */
	public Result<Account> loginAccount(Butler butler, String name, String password){
		// logs in the account with the given data
		if (!archivist.accountsArchivist().existAccount(name)){
			String error = "Login to non-existent account failed!";
			return new Result<Account>(null, error);
		} else if (loggedIn(name)){
			String error = "This account is already logged in!";// TODO: Privacy?!
			return new Result<Account>(null, error);
		} else {
			// log in:
			Result<Account> result = archivist.accountsArchivist().loadAccount(name, password);
			if (result.successful()) {
				Account account = result.getResult();
				synchronized(accnameToButler) {
					synchronized(loggedAccnameToAccount) {
						assert(!loggedAccnameToAccount.containsKey(name));
						assert(!accnameToButler.containsKey(name));
						loggedAccnameToAccount.put(name, account);
						accnameToButler.put(name, butler);
					}
				}
				Logger.log("Account "+name+" successfully logged in.");
				return new Result<Account>(account, null);
			} else {
				Logger.log("There were errors while logging in "+name+".");
				return new Result<Account>(null, result.getErrorMessage());
			}
		}
	}
	
	/** locks loggedAccounts<br/>
	 * Checks if an account of given name is already logged in. */
	private boolean loggedIn(String name){
		synchronized(loggedAccnameToAccount) {
			if (loggedAccnameToAccount.containsKey(name))
				return true;
			return false;
		}
	}
	
	
	/** locks activePlayernameToPlayer, player, loggedAccnameToAccount, account<br/>
	 * => to be called by butlers! <br/>
	 * Saves and unloads given player. <br/>
	 * NOTE: Requires that given player is not registered at any mapmans
	 * ( -> task of the butler to care about that)*/
	public void unloadPlayer(Player player) {
		String accountName;
		synchronized(activePlayernameToPlayer) {
			synchronized(player) {
				archivist.accountsArchivist().savePlayer(player);
				accountName = player.getAccountName();
				activePlayernameToPlayer.remove(player.getName());
			}
		}
		Account account;
		synchronized(loggedAccnameToAccount) {
			account = loggedAccnameToAccount.get(accountName);
		}
		synchronized(account) {
			account.setActivePlayer(null);
		}
	}
	
	/** NOTE: Doesn't wait until all threads have terminated. */
	public void shutdownAll() {
		reception.shutdown();
		synchronized(butlers) {
			for (Butler butler : butlers) {
				butler.put(new MBPoisonPillOrder(null));
			}
		}
	}
}
