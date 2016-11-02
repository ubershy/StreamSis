/** 
 * StreamSis
 * Copyright (C) 2015 Eva Balycheva
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
package com.ubershy.streamsis.gui.helperclasses;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.gui.GUIManager;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.PopupWindow.AnchorLocation;

/**
 * Class with some useful methods related to GUI.
 */
public final class GUIUtil {
	
	final static ScheduledExecutorService withDelayExecutor = Executors
			.newSingleThreadScheduledExecutor();

	/**
	 * Gets a rectangle, which coordinates represent bounds of one or multiple user's displays.
	 *
	 * @return a {@link Rectangle2D}
	 * @see <a href="http://sikulix-2014.readthedocs.org/en/latest/_images/multi.jpg">A Wonderful
	 *      picture, that explains a lot.</a>
	 */
	public static Rectangle2D getMultiScreenBounds() {
		double minX = 0, minY = 0, maxX = 0, maxY = 0;
		ObservableList<Screen> listOfScreens = Screen.getScreens();
		for (Screen screen : listOfScreens) {
			Rectangle2D bounds = screen.getVisualBounds();
			if (bounds.getMinX() < minX) {
				minX = bounds.getMinX();
			}
			if (bounds.getMinY() < minY) {
				minY = bounds.getMinY();
			}
			if (bounds.getMaxX() > maxX) {
				maxX = bounds.getMaxX();
			}
			if (bounds.getMaxY() > maxY) {
				maxY = bounds.getMaxY();
			}
		}
		// System.out.println(minX + ", " + minY + ", " + maxX + ", " + maxY);
		return new Rectangle2D(minX, minY, maxX, maxY);
	}

	/**
	 * Make {@link Alert} more cute!.
	 *
	 * @param alert
	 *            the Alert to cutify
	 */
	public static void cutifyAlert(Alert alert) {
		AlertType type = alert.getAlertType();
		switch (type) {
		case ERROR:
			Label brokenLove = new Label("💔");
			brokenLove.setStyle(
					"-fx-font-size: 36; -fx-effect: dropshadow(gaussian, hotpink, 10,0,0,0);");
			alert.setGraphic(brokenLove);
			break;
		case CONFIRMATION:
			Label confirmationLove = new Label("♥");
			confirmationLove.setTextFill(Color.HOTPINK);
			confirmationLove.setStyle(" -fx-font-size: 36");
			alert.setGraphic(confirmationLove);
			break;
		case INFORMATION:
			Label informationLove = new Label("♥");
			informationLove.setTextFill(Color.HOTPINK);
			informationLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(informationLove);
			break;
		case WARNING:
			Label warningLove = new Label("💓 ");
			warningLove.setTextFill(Color.HOTPINK);
			warningLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(warningLove);
			break;
		default: // NONE
			Label noneLove = new Label("♥");
			noneLove.setTextFill(Color.HOTPINK);
			noneLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(noneLove);
			break;
		}

	}

	/**
	 * Position Window based on current mode.
	 * <p>
	 * 
	 * @param defaultWidth
	 *            default window width
	 * @param defaultHeight
	 *            default window height
	 */
	public static void positionWindowBasedOnCurrentMode(double defaultWidth, double defaultHeight) {
		Stage window = GUIManager.getPrimaryStage();
		if (window == null)
			return;
		String modeName = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode") + "Mode";
		Rectangle2D fullBounds = getMultiScreenBounds();
		double prefWidth = CuteConfig.getDouble(CuteConfig.UTILGUI, modeName + "Width");
		double prefHeight = CuteConfig.getDouble(CuteConfig.UTILGUI, modeName + "Height");
		double prefX = CuteConfig.getDouble(CuteConfig.UTILGUI, modeName + "X");
		double prefY = CuteConfig.getDouble(CuteConfig.UTILGUI, modeName + "Y");
		Rectangle2D prefWindow = new Rectangle2D(prefX, prefY, prefWidth, prefHeight);
		// If window is hardly accessible, we need to reset it's position.
		if (prefWindow.intersects(fullBounds)) {
			window.setWidth(prefWidth);
			window.setHeight(prefHeight);
			window.setX(prefX);
			window.setY(prefY);
		} else {
			window.setWidth(defaultWidth);
			window.setHeight(defaultHeight);
			window.centerOnScreen();
		}
	}

