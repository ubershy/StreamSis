/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ubershy.streamsis.gui.cells;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * CellUtils contains static method that helps to optimize performance of cells a bit, when their
 * graphics change.
 */
public final class CellUtils {

	/** Default snapshot settings. */
	private static final SnapshotParameters snapSettings;

	static {
		snapSettings = new SnapshotParameters();
		snapSettings.setFill(Color.TRANSPARENT);
	}

	/**
	 * Sets the graphic to {@link Labeled}, but only if the same graphic is not already set.
	 *
	 * @param whereToSet
	 *            The Labeled where to set the graphic.
	 * @param graphic
	 *            The new graphic to set.
	 */
	public static void setGraphicIfNotAlready(Labeled whereToSet, Node newGraphic) {
		if (whereToSet.getGraphic() == null || !whereToSet.getGraphic().equals(newGraphic))
			whereToSet.setGraphic(newGraphic);
	}

	/**
	 * Sets the child to {@link Pane}, but only if the same child is not already set as single
	 * child.
	 *
	 * @param whereToSet
	 *            The Pane where to set the child.
	 * @param newChild
	 *            The new child to set.
	 */
	public static void setSingleChildIfNotAlready(Pane whereToSet, Node newChild) {
		ObservableList<Node> children = whereToSet.getChildren();
		if (children.isEmpty() || children.get(0) == null || !children.get(0).equals(newChild)
				|| children.size() > 1)
			children.setAll(newChild);
	}

	/**
	 * Sets the image to {@link ImageView}, but only if the same image is not already set.
	 *
	 * @param whereToSet
	 *            The ImageView where to set the image.
	 * @param image
	 *            The new image to set.
	 */
	public static void setImageIfNotAlready(ImageView whereToSet, Image image) {
		if (whereToSet.getImage() == null || !whereToSet.getImage().equals(image))
			whereToSet.setImage(image);
	}

	/**
	 * Scale the node to the chosen size. <br>
	 * If the node already has the proper size, does nothing.
	 *
	 * @param node
	 *            The node which size to set.
	 * @param scaleX
	 *            The scale on X axis.
	 * @param scaleY
	 *            The scale on Y axis.
	 */
	public static void scaleNodeToSizeIfNotAlready(Node node, double scaleX, double scaleY) {
		if (node.getScaleX() != scaleX)
			node.setScaleX(scaleX);
		if (node.getScaleY() != scaleY)
			node.setScaleY(scaleY);
	}

	/**
	 * Make a snapshot of a node with transparent background.
	 *
	 * @param node
	 *            The node to snap.
	 * @return The resulting image (snapshot).
	 */
	public static Image makeSnapshotWithTransparency(Node node) {
		WritableImage image = node.snapshot(snapSettings, null);
		return image;
	}
	
	/**
	 * Sets the color to {@link Labeled}'s text, but only if the same color is not already set.
	 *
	 * @param whereToSet
	 *            The Labeled where to set the text color.
	 * @param newColor
	 *            The new color to set.
	 */
	public static void setLabeledTextFillIfNotAlready(Labeled whereToSet, Color newColor) {
		if (whereToSet.getTextFill() == null || !whereToSet.getTextFill().equals(newColor))
			whereToSet.setTextFill(newColor);
	}

}
