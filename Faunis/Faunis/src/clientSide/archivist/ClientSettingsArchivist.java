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
package clientSide.archivist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import clientSide.ClientSettings;

public class ClientSettingsArchivist {
	public void readClientSettingsFromFile(ClientSettings clientSettings) {
		File settingsFile = new File(clientSettings.clientDataPath()+"clientSettings.properties");
		if (!settingsFile.exists()) {
			throw new RuntimeException("clientSettings.properties not found");
		}
		Properties properties = new Properties();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(settingsFile);
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while reading clientSettings.properties");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Error while reading clientSettings.properties");
				}
			}
		}
		clientSettings.loadFromProperties(properties);
	}
}
