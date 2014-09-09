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
package common.graphics;

import java.util.HashMap;
import java.util.Map;

import common.graphics.osseous.OffsetPoint;

/**
 * @author user
 *
 */
public class OffsetsScaler {
	public static Map<Integer, OffsetPoint> scale(Map<Integer, OffsetPoint> offsets, float scaleFactor) {
		HashMap<Integer, OffsetPoint> result = new HashMap<Integer, OffsetPoint>();
		for (Integer key : offsets.keySet()) {
			OffsetPoint originalOffset = offsets.get(key);
			result.put(
				key, new OffsetPoint(
					originalOffset.x*scaleFactor,
					originalOffset.y*scaleFactor
				)
			);
		}
		return result;
	}
}
