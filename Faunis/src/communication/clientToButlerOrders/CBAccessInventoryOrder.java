package communication.clientToButlerOrders;

import communication.enums.InventoryType;

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
