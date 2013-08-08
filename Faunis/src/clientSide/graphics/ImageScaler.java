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
package clientSide.graphics;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageScaler {
	public static BufferedImage downscale(BufferedImage image, float scaleFactor) {
		assert(scaleFactor > 0 && scaleFactor <= 1);
		ScalerResult first = downscaleByPowerOfTwo(image, scaleFactor);
		ScalerResult second = downscaleFull(first.image, first.remainingScaleFactor);
		return second.image;
	}
	
	private static ScalerResult downscaleByPowerOfTwo(BufferedImage image, float scaleFactor) {
		if (scaleFactor >= 0.5)
			return new ScalerResult(image, scaleFactor);
		int multiplier = 1;
		while (scaleFactor < 0.5) {
			scaleFactor *= 2;
			multiplier *= 2;
		}
		int newWidth = Math.round(image.getWidth() / ((float) multiplier));
		int newHeight = Math.round(image.getHeight() / ((float) multiplier));
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2d = (Graphics2D) scaledImage.getGraphics();
		graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics2d.drawImage(image, 0, 0, newWidth, newHeight, null);
		graphics2d.dispose();
		return new ScalerResult(scaledImage, scaleFactor);
	}
	
	private static ScalerResult downscaleFull(BufferedImage image, float scaleFactor) {
		int newWidth = Math.round(image.getWidth() * scaleFactor);
		int newHeight = Math.round(image.getHeight() * scaleFactor);
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
		Graphics2D graphics2d = (Graphics2D) scaledImage.getGraphics();
		graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics2d.drawImage(image, 0, 0, newWidth, newHeight, null);
		graphics2d.dispose();
		return new ScalerResult(scaledImage, scaleFactor);
	}
	
	private static class ScalerResult {
		ScalerResult(BufferedImage image, float scaleFactor) {
			this.image = image;
			this.remainingScaleFactor = scaleFactor;
		}
		
		BufferedImage image;
		float remainingScaleFactor;
	}
}
