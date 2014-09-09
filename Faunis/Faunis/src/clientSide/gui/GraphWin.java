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
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;


/** A basic class written for general game purpose. */
public abstract class GraphWin {
	public final JFrame win;
	public final DrawingPanel drawingPanel;
	public final BufferedImage image;
	public final Graphics graphics;
	public final Graphics2D graphics2d;
	protected int renderXOffset;
	protected int renderYOffset;
	protected int renderWidth;
	protected int renderHeight;

	public GraphWin(int imageWidth, int imageHeight, String title, boolean addDrawingPanel){
		renderXOffset = 0;
		renderYOffset = 0;
		renderWidth = imageWidth;
		renderHeight = imageHeight;
		drawingPanel = new DrawingPanel();
		drawingPanel.setPreferredSize(new Dimension(imageWidth, imageHeight));
		drawingPanel.addComponentListener(new DrawingPanelResizeListener());
		drawingPanel.setIgnoreRepaint(true);
		image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		graphics = this.image.createGraphics();
		graphics2d = (Graphics2D) this.graphics;

		win = new JFrame();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setResizable(true);
		win.setTitle(title);
		if (addDrawingPanel) {
			win.getContentPane().add(BorderLayout.CENTER, drawingPanel);
		}
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
	public void setIcon(final Image icon) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					win.setIconImage(icon);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	public int getImageWidth(){
		return this.image.getWidth();
	}
	public int getImageHeight(){
		return this.image.getHeight();
	}

	public abstract void draw();

	public Point mousePos(){
		return drawingPanel.getMousePosition();
	}
	public void repaint(){
		drawingPanel.repaint();
	}
	public void setColor(Color c){
		graphics.setColor(c);
	}
	public void setColor(int red, int green, int blue){
		Color temp = new Color(red, green, blue);
		graphics.setColor(temp);
	}
	public Color getColor(){
		return graphics.getColor();
	}
	public int getRed(){
		return graphics.getColor().getRed();
	}
	public int getGreen(){
		return graphics.getColor().getGreen();
	}
	public int getBlue(){
		return graphics.getColor().getBlue();
	}

	public void clear(){
		Color temp = graphics.getColor();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, this.getImageWidth(), this.getImageHeight());
		graphics.setColor(temp);
	}
	
	/** 
	 * Calculates the coordinates in the rendered image (independent from
	 * scale factor and current offsets) from given mouse coordinates.
	 */
	public Point calculateViewPoint(Point mousePoint) {
		if (mousePoint == null) {
			return null;
		}
		double x = mousePoint.x;
		double y = mousePoint.y;
		x -= renderXOffset;
		y -= renderYOffset;
		x *= image.getWidth() / (double) renderWidth;
		y *= image.getHeight() / (double) renderHeight;
		return new Point((int) Math.round(x), (int) Math.round(y));
	}
	
	public void resizeToScaleFactor(final double scaleFactor) {
		drawingPanel.setPreferredSize(
			new Dimension(
				(int) Math.round(scaleFactor * image.getWidth()),
				(int) Math.round(scaleFactor * image.getHeight())
			)
		);
		win.pack();
	}

	public class DrawingPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(
				GraphWin.this.image, GraphWin.this.renderXOffset, GraphWin.this.renderYOffset,
				GraphWin.this.renderWidth, GraphWin.this.renderHeight, this
			);
		}
	}
	
	public class DrawingPanelResizeListener implements ComponentListener {
		@Override
		public void componentResized(ComponentEvent e) {
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			int panelWidth = drawingPanel.getWidth();
			int panelHeight = drawingPanel.getHeight();
			double imageRelation = imageWidth / (double) imageHeight;
			double currentRelation = panelWidth / (double) panelHeight;
			double scaleFactor;
			renderXOffset = 0;
			renderYOffset = 0;
			if (currentRelation > imageRelation) {
				// there is more width than height for the image
				scaleFactor = panelHeight / (double) imageHeight;
				renderWidth = (int) Math.round(imageWidth * scaleFactor);
				renderHeight = panelHeight;
				renderXOffset = (panelWidth - renderWidth) / 2;
			} else {
				// there is more height than width for the image
				scaleFactor = panelWidth / (double) imageWidth;
				renderWidth = panelWidth;
				renderHeight = (int) Math.round(imageHeight * scaleFactor);
				renderYOffset = (panelHeight - renderHeight) / 2;
			}
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}
		@Override
		public void componentShown(ComponentEvent e) {
		}
		@Override
		public void componentHidden(ComponentEvent e) {
		}
	}
}
