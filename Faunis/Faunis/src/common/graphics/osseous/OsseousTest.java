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

import static org.junit.Assert.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.Tools;
import clientSide.graphics.Drawable;
import common.Settings;
import common.enums.AniEndType;
import common.graphics.graphicsContentManager.GraphicsContentManager;
import common.graphics.graphicsContentManager.OsseousManager;
import common.graphics.osseous.path.ClearPath;


public class OsseousTest {
	private TestSettings settings;
	private TestOsseousManager manager;
	private File tmpDir;
	private Color TRANSPARENT;
	private AlphaComposite sourceOver;

	private class TestSettings extends Settings {
		public TestSettings() {
			super();
		}

		public void graphicsPath(String path) {
			this.graphicsPath = path;
		}
	}

	private class TestOsseousManager extends OsseousManager<Drawable> {

		public TestOsseousManager(GraphicsContentManager parent) {
			super(parent);
		}

		@Override
		public String getGraphicsPath() {
			return parent.settings().graphicsPath();
		}

		@Override
		public void draw(Drawable drawable, int x, int y, Graphics drawOnto)
				throws IOException, NotFoundException {
			throw new IOException("Not supported in test");
		}

	}

	private void trySaveProperties(Properties properties, File file) {
		/** Use custom save method such that no date comment is written >:o( */
		PrintWriter printer = null;
		try {
			try {
				printer = new PrintWriter(file);
			} catch (FileNotFoundException e1) {
				fail();
				return;
			}
			for (Object key : properties.keySet()) {
				Object value = properties.get(key);
				printer.println((String)key+"="+(String)value);
			}
		} finally {
			printer.close();
		}
	}

	private void trySaveImage(BufferedImage image, String formatName, File file) {
		boolean success = false;
		try {
			success = ImageIO.write(image, formatName, file);
		} catch(IOException e) {
			fail();
		}
		assertTrue(success);
	}

