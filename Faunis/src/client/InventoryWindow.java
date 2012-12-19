package client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTable;

public class InventoryWindow {
	private JFrame win;
	private JTable invTable;
	
	public InventoryWindow(GameWindow mainWin, HashMap<Integer, Integer> playerItems){
		win = new JFrame("Inventory");
		
		String[] colData = {"ID","QNT"};
		Object[][] rowData = new Object[playerItems.size()][2];
		
		//Strips HashMap to Integer[][]
		Set<Map.Entry<Integer, Integer>> s = playerItems.entrySet();
		Iterator<Map.Entry<Integer, Integer>> i = s.iterator();
		int count = 0;
		while(i.hasNext()){
			Map.Entry<Integer, Integer> map = (Map.Entry<Integer, Integer>) i.next();
			rowData[count][0] = map.getKey();
			rowData[count][1] = map.getValue();
			count++;
		}
		
		invTable = new JTable(rowData, colData);
		win.add(invTable);
		win.setSize(350, 400);
		
		int left = mainWin.getPos().x + mainWin.getWidth();
		int top = (mainWin.getHeight() - win.getHeight())/2;
		win.setLocation(left, top);
		
		win.setVisible(true);
	}
}
