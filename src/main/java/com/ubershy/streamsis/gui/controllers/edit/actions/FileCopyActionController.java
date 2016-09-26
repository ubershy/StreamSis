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
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.FileCopyAction;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class FileCopyActionController extends AbstractCuteController {

	@FXML
	private GridPane root;
	
    @FXML
    private TextField sourceTextField;

    @FXML
    private TextField destinationTextField;

	protected FileCopyAction action;

	protected String origSrcPath = "";
	
	protected String origDstPath = "";
	
	protected ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Do nothing
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement element) {
		action = (FileCopyAction) element;
		origSrcPath = action.getSrcFilePath();
		origDstPath = action.getDstFilePath();
		bindBidirectionalAndRemember(sourceTextField.textProperty(), action.srcFilePathProperty());
		bindBidirectionalAndRemember(destinationTextField.textProperty(),
				action.dstFilePathProperty());
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
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
		// TODO: uncomment lines and modify final results once this issue will be fixed:
		// https://bitbucket.org/controlsfx/controlsfx/issues/304/need-a-validate-method-in 
		// For now let's mostly rely on FileCopyAction's init() method for finding errors.
		this.validationSupport = validationSupport;
		Validator<String> sourceFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to file", newValue.isEmpty());
			ValidationResult existingPathResult = ValidationResult.fromErrorIf(c,
					"File is not found on this path",
					!Util.checkIfPathIsAbsoluteAndFileExists(newValue));
//			ValidationResult sameExtensionsResult = ValidationResult.fromErrorIf(c,
//					"The extensions of source and destination files should be the same or none",
//					pathsAreNotEmptyAndHaveDifferentExtensions(newValue,
//							destinationTextField.getText()));
//			ValidationResult sameFileResult = ValidationResult.fromErrorIf(c,
//					"Source and destination files can't be the same",
//					newValue.equals(destinationTextField.getText()));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					existingPathResult);
			buttonStateManager.reportNewValueOfControl(origSrcPath, newValue, c, finalResult);
			return finalResult;
		};
		Validator<String> destinationFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to file", newValue.isEmpty());
			ValidationResult validPathResult = ValidationResult.fromErrorIf(c,
					"The path seems slightly... invalid.", !Util.checkIfPathSeemsValid(newValue));
//			ValidationResult sameExtensionsResult = ValidationResult.fromErrorIf(c,
//					"The extensions of source and destination files should be the same or none",
//					pathsAreNotEmptyAndHaveDifferentExtensions(newValue,
//							sourceTextField.getText()));
//			ValidationResult sameFileResult = ValidationResult.fromErrorIf(c,
//					"Source and destination files can't be the same",
//					newValue.equals(destinationTextField.getText()));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					validPathResult);
			buttonStateManager.reportNewValueOfControl(origDstPath, newValue, c, finalResult);
			return finalResult;
		};
//		// This listener is needed because the validation of one field depends of value of another
//		// field. And if one field is fixed, the other might still be marked as invalid.
//		this.validationSupport.validationResultProperty().addListener((o, oldValue, newValue) -> {
//			if (oldValue != null) {
//				// If number of validation errors or warnings has decreased...
//				if (oldValue.getMessages().size() > newValue.getMessages().size()) {
//					// Recheck everything! Somehow... Once validate on demand feature or multi
//		            // control single fire validator feature will be implemented.
//				}
//			}
//		});
		this.validationSupport.registerValidator(sourceTextField, sourceFieldValidator);
		this.validationSupport.registerValidator(destinationTextField, destinationFieldValidator);
	}
	
    @FXML
    void browseDestinationPath(ActionEvent event) {
    	GUIUtil.showCuteFileChooser("Select the destination file", true, destinationTextField);
    }

    @FXML
    void browseSourcePath(ActionEvent event) {
    	GUIUtil.showCuteFileChooser("Select the source file", false, sourceTextField);
    }
    
//    private boolean pathsAreNotEmptyAndHaveDifferentExtensions(String path1, String path2) {
//    	if (path1.isEmpty() || path2.isEmpty())
//    		return false;
//    	return !Util.checkIfExtensionsOfFilesAreSame(path1, path2);
//    }

}
