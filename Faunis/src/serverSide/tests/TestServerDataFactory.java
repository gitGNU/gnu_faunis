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
package serverSide.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

import common.enums.CharacterClass;

import serverSide.ServerSettings;
import serverSide.player.Player;

public class TestServerDataFactory {
	public static void createTestServerData(ServerSettings serverSettings, int accounts,
											int playersPerAccount) {
		String accountsPath = serverSettings.accountPath();
		String starterRegion = serverSettings.starterRegion();
		assert(accountsPath.endsWith(File.separator));
		for (int accountIndex = 0; accountIndex < accounts; accountIndex++) {
			String accountName = "robot"+accountIndex;
			String accountPath = accountsPath+accountName+"/";
			File accountDir = new File(accountPath);
			boolean success = accountDir.mkdir();
			assert(success);
			Properties accountProperties = new Properties();
			accountProperties.put("password", "");
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(accountPath+"account.properties");
				accountProperties.store(out, null);
			} catch(FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch(IOException e) {
				e.printStackTrace();
				return;
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			String playersPath = accountPath+"players/";
			File playersDir = new File(playersPath);
			playersDir.mkdir();
			for (int playerIndex = 0; playerIndex < playersPerAccount; playerIndex++) {
				String playerName = accountName+"player"+playerIndex;
				String playerPath = playersPath+playerName+"/";
				File playerDir = new File(playerPath);
				playerDir.mkdir();
				Player player = new Player(playerName, CharacterClass.ursine, starterRegion, accountName);
				FileOutputStream playerFos = null;
				ObjectOutputStream playerOos = null;
				try {
					playerFos = new FileOutputStream(playerPath+playerName);
					playerOos = new ObjectOutputStream(playerFos);
					playerOos.writeObject(player);
				} catch(FileNotFoundException e) {
					e.printStackTrace();
					return;
				} catch(IOException e) {
					e.printStackTrace();
					return;
				} finally {
					try {
						if (playerFos != null)
							playerFos.close();
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
	}
}
