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
package serverSide;

import java.util.ArrayList;

import serverSide.player.Player;


public class Account {
	private String name;
	private String password;
	private ArrayList<String> playerNames;	// Names of the players as they exist in
											// the directory "players"
	private Player activePlayer;	// player which is currently loaded and controlled
	
	public Account(String name, String password, ArrayList<String> playerNames){
		this.name = name;
		this.password = password;
		this.playerNames = playerNames;
		this.activePlayer = null;
	}
	
	
	
	public String getName(){
		return this.name;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public ArrayList<String> getPlayerNames() {
		return this.playerNames;
	}
	
	public Player getActivePlayer() {
		return this.activePlayer;
	}

	public void setActivePlayer(Player player) {
		this.activePlayer = player;
	}
	
	
	
}
