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
package communication;

import java.io.Serializable;
import java.util.ArrayList;


public class Map implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private int width;
	private int height;
	private ArrayList<GraphicalDecoStatus> decoInfos;
	private ArrayList<Link> links;
	
	public Map(String name, int width, int height,
				ArrayList<GraphicalDecoStatus> decoInfos,
				ArrayList<Link> links) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.decoInfos = decoInfos;
		this.links = links;
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public ArrayList<GraphicalDecoStatus> getDecoInfos() {
		return decoInfos;
	}
	
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	public Link getOutgoingLink(int x, int y) {
		if (links == null)
			return null;
		for (Link link : links) {
			if (x==link.getSourceX() && y==link.getSourceY()) {
				return link;
			}
		}
		return null;
	}
}