	@Before
	public void setUp() {
		/**
		 * Define the following structure:
		 * collection a [someMetaKey=someMetaValue, numberOfFrames=2] {
		 *   aBone1 [someOtherMetaKey=someOtherMetaValue, endType="end", millisecsPerFrame=200] {
		 *   	frame0 [has offset with id 0 at left upper pixel],
		 *   	frame1 [has offset with id 0 at right upper pixel]
		 *   };
		 *   aBone2 [endType="revert", millisecsPerFrame=100, numberOfFrames=3] {
		 *     frame0 [has offset with id 0 at right lower pixel],
		 *     frame1 [redirect=../aBone1/0, mirrorHorizontally=true]
		 *     frame2 [redirect=aBone2/0, mirrorHorizontally=true]
		 *   };
		 * }
		 * collection b [redirect=../../a/aBone1, replaceOffsets=0/1] {
		 *   bBone1 [] {};
		 * }
		 */
		settings = new TestSettings();
		manager = new TestOsseousManager(new GraphicsContentManager(settings));
		TRANSPARENT = new Color(0, 0, 0, 0);
		sourceOver = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

		boolean success = false;
		File systemTmpDir = new File(System.getProperty("java.io.tmpdir"));
		assertTrue(systemTmpDir.exists() && systemTmpDir.isDirectory() && systemTmpDir.canWrite());
		for (int i=0; i<Integer.MAX_VALUE; i++) {
			tmpDir = new File(systemTmpDir, "tmp"+i);
			if (!tmpDir.exists()) {
				success = tmpDir.mkdir();
				if (success) {
					break;
				}
			}
		}
		assertTrue(success);
		System.out.println("Created tmpDir "+tmpDir.getPath());
		settings.graphicsPath(tmpDir.getPath());

		// write property files
		new File(tmpDir, "a/aBone1").mkdirs();
		new File(tmpDir, "a/aBone2").mkdirs();
		new File(tmpDir, "b/bBone1").mkdirs();
		Properties aProperties = new Properties();
		aProperties.setProperty("someMetaKey", "someMetaValue");
		aProperties.setProperty("numberOfFrames", "2");
		trySaveProperties(aProperties, new File(tmpDir, "a/collection.properties"));
		Properties bProperties = new Properties();
		bProperties.setProperty("redirectBone", "../../a/aBone1");
		bProperties.setProperty("replaceOffsets", "0/1");
		trySaveProperties(bProperties, new File(tmpDir, "b/collection.properties"));
		Properties bBone1Properties = new Properties();
		trySaveProperties(bBone1Properties, new File(tmpDir, "b/bBone1/bone.properties"));
		Properties aBone1Properties = new Properties();
		aBone1Properties.setProperty("someOtherMetaKey", "someOtherMetaValue");
		aBone1Properties.setProperty("endType", "end");
		aBone1Properties.setProperty("millisecsPerFrame", "200");
		trySaveProperties(aBone1Properties, new File(tmpDir, "a/aBone1/bone.properties"));
		Properties aBone2Properties = new Properties();
		aBone2Properties.setProperty("endType", "revert");
		aBone2Properties.setProperty("millisecsPerFrame", "100");
		aBone2Properties.setProperty("numberOfFrames", "3");
		trySaveProperties(aBone2Properties, new File(tmpDir, "a/aBone2/bone.properties"));
		Properties aBone2Frame2Properties = new Properties();
		aBone2Frame2Properties.setProperty("redirectBone", "../aBone1/0");
		aBone2Frame2Properties.setProperty("mirrorHorizontally", "true");
		trySaveProperties(aBone2Frame2Properties, new File(tmpDir, "a/aBone2/image1.properties"));
		Properties aBone2Frame3Properties = new Properties();
		aBone2Frame3Properties.setProperty("redirectFrame", "0");
		aBone2Frame3Properties.setProperty("mirrorHorizontally", "true");
		trySaveProperties(aBone2Frame3Properties, new File(tmpDir, "a/aBone2/image2.properties"));

		// write image mask files
		BufferedImage emptyImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		trySaveImage(emptyImage, "png", new File(tmpDir, "a/aBone1/image0.png"));
		trySaveImage(emptyImage, "png", new File(tmpDir, "a/aBone1/image1.png"));
		trySaveImage(emptyImage, "png", new File(tmpDir, "a/aBone2/image0.png"));
		{
			BufferedImage mask = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = mask.createGraphics();
			assertEquals(TRANSPARENT.getRGB(), mask.getRGB(0, 0));
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 1, 1);
			graphics.dispose();
			assertEquals(Color.BLACK.getRGB(), mask.getRGB(0, 0));
			trySaveImage(mask, "png", new File(tmpDir, "a/aBone1/image0.mask.png"));
		}
		{
			BufferedImage mask = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = mask.createGraphics();
			graphics.setComposite(sourceOver);
			assertEquals(TRANSPARENT.getRGB(), mask.getRGB(0, 0));
			graphics.setColor(Color.BLACK);
			graphics.fillRect(1, 0, 1, 1);
			graphics.dispose();
			assertEquals(Color.BLACK.getRGB(), mask.getRGB(1, 0));
			trySaveImage(mask, "png", new File(tmpDir, "a/aBone1/image1.mask.png"));
		}
		{
			BufferedImage mask = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = mask.createGraphics();
			assertEquals(TRANSPARENT.getRGB(), mask.getRGB(0, 0));
			graphics.setColor(Color.BLACK);
			graphics.fillRect(1, 1, 1, 1);
			graphics.dispose();
			trySaveImage(mask, "png", new File(tmpDir, "a/aBone2/image0.mask.png"));
		}
	}

	@After
	public void tearDown() {
		assertTrue(Tools.deleteRecursive(tmpDir));
	}

	@Test
	public void testStructure() throws IOException, NotFoundException {
		BoneCollection a = (BoneCollection) manager.resolve(new ClearPath("a"));
		assertEquals("2", a.getBequeathedProperty("numberOfFrames"));
		assertEquals("someMetaValue", a.getMeta("someMetaKey"));

		Bone aBone1 = (Bone) manager.resolve(new ClearPath("a", "aBone1"));
		assertEquals("someOtherMetaValue", aBone1.getMeta("someOtherMetaKey"));
		assertEquals(2, aBone1.numberOfFrames());
		assertEquals(AniEndType.end, aBone1.endType());
		assertEquals(200, aBone1.millisecsPerFrame());
		assertEquals(new Point(0, 0), aBone1.frameAndOffsets(0).offsets(1.0f).get(0));
		assertEquals(new Point(1, 0), aBone1.frameAndOffsets(1).offsets(1.0f).get(0));
		assertEquals(new Point(3, 0), aBone1.frameAndOffsets(1).offsets(3.0f).get(0));

		Bone aBone2 = (Bone) manager.resolve(new ClearPath("a", "aBone2"));
		assertEquals(3, aBone2.numberOfFrames());
		assertEquals(AniEndType.revert, aBone2.endType());
		assertEquals(100, aBone2.millisecsPerFrame());
		assertEquals(new Point(1, 1), aBone2.frameAndOffsets(0).offsets(1.0f).get(0));
		assertEquals(new Point(2, 2), aBone2.frameAndOffsets(0).offsets(2.0f).get(0));
		assertEquals(new Point(1, 0), aBone2.frameAndOffsets(1).offsets(1.0f).get(0));
		assertEquals(new Point(0, 1), aBone2.frameAndOffsets(2).offsets(1.0f).get(0));
		assertTrue(aBone2.frameAndOffsets(1).mirrored());
		assertTrue(aBone2.frameAndOffsets(2).mirrored());

		BoneCollection b = (BoneCollection) manager.resolve(new ClearPath("b"));
		Bone bBone1 = (Bone) manager.resolve(new ClearPath("b", "bBone1"));
		assertEquals(2, bBone1.numberOfFrames());
		assertEquals(AniEndType.end, bBone1.endType());
		assertEquals(200, bBone1.millisecsPerFrame());
		assertEquals(new Point(0, 0), bBone1.frameAndOffsets(0).offsets(1.0f).get(1));
		assertEquals(new Point(1, 0), bBone1.frameAndOffsets(1).offsets(1.0f).get(1));
	}

	@Test
	public void testStructureInDifferentOrder() throws IOException, NotFoundException {
		Bone bBone1 = (Bone) manager.resolve(new ClearPath("b", "bBone1"));
		assertEquals(2, bBone1.numberOfFrames());
		assertEquals(AniEndType.end, bBone1.endType());
		assertEquals(200, bBone1.millisecsPerFrame());
		assertEquals(new Point(0, 0), bBone1.frameAndOffsets(0).offsets(5.0f).get(1));
		assertEquals(new Point(7, 0), bBone1.frameAndOffsets(1).offsets(7.0f).get(1));

		Bone aBone2 = (Bone) manager.resolve(new ClearPath("a", "aBone2"));
		assertEquals(3, aBone2.numberOfFrames());
		assertEquals(AniEndType.revert, aBone2.endType());
		assertEquals(100, aBone2.millisecsPerFrame());
		assertEquals(new Point(4, 4), aBone2.frameAndOffsets(0).offsets(4.0f).get(0));
		assertEquals(new Point(3, 0), aBone2.frameAndOffsets(1).offsets(3.0f).get(0));
		assertEquals(new Point(0, 5), aBone2.frameAndOffsets(2).offsets(5.0f).get(0));
		assertTrue(aBone2.frameAndOffsets(1).mirrored());
		assertTrue(aBone2.frameAndOffsets(2).mirrored());

		Bone aBone1 = (Bone) manager.resolve(new ClearPath("a", "aBone1"));
		assertEquals("someOtherMetaValue", aBone1.getMeta("someOtherMetaKey"));
		assertEquals(2, aBone1.numberOfFrames());
		assertEquals(AniEndType.end, aBone1.endType());
		assertEquals(200, aBone1.millisecsPerFrame());
		assertEquals(new Point(0, 0), aBone1.frameAndOffsets(0).offsets(5.0f).get(0));
		assertEquals(new Point(6, 0), aBone1.frameAndOffsets(1).offsets(6.0f).get(0));
	}

}
