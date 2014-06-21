/* Copyright 2012 - 2014 Simon Ley alias "skarute"
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
package clientSide.gui;

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
			Map.Entry<Integer, Integer> map = i.next();
			rowData[count][0] = map.getKey();
			rowData[count][1] = map.getValue();
			count++;
		}
		
		invTable = new JTable(rowData, colData);
		win.add(invTable);
		win.setSize(350, 400);
		
		int left = mainWin.getPosition().x + mainWin.getWidth();
		int top = (mainWin.getHeight() - win.getHeight())/2;
		win.setLocation(left, top);
		
		win.setVisible(true);
	}
}