	/**
	 * Show {@link Alert} in Primary Stage center.
	 *
	 * @param alert
	 *            the Alert to show in Primary Stage center
	 * @return Optional object
	 */
	public static Optional<ButtonType> showAlertInPrimaryStageCenter(Alert alert) {
		Stage window = GUIManager.getPrimaryStage();
		if (window.getOwner() != null) {
			alert.initOwner(window.getOwner());
		}
		return alert.showAndWait();
	}

	/**
	 * Save current mode window state and everything. <br>
	 * Writes the parameters of current window to file.
	 */
	public static void saveCurrentModeWindowStateAndEverything() {
		if (GUIManager.getPrimaryStage() == null || !GUIManager.getPrimaryStage().isShowing())
			return;
		String currentMode = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode") + "Mode";
		saveModeCoordinates(currentMode);
	}

	/**
	 * Saves the coordinates of current window and assigns them to current mode.
	 *
	 * @param modeName
	 *            mode name
	 */
	private static void saveModeCoordinates(String modeName) {
		Stage window = GUIManager.getPrimaryStage();
		if (window == null || !window.isShowing())
			return;
		CuteConfig.setDoubleValue(CuteConfig.UTILGUI, modeName + "Width", window.getWidth());
		CuteConfig.setDoubleValue(CuteConfig.UTILGUI, modeName + "Height", window.getHeight());
		CuteConfig.setDoubleValue(CuteConfig.UTILGUI, modeName + "X", window.getX());
		CuteConfig.setDoubleValue(CuteConfig.UTILGUI, modeName + "Y", window.getY());
		CuteConfig.saveConfig();
	}

	/**
	 * Creates the tooltiped menu item. <br>
	 * It's impossible to add Tooltip to default MenuItem, so creating label, adding tooltip inside
	 * of it and putting this label inside CustomMenuItem.
	 *
	 * @param text
	 *            the menu item's text
	 * @param tooltip
	 *            the tooltip to install to newly created custom menu item
	 * @return newly created custom menu item
	 */
	public static CustomMenuItem createTooltipedMenuItem(String text, String tooltip) {
		Label l = new Label(text);
		// If we dont' set up preferred size, tooltips and mouse clicks will be processed only on
		// text area
		l.setPrefSize(200, 20);
		CustomMenuItem menuItem = new CustomMenuItem(l);
		Tooltip tp = new Tooltip(tooltip);
		Tooltip.install(l, tp);
		return menuItem;
	}

	/**
	 * Replaces {@link Pane}'s child to another specified child. <br>
	 * Tries to maintain layout constraints.
	 *
	 * @param fromNode
	 *            The child to replace.
	 * @param toNode
	 *            The child which will replace the other one.
	 * @throws IllegalArgumentException
	 *             If fromNode's parent is not of type Pane.
	 */
	public static void replaceChildInPane(Node fromNode, Node toNode) {
		Pane papa = (Pane) fromNode.getParent();
		if (!(papa instanceof Pane)) {
			throw new IllegalArgumentException("This parent won't give up on his child.");
		}
		if (papa instanceof GridPane) {
			GridPane gridPapa = (GridPane) papa;
			Integer columnIndex = Util.ifNullReturnOther(GridPane.getColumnIndex(fromNode), 0);
			Integer rowIndex = Util.ifNullReturnOther(GridPane.getRowIndex(fromNode), 0);
			Integer columnspan = Util.ifNullReturnOther(GridPane.getColumnSpan(fromNode), 1);
			Integer rowspan = Util.ifNullReturnOther(GridPane.getRowSpan(fromNode), 1);
			HPos halignment = GridPane.getHalignment(fromNode);
			VPos valignment = GridPane.getValignment(fromNode);
			Priority hgrow = GridPane.getHgrow(fromNode);
			Priority vgrow = GridPane.getVgrow(fromNode);
			Insets margin = GridPane.getMargin(fromNode);
			gridPapa.getChildren().remove(fromNode);
			gridPapa.add(toNode, columnIndex, rowIndex, columnspan, rowspan);
			GridPane.setConstraints(toNode, columnIndex, rowIndex, columnspan, rowspan, halignment,
					valignment, hgrow, vgrow, margin);
		} else if (papa instanceof BorderPane) {
			BorderPane borderPapa = (BorderPane) papa;
			Pos pos = BorderPane.getAlignment(fromNode);
			Insets margin = BorderPane.getMargin(fromNode);
			borderPapa.getChildren().remove(fromNode);
			borderPapa.getChildren().add(toNode);
			BorderPane.setAlignment(toNode, pos);
			BorderPane.setMargin(toNode, margin);
		} else {
			papa.getChildren().remove(fromNode);
			papa.getChildren().add(toNode);
		}
	}
	
