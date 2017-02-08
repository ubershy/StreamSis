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
package com.ubershy.streamsis.gui.controllers.editor.littlethings;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;
import org.sikuli.util.OverlayCapturePrompt;

import com.ubershy.streamsis.HotkeyManager;
import com.ubershy.streamsis.SneakyExceptionHandler;
import com.ubershy.streamsis.HotkeyManager.Hotkey;
import com.ubershy.streamsis.elements.parts.Coordinates;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.IntegerTextField;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * CoordinatesController, the controller that allows to edit {@link Coordinates} (a thingy for
 * specifying region on screen) in Element Editor panel.
 */
public class CoordinatesController extends AbstractCuteController implements EventObserver {

	@FXML
	private GridPane root;

    @FXML
    private HBox xHBox;

    @FXML
    private HBox wHBox;

    @FXML
    private HBox yHBox;

    @FXML
    private HBox hHBox;
    
    @FXML
    private Button selectRegionButton;

    @FXML
    private Button showRegionButton;
    
    /** The IntegerTextField for editing {@link Coordinates#xProperty()}. */
    private IntegerTextField xTextField = new IntegerTextField(100000, true);
    
    /** The IntegerTextField for editing {@link Coordinates#yProperty()}. */
    private IntegerTextField yTextField = new IntegerTextField(100000, true);
    
    /** The IntegerTextField for editing {@link Coordinates#xProperty()}. */
    private IntegerTextField wTextField = new IntegerTextField(100000, false);
    
    /** The IntegerTextField for editing {@link Coordinates#xProperty()}. */
    private IntegerTextField hTextField = new IntegerTextField(100000, false);

	/** The {@link Coordinates} to edit. */
	protected Coordinates coords;

	/** The original {@link Coordinates} to compare values with {@link #coords}. */
	protected Coordinates origCoords;

	protected ValidationSupport validationSupport;
	
	private Text crosshairIcon = GlyphsDude.createIcon(FontAwesomeIcon.CROSSHAIRS);

	private Runnable selectRegionRunnable = () -> {
		SneakyExceptionHandler.setTemporaryUncaughtExceptionHandler((e, t) -> {
			GUIManager.showNotification(null, "An error occurred while selecting the Region.");
			selectionErrorOccured = true;
		});
		selectionErrorOccured = false;
		Screen.doPrompt("Select Region on screen", this);
	};

	private String selectRegionButtonOrigText;
	
	private ChangeListener<? super KeyCodeCombination> selectRegionHotkeyListener = (o, oldVal,
			newVal) -> {
		changeSelectRegionButtonBasedOnKeyCombination(newVal);
	};

