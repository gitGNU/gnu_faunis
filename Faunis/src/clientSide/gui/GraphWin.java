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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

import common.Logger;

/** A basic class written for general game purpose. */
public abstract class GraphWin {
	public boolean repaint = false;
	public final JFrame win;
	public final DrawingPanel drawingPanel;
	public final BufferedImage img;
	public final Graphics graph;
	public final Graphics2D graph2d;
	private int width;
	private int height;
	
	public GraphWin(int width, int height, String title, boolean addDrawingPanel){
		this.width = width;
		this.height = height;
		this.drawingPanel = new DrawingPanel();
		drawingPanel.setIgnoreRepaint(true);
		this.img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		this.graph = this.img.createGraphics();
		this.graph2d = (Graphics2D) this.graph;
		
		drawingPanel.parent = this;
		win = new JFrame();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setSize(width, height);
		win.setResizable(false);
		win.setTitle(title);
		if (addDrawingPanel)
			win.getContentPane().add(BorderLayout.CENTER, drawingPanel);
	}
	
	public void show(){
		/* NOTE: Swing is single-threaded. And as I have learned, it
		 * causes problems even when only one thread is accessing it at all.
		 * So we introduce the "good" / complicated way to deal with Swing
		 * stuff: Runnables for the Event Dispatching Thread (EDT).
		 */
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					win.setVisible(true);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void hide(){
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					win.setVisible(false);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}	}
	
	public void setTitle(final String title){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				win.setTitle(title);
			}
		});
	}
	public String getTitle(){
		return win.getTitle();
	}
	public int getWidth(){
		return this.width;
	}
	public int getHeight(){
		return this.height;
	}
	
	public abstract void draw();
	
	public Point mousePos(){
		return drawingPanel.getMousePosition();
	}
	public void repaint(){
		drawingPanel.repaint();
	}
	public void setColor(Color c){
		graph.setColor(c);
	}
	public void setColor(int red, int green, int blue){
		Color temp = new Color(red, green, blue);
		graph.setColor(temp);
	}
	public Color getColor(){
		return graph.getColor();
	}
	public int getRed(){
		return graph.getColor().getRed();
	}
	public int getGreen(){
		return graph.getColor().getGreen();
	}
	public int getBlue(){
		return graph.getColor().getBlue();
	}
	
	public void clear(){
		Color temp = graph.getColor();
		graph.setColor(Color.white);
		graph.fillRect(0, 0, this.getWidth(), this.getHeight());
		graph.setColor(temp);
	}
	
	public static void delay(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Logger.log("could not delay!");
			e.printStackTrace();
		}
	}
	
	public class DrawingPanel extends JPanel{
		private static final long serialVersionUID = 1L;
		public GraphWin parent;
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(parent.img, 0, 0, this);
		}
	}
}
