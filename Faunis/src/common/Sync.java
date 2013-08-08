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
package common;
import java.util.IdentityHashMap;

/** Represents a helper for synchronization. Offers functionality to
 * synchronize on arbitrary lists of objects. Moreover, sorts these lists
 * by an order defined by you before synchronizing, such that deadlocks are
 * prevented. */
public class Sync {
	private final IdentityHashMap<Object, Integer> syncOrder;
	
	public Sync(Object[] _syncOrder) {
		this.syncOrder = new IdentityHashMap<Object, Integer>(_syncOrder.length);
		for (int i = 0; i < _syncOrder.length; i++)
			this.syncOrder.put(_syncOrder[i], i);
	}
	
	/** Orders list according to syncOrder. Any unknown objects are added
	 * at the end in order of their appearance. Null values are removed. */
	private Object[] orderList(Object[] list) {
		// bucket sort
		Object[] addedUnknown = new Object[list.length];
		int countUnknownElems = 0;
		Object[] orderedWithNull = new Object[syncOrder.size()];
		int countAssignedElems = 0;
		for (int i = 0; i < list.length; i++) {
			Object elem = list[i];
			if (elem == null)
				continue;
			Integer index = syncOrder.get(elem);
			if (index != null) {
				if (orderedWithNull[index] == null) {
					orderedWithNull[index] = elem;
					countAssignedElems++;
				}
			} else {
				addedUnknown[countUnknownElems] = elem;
				countUnknownElems++;
			}
		}
		Object[] orderedWithoutNull = new Object[countAssignedElems + countUnknownElems];
		int index = 0;
		for (Object elem : orderedWithNull) {
			if (elem != null) {
				orderedWithoutNull[index] = elem;
				index++;
			}
		}
		for (int unknownIndex = 0; unknownIndex < countUnknownElems; unknownIndex++) {
			orderedWithoutNull[index] = addedUnknown[unknownIndex];
			index++;
		}
		assert(index == orderedWithoutNull.length);
		return orderedWithoutNull;
	}
	
	private void assertNoNullValues(Object[] list) {
		for (Object elem : list) {
			assert(elem != null);
		}
	}
	
	/** Sorts given list by the order you gave in the constructor, then
	 * synchronizes on each object, finally executes given Runnable. Any objects
	 * not defined in the order are added at the end in order of their appearance.
	 * @throws AssertionException if given list contains null values */
	public void multisync(Object[] lockOnList, Runnable innerCode) {
		assertNoNullValues(lockOnList);
		multisync(orderList(lockOnList), 0, innerCode);
	}
	/** Sorts given list by the order you gave in the constructor, then
	 * synchronizes on each object, finally executes given Runnable. Any objects
	 * not defined in the order are added at the end in order of their appearance.
	 * If skipNullValues is set, null values are ignored, or else an
	 * AssertionException is thrown. */
	public void multisync(Object[] lockOnList, boolean skipNullValues, Runnable innerCode) {
		if (!skipNullValues)
			assertNoNullValues(lockOnList);
		multisync(orderList(lockOnList), 0, innerCode);
	}
	private void multisync(Object[] lockOnList, int index, Runnable innerCode) {
		if (index < lockOnList.length) {
			synchronized(lockOnList[index]) {
				multisync(lockOnList, index+1, innerCode);
			}
		} else {
			innerCode.run();
		}
	}
}
