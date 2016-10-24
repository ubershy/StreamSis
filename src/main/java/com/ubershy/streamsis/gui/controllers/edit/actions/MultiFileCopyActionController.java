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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.MultiFileCopyAction;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.LittleCuteControllerType;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.edit.littlethings.MultiSourceFileChooserController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class MultiFileCopyActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;

    @FXML
    private TextField destinationTextField;
    
    @FXML
    private HBox fileChooserHBox;
    
    /** The {@link MultiFileCopyAction} to edit. */
	protected MultiFileCopyAction mFCAction;
    
	/** The original {@link MultiFileCopyAction} to compare values with {@link #mFCAction}. */
	protected MultiFileCopyAction origMFCAction;

	protected ValidationSupport validationSupport;
	
	
	protected MultiSourceFileChooserController MSFCController = (MultiSourceFileChooserController)
			StreamSisAppFactory.buildLittleCuteController(
					LittleCuteControllerType.MULTISOURCEFILECHOOSER);

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		MSFCController.replaceFileTypeNameTagInLabeledControls("source");
		fileChooserHBox.getChildren().add(MSFCController.getView());
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		super.setCuteButtonsStatesManager(buttonStateManager);
		MSFCController.setCuteButtonsStatesManager(buttonStateManager);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		mFCAction = (MultiFileCopyAction) editableCopyOfCE;
		origMFCAction = (MultiFileCopyAction) origCE;
		bindBidirectionalAndRemember(destinationTextField.textProperty(),
				mFCAction.dstFilePathProperty());
		MSFCController.bindToMultiSourceFileChooser(mFCAction.getFileChooser(),
				origMFCAction.getFileChooser());
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		MSFCController.unbindFromMultiSourceFileChooser();
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
		Validator<String> destinationFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to file", newValue.isEmpty());
			ValidationResult invalidPathResult = ValidationResult.fromErrorIf(c,
					"The path seems slightly... invalid",
					!Util.checkIfAbsolutePathSeemsValid(newValue));
			ValidationResult noExtensionResult = ValidationResult.fromErrorIf(c,
					"The file should have an extension",
					!newValue.isEmpty() && Util.extractFileExtensionFromPath(newValue) == null);
			ValidationResult notAbsolutePathResult = ValidationResult.fromErrorIf(c,
					"The path should be absolute. No exceptions.",
					!(new File(newValue).isAbsolute()));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					noExtensionResult, invalidPathResult, notAbsolutePathResult);
			if (finalResult.getMessages().size() == 0) {
				fileChooserHBox.setDisable(false);
			} else {
				fileChooserHBox.setDisable(true);
			}
			buttonStateManager.reportNewValueOfControl(origMFCAction.getDstFilePath(), newValue, c,
					finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(destinationTextField, destinationFieldValidator);
		MSFCController.setValidationSupport(validationSupport);
	}
	
    @FXML
	void browseDestinationPath(ActionEvent event) {
		File file = GUIUtil.showJavaSingleFileChooser("Specify(or create) the destination file",
				"Any file", true, root.getScene().getWindow(), null);
		if (file != null) {
			destinationTextField.setText(file.getAbsolutePath());
		}
	}

}
