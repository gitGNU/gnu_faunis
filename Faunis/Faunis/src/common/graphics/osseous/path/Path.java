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
package common.graphics.osseous.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tools.Tools;


/** Mainly a wrapper for List&lt;String&gt;. Mutable. */
public abstract class Path implements Iterable<String> {
	protected ArrayList<String> path;
	
	public Path(List<String> path) {
		for (String element : path) {
			checkElement(element);
		}
		this.path = new ArrayList<String>(path);
	}
	
	/** Usage: Either Path("a/b/c") or Path("a", "b", "c") 
	 */
	public Path(String... elements) {
		if (elements.length == 1 && elements[0].contains("/")) {
			elements = elements[0].split("/");
		}
		for (String element : elements) {
			checkElement(element);
		}
		this.path = new ArrayList<String>();
		for (String element : elements) {
			this.path.add(element);
		}
	}
	
	/** 
	 * Throws an IllegalArgumentException if given element contains the path separator '/'.
	 * You should call this method on any element that will be added to this path.
	 */
	protected void checkElement(String element) {
		if (element.contains("/")) {
			throw new IllegalArgumentException(
				"A given path element contains the reserved path separator '/': " + element
			);
		}
	}
	
	@Override
	public Iterator<String> iterator() {
		return path.iterator();
	}
	
	public String get(int index) {
		return path.get(index);
	}
	
	public String set(int index, String element) {
		checkElement(element);
		return path.set(index, element);
	}
	
	public boolean add(String element) {
		checkElement(element);
		return path.add(element);
	}
	
	/** Shallow copy: The contained element references will be the same. */
	public abstract Path copy();
	
	/** Removes and returns the first element of this path. */
	public String popFirst() {
		return path.remove(0);
	}
	
	/** Removes and returns the last element of this path. */
	public String popLast() {
		return path.remove(path.size()-1);
	}
	
	public int size() {
		return path.size();
	}
	
	@Override
	public String toString() {
		return Tools.join("/", path);
	}

}
