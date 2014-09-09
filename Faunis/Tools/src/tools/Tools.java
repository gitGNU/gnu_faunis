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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;


public class Tools {

	public static String join(String separator, String[] array) {
		if (array.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			builder.append(array[i]);
			builder.append(separator);
		}
		builder.append(array[array.length - 1]);
		return builder.toString();
	}

	public static String join(String separator, Iterable<String> iterable) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = iterable.iterator();
		while(iterator.hasNext()) {
			builder.append(iterator.next());
			if (iterator.hasNext()) {
				builder.append(separator);
			}
		}
		return builder.toString();
	}

	/** Returns a concatenated copy of given arrays. Taken from StackOverflow. */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static boolean deleteRecursive(File file) {
		// resolve symbolic links
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			return false;
		}
		return deleteRecursive(file, file);
	}

	private static boolean deleteRecursive(File parent, File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				// Check if subFile is still a "true" subelement of originalFile
				// (Beware of symbolic links that break out!)
				boolean isParent = false;
				try {
					isParent = isParent(parent, subFile);
				} catch (IOException e) {
					return false;
				}
				if (isParent) {
					if (!deleteRecursive(subFile)) {
						return false;
					}
				}
			}
		}
		if (!file.delete()) {
			return false;
		}
		return true;
	}

	/** First resolves relative paths and symlinks of both files,
	 * then checks if given child is contained in given parent over
	 * an arbitrary number of levels.
	 * @throws IOException if path resolution fails
	 */
	public static boolean isParent(File parent, File child) throws IOException {
		parent = parent.getCanonicalFile();
		child = child.getCanonicalFile();
		File current = child;
		while(!current.equals(parent)) {
			current = current.getParentFile();
			if (current == null) {
				return false;
			}
		}
		return true;
	}
}
