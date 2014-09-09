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
package common.graphics.graphicsContentManager;

import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.enums.BodyPart;
import common.enums.Direction;
import common.graphics.osseous.Bone;
import common.graphics.osseous.BoneCollection;
import common.graphics.osseous.FrameAndOffsets;
import common.graphics.osseous.NotFoundException;
import common.graphics.osseous.OffsetPoint;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;
import clientSide.graphics.ColourBoneTranslator;
import clientSide.player.ClientPlayer;



public class PlayerGraphicsContentManager extends OsseousManager<ClientPlayer> {

	private ColourBoneTranslator translator;
	/**
	 * indicates for each direction in which order the body parts
	 * (if limbed composition) should be drawn
	 */
	private HashMap<Direction, List<BodyPart>> drawingOrders;

	public PlayerGraphicsContentManager(GraphicsContentManager parent) {
		super(parent);
		this.translator = ColourBoneTranslator.getInstance();
		loadDrawingOrders();
	}

	public HashMap<Direction, List<BodyPart>> getDrawingOrders() {
		return this.drawingOrders;
	}

	public List<BodyPart> getDrawingOrder(Direction direction) {
		return this.drawingOrders.get(direction);
	}

	/** determines in which order body parts have to be drawn */
	public void loadDrawingOrders() {
		drawingOrders = new HashMap<Direction, List<BodyPart>>();
		List<BodyPart> leftList = Arrays.asList(
				  new BodyPart[] {BodyPart.rightArm,
				  BodyPart.rightLeg, BodyPart.body, BodyPart.tail,
				  BodyPart.head, BodyPart.leftLeg, BodyPart.leftArm}
				);
		drawingOrders.put(Direction.left, leftList);

		List<BodyPart> rightList = Arrays.asList(
				  new BodyPart[] {BodyPart.leftArm,
				  BodyPart.leftLeg, BodyPart.body, BodyPart.tail,
				  BodyPart.head, BodyPart.rightLeg, BodyPart.rightArm}
				);
		drawingOrders.put(Direction.right, rightList);

		List<BodyPart> downList = Arrays.asList(
				  new BodyPart[] {BodyPart.tail, BodyPart.body,
				  BodyPart.leftLeg, BodyPart.rightLeg, BodyPart.leftArm,
				  BodyPart.rightArm, BodyPart.head}
				);
		drawingOrders.put(Direction.down, downList);

		List<BodyPart> upList = Arrays.asList(
				  new BodyPart[] {BodyPart.body,
				  BodyPart.leftLeg, BodyPart.rightLeg, BodyPart.leftArm,
				  BodyPart.rightArm, BodyPart.head, BodyPart.tail}
				);
		drawingOrders.put(Direction.up, upList);
	}

	@Override
	public void draw(
		ClientPlayer drawable, int x, int y, Graphics drawOnto
	) throws IOException, NotFoundException {
		BodyPart origin = BodyPart.body;
		drawRecursive(
			drawable, drawOnto, origin, translator.bodyPartToId(origin),
			null, x, y, getDrawingOrder(drawable.getDirection())
		);
	}

	private void drawRecursive(
		ClientPlayer drawable, Graphics drawOnto, BodyPart currentPart, int currentId,
		BodyPart sourcePart, float x, float y, List<BodyPart> drawingOrder
	) throws IOException, NotFoundException {
		ClearPath path = new ClearPath();
		String characterClass = drawable.getType().toString();
		path.add(characterClass);
		String animation;
		if (drawable.hasPath()) {
			animation = "walk";
		} else if (drawable.getAnimation() != null) {
			animation = drawable.getAnimation();
		} else {
			animation = "stand";
		}
		path.add(animation);
		path.add(currentPart.toString());
		String direction = drawable.getDirection().toString();
		path.add(direction);
		// Now fetch the current bone to draw. If it is not found, fall back to
		// the "stand" animation with frame 0.
		Bone currentBone = null;
		MacroInfo macroInfo = new MacroInfo(drawable.getFrame(), drawable.getMood());
		boolean found = true;
		try {
			currentBone = (Bone) this.resolve(path, macroInfo);
		} catch(NotFoundException e) {
			found = false;
		}
		if (!found) {
			path.set(1, "stand");
			// use macroInfo with frame index = 0
			macroInfo = new MacroInfo(0, drawable.getMood());
			currentBone = (Bone) this.resolve(path, macroInfo);
		} else {
			
		}

		FrameAndOffsets frameAndOffsets = currentBone.frameAndOffsets(macroInfo);

		Map<Integer, OffsetPoint> offsets = frameAndOffsets.offsets(currentBone.defaultScale());
		OffsetPoint currentOffset = offsets.get(currentId);

		for (BodyPart toDraw : drawingOrder) {
			for (Integer outgoingId : offsets.keySet()) {
				OffsetPoint outgoingOffset = offsets.get(outgoingId);
				BodyPart outgoingPart = translator.idToBodyPart(outgoingId);
				if (outgoingPart != sourcePart && outgoingPart == toDraw) {

					drawRecursive(
						drawable, drawOnto, outgoingPart, outgoingId, currentPart,
						x-currentOffset.x+outgoingOffset.x, y-currentOffset.y+outgoingOffset.y,
						drawingOrder
					);
				}
				if (toDraw == currentPart) {
					Point roundedPosition = new OffsetPoint(x, y).roundToInt();
					frameAndOffsets.draw(
						currentBone.defaultScale(), drawOnto, roundedPosition,
						currentId
					);
					// Check if there are any accessoires that we may paint over for this body part:
					for (String accessoireName : drawable.getAccessoires()) {
						BoneCollection accessoire = (BoneCollection) resolve(
							new ClearPath(
								characterClass, "accessoires",
								accessoireName, animation
							), macroInfo
						);
						if (accessoire.contains(currentPart.toString())) {
							Bone accessoireBone = (Bone) accessoire.resolve(
								new ClearPath(currentPart.toString(), direction), macroInfo
							);
							FrameAndOffsets accessoireFrame = accessoireBone.frameAndOffsets(
								macroInfo
							);
							accessoireFrame.draw(
								accessoireBone.defaultScale(), drawOnto, roundedPosition,
								currentId
							);
						}
					}
				}
			}
		}
	}

	@Override
	public String getGraphicsPath() {
		return parent.settings.playerGraphicsPath();
	}


}
