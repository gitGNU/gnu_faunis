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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import server.butlerToInvmanOrders.BIAccessInventoryOrder;
import server.butlerToInvmanOrders.BIOrder;
import server.butlerToInvmanOrders.BISetPlayerItemFileOrder;
import server.butlerToInvmanOrders.BIUnsetPlayerItemFileOrder;

import communication.enums.InventoryType;

/** This manages inventory commands, such as: ADD, GIVE, THROW, VIEW, USE*/
public class InventoryManager {
	private MainServer parent;
	private Thread thread;
	private Runnable runnable;
	protected BlockingQueue<BIOrder> orders;
	private HashMap<String, File> playerItems;

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
			frame.setVisible(true);
		}
		this.parent = parent;
		this.playerItems = new HashMap<String, File>();
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
			break;
		case THROW:
			throwItem(playerName, itemID, qnt);
			break;
		case USE:
			break;
		case VIEW:
			break;
		default:
			break;
		
		}
	}

	private void throwItem(String playerName, int itemID, int qnt) {
		logMessageLn("throwItem method invoked by [" + playerName + "]: (" + itemID + ", " + qnt + ")");
		HashMap<Integer, Integer> hashmap = readFile(playerName);
		if(hashmap.containsKey(itemID)){ //Therefore the itemID is throwable
			int prevQnt = hashmap.get(itemID);
			int newQnt = prevQnt - qnt;
			
			if(newQnt > 0){ //Catches non-negative quantity
				logMessageLn("\tAssessed new value: " + newQnt);
				hashmap.put(itemID, newQnt);
			}
			else if(newQnt == 0){
				logMessageLn("\tAssessed new value: [Delete]");
				hashmap.remove(itemID);
			}else{
				logMessageLn("\tCannot invoke change, Qnt negates");
			}
			writeFile(playerItems.get(playerItems), hashmap);
		}else{
			logMessageLn("\tItem not found for user [" + playerName + "]");
		}
	}

	/** Sets player items file into the InvMan active HashMaps*/
	private void registerPlayerFile(BISetPlayerItemFileOrder order) {
		String playerName = order.getPlayerName();
		File playerFile = order.getPlayerFile();
		logMessage("Checking " + playerName + " items file... ");
		checkFileIntegrity(playerFile);
		this.playerItems.put(playerName, playerFile);
		if(this.playerItems.containsKey(playerName)) logMessageLn("User "
				+ playerName 
				+ " registered in Inventory Manager");
	}

	/** Checks whether a items file is present in Player's directory*/
	private void checkFileIntegrity(File playerFile){
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
			writeFile(playerFile, playerInventory);
		}else{
			logMessageLn("Present! "+playerFile.getPath());
		}
	}
	
	/** Writes items files*/
	private void writeFile(File playerFile, HashMap<Integer, Integer> playerInventory){
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
	}
	
	/** Converts playerName into HashMap<Integer, Integer>*/
	private HashMap<Integer, Integer> readFile(String playerName){
		logMessageLn("readFile [" + playerName + "]");
		HashMap<Integer, Integer> container = new HashMap<Integer, Integer>();
		try {
			FileInputStream input = new FileInputStream(playerItems.get(playerName));
			ObjectInputStream objIn = new ObjectInputStream(input);
			container = (HashMap<Integer, Integer>) objIn.readObject();
			objIn.close();
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
