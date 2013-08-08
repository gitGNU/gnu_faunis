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
package clientSide.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import clientSide.ClientSettings;
import clientSide.client.Client;
import clientSide.graphics.Decoration;
import clientSide.graphics.Drawable;
import clientSide.player.PlayerGraphics;

import common.Logger;
import common.enums.ClientStatus;
import common.enums.Direction;
import common.movement.MovingTask;

/** Extends GraphWin by functionality specific to Faunis. */
public class GameWindow extends GraphWin {
	protected final Client parent;
	
	private final MenuManager menuManager;
	private final JPanel commandPanel;
	private final JScrollPane loggingScrollPane;
	private final JTextPane loggingTextPane;
	private final StyledDocument loggingDocument;
	private final JButton commandSendButton;
	protected final JTextField commandEdit;
	
	private final SimpleAttributeSet errorTextStyle;
	private final SimpleAttributeSet systemTextStyle;
	private final SimpleAttributeSet whisperTextStyle;
	private final SimpleAttributeSet oldCommandTextStyle;
	private final SimpleAttributeSet broadcastTextStyle;
	
	private MyMouseListener mouseListener;
	
	public GameWindow(Client parent, int width, int height, String title) {
		super(width, height, title, false);
		this.parent = parent;
		
		graph2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		
		errorTextStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(errorTextStyle, Color.red);
		systemTextStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(systemTextStyle, new Color(0, 120, 120));
		whisperTextStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(whisperTextStyle, Color.blue);
		oldCommandTextStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(oldCommandTextStyle, Color.gray);
		broadcastTextStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(broadcastTextStyle, new Color(140, 80, 0));
		
		loggingTextPane = new JTextPane();
		loggingTextPane.setEditable(false);
		loggingTextPane.setBackground(new Color(230, 230, 230));
		
		loggingDocument = loggingTextPane.getStyledDocument();
		assert(loggingDocument != null);
		
		loggingScrollPane = new JScrollPane(loggingTextPane);
		loggingScrollPane.setPreferredSize(new Dimension(100,100));
		JPanel loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.add(BorderLayout.CENTER, loggingScrollPane);
		
		commandEdit = new JTextField();
		commandEdit.setPreferredSize(new Dimension(300,25));
		
		commandSendButton = new JButton("Send");
		commandSendButton.addActionListener(new CommandSendButtonListener());
		
		commandPanel = new JPanel();
		commandPanel.add(commandEdit);
		commandPanel.add(commandSendButton);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawingPanel, loggingPanel);
		drawingPanel.setPreferredSize(new Dimension(width, height));
		drawingPanel.setMaximumSize(new Dimension(width, height));
		loggingPanel.setMinimumSize(new Dimension(100, 100));
		splitPane.setResizeWeight(0); // commandPanel gets any extra space occurring
		
		win.getRootPane().setDefaultButton(commandSendButton);
		mouseListener = new MyMouseListener();
		drawingPanel.addMouseListener(mouseListener);
		
		menuManager = new MenuManager(parent);
		win.setJMenuBar(menuManager.menuBar);
		
		win.getContentPane().add(BorderLayout.CENTER, splitPane);
		win.getContentPane().add(BorderLayout.SOUTH, commandPanel);
		
