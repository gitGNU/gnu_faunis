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
package serverSide.archivist;

import java.util.HashSet;

import serverSide.Account;
import serverSide.Result;
import serverSide.player.Player;

import common.enums.CharacterClass;

public interface AccountsArchivist {
	/** Creates a new account, but doesn't load it. */
	Result<Boolean> createAccount(String name, String password);

	Result<Account> loadAccount(String name, String password);
	
	boolean existAccount(String accountName);
	
	HashSet<String> loadAllExistingPlayerNames();
	
	/** locks allExistingPlayerNames, account, player <br/>
	 * Creates a new player on hard disk. You then still have to load it
	 * by calling loadAndActivatePlayer().*/
	Result<Boolean> createNewPlayer(Account account, String playerName,
			CharacterClass type);

	/** locks player<br/>
	 * Saves the current state of given player. */
	boolean savePlayer(Player player);

	/** locks account, activePlayernameToPlayer<br/>
	 * Loads a player object from disk and returns it, or returns null if it failed.
	 * => To be called by butlers!<br/>
	 * NOTE: The butler will yet have to assign the player to a mapman
	 */
	Result<Player> loadAndActivatePlayer(Account account, String playerName);
}
