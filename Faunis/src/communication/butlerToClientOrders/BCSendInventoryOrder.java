package communication.butlerToClientOrders;

import java.util.HashMap;

public class BCSendInventoryOrder extends BCOrder{
	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Integer> playerItems;
	
	public BCSendInventoryOrder(HashMap<Integer, Integer> playerItems){
		this.playerItems = playerItems;
	}
	public HashMap<Integer, Integer> getPlayerItems(){
		return this.playerItems;
	}
}
