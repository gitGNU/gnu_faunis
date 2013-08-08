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
package common.graphics;

import java.awt.image.BufferedImage;

public class ResolvedImageResult {
	
	public ResolvedImageResult(BufferedImage image, String path) {
		this.image = image;
		this.path = path;
	}
	
	private final BufferedImage image;
	private final String path;
	
	public BufferedImage getImage() {
		return image;
	}
	public String getPath() {
		return path;
	}
	
}