	/**
	 * Show Java's {@link FileChooser} window that allows the user to choose a single file.
	 * <p>
	 * Internally it automatically opens last opened directory, lets the user to choose a file and
	 * remembers the directory where the last file was chosen.
	 * 
	 * @param title
	 *            The title of window to show.
	 * @param extensionsDescription
	 *            How to describe files that are allowed to be chosen. Can't be empty.
	 * @param saveOrOpen
	 *            True to save file, false to open file.
	 * @param allowedExtensions
	 *            Array with allowed extensions in "*.extension" format. If null or empty, the user
	 *            would be able to choose a file with any extension.
	 */
	public static File showJavaSingleFileChooser(String title, String extensionsDescription,
			boolean saveOrOpen, Window window, String[] allowedExtensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		if (allowedExtensions != null && allowedExtensions.length != 0) {
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
					extensionsDescription, allowedExtensions);
			fileChooser.getExtensionFilters().add(extFilter);
		}
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			fileChooser.setInitialDirectory(new File(lastDir));
		}
		File file = null;
		if (saveOrOpen) {
			file = fileChooser.showSaveDialog(window);
		} else {
			file = fileChooser.showOpenDialog(window);
		}
		if (file != null) {
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastFileDirectory",
					file.getParentFile().getAbsolutePath());
		}
		return file;
	}
	
	/**
	 * Show Java's {@link FileChooser} window that allows the user to choose many files. Returns a
	 * a list of files.
	 * <p>
	 * Internally it automatically opens last opened directory, lets the user to choose files, sets
	 * and remembers the directory where the last file was chosen.
	 * 
	 * @param title
	 *            The title of window to show.
	 * @param extensionsDescription
	 *            How to describe files that are allowed to be chosen. Can't be empty.
	 * @param window
	 *            Window from where {@link FileChooser} will be shown.
	 * @param allowedExtensions
	 *            Array with allowed extensions in "*.extension" format. If null or empty, the user
	 *            would be able to choose a file with any extension.
	 * @return List of files if at least one was chosen, null if no files were chosen.
	 */
	public static List<File> showJavaMultiFileChooser(String title, String extensionsDescription,
			Window window, String[] allowedExtensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		if (allowedExtensions != null && allowedExtensions.length != 0) {
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
					extensionsDescription, allowedExtensions);
			fileChooser.getExtensionFilters().add(extFilter);
		}
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			fileChooser.setInitialDirectory(new File(lastDir));
		}
		List<File> files = fileChooser.showOpenMultipleDialog(window);
		if (files != null) {
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastFileDirectory",
					files.get(0).getParentFile().getAbsolutePath());
		}
		return files;
	}
	
	/**
	 * Show Java's {@link DirectoryChooser} window that allows the user to choose a single
	 * directory.
	 * <p>
	 * Internally it automatically opens last opened directory, lets the user to choose a new
	 * directory, sets TextField (provided as argument in this method) to this directory's path and
	 * remembers this directory.
	 * 
	 * @param title
	 *            The title of window to show.
	 * @param tfToSet
	 *            The TextField to set after the user has chosen the directory.
	 */
	public static void showJavaSingleDirectoryChooser(String title, TextField tfToSet) {
		Window window = tfToSet.getScene().getWindow();
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(title);
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			dirChooser.setInitialDirectory(new File(lastDir));
		}
		File file = null;
		file = dirChooser.showDialog(window);
		if (file != null) {
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastFileDirectory",
					file.getAbsolutePath());
			tfToSet.setText(file.getAbsolutePath());
		}
	}
	
	/**
	 * Create fake {@link Validator} that returns always successful result. Such Validator can be
	 * used to substitute some normal Validator to disable validation for {@link Control}, because
	 * currently the validation library doesn't support unregistering Validators from Controls.
	 */
	public static Validator<?> createFakeAlwaysSuccessfulValidator() {
		Validator<?> fakeValidator = (c, newValue) -> {
			return fakeSuccessfulValidationResult(c);
		};
		return fakeValidator;
	}
	
	/**
	 * Create fake successful {@link ValidationResult}. Such validation result can be used to
	 * manually set {@link Control} as successfully validated in cases when validation of this
	 * {@link Control} don't matter (anymore).
	 * 
	 * @param c
	 *            The Control used to create this fake ValidationResult.
	 */
	public static ValidationResult fakeSuccessfulValidationResult(Control c) {
		return ValidationResult.fromErrorIf(c, "hehe", false);
	}

	/**
	 * Sets {@link PopOver} as tooltip to a specified node to show specified content.
	 * 
	 * @param nodeToTooltip
	 *            The node over which to show the PopOver.
	 * @param popoverContent
	 *            The node to show inside PopOver.
	 * @param offset
	 *            The offset of PopOver relative to nodeToTooltip.
	 */
	public static void setPopOverTooltipToNode(Node nodeToTooltip, Node popoverContent,
			double offset) {
		PopOver over = new PopOver();
		over.setConsumeAutoHidingEvents(false);
		over.setAutoHide(false);
		over.setDetachable(true);
		over.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
		over.setContentNode(popoverContent);
		// This tooltip's behavior of showing will be used to show popover at right time.
		Tooltip invisibleTooltip = new Tooltip();
		invisibleTooltip.setAutoHide(false);
		invisibleTooltip.setConsumeAutoHidingEvents(false);
		invisibleTooltip.setOpacity(0.0);
		invisibleTooltip.setOnShown((e) -> {
			over.show(nodeToTooltip, offset);
		});
		invisibleTooltip.setOnHidden((e) -> {
			over.hide();
		});
		Tooltip.install(nodeToTooltip, invisibleTooltip);
	}

	/**
	 * Sets {@link Tooltip} for a {@link Node} which nicely shows {@link ImageView} inside.
	 * 
	 * @param nodeToHaveTooltip
	 *            The Node for installing Tooltip.
	 * 
	 * @param imageView
	 *            The ImageView to show inside Tooltip.
	 */
	public static void setImageViewTooltip(Node nodeToHaveTooltip,
			ImageView imageView) {
		// Add blue shadow to ImageView so the actual image will be clearly distinguishable from
		// tooltip's background.
		DropShadow shadow = new DropShadow(25, Color.LIGHTGREY);
		imageView.setEffect(shadow);
		// TODO: If image is big and StreamSis window is close to the edge, Tooltip is hiding right
		// after it is shown. Need to find workaround.
		Tooltip fullPreviewTP = new Tooltip();
		fullPreviewTP.setGraphic(imageView);
		fullPreviewTP.setAnchorLocation(AnchorLocation.WINDOW_BOTTOM_LEFT);
		fullPreviewTP.setStyle("-fx-effect: dropshadow(three-pass-box, black, 10,0.5,0,0);");
		Tooltip.install(nodeToHaveTooltip, fullPreviewTP);
	}
	
}
