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
package common.graphics.osseous;

import java.io.IOException;
import java.util.HashMap;

import common.graphics.graphicsContentManager.OsseousManager;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.PathMacros;

/**
 * Applies "redirectCollection":
 * Modifies a given sourceCollection to redirect to given targetCollection, whereas
 * given redirectCollectionPath is the "redirectCollection" property.
 */
public class RedirectedCollectionCreator {

	private BoneCollection targetCollection;
	private ClearPath redirectCollectionPath;
	private BoneCollection sourceCollection;

	public RedirectedCollectionCreator(
		ClearPath redirectCollectionPath, BoneCollection sourceCollection, BoneCollection targetCollection
	) {
		this.targetCollection = targetCollection;
		this.sourceCollection = sourceCollection;
		this.redirectCollectionPath = redirectCollectionPath;
		assert(this.redirectCollectionPath.toString().length() > 0);
	}

	/**
	 * 
	 * Requires targetCollection to be loaded.
	 * NOTE: This will recursively load the whole redirected targetCollection down to the
	 * Bones (exclusive). */
	public void create() throws IOException, NotFoundException {
		targetCollection.assertLoad();
		createRecursive(sourceCollection, targetCollection, 0);
	}

	private void createRecursive(
		BoneCollection createdCurrent, BoneCollection currentTargetCollection, int currentDepth
	) throws IOException, NotFoundException {
		// we require currentTargetCollection to be loaded
		OsseousManager<?> manager = currentTargetCollection.manager;
		if (currentDepth > 0) {
			createdCurrent.meta = new HashMap<String, String>(createdCurrent.parent.meta);
			createdCurrent.bequeathToBones = new HashMap<String, String>(
				createdCurrent.parent.bequeathToBones
			);
			createdCurrent.loaded = true;
		}
		createdCurrent.bequeathToBones.put("redirectBone", redirectBonePath(currentDepth));

		for (Osseous childOsseous : currentTargetCollection.elements.values()) {
			if (childOsseous instanceof BoneCollection) {
				BoneCollection child = (BoneCollection) childOsseous;
				// assert that the child collection is loaded:
				child.assertLoad();
				BoneCollection createdChild = new BoneCollection(manager, createdCurrent, child.name());
				createRecursive(createdChild, child, currentDepth+1);
				createdCurrent.elements.put(child.name(), createdChild);
			} else if (childOsseous instanceof Bone) {
				Bone child = (Bone) childOsseous;
				Bone createdChild = new Bone(manager, createdCurrent, child.name());
				createdCurrent.elements.put(child.name(), createdChild);
			}
		}
	}

	private String redirectBonePath(int depth) {
		StringBuilder builder = new StringBuilder();
		// first, go up to sourceCollection
		for (int i = 0; i <= depth; i++) {
			builder.append(PathMacros.up + "/");
		}
		// second, go to redirected collection
		builder.append(redirectCollectionPath);
		// third, reconstruct this bone's subpath
		for (int i = depth; i >= 0; i--) {
			builder.append("/" + PathMacros.ancestorName + i);
		}
		return builder.toString();
	}

}
