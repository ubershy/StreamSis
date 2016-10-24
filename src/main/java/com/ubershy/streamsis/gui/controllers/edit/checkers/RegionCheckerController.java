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
package com.ubershy.streamsis.gui.controllers.edit.checkers;

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

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.checkers.RegionChecker;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.LittleCuteControllerType;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.edit.littlethings.CoordinatesController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Slider similaritySlider;

    @FXML
    private Label similarityLabel;
    
    @FXML
    private Label targetSizeLabel;
    
    @FXML
    private Button selectTargetButton;
    
    @FXML
    private Label similarityDescriptionLabel;
    
    @FXML
    private StackPane targetImageViewPane;
    
    @FXML
    private ImageView targetImageView;
    
    @FXML
    private VBox coordsVBox;

    /** The {@link RegionChecker} to edit. */
	protected RegionChecker regChecker;
	
	/** The original {@link RegionChecker} to compare values with {@link #regChecker}. */
	protected RegionChecker origRegChecker;
	
	protected ValidationSupport validationSupport;
	
    private String[] allowedExtensions;
    
	protected CoordinatesController coordsController = (CoordinatesController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.COORDINATES);

	private String similarityLabelOrigText;
	
	private ImageView fullTargetImageView = new ImageView();
	
	private Text crosshairIcon = GlyphsDude.createIcon(FontAwesomeIcon.CROSSHAIRS);
	
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
		similarityLabelOrigText = similarityLabel.getText();
		similarityLabel.setText(similarityLabelOrigText + 100 + "%");
		selectTargetButton.setGraphic(crosshairIcon);
		
		// Set tooltip with full size Target image on targetImageViewPane mouse hover.
		GUIUtil.setImageViewTooltip(targetImageViewPane, fullTargetImageView);
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
		similaritySlider.setValue(regChecker.getSimilarity()*100.0);
		bindNormalAndRemember(regChecker.similarityProperty(),
				similaritySlider.valueProperty().divide(100.0));
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
		Validator<Number> similaritySliderValidator = (c, newValue) -> {
			// Not using newValue.intValue(), because we want rounded value, not truncated.
			int intValue = Math.round(newValue.floatValue());
			ValidationResult zeroResult = ValidationResult.fromErrorIf(c,
					"Zero minimum acceptable similarity means any random image on screen can be "
							+ "matched with Target image. It's pointless. Forget about it.",
					intValue < 1);
			ValidationResult lowResult = ValidationResult.fromWarningIf(c,
					"Minimum acceptable similarity is too low. This Checker will often react like "
					+ "it sees the Target image on screen when something else is on the screen. "
					+ "False positive result.",
					intValue >= 1 && intValue <= 40);
			int originalValue = Math.round(origRegChecker.getSimilarity()*100);
			buttonStateManager.reportNewValueOfControl(originalValue, intValue, c, zeroResult);
			String similarityComment;
			if (intValue > 98) {
				similarityComment = "Find an exact match. May not recognize even identical image "
						+ "in some cases.";
			} else if (intValue >= 95) {
				similarityComment = "Find a precise match. Recommended to use.";
			} else if (intValue >= 70) {
				similarityComment = "Find a good match or better. Some risk of false positive "
						+ "results.";
			} else if (intValue >= 40) {
				similarityComment = "Find a bad match or better. "
						+ "High risk of false positive results.";
			} else if (intValue >= 30) {
				similarityComment = "Find almost any match. Super high risk of false positive "
						+ "results.";
			} else if (intValue >= 20) {
				similarityComment = "You want false positive results? Because that's how you get "
						+ "false positive results.";
			} else {
				similarityComment = "No. Just no.";
			}
			similarityDescriptionLabel.setText(similarityComment);
			similarityLabel.setText(similarityLabelOrigText + newValue.intValue() + "%");
			ValidationResult finalResult = ValidationResult.fromResults(zeroResult, lowResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(similaritySlider, similaritySliderValidator);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		super.setCuteButtonsStatesManager(buttonStateManager);
		coordsController.setCuteButtonsStatesManager(buttonStateManager);
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
    	Screen.doPrompt("Select Target image on screen", this);
    }

	@Override
	public void update(EventSubject s) {
		Screen.closePrompt();
		// This code is run in AWT thread. Using atomic reference to pass ScreenImage later to
		// JavaFX thread.
		AtomicReference<ScreenImage> atomicScreenImage = new AtomicReference<>(null);
		// FIXME: a lot of stuff here is "Internal use" and "deprecated" in SikuliX library. Add
		// unit tests to be able to notice any related problems before each release. Or some day the
		// users might get angry.
		if (s != null) {
			OverlayCapturePrompt prompt = (OverlayCapturePrompt) s;
			atomicScreenImage.set(prompt.getSelection());
			Screen.closePrompt();
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
						GUIUtil.showAlertInStageCenter(alert);
					}
				}
			}
		});
		Screen.resetPrompt((OverlayCapturePrompt) s);
	}

}
