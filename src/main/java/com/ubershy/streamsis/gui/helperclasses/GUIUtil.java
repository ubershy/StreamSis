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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.actors.UniversalActor;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Class with some useful methods related to GUI.
 */
public final class GUIUtil {

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
	 * Show {@link Alert} in Stage center.
	 *
	 * @param alert
	 *            the Alert to show in Stage center
	 * @return Optional object
	 */
	public static Optional<ButtonType> showAlertInStageCenter(Alert alert) {
		// Can't make it more pretty/convenient in Java 1.8.0_45 (Not yet shown alert has width, but
		// has no height!)
		// TODO: make it pretty in the future!
		Stage window = GUIManager.getPrimaryStage();
		alert.setOnShown((e) -> {
			alert.setX(
					window.getX() + window.getWidth() / 2 - alert.getDialogPane().getWidth() / 2);
			// "110" to compensate
			alert.setY(window.getY() + window.getHeight() / 2
					- alert.getDialogPane().getHeight() / 2 - 110);
		});
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
	 * Adds a new empty SisScene with automatically generated name to the current Project.
	 */
	public static void addNewSisScene() {
		String genericName = "New SisScene";
		String alteredName = genericName;
		int counter = 0;
		while (ProjectManager.getProject().getSisSceneByName(alteredName) != null) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		SisScene newSisScene = new SisScene(alteredName, new String[] {});
		ProjectManager.getProject().addSisScene(newSisScene);
		// GUIManager.setSisSceneToRenameIndex(
		// ProjectManager.getProject().getSisScenes().indexOf(newSisScene));
	}

	/**
	 * Adds a new empty Actor with automatically generated name to the current Project.
	 */
	public static void addNewActor() {
		String genericName = "New Actor";
		String alteredName = genericName;
		int counter = 0;
		while (ProjectManager.getProject().getActorByName(alteredName) != null) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		Actor newActor = new UniversalActor(alteredName, 1000, 0, false, false);
		ProjectManager.getProject().addActorToGlobalActors(newActor);
		ProjectManager.getProject().addExistingActorToCurrentSisScene(newActor);
	}

	/**
	 * Manage validation tooltips. <br>
	 * This method creates listener for {@link ValidationSupport} object. <br>
	 * When new message arrives to ValidationSupport object, the new tooltip will automatically
	 * appear near {@link Control} that generated this message.
	 *
	 * @param validationSupport
	 *            the {@link ValidationSupport} object
	 */
	public static void manageValidationTooltips(ValidationSupport validationSupport) {
		// container with current tooltip
		AtomicReference<Tooltip> TPContainer = new AtomicReference<>();
		Consumer<? super ValidationMessage> tooltipAction = new Consumer<ValidationMessage>() {
			@Override
			public void accept(ValidationMessage t) {
				Control control = t.getTarget();
				Point2D p = control.localToScreen(control.getLayoutBounds().getMaxX(),
						control.getLayoutBounds().getMaxY());
				if (p != null) {
					Tooltip tp = new Tooltip(t.getText());
					tp.setAutoHide(true);
					tp.show(control, p.getX(), p.getY());
					TPContainer.set(tp);
				}
			}
		};
		ChangeListener<? super ValidationResult> listener = new ChangeListener<ValidationResult>() {
			@Override
			public void changed(ObservableValue<? extends ValidationResult> observable,
					ValidationResult oldValue, ValidationResult newValue) {
				// Lets destroy current tooltip if exists
				if (TPContainer.get() != null) {
					TPContainer.get().hide();
					TPContainer.set(null);
				}
				// Lets create a new List with with newValue's messages
				Collection<ValidationMessage> currentMessages = new ArrayList<ValidationMessage>(
						newValue.getMessages());
				if (oldValue != null) {
					Collection<ValidationMessage> oldMessages = oldValue.getMessages();
					// Lets find substract OldMessages from currentMessages
					// This will allow to show tooltips only for newest Messages later
					currentMessages.removeAll(oldMessages);
				}
				// Lets show new tooltips near corresponding controls
				currentMessages.forEach(tooltipAction);
			}
		};
		validationSupport.validationResultProperty().addListener(listener);
	}

	/**
	 * This method reports to chosen {@link ApplyAndOkButtonsStateManager} about existing errors and
	 * changes of value in chosen {@link Control}. <br>
	 * The information from this and other reports will be used to determine if Apply Button and OK
	 * Button are enabled or not.
	 * 
	 * @param <T>
	 *
	 * @param originalValue
	 *            the original value of {@link Control}
	 * @param newValue
	 *            the new value of {@link Control}
	 * @param c
	 *            the {@link Control}
	 * @param finalValidationResult
	 *            the {@link ValidationResult} (provides errors in validation)
	 *            <br>
	 *            If NULL there will be no error reporting to buttonStateManager
	 * @param buttonStateManager
	 *            the {@link ApplyAndOkButtonsStateManager} where to report
	 */
	public static <T> void reportToButtonStateManager(T originalValue, T newValue, Control c,
			ValidationResult finalValidationResult,
			ApplyAndOkButtonsStateManager buttonStateManager) {
		// Lets tell buttonStateManager if changes on this Control are made or not
		// Lets get information about change by comparing original value of control with new one
		if (newValue.equals(originalValue)) {
			buttonStateManager.setChangeStateForControl(c, false);
		} else {
			buttonStateManager.setChangeStateForControl(c, true);
		}
		// Lets tell buttonStateManager if errors in this Control exist or not
		// Lets get information about existing errors from finalValidationResult
		if (finalValidationResult != null) {
			if (finalValidationResult.getErrors().isEmpty()) {
				buttonStateManager.setErrorStateForControl(c, false);
			} else {
				buttonStateManager.setErrorStateForControl(c, true);
			}
		}
	}
}
