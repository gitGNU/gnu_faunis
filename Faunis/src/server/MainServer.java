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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;

import server.mapmanToButlerOrders.MBStopThreadOrder;

import communication.GraphicsContentManager;
import communication.Map;
import communication.enums.CharacterClass;
import communication.movement.Path;


/** The main program; Holds all references and manages file access. */
public class MainServer {
	private ServerSettings serverSettings;
	private Reception reception;
	private List<Butler> butlers;	// To avoid deadlocks, these resources have to
									// be always locked from top to bottom
	private HashMap<String, Butler> accnameToButler;
	private HashMap<String, MapManager> mapnameToMapman;
	private HashMap<String, Player> activePlayernameToPlayer;
	private HashMap<String, Account> loggedAccnameToAccount;
	private GraphicsContentManager graphicsContentManager;
	private HashSet<String> allExistingPlayerNames;
	public final static FileFilter directoryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	
	public static void main(String[] args) {
		MainServer server = new MainServer();
		server.run();
	}
	
	public void run() {
		System.out.println("Welcome to the Faunis server!");
		System.out.println("Copyright 2012 Simon Ley alias \"skarute\"");
		System.out.println("Licensed under GNU AGPL v3 or later");
		serverSettings = new ServerSettings();
		butlers = new ArrayList<Butler>();
		accnameToButler = new HashMap<String, Butler>();
		mapnameToMapman = new HashMap<String, MapManager>();
		activePlayernameToPlayer = new HashMap<String, Player>();
		loggedAccnameToAccount = new HashMap<String, Account>();
		System.out.println("account storage in "+serverSettings.accountPath());
		// Check if serverSettings paths exist:
		serverSettings.checkPaths();
		
		allExistingPlayerNames = loadAllExistingPlayerNames();
		reception = new Reception(this, serverSettings.receptionPort());

		System.out.println("Reception has been created.");
		
		JFrame control = new JFrame("MainServer");
		control.setSize(200, 200);
		control.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		control.setVisible(true);
		
		// load content:
		graphicsContentManager = new GraphicsContentManager(
				serverSettings.playerGraphicsPath(), serverSettings.imageFileEnding());
		graphicsContentManager.loadResourcesForServer();
		// TODO
		
		// DEBUG - load testing content:
		String starterRegion = serverSettings.starterRegion();
		Map starterMap = new Map(starterRegion, 50, 50);
		MapManager starterMapman = new MapManager(this, starterMap);
		synchronized(mapnameToMapman) {
			this.mapnameToMapman.put(starterRegion, starterMapman);
		}
	}
	
	public ServerSettings getServerSettings() {
		return serverSettings;
	}
	
