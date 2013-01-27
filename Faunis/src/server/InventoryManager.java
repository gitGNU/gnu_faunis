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
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import server.butlerToInvmanOrders.BIAccessInventoryOrder;
import server.butlerToInvmanOrders.BIOrder;
import server.butlerToInvmanOrders.BISetPlayerItemFileOrder;
import server.butlerToInvmanOrders.BIUnsetPlayerItemFileOrder;
import server.invmanToButlerOrders.IBSendErrorMessageOrder;
import server.invmanToButlerOrders.IBSendInventoryOrder;

import communication.enums.InventoryType;

/** This manages inventory commands, such as: ADD, GIVE, THROW, VIEW, USE*/
public class InventoryManager {
	@SuppressWarnings("unused")
	private MainServer parent;
	private Thread thread;
	private Runnable runnable;
	protected BlockingQueue<BIOrder> orders;
	private HashMap<String, String> playerItems;

	private JFrame frame;
	private JTextPane area;
	
	private void logMessageLn(String message){
		area.setText(area.getText() + message + "\n");
	}
	private void logMessage(String message){
		area.setText(area.getText() + message);
	}
	public InventoryManager(MainServer parent){
		{
			frame = new JFrame("Inventory Window");
			area = new JTextPane();
			area.setEditable(false);
			JScrollPane spane = new JScrollPane(area);
			spane.setSize(350, 200);
			frame.add(spane);
			frame.setSize(400, 250);
			frame.setLocation(850, 0);
			frame.setResizable(false);
			frame.setAlwaysOnTop(true);
			frame.setVisible(true);
		}
		this.parent = parent;
		this.playerItems = new HashMap<String, String>();
		orders = new ArrayBlockingQueue<BIOrder>(50);
		runnable = new InvManRunnable(this);
		thread = new Thread(runnable);
		thread.start();
	}

	protected void handleButlerOrder(BIOrder order){
		assert(order!=null);
		if(order instanceof BISetPlayerItemFileOrder){
			registerPlayerFile((BISetPlayerItemFileOrder) order);
		}else if(order instanceof BIAccessInventoryOrder){
			handleCommand((BIAccessInventoryOrder) order);
		}else if(order instanceof BIUnsetPlayerItemFileOrder){
			unregisterPlayerFile((BIUnsetPlayerItemFileOrder) order);
		}
	}
	
	private void unregisterPlayerFile(BIUnsetPlayerItemFileOrder order) {
		String playerName = order.getPlayerName();
		
		playerItems.remove(playerName);
		logMessageLn("Player [" + playerName + "] removed in Inventory Manager");
	}
	private void handleCommand(BIAccessInventoryOrder order) {
		InventoryType invType = order.getInvType();
		String playerName = order.getPlayerName();
		String otherPlayerName = order.getOtherPlayerName();
		int itemID = order.getItemID();
		int qnt = order.getQnt();
		
		if(itemID < 0 && (invType != InventoryType.VIEW)){ //Catches non-positive itemID
			logMessageLn("Cannot get non-positive itemID");
			return;
		}
		if(qnt < 0 && (invType != InventoryType.VIEW)){ //Catches non-positive quantity
			logMessageLn("Cannot get non-positive quantity");
			return;
		}
		
		logMessageLn("Inventory request handled, type " + invType.name());
		
		switch(invType){
		case ADD:
			//TODO This task may or maynot be used by PathMan
			break;
		case GIVE:
			giveItem(playerName, otherPlayerName, itemID, qnt, order.getSource());
			break;
		case THROW:
			throwItem(playerName, itemID, qnt, order.getSource());
			break;
		case USE:
			break;
		case VIEW:
			order.getSource().put(new IBSendInventoryOrder(readFile(playerName)));
			break;
		default:
			break;
		
		}
	}

	
	private void giveItem(String playerName, String otherPlayerName, int itemID, int qnt, Butler source) {
		logMessageLn("Other player [" + otherPlayerName + "] status: " + this.playerItems.containsKey(otherPlayerName));
		//Checks giving player's command to be correct
		if(playerName.equals(otherPlayerName)){
			source.put(new IBSendErrorMessageOrder("Cannot give items to yourself"));
			return;
		}
		if(this.playerItems.containsKey(otherPlayerName)){
			HashMap<Integer, Integer> playerItems = readFile(playerName);
			HashMap<Integer, Integer> otherPlayerItems = readFile(otherPlayerName);
			
			if(playerItems.containsKey(itemID)){ //Makes sure that something is to be given
			
				int playerItemQnt = playerItems.get(itemID);
				int otherPlayerItemQnt = 0;
				if(otherPlayerItems.containsKey((itemID))) otherPlayerItemQnt = otherPlayerItems.get(itemID);
				
				int newPlayerItemQnt = playerItemQnt - qnt;
				int newOtherPlayerItemQnt;
				boolean allowWrite = false;
				
				if(newPlayerItemQnt > 0){ //The player has more than than what is to be given	
					playerItems.put(itemID, newPlayerItemQnt);
					allowWrite = true;
				}else if(newPlayerItemQnt == 0){ //The player exhausted this item
					playerItems.remove(itemID);
					allowWrite = true;
				}else{ //The player doesn't have enough to give
					source.put(new IBSendErrorMessageOrder("Cannot give items more than you have"));
					allowWrite = false;
				}
				
				if(allowWrite){ //Writes items file
					newOtherPlayerItemQnt = otherPlayerItemQnt + qnt;
					otherPlayerItems.put(itemID, newOtherPlayerItemQnt);
					
					writeFile(this.playerItems.get(playerName), playerItems);
					writeFile(this.playerItems.get(otherPlayerName), otherPlayerItems);
					
					logMessageLn("Give command successful");
				}
				
			}else{
				source.put(new IBSendErrorMessageOrder("Cannot give items you don't have"));
			}
		}else{
			source.put(new IBSendErrorMessageOrder("Player [" + otherPlayerName +"] unavailable"));
		}
		
	}
	
