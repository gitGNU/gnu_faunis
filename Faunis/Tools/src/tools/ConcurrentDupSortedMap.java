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
package tools;

import java.util.ArrayList;

/** A tree map that allows duplicate keys.
 * Since the key isn't unique for an element anymore, one always has to
 * give both element and key for all operations. */
public class ConcurrentDupSortedMap<K, T> {
	private DupSortedMap<K, T> dupSortedMap;

	public ConcurrentDupSortedMap() {
		this.dupSortedMap = new DupSortedMap<K, T>();
	}

	/** Returns true if this map did not already contain given element. */
	public boolean add(T element, K keyOfElement) {
		synchronized (dupSortedMap) {
			return dupSortedMap.add(element, keyOfElement);
		}
	}

	public boolean contains(T element, K keyOfElement) {
		synchronized (dupSortedMap) {
			return dupSortedMap.contains(element, keyOfElement);
		}
	}

	/** Returns true if the element could be removed,
	 * or false if it wasn't contained. */
	public boolean remove(T element, K keyOfElement) {
		synchronized (dupSortedMap) {
			return dupSortedMap.remove(element, keyOfElement);
		}
	}

	public void clear() {
		synchronized (dupSortedMap) {
			dupSortedMap.clear();
		}
	}

	/** Returns all values, sorted by keys. The array list is NOT backed by
	 * this sorted map, so feel free to modify the returned array list. */
	public ArrayList<T> values() {
		synchronized (dupSortedMap) {
			return dupSortedMap.values();
		}
	}
}
