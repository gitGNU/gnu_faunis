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
