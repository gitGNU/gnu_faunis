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
package serverSide.butlerToInvmanOrders;

import common.enums.InventoryType;

import serverSide.butler.Butler;

public class BIAccessInventoryOrder extends BIOrder {
	private InventoryType invType;
	private String playerName;
	private String otherPlayerName;
	private int itemID;
	private int qnt;

	public BIAccessInventoryOrder(InventoryType invType, String playerName, int itemID, int qnt, Butler source) {
		super(source);
		this.invType = invType;
		this.playerName = playerName;
		this.otherPlayerName = new String();
		this.itemID = itemID;
		this.qnt = qnt;
	}

	public BIAccessInventoryOrder(InventoryType invType, String playerName, Butler source){
		super(source);
		this.invType = invType;
		this.playerName = playerName;
		this.otherPlayerName = new String();
		this.itemID = new Integer(null);
		this.qnt = new Integer(null);
	}

	public BIAccessInventoryOrder(InventoryType invType, String playerName, String otherPlayerName, int itemID, int qnt, Butler source){
		super(source);
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
