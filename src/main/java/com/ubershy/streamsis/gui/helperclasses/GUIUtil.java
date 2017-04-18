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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.controllers.AboutController;
import com.ubershy.streamsis.gui.controllers.settings.SettingsController;
import com.ubershy.streamsis.project.StuffSerializator;

import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.PopupWindow.AnchorLocation;

/**
 * Class with some useful methods related to GUI.
 */
public final class GUIUtil {
	
	static final Logger logger = LoggerFactory.getLogger(GUIUtil.class);
	
	private final static HashMap<Color, String> fxColors;
	
	static {
		fxColors = new HashMap<>();
		Class<?> clazz;
		try {
			clazz = Class.forName("javafx.scene.paint.Color");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Is JavaFX available?", e);
		}
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Object obj;
			try {
				obj = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			if (obj instanceof Color) {
				fxColors.put((Color) obj, field.getName());
			}
		}
	}
	
	final static ScheduledExecutorService withDelayExecutor = Executors
			.newSingleThreadScheduledExecutor();

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
			confirmationLove.setTextFill(CuteColor.GENTLEPINK);
			confirmationLove.setStyle(" -fx-font-size: 36");
			alert.setGraphic(confirmationLove);
			break;
		case INFORMATION:
			Label informationLove = new Label("♥");
			informationLove.setTextFill(CuteColor.GENTLEPINK);
			informationLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(informationLove);
			break;
		case WARNING:
			Label warningLove = new Label("💓 ");
			warningLove.setTextFill(CuteColor.GENTLEPINK);
			warningLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(warningLove);
			break;
		default: // NONE
			Label noneLove = new Label("♥");
			noneLove.setTextFill(CuteColor.GENTLEPINK);
			noneLove.setStyle(" -fx-font-size: 60");
			alert.setGraphic(noneLove);
			break;
		}

	}

	/**
	 * Position window based on coordinates from config.
	 * 
	 * @param windowName
	 *            The name of window in config.
	 * @param window
	 *            The window to position.
	 */
	@SuppressWarnings("unchecked")
	public static void positionWindowBasedOnConfigCoordinates(String windowName, Window window) {
		String subKey = windowName + "Coordinates";
		String defaultCoordsString = CuteConfig.getStringDefault(CuteConfig.UTILGUI, subKey);
		ArrayList<Double> defaultCoords;
		try {
			defaultCoords = (ArrayList<Double>) StuffSerializator
					.deserializeFromString(defaultCoordsString, ArrayList.class);
		} catch (IOException e1) {
			throw new RuntimeException("The default " + subKey + "in config is not deserializable");
		}
		String coordsString = CuteConfig.getString(CuteConfig.UTILGUI, subKey);
		boolean usingDefaultCoords = false;
		ArrayList<Double> coords;
		try {
			coords = (ArrayList<Double>) StuffSerializator.deserializeFromString(coordsString,
					ArrayList.class);
		} catch (IOException e) {
			// If there's a problem deserializing values, let's use default coordinates.
			logger.error("Can't deserialize " + subKey + " from current config,"
					+ " using coordinates from fallback config.");
			coords = defaultCoords;
			usingDefaultCoords = true;
		}
		// Let's check if coordinates are actual numbers.
		for (Object coord : coords) {
			if (!(coord instanceof Double) || ((Double) coord).isNaN()) {
				if (usingDefaultCoords) {
					throw new RuntimeException(
							"The default " + subKey + " in config contain NaN values");
				} else {
					logger.error(subKey + " from current config contain NaN values,"
							+ " using coordinates from fallback config.");
					coords = defaultCoords;
				}
				break;
			}
		}
		double prefX = coords.get(0);
		double prefY = coords.get(1);
		double prefWidth = coords.get(2);
		double prefHeight = coords.get(3);
		Rectangle2D prefWindow = new Rectangle2D(prefX, prefY, prefWidth, prefHeight);
		// If window is hardly accessible, we need to reset it's position.
		// Let's find out if it's accessible.
		boolean prefWindowWillBeAccessible = false;
		ObservableList<Screen> screens = Screen.getScreens();
		for (Screen screen: screens) {
			if (prefWindow.intersects(screen.getVisualBounds())) {
				prefWindowWillBeAccessible = true;
				break;
			}
		}
		if (prefWindowWillBeAccessible) {
			// Everything is alright, showing window in the specified position.
			window.setWidth(prefWidth);
			window.setHeight(prefHeight);
			window.setX(prefX);
			window.setY(prefY);
		} else {
			// Resetting position.
			logger.error("The window " + windowName
					+ " doesn't fit in screen bounds. Resetting position.");
			logger.error(prefWindow.toString());
			window.setWidth(defaultCoords.get(2));
			window.setHeight(defaultCoords.get(3));
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
	 * Saves coordinates of window that is currently showing on screen.
	 *
	 * @param windowName
	 *            The name of window in config.
	 * @param newWindow
	 *            The window which coordinates to save.
	 * @throws IllegalArgumentException
	 *             If window is not currently showing.
	 */
	public static void saveWindowCoordinates(String windowName, Window newWindow) {
		if (!newWindow.isShowing()) {
			throw new IllegalArgumentException(
					"Can't save coordinates of the window that is not showing.");
		}
		String subKey = windowName + "Coordinates";
		String serialized;
		try {
			serialized = StuffSerializator.serializeToString(Arrays.asList(newWindow.getX(),
					newWindow.getY(), newWindow.getWidth(), newWindow.getHeight()), false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		CuteConfig.setString(CuteConfig.UTILGUI, subKey, serialized);
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
			CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory",
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
			CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory",
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
			CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory",
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
		// Add shadow to ImageView so the actual image will be clearly distinguishable from
		// tooltip's background.
		DropShadow shadow = new DropShadow(BlurType.ONE_PASS_BOX, Color.GREY, 5.0, 1.0, 0.0, 0.0);
		imageView.setEffect(shadow);
		// TODO: If image is big and StreamSis window is close to the edge, Tooltip is hiding right
		// after it is shown. Need to find workaround.
		Tooltip fullPreviewTP = new Tooltip();
		fullPreviewTP.setGraphic(imageView);
		fullPreviewTP.setAnchorLocation(AnchorLocation.WINDOW_BOTTOM_LEFT);
//		fullPreviewTP.setStyle("-fx-effect: dropshadow(three-pass-box, black, 10,0.5,0,0);");
		Tooltip.install(nodeToHaveTooltip, fullPreviewTP);
	}
	
	/**
	 * Shows window for editing Settings.
	 */
    public static void showSettingsWindow() {
    	SettingsController sController = StreamSisAppFactory.buildSettingsController();
    	Stage settingsStage = new Stage();
    	Scene settingsScene = new Scene(sController.getView());
    	settingsStage.setMaxWidth(1000);
    	settingsStage.setMaxHeight(1000);
    	settingsStage.setMinWidth(400);
    	settingsStage.setMinHeight(300);
    	settingsStage.setWidth(600);
    	settingsStage.setHeight(400);
    	settingsStage.setScene(settingsScene);
    	settingsStage.initOwner(GUIManager.getPrimaryStage());
    	settingsStage.initModality(Modality.APPLICATION_MODAL);
    	settingsStage.setTitle("StreamSis Settings");
    	settingsStage.show();
    }

	/**
	 * Generates possible name for new item that will be added somewhere. <br>
	 * The name will look like this: "New 'baseName'". <br>
	 * An incrementing number in parentheses might be added to ensure this name is unique.
	 * 
	 * @param baseName
	 *            Base name to use.
	 * @param existingNames
	 *            A list with the existing names.
	 * 
	 * @return A Name that contains baseName which unique to the names in the existingNames list.
	 */
	public static String generateUniqueNameForNewItem(String baseName,
			List<String> existingNames) {
		String genericName = "New " + baseName;
		String alteredName = genericName;
		int counter = 0;
		while (existingNames.contains(alteredName)) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		return alteredName;
	}

	/**
	 * Adds listener to TableView's width that changes it's columns sizes to make them equal and
	 * covering all TableView's width.
	 * 
	 * @param tableView The TableView which columns to resize automatically.
	 */
	public static void maintainEqualWidthOfTableColumns(TableView<?> tableView) {
		tableView.widthProperty().addListener((o, oldVal, newVal) -> {
			double newWidth = newVal.doubleValue() / tableView.getColumns().size();
			for (TableColumn<?, ?> column: tableView.getColumns()) {
				column.setMinWidth(newWidth);
				column.setMinWidth(newWidth);
			}
		});
	}

	/**
	 * Shows "About" window.
	 */
	public static void showAboutWindow() {
    	AboutController aController = StreamSisAppFactory.buildAboutController();
    	Stage aboutStage = new Stage();
    	Scene aboutScene = new Scene(aController.getView());
    	double width = 640.0;
    	double height = 430.0;
    	aboutStage.setMinWidth(width);
    	aboutStage.setMinHeight(height);
    	aboutStage.setWidth(width);
    	aboutStage.setHeight(height);
    	aboutStage.setScene(aboutScene);
    	aboutStage.initOwner(GUIManager.getPrimaryStage());
    	aboutStage.initModality(Modality.APPLICATION_MODAL);
    	aboutStage.setTitle("About StreamSis");
    	aboutStage.show();
	}

	/**
	 * Opens web page at specified path.
	 * 
	 * @param path
	 *            The string URL of web page.
	 */
	public static void openWebPage(String path) {
		try {
			Desktop.getDesktop().browse(new URL(path).toURI());
		} catch (IOException | URISyntaxException e) {
			logger.error("Can't open web page", e);
		}
	}

	/**
	 * Gets the name of Color. If Color is one of the pre-defined instances in the {@link Color}
	 * class, returns the instance's name. If not - returns "UnknownColor".
	 * 
	 * @param color
	 *            The pre-defined instance of Color inside {@link Color} class.
	 * @return If found, returns the Color instance's name inside the Class. If not - returns
	 *         "UnknownColor".
	 */
	public static String getNameOfColorInstance(Color color) {
		String colorName = fxColors.get(color);
		if (colorName == null)
			colorName = "UnknownColor";
	    return colorName;
	}

	/**
	 * Creates a tooltip for the TextField which is bound to the TextField's text.<br>
	 * Useful for textfields containing file paths.
	 * 
	 * @param textField
	 *            The TextField for which to create a tooltip.
	 */
	public static void createAndBindTooltipToTextfield(TextField textField) {
		Tooltip filePathTP = new Tooltip();
		filePathTP.textProperty().bind(textField.textProperty());
		textField.setTooltip(filePathTP);
	}
	
}
