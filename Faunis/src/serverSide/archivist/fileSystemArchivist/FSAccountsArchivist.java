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
package serverSide.archivist.fileSystemArchivist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import common.Logger;
import common.enums.CharacterClass;
import common.movement.Path;

import serverSide.Account;
import serverSide.Result;
import serverSide.ServerSettings;
import serverSide.archivist.AccountsArchivist;
import serverSide.player.Player;

public class FSAccountsArchivist implements AccountsArchivist {
	private FileSystemArchivist parent;
	
	public FSAccountsArchivist(FileSystemArchivist parent) {
		this.parent = parent;
	}
	
	@Override
	public Result<Boolean> createAccount(String name, String password){
		// TODO: Synchronise on file system? Or else two accounts could be
		// created under the same name, for example!
		// Check if an account already exists under given name:
		if (!existAccountSubdir(name)){
			// account doesn't yet exist, create it (without players)
			Account acc = new Account(name, password, new ArrayList<String>());
			boolean createAccSuccess=createAccountSubdir(acc);
			if (createAccSuccess){
				Logger.log("Account "+name+" successfully created.");
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
	
	@Override
	public boolean existAccount(String accountName) {
		return existAccountSubdir(accountName);
	}
	/** checks if a directory for given account name exists */
	private boolean existAccountSubdir(String accountName){
		File accountSubdir = new File(parent.getAccountPath()+accountName+"/");
		Logger.log("Check if "+accountSubdir.getPath()+" exists...");
		return (accountSubdir.exists() && accountSubdir.isDirectory());
	}
	
	/** Creates the directory structure in the account directory for given account.
	  * Returns true if successful, else false. */
	private boolean createAccountSubdir(Account newAccount){
		String accountPath = parent.getAccountPath();
		String newAccountName = newAccount.getName();
		String newAccountPassword = newAccount.getPassword();
		File newAccountDir = new File(accountPath+newAccountName+"/");
		File newAccountPlayersDir = new File(accountPath+newAccountName+"/players");
		boolean check = false;
		check = newAccountDir.mkdir();
		check = check && newAccountPlayersDir.mkdir();
		if (!check) return false;
		File newAccountFile = new File(accountPath+newAccountName+"/account.properties");
		Properties newAccountProperties = new Properties();
		newAccountProperties.setProperty("password", newAccountPassword);
		FileOutputStream newAccountOutputStream = null;
		try {
			newAccountOutputStream = new FileOutputStream(newAccountFile);
			newAccountProperties.store(newAccountOutputStream, null);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (newAccountOutputStream != null)
				try {
					newAccountOutputStream.close();
				} catch(IOException e) {
					e.printStackTrace();
					return false;
				}
		}
		return true;
	}
	
	
	private String playerDirString(String accountName, String playerName) {
		return parent.getAccountPath()+accountName+"/players/"+playerName;
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
	
	@Override
	public HashSet<String> loadAllExistingPlayerNames() {
		HashSet<String> result = new HashSet<String>();
		File accountDir = new File(parent.getAccountPath());
		File[] accounts = accountDir.listFiles(FileSystemArchivist.directoryFilter);
		for (File account : accounts) {
			File playersDir = new File(account.getPath()+"/players");
			File[] players = playersDir.listFiles(FileSystemArchivist.directoryFilter);
			for (File player : players) {
				boolean added = result.add(player.getName());
				if (!added)
					Logger.log("ERROR: Couldn't add "+player.getName());
			}
		}
		return result;
	}
	
	/** Pure function. <br/>
	 * Loads an account from hard disk if exists and given login data is valid.
	 * Additionally returns the loaded account if successful. */
	@Override
	public Result<Account> loadAccount(String name, String password){
		if (!existAccountSubdir(name)){
			String error = "Account "+name+" couldn't be loaded since it does not exist!";
			return new Result<Account>(null, error);
		}
		String accountPath = parent.getAccountPath();
		// read account.properties:
		File accFile = new File(accountPath+name+"/account.properties");
		Properties accProperties = new Properties();
		String readPassword;
		FileInputStream accStream = null;
		try {
			accStream = new FileInputStream(accFile);
			accProperties.load(accStream);
			readPassword = accProperties.getProperty("password");
		} catch (FileNotFoundException e) {
			Logger.log("Couldn't find account.properties for "+name);
			e.printStackTrace();
			return new Result<Account>(null, "Error while reading account data!");
		} catch (IOException e) {
			Logger.log("Error while reading account data!");
			e.printStackTrace();
			return new Result<Account>(null, "Error while reading account data!");
		} finally {
			if (accStream != null)
				try {
					accStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return new Result<Account>(null, "Error while reading account data!");
				}
		}
		// compare passwords
		if (!password.equals(readPassword)){
			Logger.log("Password invalid, since "+password+" != "+readPassword);
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
		Logger.log("Account "+name+" successfully loaded.");
		return new Result<Account>(acc, null);
	}

	@Override
	public boolean savePlayer(Player player) {
		synchronized(player) {
			Path oldPath = player.getPath();
			if (oldPath != null) Logger.log("WARNING: MainServer.savePlayer(): Player was still moving!");
			// It's bad if there's still a path referenced in the given player object:
			// In no way may it be serialised with the player
			player.resetPath();
			player.resetAnimation();
			String accountName = player.getAccountName();
			String playerName = player.getName();
			assert(existAccountSubdir(accountName));
			String playerDirString = playerDirString(accountName, playerName);
			if (!existPlayerDir(accountName, playerName))
				createPlayerDir(accountName, playerName);
			File playerFile = new File(playerDirString+"/"+playerName);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(playerFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(player);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (oldPath != null)
					player.setPath(oldPath);
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
			}
		}
		Logger.log("MainServer: Player successfully saved.");
		return true;
	}

	@Override
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
		FileInputStream fis = null;
		Player player = null;
		try {
			fis = new FileInputStream(playerDirString(account.getName(), playerName)+"/"+playerName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			player = (Player) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
			return new Result<Player>(null, "IOException");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return new Result<Player>(null, "ClassNotFoundException");
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		assert(player != null);
		synchronized(account) {
			account.setActivePlayer(player);
		}
		return new Result<Player>(player, null);
	}

	@Override
	public Result<Boolean> createNewPlayer(Account account, String playerName, CharacterClass type) {
		ServerSettings settings = parent.parent.getServerSettings();
		String starterRegion = settings.starterRegion();
		synchronized(parent.allExistingPlayerNames) {
			synchronized(account) {
				Player player;
				String accountName = account.getName();
				int maxPlayers = settings.maxPlayersPerAccount();
				ArrayList<String> playerNames = account.getPlayerNames();
				if (playerNames.size() >= maxPlayers) {
					String error = "You cannot exceed the limit of "+maxPlayers+" players per account!";
					return new Result<Boolean>(null, error);
				}
				if (parent.allExistingPlayerNames.contains(playerName)) {
					String error = "A player with the name "+playerName+" already exists!";
					return new Result<Boolean>(null, error);
				}
				player = new Player(playerName, type, starterRegion, accountName);
				boolean success = savePlayer(player);
				if (success) {
					playerNames.add(playerName);
					parent.allExistingPlayerNames.add(playerName);
					return new Result<Boolean>(true, null);
				} else {
					String error = "The player could not be created!";
					return new Result<Boolean>(null, error);
				}
			}
		}
	}
}
