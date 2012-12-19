package server.invmanToButlerOrders;

import java.util.HashMap;

public class IBSendInventoryOrder extends IBOrder{
	private HashMap<Integer, Integer> playerItems;
	
	public IBSendInventoryOrder(HashMap<Integer, Integer> playerItems){
		this.playerItems = playerItems;
	}
	public HashMap<Integer, Integer> getPlayerItems(){
		return this.playerItems;
	}
}
