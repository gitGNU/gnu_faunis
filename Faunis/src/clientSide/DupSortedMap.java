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
package clientSide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

/** A tree map that allows duplicate keys.
 * Since the key isn't unique for an element anymore, one always has to
 * give both element and key for all operations. */
public class DupSortedMap<K, T> {
	private SortedMap<K, HashSet<T>> sortedMap;
	
	public DupSortedMap() {
		this.sortedMap = new TreeMap<K, HashSet<T>>();
	}
	
	/** Returns true if this map did not already contain given element. */
	public boolean add(T element, K keyOfElement) {
		HashSet<T> hashSet = sortedMap.get(keyOfElement);
		if (hashSet == null) {
			hashSet = new HashSet<T>();
			hashSet.add(element);
			sortedMap.put(keyOfElement, hashSet);
			return true;
		} else {
			boolean alreadyContained = hashSet.add(element);
			return alreadyContained;
		}
	}
	
	public boolean contains(T element, K keyOfElement) {
		HashSet<T> hashSet = sortedMap.get(keyOfElement);
		if (hashSet == null) {
			return false;
		} else {
			return hashSet.contains(element);
		}
	}
	
	/** Returns true if the element could be removed,
	 * or false if it wasn't contained. */
	public boolean remove(T element, K keyOfElement) {
		HashSet<T> hashSet = sortedMap.get(keyOfElement);
		if (hashSet == null) {
			return false;
		} else {
			boolean couldBeRemoved = hashSet.remove(element);
			if (hashSet.isEmpty())
				sortedMap.remove(keyOfElement);
			return couldBeRemoved;
		}
	}
	
	public void clear() {
		sortedMap.clear();
	}
	
	/** Returns all values, sorted by keys. The array list is NOT backed by
	 * this sorted map, so feel free to modify the returned array list. */
	public ArrayList<T> values() {
		ArrayList<T> result = new ArrayList<T>();
		for (HashSet<T> hashSet : sortedMap.values()) {
			result.addAll(hashSet);
		}
		return result;
	}
}
