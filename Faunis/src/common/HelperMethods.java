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

public class HelperMethods {
	public String underscoreToCamelcase(String underscore, boolean capitaliseFirstLetter) {
		String[] split = underscore.split("_");
		StringBuilder builder = new StringBuilder();
		// Treat first element separately:
		{
			String part = split[0];
			String lowercaseRest = part.substring(1).toLowerCase();
			char firstLetter = part.charAt(0);
			if (capitaliseFirstLetter)
				firstLetter = Character.toUpperCase(firstLetter);
			else
				firstLetter = Character.toLowerCase(firstLetter);
			builder.append(firstLetter);
			builder.append(lowercaseRest);
		}
		
		for (int i = 1; i < split.length; i++) {
			String part = split[i];
			String lowercaseRest = part.substring(1).toLowerCase();
			char firstLetter = Character.toUpperCase(part.charAt(0));
			builder.append(firstLetter);
			builder.append(lowercaseRest);
		}
		
		return builder.toString();
	}
	
	

	public static String concatenateHelper(String[] array, int startIndex) {
		assert(startIndex < array.length);
		StringBuilder message = new StringBuilder();
		message.append(array[startIndex]);
		for (int i = startIndex+1; i < array.length; i++) {
			message.append(" ");
			message.append(array[i]);
		}
		return message.toString();
	}
}
