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

public class Logger {
	private static boolean logThread = true;
	private static boolean logTopmostStackTrace = false;
	
	public static void log(Object o) {
		StringBuilder toPrint = new StringBuilder();
		
		Thread currentThread = Thread.currentThread();
		
		if (logThread) {
			String threadName = currentThread.getName();
			toPrint.append(threadName);
			toPrint.append(": ");
		}
		if (logTopmostStackTrace) {
			StackTraceElement[] stackTrace = currentThread.getStackTrace();
			StackTraceElement topmostStackTraceElement = null;
			if (stackTrace.length > 0)
				topmostStackTraceElement = stackTrace[0];
			toPrint.append(topmostStackTraceElement);
			toPrint.append(": ");
		}
		toPrint.append(o.toString());
		System.out.println(toPrint);
	}
}