	private boolean selectionErrorOccured = false;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		xHBox.getChildren().add(xTextField);
		yHBox.getChildren().add(yTextField);
		wHBox.getChildren().add(wTextField);
		hHBox.getChildren().add(hTextField);
		selectRegionButtonOrigText = selectRegionButton.getText();
		selectRegionButton.setGraphic(crosshairIcon);
		selectRegionButton.sceneProperty().addListener((o, oldVal, newVal) -> {
			ObjectProperty<KeyCodeCombination> opkcc = HotkeyManager
					.getKeyCodeCombinationPropertyOfHotkey(Hotkey.SELECTREGION);
			if (newVal != null) {
				// If inside the Scene, add runnable of this button to Hotkey.
				HotkeyManager.setSelectRegionRunnable(selectRegionRunnable);
				// Change text of the button to show Hotkey.
				changeSelectRegionButtonBasedOnKeyCombination(opkcc.get());
				opkcc.addListener(selectRegionHotkeyListener);
			} else {
				opkcc.removeListener(selectRegionHotkeyListener);
				changeSelectRegionButtonBasedOnKeyCombination(null);
				// If outside the Scene, remove runnable of this button from Hotkey.
				HotkeyManager.setSelectRegionRunnable(null);
			}
		});
	}
	
	/**
	 * Changes text of {@link #selectRegionButton} to show the Hotkey the user can to press to
	 * select Region.
	 * 
	 * @param kcc
	 *            The current KeyCodeCombination of "Select Region" hotkey.
	 */
	private void changeSelectRegionButtonBasedOnKeyCombination(KeyCodeCombination kcc) {
		if (kcc != null) {
			selectRegionButton
					.setText(selectRegionButtonOrigText + " (" + kcc.getDisplayText() + ")");
		} else {
			selectRegionButton.setText(selectRegionButtonOrigText);
		}
	}

	/**
	 * Sets the {@link Coordinates} to work with and binds view's controls to Coordinates's
	 * properties.
	 *
	 * @param editableCopyOfCoords
	 *            The Coordinates to edit. (Actually a copy of the Coordinates the user wishes to
	 *            edit. The changes made in the copy will be transferred to original Coordinates
	 *            once the user hit "Apply" or "OK" button).
	 * @param origCoords
	 *            The Original Coordinates to use as storage of original values of CuteElement's
	 *            attributes. Should not be edited in controllers.
	 */
	public void bindToCoordinates(Coordinates editableCopyOfCoords, Coordinates origCoords) {
		this.coords = editableCopyOfCoords;
		this.origCoords = origCoords;
		// Bind to the new Coordinates.
		bindBidirectionalAndRemember(xTextField.numberProperty(), editableCopyOfCoords.xProperty());
		bindBidirectionalAndRemember(yTextField.numberProperty(), editableCopyOfCoords.yProperty());
		bindBidirectionalAndRemember(wTextField.numberProperty(), editableCopyOfCoords.wProperty());
		bindBidirectionalAndRemember(hTextField.numberProperty(), editableCopyOfCoords.hProperty());
	}

	public void unbindFromCoordinates() {
		unbindAllRememberedBinds();
		showRegionButton.disableProperty().unbind();
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public Node getView() {
		return root;
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		Validator<String> xValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify X coordinate of Region to scan", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(String.valueOf(origCoords.getX()),
					newValue, c, emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(xTextField, xValidator);
		Validator<String> yValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify Y coordinate of Region to scan", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(String.valueOf(origCoords.getY()),
					newValue, c, emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(yTextField, yValidator);
		Validator<String> wValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify width of Region to scan", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(String.valueOf(origCoords.getW()),
					newValue, c, emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(wTextField, wValidator);
		Validator<String> hValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify height of Region to scan", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(String.valueOf(origCoords.getH()),
					newValue, c, emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(hTextField, hValidator);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		this.buttonStateManager = buttonStateManager;
		showRegionButton.disableProperty()
				.bind(buttonStateManager.performTestButtonOnProperty().not());
	}
	

    @FXML
    void onSelectRegion(ActionEvent event) {
    	selectRegionRunnable.run();
    }
    
	@Override
	public void update(EventSubject s) {
		SneakyExceptionHandler.removeTemporaryUncaughtExceptionHandler();
		Screen.closePrompt();
		if (selectionErrorOccured ) {
			Screen.resetPrompt((OverlayCapturePrompt) s);
			return;
		}
		// This code is run in AWT thread. Using atomic reference to pass ScreenImage later to
		// JavaFX thread.
		AtomicReference<ScreenImage> atomicScreenImage = new AtomicReference<>(null);
		// FIXME: a lot of stuff here is "Internal use" and "deprecated" in SikuliX library. Add
		// unit tests to be able to notice any related problems before each release. Or some day the
		// users might get angry.
		if (s != null) {
			OverlayCapturePrompt prompt = (OverlayCapturePrompt) s;
			atomicScreenImage.set(prompt.getSelection());
		}
		Platform.runLater(() -> {
			ScreenImage screenImage = atomicScreenImage.get();
			if (screenImage != null) { // Image selected, selection wasn't cancelled.
				setX(screenImage.x);
				setY(screenImage.y);
				setW(screenImage.w);
				setH(screenImage.h);
			}
		});
		Screen.resetPrompt((OverlayCapturePrompt) s);
	}

    @FXML
    void onShowRegion(ActionEvent event) {
    	coords.highlightRegion();
    }
    
    public void setX(int x) {
    	set(xTextField, x);
    }
    
    public void setY(int y) {
    	set(yTextField, y);
    }
    
    public void setW(int w) {
    	set(wTextField, w);
    }
    
    public void setH(int h) {
    	set(hTextField, h);
    }
    
    private void set(IntegerTextField tf, int value) {
    	tf.setText(String.valueOf(value));
    }

}