	/** Throws item*/
	private void throwItem(String playerName, int itemID, int qnt, Butler source) {
		logMessageLn("throwItem method invoked by [" + playerName + "]: (" + itemID + ", " + qnt + ")");
		HashMap<Integer, Integer> hashmap = readFile(playerName);
		if(hashmap.containsKey(itemID)){ //Therefore the itemID is throwable
			int prevQnt = hashmap.get(itemID);
			int newQnt = prevQnt - qnt;
			
			if(newQnt > 0){ //Catches non-negative quantity
				logMessageLn("\tAssessed new value: " + newQnt);
				hashmap.put(itemID, newQnt);
				logMessageLn("\tHashMap Val: " + hashmap.get(itemID));
			}
			else if(newQnt == 0){
				logMessageLn("\tAssessed new value: [Delete]");
				hashmap.remove(itemID);
			}else{
				logMessageLn("\tCannot invoke change, Qnt negates");
				source.put(new IBSendErrorMessageOrder("Cannot throw item more than you have"));
			}
			writeFile(playerItems.get(playerName), hashmap);
		}else{
			logMessageLn("\tItem not found for user [" + playerName + "]");
			source.put(new IBSendErrorMessageOrder("Cannot throw item that you don't have"));
		}
	}

	/** Sets player items file into the InvMan active HashMaps*/
	private void registerPlayerFile(BISetPlayerItemFileOrder order) {
		String playerName = order.getPlayerName();
		String playerFile = order.getPlayerFile();
		logMessage("Checking " + playerName + " items file... ");
		checkFileIntegrity(playerFile);
		this.playerItems.put(playerName, playerFile);
		if(this.playerItems.containsKey(playerName)) logMessageLn("User "
				+ playerName 
				+ " registered in Inventory Manager");
	}

	/** Checks whether a items file is present in Player's directory*/
	private void checkFileIntegrity(String playerFilename){
		File playerFile = new File(playerFilename);
		if(!(playerFile.exists())){
			logMessageLn("Not present!");
			logMessage("\tCreating file... ");
			try {
				logMessage(playerFile.getPath() + "... ");
				playerFile.createNewFile();
				logMessageLn("Created!" + playerFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
				logMessageLn("Error!");
			}
			HashMap<Integer, Integer> playerInventory = new HashMap<Integer, Integer>();
			writeFile(playerFilename, playerInventory);
		}else{
			logMessageLn("Present! "+playerFile.getPath());
		}
		playerFile = null;
	}
	
	/** Writes items files*/
	private void writeFile(String playerFilename, HashMap<Integer, Integer> playerInventory){
		File playerFile = new File(playerFilename);
		logMessageLn("Writing: " + playerFile.getAbsolutePath());
		FileOutputStream out;
		ObjectOutputStream objOut;
		try {
			out = new FileOutputStream(playerFile);
			objOut = new ObjectOutputStream(out);
			objOut.writeObject(playerInventory);
			objOut.flush();
			objOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		playerFile = null;
	}
	
	/** Converts playerName into HashMap<Integer, Integer>*/
	@SuppressWarnings("unchecked")
	private HashMap<Integer, Integer> readFile(String playerName){
		logMessageLn("readFile [" + playerName + "]");
		HashMap<Integer, Integer> container = new HashMap<Integer, Integer>();
		try {
			File playerFile = new File(playerItems.get(playerName));
			FileInputStream input = new FileInputStream(playerFile);
			ObjectInputStream objIn = new ObjectInputStream(input);
			container = (HashMap<Integer, Integer>) objIn.readObject();
			objIn.close();
			playerFile = null;
			logMessageLn("readFile successful!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logMessageLn("readFile failed[1]");
		}  catch (IOException e) {
			e.printStackTrace();
			logMessageLn("readFile failed[2]");
		}  catch (ClassNotFoundException e) {
			e.printStackTrace();
			logMessageLn("readFile failed[3]");
		}
		return container;
	}
	
	/** My runnable instance*/
	private class InvManRunnable implements Runnable{
		private InventoryManager myInvMan;
		
		public InvManRunnable(InventoryManager parent){
			this.myInvMan = parent;
		}
		
		@Override
		public void run() {
			logMessageLn("Inventory Manager started...");
			logMessageLn("System separator: " + File.separator);
			BIOrder order;
			while(true){
				order = null;
				try{
					order = myInvMan.orders.take();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				if(order != null)
					myInvMan.handleButlerOrder(order);
			}
		}
		
	}
}
