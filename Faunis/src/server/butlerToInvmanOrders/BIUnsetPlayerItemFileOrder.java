package server.butlerToInvmanOrders;

import server.Butler;

public class BIUnsetPlayerItemFileOrder extends BIOrder{
	
	private String playerName;
	
	public BIUnsetPlayerItemFileOrder(String playerName, Butler source){
		super(source);
		this.playerName = playerName;
	}
	
	public String getPlayerName(){
		return playerName;
	}
}