	public GraphicsContentManager getGraphicsContentManager() {
		return graphicsContentManager;
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
		synchronized(butlers) {
			butlers.add(b);
		}
		System.out.println("new butler for client at port "+c.getPort());
		return b;
	}
	
	
	/** locks butlers */
	public void deleteButler(Butler b){
		synchronized(butlers) {
			assert(butlers.contains(b));
			butlers.remove(b);
		}
		System.out.println("Butler at port " + b.getClientSocket().getPort()+" destroyed!");
	}
	
	
	/** Creates a new account, but doesn't load it. */
	public Result<Boolean> createAccount(String name, String password){
		// TODO: Synchronise on file system? Or else two accounts could be
		// created under the same name, for example!
		// Check if an account already exists under given name:
		if (!existAccountSubdir(name)){
			// account doesn't yet exist, create it (without players)
			Account acc = new Account(name, password, new ArrayList<String>());
			boolean createAccSuccess=createAccountSubdir(acc);
			if (createAccSuccess){
				System.out.println("Account "+name+" successfully created.");
				return new Result<Boolean>(true, null);
			}
			else{
				String error = "Account couldn't be created!?";
				return new Result<Boolean>(null, error);
			}
		} else {
			String error = "Account name already exists!";
			return new Result<Boolean>(null, error);
		}
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
		System.out.println("Account "+accountName+" successfully logged out.");
		return new Result<Boolean>(true, null);
	}
	
	
	/** locks loggedAccounts, accnameToButler */
	public Result<Account> loginAccount(Butler butler, String name, String password){
		// logs in the account with the given data
		if (!existAccountSubdir(name)){
			String error = "Login to non-existent account failed!";
			return new Result<Account>(null, error);
		} else if (loggedIn(name)){
			String error = "This account is already logged in!";// TODO: Privacy?!
			return new Result<Account>(null, error);
		} else {
			// log in:
			Result<Account> result = loadAccount(name, password);
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
				System.out.println("Account "+name+" successfully logged in.");
				return new Result<Account>(account, null);
			} else {
				System.out.println("There were errors while logging in "+name+".");
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
	
	
	/** checks if a directory for given account name exists */
	private boolean existAccountSubdir(String accountName){
		File accountSubdir = new File(serverSettings.accountPath()+accountName+"/");
		System.out.println("Check if "+accountSubdir.getPath()+" exists...");
		return (accountSubdir.exists() && accountSubdir.isDirectory());
	}
	
	
	/** Pure function. <br/>
	 * Loads an account from hard disk if exists and given login data is valid. 
	 * Additionally returns the loaded account if successful. */
	private Result<Account> loadAccount(String name, String password){
		if (!existAccountSubdir(name)){
			String error = "Account "+name+" couldn't be loaded since it does not exist!";
			return new Result<Account>(null, error);
		}
		String accountPath = serverSettings.accountPath();
		// read account.txt:
		File accFile = new File(accountPath+name+"/account.txt");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(accFile)));
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't find account.txt for "+name);
			e.printStackTrace();
			return new Result<Account>(null, "Error while reading account data!");
		}
		String readPassword;
		try {
			readPassword = reader.readLine();
			reader.close();
		} catch (IOException e) {
			System.out.println("Error while reading account data!");
			e.printStackTrace();
			return new Result<Account>(null, "Error while reading account data!");
		}
		// compare passwords
		if (!password.equals(readPassword)){
			System.out.println("Password invalid, since "+password+" != "+readPassword);
			return new Result<Account>(null, "Invalid login data!");
		}
		/*
		  Login data valid, read further data:
		  Read player names
		  NOTE: The serialised player objects of the files in the directory
		  "players" will only be loaded when a player is activated;
		  Here we will just collect the player names. 
		 */
		File accPlayerDir = new File(accountPath+name+"/players/");
		File[] accPlayers = accPlayerDir.listFiles();
		ArrayList<String> playerNames = new ArrayList<String>();
		for (File p : accPlayers){
			playerNames.add(p.getName());
		}
		// Finally create the account object:
		Account acc = new Account(name, password, playerNames);
		System.out.println("Account "+name+" successfully loaded.");
		return new Result<Account>(acc, null);
	}
	
	/** locks allExistingPlayerNames, account, player <br/>
	 * Creates a new player on hard disk. You then still have to load it
	 * by calling loadAndActivatePlayer().*/
	public Result<Boolean> createNewPlayer(Account account, String playerName, CharacterClass type) {
		String starterRegion = serverSettings.starterRegion();

		synchronized(allExistingPlayerNames) {
			synchronized(account) {
				Player player;
				String accountName = account.getName();
				int maxPlayers = serverSettings.maxPlayersPerAccount();
				ArrayList<String> playerNames = account.getPlayerNames();
				if (playerNames.size() >= maxPlayers) {
					String error = "You cannot exceed the limit of "+maxPlayers+" players per account!";
					return new Result<Boolean>(null, error);
				}
				if (allExistingPlayerNames.contains(playerName)) {
					String error = "A player with the name "+playerName+" already exists!";
					return new Result<Boolean>(null, error);
				}
				player = new Player(playerName, type, starterRegion, accountName);
				boolean success = savePlayer(player);
				if (success) {
					playerNames.add(playerName);
					allExistingPlayerNames.add(playerName);
					return new Result<Boolean>(true, null);
				} else {
					String error = "The player could not be created!";
					return new Result<Boolean>(null, error);
				}
			}
		}
	}
	
	/** locks player<br/>
	 * Saves the current state of given player. */
	public boolean savePlayer(Player player) {
		synchronized(player) {
			Path oldPath = player.getPath();
			if (oldPath != null) System.out.println("WARNING: MainServer.savePlayer(): Player was still moving!");
			// It's bad if there's still a path referenced in the given player object:
			// On no account may it be serialised with the player
			player.resetPath();
			player.resetEmote();
			String accountName = player.getAccountName();
			String playerName = player.getName();
			assert(existAccountSubdir(accountName));
			String playerDirString = playerDirString(accountName, playerName);
			if (!existPlayerDir(accountName, playerName))
				createPlayerDir(accountName, playerName);
			File playerFile = new File(playerDirString+"/"+playerName);
			FileOutputStream fos;
			ObjectOutputStream oos;
			try {
				fos = new FileOutputStream(playerFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(player);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
				player.setPath(oldPath);
				return false;
			}
			player.setPath(oldPath);
		}
		System.out.println("MainServer: Player successfully saved.");
		return true;
	}
	
	/** locks account, activePlayernameToPlayer<br/>
	 * Loads a player object from disk and returns it, or returns null if it failed.
	 * => To be called by butlers!<br/>
	 * NOTE: The butler will yet have to assign the player to a mapman
	*/
	public Result<Player> loadAndActivatePlayer(Account account, String playerName) {
		if (account.getActivePlayer() != null) {
			String error = "Couldn't load "+playerName+" since there's still another player active!";
			return new Result<Player>(null, error);
		}
		if (!account.getPlayerNames().contains(playerName)) {
			String error = "Couldn't load "+playerName+" since a player of that name doesn't exist!";
			return new Result<Player>(null, error);
		}
		assert(this.existPlayerDir(account.getName(), playerName));
		FileInputStream fis;
		ObjectInputStream ois;
		
		Player player = null;
		try {
			fis = new FileInputStream(playerDirString(account.getName(), playerName)+"/"+playerName);
			ois = new ObjectInputStream(fis);
			player = (Player) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		assert(player != null);
		synchronized(account) {
			account.setActivePlayer(player);
		}
		synchronized(activePlayernameToPlayer) {
			activePlayernameToPlayer.put(playerName, player);
		}
		return new Result<Player>(player, null);
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
				this.savePlayer(player);
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
	
	/** Creates the directory structure in the account directory for given account.
	  * Returns true if successful, else false. */
	private boolean createAccountSubdir(Account newAccount){
		String accountPath = serverSettings.accountPath();
		String newAccountName = newAccount.getName();
		String newAccountPassword = newAccount.getPassword();
		File newAccountDir = new File(accountPath+newAccountName+"/");
		File newAccountPlayersDir = new File(accountPath+newAccountName+"/players");
		File newAccountFile = new File(accountPath+newAccountName+"/account.txt");
		
		boolean check = false;
		check = newAccountDir.mkdir();
		check = check && newAccountPlayersDir.mkdir();
		if (!check) return false;
		try {
			PrintWriter pw = new PrintWriter(newAccountFile);
			pw.println(newAccountPassword);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String playerDirString(String accountName, String playerName) {
		return serverSettings.accountPath()+accountName+"/players/"+playerName;
	}
	
	private boolean existPlayerDir(String accountName, String playerName){
		File playerDir = new File(playerDirString(accountName, playerName));
		return (playerDir.exists() && playerDir.isDirectory());
	}
	
	private void createPlayerDir(String accountName, String playerName) {
		assert(!existPlayerDir(accountName, playerName));
		File playerDir = new File(playerDirString(accountName, playerName));
		playerDir.mkdir();
	}
	
	/** NOTE: Doesn't wait until all threads have terminated. */
	public void shutdownAll() {
		reception.shutdown();
		synchronized(butlers) {
			for (Butler butler : butlers) {
				butler.put(new MBStopThreadOrder(null));
			}
		}
	}
	
	private HashSet<String> loadAllExistingPlayerNames() {
		HashSet<String> result = new HashSet<String>();
		File accountDir = new File(serverSettings.accountPath());
		File[] accounts = accountDir.listFiles(directoryFilter);
		for (File account : accounts) {
			File playersDir = new File(account.getPath()+"/players");
			File[] players = playersDir.listFiles(directoryFilter);
			for (File player : players) {
				boolean added = result.add(player.getName());
				if (!added)
					System.out.println("ERROR: Couldn't add "+player.getName());
			}
		}
		return result;
	}
}
