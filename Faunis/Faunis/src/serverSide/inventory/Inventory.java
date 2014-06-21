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
package serverSide.inventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Non-synchronized class. */
public class Inventory implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<Item, Integer> itemAndAmount;
	
	public Inventory() {
		this.itemAndAmount = new HashMap<Item, Integer>();
	}
	
	public boolean canAdd(Item item, int amount) {
		if (amount < 0)
			return false;
		return true;
	}
	
	public boolean add(Item item, int amount) {
		if (!canAdd(item, amount))
			return false;
		Integer oldAmount = itemAndAmount.get(item);
		if (oldAmount != null) {
			int newAmount = oldAmount + amount;
			assert(newAmount >= 0);
			if (newAmount == 0)
				itemAndAmount.remove(item);
			else
				itemAndAmount.put(item, newAmount);
		}
		return true;
	}
	
	public boolean canRemove(Item item, int amount) {
		if (amount < 0 || getAmount(item) < amount)
			return false;
		return true;
	}
	
	public boolean remove(Item item, int amount) {
		if (!canRemove(item, amount))
			return false;
		Integer oldAmount = itemAndAmount.get(item);
		if (oldAmount != null) {
			int newAmount = oldAmount - amount;
			assert(newAmount >= 0);
			if (newAmount == 0)
				itemAndAmount.remove(item);
			else
				itemAndAmount.put(item, newAmount);
		}
		return true;
	}
	
	public int getAmount(Item item) {
		Integer amount = itemAndAmount.get(item);
		if (amount == null)
			return 0;
		else
			return amount;
	}
	
	public boolean hasAny(Item item) {
		return (getAmount(item) > 0);
	}
}
