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
package server.butlerToInvmanOrders;

import server.Butler;

public class BISetPlayerItemFileOrder extends BIOrder{

	private String playerName;
	private String playerFile;
	
	public BISetPlayerItemFileOrder(String playerName, String playerFile, Butler source) {
		super(source);
		this.playerName = playerName;
		this.playerFile = playerFile;
	}

	public String getPlayerName(){
		return this.playerName;
	}
	
	public String getPlayerFile(){
		return this.playerFile;
	}
}