		win.pack();
		Logger.log("packed");
	}
	
	
	
	public void notifyClientStatus(ClientStatus newStatus) {
		setTitle("Faunis - "+newStatus);
		menuManager.updateStatusMenu(newStatus);
	}
	
	

	@Override
	/** locks parent.clientStatus, (parent.currentPlayers; playerGraphics)
	 * redraws Backbuffer (please call repaint() to copy it to the front) */
	public void draw() {
		graph.setColor(parent.getClientSettings().mapBackgroundColor());
		graph.fillRect(0, 0, img.getWidth(), img.getHeight());
		
		Point mausPos = this.mousePos();
		if (mausPos == null) mausPos = new Point(0,0);

		// depending on Client.clientStatus, other things have to be drawn
		synchronized(parent.getClientStatus()) {
			ClientStatus status = parent.getClientStatus();
			
			if (status == ClientStatus.exploring || status == ClientStatus.fighting) {
				// draw grid:
				drawFieldGrid();
				// write map and player name:
				writeMapAndPlayerInfo();
				// draw all player graphics:
				drawAllDrawables();
			}
			
			// draw clientStatus:
			drawClientStatus();
		}
	}
	
	private void drawFieldGrid() {
		graph.setColor(Color.lightGray);
		int width = img.getWidth();
		int height = img.getHeight();
		int fieldWidth = parent.getClientSettings().fieldWidth();
		int fieldHeight = parent.getClientSettings().fieldHeight();
		int maxRow = height / fieldHeight + 1;
		int maxColumn = width / fieldWidth + 1;
		// draw horizontal lines:
		for (int y = 0; y < maxRow; y++) {
			graph.drawRect(0, y*fieldHeight, width, 0);
		}
		// draw vertical lines:
		for (int x = 0; x < maxColumn; x++) {
			graph.drawRect(x*fieldWidth, 0, 0, height);
		}
	}
	
	private void writeMapAndPlayerInfo() {
		String mapName = parent.getCurrentMapName();
		String playerName = parent.getCurrentPlayerName();

		graph.setColor(Color.black);
		if (mapName != null) graph.drawString(mapName, 10, 20);
		if (playerName != null) {
			PlayerGraphics player = parent.getPlayerGraphics(playerName);
			int x = player.getX();
			int y = player.getY();
			graph.drawString(playerName, 10, 30);
			graph.drawString("("+x+","+y+")", 10, 40);
		}
	}
	
	private void drawClientStatus() {
		ClientStatus status = parent.getClientStatus();
		graph.setColor(Color.black);
		graph.drawString(status.toString(), 10, 10);
	}
	
	/** locks zOrderedDrawables; single playerGraphics / decorations <br/>
	 * Draws all characters. */
	private void drawAllDrawables() {
		ArrayList<Drawable> allDrawables = parent.getAllDrawablesToDraw();
		for (Drawable drawable : allDrawables) {
			synchronized(drawable) {
				if (drawable instanceof PlayerGraphics) {
					PlayerGraphics playerGraphics = (PlayerGraphics) drawable;
					Point offset = calculateDrawingOffset(playerGraphics);
					String playerName = playerGraphics.getName();
					int playerNameWidth = graph.getFontMetrics().stringWidth(playerName);
					graph.setColor(Color.black);
					graph.drawString(playerName, offset.x - playerNameWidth/2, offset.y + 15);
					playerGraphics.draw(graph, offset.x, offset.y);
				} else if (drawable instanceof Decoration) {
					Decoration decoration = (Decoration) drawable;
					Point offset = calculateDrawingOffset(decoration);
					decoration.draw(graph, offset.x, offset.y);
				} else {
					throw new RuntimeException("drawAllDrawables(): Could not identify Drawable!");
				}
			}
		}
	}
	
	private Point calculateDrawingOffset(PlayerGraphics playerGraphics) {
		ClientSettings clientSettings = parent.getClientSettings();
		int numDeltaLevels = clientSettings.numberOfDeltaLevelStates();
		int fieldWidth = clientSettings.fieldWidth();
		int fieldHeight = clientSettings.fieldHeight();
		Direction direction = playerGraphics.getDirection();
		Point delta = MovingTask.directionToDelta(direction);
		int deltaHoriz = (fieldWidth/numDeltaLevels) * delta.x;
		int deltaVert = (fieldHeight/numDeltaLevels) * delta.y;
		int deltaLevel = playerGraphics.getDeltaLevel();
		int fieldX = playerGraphics.getX();
		int fieldY = playerGraphics.getY();
		// calculate resulting coordinates:
		int offsetX = fieldWidth/2 + fieldWidth * fieldX + deltaLevel*deltaHoriz;
		int offsetY = fieldHeight/2 + fieldHeight * fieldY + deltaLevel*deltaVert;
		return new Point(offsetX, offsetY);
	}
	private Point calculateDrawingOffset(Decoration decoration) {
		ClientSettings clientSettings = parent.getClientSettings();
		Point decorationPoint = new Point(decoration.getX(), decoration.getY());
		return clientSettings.mapFieldToCenterPixel(decorationPoint);
	}
	
	public Point getPosition(){
		return new Point(win.getLocation());
	}
	
	protected class CommandSendButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String command = commandEdit.getText();
			logOldCommandMessage(command);
			if (command.length() > 0 &&
				command.startsWith("/"))
			{
				String[] commandSplit = command.split(" ");
				assert(commandSplit.length > 0);
				final String commandPrefix = commandSplit[0];
				final String[] commandRest = new String[commandSplit.length-1];
				for (int i = 1; i < commandSplit.length; i++) {
					commandRest[i-1] = commandSplit[i];
				}
				// evaluate command:
		        boolean success = parent.parseCommand(commandPrefix, commandRest);
		        if (success)
		        	commandEdit.setText(null);
			}
		}
	}
	
	protected class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			parent.mouseClick(e.getPoint());
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
	
	private void logMessage(String message, SimpleAttributeSet set) {
		try {
			loggingDocument.insertString(loggingDocument.getLength(),
										 message+"\n", set);
			this.loggingTextPane.setCaretPosition(loggingDocument.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
			Logger.log("Error appending a message to the loggingPane!");
		}
	}
	
	public void logErrorMessage(String message) {
		logMessage(message, errorTextStyle);
	}
	public void logWhisperMessage(String message) {
		logMessage(message, whisperTextStyle);
	}
	public void logOldCommandMessage(String message) {
		logMessage(message, oldCommandTextStyle);
	}
	public void logBroadcastMessage(String message) {
		logMessage(message, broadcastTextStyle);
	}
	
	public void logSystemMessage(String message) {
		logMessage(message, systemTextStyle);
	}
}
