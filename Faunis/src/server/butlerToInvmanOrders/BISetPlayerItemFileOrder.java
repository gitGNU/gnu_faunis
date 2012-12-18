package server.butlerToInvmanOrders;

import java.io.File;

import server.Butler;

public class BISetPlayerItemFileOrder extends BIOrder{

	private String playerName;
	private File playerFile;
	
	public BISetPlayerItemFileOrder(String playerName, File playerFile, Butler source) {
		super(source);
		this.playerName = playerName;
		this.playerFile = playerFile;
	}

	public String getPlayerName(){
		return this.playerName;
	}
	
	public File getPlayerFile(){
		return this.playerFile;
	}
}
