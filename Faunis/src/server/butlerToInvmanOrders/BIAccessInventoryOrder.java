package server.butlerToInvmanOrders;

import communication.enums.InventoryType;

import server.Butler;

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
