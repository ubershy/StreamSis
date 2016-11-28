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
package com.ubershy.streamsis.gui.controllers.editor.checkers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

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
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.HotkeyManager.Hotkey;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.checkers.RegionChecker;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.LittleCuteControllerType;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.controllers.editor.littlethings.CoordinatesController;
import com.ubershy.streamsis.gui.controllers.editor.littlethings.SimilarityController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class RegionCheckerController extends AbstractCuteController
		implements CuteElementController, EventObserver {

    @FXML
    private GridPane root;

    @FXML
    private TextField targetTextField;

    @FXML
    private Button selectTargetButton;

    @FXML
    private StackPane targetImageViewPane;

    @FXML
    private ImageView targetImageView;

    @FXML
    private Label targetSizeLabel;

    @FXML
    private VBox coordsVBox;

    @FXML
    private VBox similarityVBox;

    /** The {@link RegionChecker} to edit. */
	protected RegionChecker regChecker;
	
	/** The original {@link RegionChecker} to compare values with {@link #regChecker}. */
	protected RegionChecker origRegChecker;
	
	protected ValidationSupport validationSupport;
	
    private String[] allowedExtensions;
    
	protected CoordinatesController coordsController = (CoordinatesController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.COORDINATES);
	
	protected SimilarityController simController = (SimilarityController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.SIMILARITY);

	private ImageView fullTargetImageView = new ImageView();
	
	private Text crosshairIcon = GlyphsDude.createIcon(FontAwesomeIcon.CROSSHAIRS);

	private boolean selectionErrorOccured = false;
	
	private Runnable selectImageRunnable = () -> {
		SneakyExceptionHandler.setTemporaryUncaughtExceptionHandler((e, t) -> {
			GUIManager.showNotification(null, "An error occurred while selecting the Image.");
			selectionErrorOccured = true;
		});
		selectionErrorOccured = false;
		Screen.doPrompt("Select Target Image on screen", this);
	};
	
	private String selectTargetButtonOrigText;
	
	private ChangeListener<? super KeyCodeCombination> selectImageHotkeyListener = (o, oldVal,
			newVal) -> {
		changeSelectTargetButtonBasedOnKeyCombination(newVal);
	};
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Let's add nice noise as background to target image.
		Image im = new Image(getClass().getResourceAsStream("/images/targetImageNoise.gif"));
		BackgroundImage bgim = new BackgroundImage(im, null, null, null, null);
		targetImageViewPane.setBackground(new Background(bgim));
		targetImageViewPane.setCache(true);
		targetImageView.setSmooth(true);
		targetImageView.setCache(true);
		allowedExtensions = RegionChecker.allowedExtensions.toArray(new String[0]);
		coordsVBox.getChildren().add(coordsController.getView());
		similarityVBox.getChildren().add(simController.getView());
		
		// Set tooltip with full size Target image on targetImageViewPane mouse hover.
		GUIUtil.setImageViewTooltip(targetImageViewPane, fullTargetImageView);
		
		// Setup selectTargetButton.
		selectTargetButton.setGraphic(crosshairIcon);
		selectTargetButtonOrigText = selectTargetButton.getText();
		selectTargetButton.sceneProperty().addListener((o, oldVal, newVal) -> {
			ObjectProperty<KeyCodeCombination> opkcc = HotkeyManager
					.getKeyCodeCombinationPropertyOfHotkey(Hotkey.SELECTIMAGE);
			if (newVal != null) {
				// If inside the Scene, add runnable of this button to Hotkey.
				HotkeyManager.setSelectImageRunnable(selectImageRunnable);
				// Change text of the button to show Hotkey.
				changeSelectTargetButtonBasedOnKeyCombination(opkcc.get());
				opkcc.addListener(selectImageHotkeyListener);
			} else {
				opkcc.removeListener(selectImageHotkeyListener);
				changeSelectTargetButtonBasedOnKeyCombination(null);
				// If outside the Scene, remove runnable of this button from Hotkey.
				HotkeyManager.setSelectImageRunnable(null);
			}
		});
	}

	/**
	 * Changes text of {@link #selectTargetButton} to show the Hotkey the user can to press to
	 * select Image and Region.
	 * 
	 * @param kcc
	 *            The current KeyCodeCombination of "Select Image" Hotkey.
	 */
	private void changeSelectTargetButtonBasedOnKeyCombination(KeyCodeCombination kcc) {
		if (kcc != null) {
			selectTargetButton
					.setText(selectTargetButtonOrigText + " (" + kcc.getDisplayText() + ")");
		} else {
			selectTargetButton.setText(selectTargetButtonOrigText);
		}
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		regChecker = (RegionChecker) editableCopyOfCE;
		origRegChecker = (RegionChecker) origCE;
		bindBidirectionalAndRemember(targetTextField.textProperty(),
				regChecker.targetImagePathProperty());
		simController.bindToSimilarity(regChecker.similarityProperty(),
				origRegChecker.similarityProperty());
		coordsController.bindToCoordinates(regChecker.getCoords(), origRegChecker.getCoords());
	}
	
	private void updateViewBasedOnTargetImagePath(String targetImagePath) {
		if (targetImagePath == null || targetImagePath.isEmpty()) {
			targetImageView.setImage(null);
			fullTargetImageView.setImage(null);
			targetSizeLabel.setText("∞ ✕ ∞");
		} else {
			Image image = new Image(new File(targetImagePath).toURI().toString());
			targetImageView.setImage(image);
			fullTargetImageView.setImage(image);
			targetSizeLabel
					.setText(String.format("%.0f ✕ %.0f", image.getWidth(), image.getHeight()));
		}
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		coordsController.unbindFromCoordinates();
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
		coordsController.setValidationSupport(validationSupport);
		simController.setValidationSupport(validationSupport);
		Validator<String> targetFieldValidator = (c, newValue) -> {
			boolean extensionsValidationPassed = true;
			if (!newValue.isEmpty()) {
				extensionsValidationPassed = Util.checkFileExtension(newValue, allowedExtensions);
			}
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to Target image file", newValue.isEmpty());
			ValidationResult badExtensionResult = ValidationResult.fromErrorIf(c,
					"The selected target Image file has wrong extension",
					!extensionsValidationPassed);
			ValidationResult invalidPathResult = ValidationResult.fromErrorIf(c,
					"The path seems slightly... invalid",
					!Util.checkIfAbsolutePathSeemsValid(newValue));
			ValidationResult validPathResult = ValidationResult.fromErrorIf(c,
					"Image file is not found on this path",
					!Util.checkIfPathIsAbsoluteAndFileExists(newValue));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					invalidPathResult, badExtensionResult, validPathResult);
			if (finalResult.getErrors().isEmpty()) {
				updateViewBasedOnTargetImagePath(newValue);
			} else {
				// If there are errors, let's not show image in ImageView.
				updateViewBasedOnTargetImagePath(null);
			}
			buttonStateManager.reportNewValueOfControl(origRegChecker.getTargetImagePath(),
					newValue, c, finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(targetTextField, targetFieldValidator);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		super.setCuteButtonsStatesManager(buttonStateManager);
		coordsController.setCuteButtonsStatesManager(buttonStateManager);
		simController.setCuteButtonsStatesManager(buttonStateManager);
	}
	
    @FXML
    void browseTargetPath(ActionEvent event) {
		File file = GUIUtil.showJavaSingleFileChooser("Choose the Target image to find",
				"Portable Network Graphics image file", false, root.getScene().getWindow(),
				allowedExtensions);
		if (file != null) {
			targetTextField.setText(file.getAbsolutePath());
		}
    }

    @FXML
    void selectTarget(ActionEvent event) {
    	selectImageRunnable.run();
    }

	@Override
	public void update(EventSubject s) {
		SneakyExceptionHandler.removeTemporaryUncaughtExceptionHandler();
		Screen.closePrompt();
		if (selectionErrorOccured) {
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
				BufferedImage bi = screenImage.getImage();
				File file = GUIUtil.showJavaSingleFileChooser(
						"Choose where to save captured Target image",
						"Portable Network Graphics image file", true, root.getScene().getWindow(),
						allowedExtensions);
				if (file != null) { // File was chosen.
					try {
						ImageIO.write(bi, "png", file);
						// Write was successful if there were no exceptions, let's modify controls.
						targetTextField.setText(file.getAbsolutePath());
						updateViewBasedOnTargetImagePath(file.getAbsolutePath());
						this.coordsController.setX(screenImage.x);
						this.coordsController.setY(screenImage.y);
						this.coordsController.setW(screenImage.w);
						this.coordsController.setH(screenImage.h);
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR);
						GUIUtil.cutifyAlert(alert);
						alert.setTitle("Can't save Target image file");
						alert.setHeaderText("Dear user, StreamSis can't save Target image file.");
						if ("IOException".equals(e.getClass().getSimpleName())) {
							alert.setContentText("You probably don't have system rights to write "
									+ "image file there.");
						} else {
							alert.setContentText("Image file can't be saved, because of "
									+ "IllegalArgumentException. It's one of the Seven Buggy Sins "
									+ "that a programmer can make.");
						}
						GUIUtil.showAlertInPrimaryStageCenter(alert);
					}
				}
			}
		});
		Screen.resetPrompt((OverlayCapturePrompt) s);
	}

}
