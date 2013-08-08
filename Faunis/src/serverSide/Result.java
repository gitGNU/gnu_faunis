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
package serverSide;

/** Represents a result of the main server's methods when the method
 * may fail. <br/>
 * The method execution was successful if errorMessage is null,
 * you will then find the requested object in the "requested" field.
 * If it failed, errorMessage contains a string with the reason. */
public class Result<T> {
	private T result;
	private String errorMessage;
	
	public Result(T requested, String errorMessage) {
		this.result = requested;
		this.errorMessage = errorMessage;
	}
	
	public T getResult() {
		return result;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean successful() {
		return (errorMessage == null);
	}
}
