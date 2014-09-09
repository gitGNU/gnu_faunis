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
package common.clientToButlerOrders;

import common.enums.InventoryType;

public class CBAccessInventoryOrder extends CBOrder{
	private static final long serialVersionUID = 1L;
	private InventoryType invType;
	private String playerName;
	private String otherPlayerName;
	private int itemID;
	private int qnt;

	/** Constructor used for ADD, THROW, GIVE*/
	public CBAccessInventoryOrder(InventoryType invType, String playerName, int itemID, int qnt){
		this.invType = invType;
		this.playerName = playerName;
		this.otherPlayerName = new String();
		this.itemID = itemID;
		this.qnt = qnt;
	}

	/** Constructor used for VIEW, USE*/
	public CBAccessInventoryOrder(InventoryType invType, String playerName){
		this.invType = invType;
		this.playerName = playerName;
		this.otherPlayerName = new String();
		this.itemID = -1;
		this.qnt = 0;
	}

	/** Constructor used for GIVE*/
	public CBAccessInventoryOrder(InventoryType invType, String playerName, String otherPlayerName, int itemID, int qnt){
		this.invType = invType;
		this.playerName = playerName;
		this.otherPlayerName = otherPlayerName;
		this.itemID = itemID;
		this.qnt = qnt;
	}

	public InventoryType getInvType(){
		return invType;
	}
	public String getPlayerName(){
		return playerName;
	}
	public int getItemID(){
		return itemID;
	}
	public int getQnt(){
		return qnt;
	}
	public String getOtherPlayerName(){
		return otherPlayerName;
	}
}
