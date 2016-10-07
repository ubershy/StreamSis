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
package com.ubershy.streamsis.gui.controllers.edit.actions;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.SoundAction;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SoundActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
    
    @FXML
    private Label labelToAddExtensions;
	
    @FXML
    private TextField soundFileTextField;
    
    @FXML
    private Slider volumeSlider;
    
    @FXML
    private Label soundVolumeLabel;
    
    private String[] allowedExtensions;

    /** The {@link SoundAction} to edit. */
	protected SoundAction soundAction;
	
	/** The original {@link SoundAction} to compare values with {@link #soundAction}. */
	protected SoundAction origSoundAction;
	
	protected ChangeListener<? super Number> volumeListener = (o, oldVal, newVal) -> {
		buttonStateManager.reportNewValueOfControl(origSoundAction.getVolume(),
				newVal, volumeSlider, null);
	};
	
	protected ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		allowedExtensions = SoundAction.allowedExtensions.toArray(new String[0]);
		labelToAddExtensions
				.setText(labelToAddExtensions.getText() + Arrays.toString(allowedExtensions));
		String soundVolumeLabelOrigText = soundVolumeLabel.getText();
		soundVolumeLabel.setText(soundVolumeLabelOrigText + 100 + "%");
		volumeSlider.valueProperty().addListener((o, oldVal, newVal) -> {
			soundVolumeLabel.setText(soundVolumeLabelOrigText + newVal.intValue() + "%");
		});
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		soundAction = (SoundAction) editableCopyOfCE;
		origSoundAction = (SoundAction) origCE;
		bindBidirectionalAndRemember(soundFileTextField.textProperty(),
				soundAction.soundPathProperty());
		volumeSlider.setValue(soundAction.getVolume()*100.0);
		bindNormalAndRemember(soundAction.volumeProperty(),
				volumeSlider.valueProperty().divide(100.0));
		volumeSlider.valueProperty().addListener(volumeListener);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		volumeSlider.valueProperty().removeListener(volumeListener);
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
		Validator<String> soundFileTextFieldValidator = (c, newValue) -> {
			boolean extensionsValidationPassed = true;
			if (!newValue.isEmpty()) {
				extensionsValidationPassed = Util.checkFileExtension(newValue, allowedExtensions);
			}
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to sound file", newValue.isEmpty());
			ValidationResult badExtensionResult = ValidationResult.fromErrorIf(c,
					"The selected sound file has wrong extension", !extensionsValidationPassed);
			ValidationResult existingPathResult = ValidationResult.fromErrorIf(c,
					"Sound file is not found on this path",
					!Util.checkIfPathIsAbsoluteAndFileExists(newValue));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					existingPathResult, badExtensionResult);
			buttonStateManager.reportNewValueOfControl(origSoundAction.getSoundPath(),
					newValue, c, finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(soundFileTextField, soundFileTextFieldValidator);
	}
	

    @FXML
    void browseSoundFilePath(ActionEvent event) {
		GUIUtil.showJavaSingleFileChooser("Specify the sound file", "Sound file", false,
				soundFileTextField, allowedExtensions);
    }

}
