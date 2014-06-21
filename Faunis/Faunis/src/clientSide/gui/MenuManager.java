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
package clientSide.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import clientSide.client.Client;
import clientSide.userToClientOrders.UCConnectOrder;
import clientSide.userToClientOrders.UCCreatePlayerOrder;
import clientSide.userToClientOrders.UCDisconnectOrder;
import clientSide.userToClientOrders.UCHelpOrder;
import clientSide.userToClientOrders.UCLoadPlayerOrder;
import clientSide.userToClientOrders.UCLoginOrder;
import clientSide.userToClientOrders.UCLogoutOrder;
import clientSide.userToClientOrders.UCQueryOwnPlayersOrder;
import clientSide.userToClientOrders.UCServerSourceOrder;
import clientSide.userToClientOrders.UCUnloadPlayerOrder;
import common.enums.ClientStatus;

public class MenuManager {
	public final JMenuBar menuBar;
	
	public final JMenu gameMenu;
	public final JMenuItem settingsItem;
	public final JMenuItem exitItem;
	
	public final JMenu statusMenu;
	public final JMenuItem connectItem;
	public final JMenuItem disconnectItem;
	public final JMenuItem loginItem;
	public final JMenuItem logoutItem;
	public final JMenuItem loadPlayerItem;
	public final JMenuItem unloadPlayerItem;
	public final JMenuItem queryOwnPlayersItem;
	public final JMenuItem newPlayerItem;
	
	public final JMenu helpMenu;
	public final JMenuItem listCommandsItem;
	public final JMenuItem queryServerSourceItem;
	public final JMenuItem aboutItem;
	
	public MenuManager(final Client client) {
		
		settingsItem = new JMenuItem("Settings");
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.shutdown();
			}
		});
		gameMenu = new JMenu("Game");
		gameMenu.setMnemonic('G');
		gameMenu.add(settingsItem);
		gameMenu.add(exitItem);
		
		connectItem = new JMenuItem("Connect");
		connectItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.putUCOrder(new UCConnectOrder());
			}
		});
		disconnectItem = new JMenuItem("Disconnect");
		disconnectItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.putUCOrder(new UCDisconnectOrder());
			}
		});
		loginItem = new JMenuItem("Log in");
		loginItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: Create a custom dialog class from this, giving usernameField the focus
				JPanel panel = new JPanel();
				JTextField usernameField = new JTextField(12);
				JTextField passwordField = new JPasswordField(12);
				panel.add(new JLabel("Username:"));
				panel.add(usernameField);
				panel.add(new JLabel("Password:"));
				panel.add(passwordField);
				int result = JOptionPane.showConfirmDialog(null, panel, "Login credentials",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					String username = usernameField.getText();
					String password = passwordField.getText();
					client.putUCOrder(new UCLoginOrder(username, password));
				}
			}
		});
		logoutItem = new JMenuItem("Log out");
		logoutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.putUCOrder(new UCLogoutOrder());
			}
		});
		loadPlayerItem = new JMenuItem("Load player");
		loadPlayerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel panel = new JPanel();
				JTextField playernameField = new JTextField(12);
				panel.add(new JLabel("Player name:"));
				panel.add(playernameField);
				int result = JOptionPane.showConfirmDialog(null, panel, "Load player",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					String playername = playernameField.getText();
					client.putUCOrder(new UCLoadPlayerOrder(playername));
				}
			}
		});
		unloadPlayerItem = new JMenuItem("Unload player");
		unloadPlayerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.putUCOrder(new UCUnloadPlayerOrder());
			}
		});
		queryOwnPlayersItem = new JMenuItem("Query own players");
		queryOwnPlayersItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.putUCOrder(new UCQueryOwnPlayersOrder());
			}
		});
		newPlayerItem = new JMenuItem("Create new player");
		newPlayerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel panel = new JPanel();
				JTextField playernameField = new JTextField(12);
				panel.add(new JLabel("New player name:"));
				panel.add(playernameField);
				int result = JOptionPane.showConfirmDialog(null, panel, "Create new player",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					String playername = playernameField.getText();
					client.putUCOrder(new UCCreatePlayerOrder(playername));
				}
			}
		});
		
		statusMenu = new JMenu("Status");
		statusMenu.setMnemonic('S');
		statusMenu.add(connectItem);
		statusMenu.add(disconnectItem);
		statusMenu.addSeparator();
		statusMenu.add(loginItem);
		statusMenu.add(logoutItem);
		statusMenu.addSeparator();
		statusMenu.add(loadPlayerItem);
		statusMenu.add(unloadPlayerItem);
		statusMenu.add(queryOwnPlayersItem);
		statusMenu.add(newPlayerItem);
		
		listCommandsItem = new JMenuItem("Instructions");
		listCommandsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						client.putUCOrder(new UCHelpOrder());
					}
				}).start();
			}
		});
		queryServerSourceItem = new JMenuItem("Query server source");
		queryServerSourceItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						client.putUCOrder(new UCServerSourceOrder());
					}
				}).start();
			}
		});
		aboutItem = new JMenuItem("About Faunis");
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageIcon aboutIcon = new ImageIcon(
					client.getGraphicsContentManager().getGUIGraphics("about")
				);
				JOptionPane.showMessageDialog(null, "Faunis - a furry MMORPG\n" +
						"Copyright 2012 - 2014 Simon Ley alias \"skarute\"",
						"About Faunis", JOptionPane.INFORMATION_MESSAGE, aboutIcon);
			}
		});
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		helpMenu.add(listCommandsItem);
		helpMenu.add(queryServerSourceItem);
		helpMenu.add(aboutItem);
		
		menuBar = new JMenuBar();
		menuBar.add(gameMenu);
		menuBar.add(statusMenu);
		menuBar.add(helpMenu);
	}
	
	public void updateStatusMenu(final ClientStatus newStatus) {
		switch(newStatus) {
		case disconnected:
			statusMenuDisableAllBut(new JMenuItem[] {connectItem});
			break;
		case loggedOut:
			statusMenuDisableAllBut(new JMenuItem[] {disconnectItem, loginItem});
			break;
		case noCharLoaded:
			statusMenuDisableAllBut(new JMenuItem[] {disconnectItem, logoutItem, loadPlayerItem, queryOwnPlayersItem, newPlayerItem});
			break;
		case exploring:
		case fighting:
			statusMenuDisableAllBut(new JMenuItem[] {disconnectItem, logoutItem, unloadPlayerItem, queryOwnPlayersItem, newPlayerItem});
			break;
		}
	}
	
	protected void statusMenuDisableAllBut(JMenuItem[] toEnable) {
		final HashSet<JMenuItem> toEnableSet = new HashSet<JMenuItem>();
		Collections.addAll(toEnableSet, toEnable);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (Component component : statusMenu.getMenuComponents()) {
					if (component instanceof JMenuItem) {
						JMenuItem item = (JMenuItem) component;
						if (toEnableSet.contains(item))
							item.setEnabled(true);
						else
							item.setEnabled(false);
					}
				}
			}
		});
	}
}
