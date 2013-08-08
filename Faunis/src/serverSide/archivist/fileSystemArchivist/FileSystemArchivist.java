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
package serverSide.archivist.fileSystemArchivist;

import java.io.FileFilter;
import java.util.HashSet;

import serverSide.MainServer;
import serverSide.archivist.AccountsArchivist;
import serverSide.archivist.CoreArchivist;
import serverSide.archivist.MapArchivist;

import common.archivist.fileSystemArchivist.DirectoryFilter;

public class FileSystemArchivist implements CoreArchivist {
	MainServer parent;
	FSAccountsArchivist accountsArchivist;
	FSMapArchivist mapArchivist;
	
	
	HashSet<String> allExistingPlayerNames;
	public static final FileFilter directoryFilter = new DirectoryFilter();
	
	
	public FileSystemArchivist(MainServer parent) {
		this.parent = parent;
	}
	
	@Override
	public void init() {
		accountsArchivist = new FSAccountsArchivist(this);
		mapArchivist = new FSMapArchivist();
		
		allExistingPlayerNames = accountsArchivist.loadAllExistingPlayerNames();
	}
	
	public String getAccountPath() {
		return parent.getAccountPath();
	}
	
	@Override
	public MapArchivist mapArchivist() {
		return mapArchivist;
	}
	
	@Override
	public AccountsArchivist accountsArchivist() {
		return accountsArchivist;
	}

}
