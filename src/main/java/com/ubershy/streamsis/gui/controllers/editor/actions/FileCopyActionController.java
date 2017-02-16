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
package com.ubershy.streamsis.gui.controllers.editor.actions;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.actions.FileCopyAction;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class FileCopyActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
    
	@FXML
    private Label sourceFileLabel;
    
    @FXML
    private TextField sourceTextField;
    
    @FXML
    private HBox sourceHBox;

    @FXML
    private TextField destinationTextField;

    /** The {@link FileCopyAction} to edit. */
	protected FileCopyAction fileCopyAction;
	
	/** The original {@link FileCopyAction} to compare values with {@link #fileCopyAction}. */
	protected FileCopyAction origFileCopyAction;
	
	private String dstExtension = "";
	
	protected ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set tooltips showing file paths.
		GUIUtil.createAndBindTooltipToTextfield(sourceTextField);
		GUIUtil.createAndBindTooltipToTextfield(destinationTextField);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		fileCopyAction = (FileCopyAction) editableCopyOfCE;
		origFileCopyAction = (FileCopyAction) origCE;
		bindBidirectionalAndRemember(sourceTextField.textProperty(),
				fileCopyAction.srcFilePathProperty());
		bindBidirectionalAndRemember(destinationTextField.textProperty(),
				fileCopyAction.dstFilePathProperty());
//		modifyViewBasedOnDestinationPath(fileCopyAction.getDstFilePath());
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
			buttonStateManager.reportNewValueOfControl(origFileCopyAction.getSrcFilePath(),
					newValue, c, finalResult);
			return finalResult;
		};
		Validator<String> destinationFieldValidator = (c, newValue) -> {
			if (!newValue.isEmpty()) {
				dstExtension = Util.extractFileExtensionFromPath(newValue);
			}
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to file", newValue.isEmpty());
			ValidationResult noExtensionResult = ValidationResult.fromErrorIf(c,
					"The file should have an extension",
					!newValue.isEmpty() && dstExtension == null);
			ValidationResult invalidPathResult = ValidationResult.fromErrorIf(c,
					"The path seems slightly... invalid",
					!Util.checkIfAbsolutePathSeemsValid(newValue));
			ValidationResult notAbsolutePathResult = ValidationResult.fromErrorIf(c,
					"The path should be absolute. No exceptions.",
					!(new File(newValue).isAbsolute()));
//			ValidationResult sameExtensionsResult = ValidationResult.fromErrorIf(c,
//					"The extensions of source and destination files should be the same or none",
//					pathsAreNotEmptyAndHaveDifferentExtensions(newValue,
//							sourceTextField.getText()));
//			ValidationResult sameFileResult = ValidationResult.fromErrorIf(c,
//					"Source and destination files can't be the same",
//					newValue.equals(destinationTextField.getText()));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					invalidPathResult, noExtensionResult, notAbsolutePathResult);
			String sourceFileLabelExtension;
			if (finalResult.getMessages().size() == 0) {
				// No errors. sourceHBox can be enabled for editing.
				sourceFileLabelExtension = dstExtension;
				sourceHBox.setDisable(false);
			} else {
				// Errors. sourceHBox should be disabled from editing.
				sourceFileLabelExtension = "?";
				sourceHBox.setDisable(true);
			}
			sourceFileLabel.setText("Source file [" + sourceFileLabelExtension + "]");
			buttonStateManager.reportNewValueOfControl(origFileCopyAction.getDstFilePath(),
					newValue, c, finalResult);
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
		File file = GUIUtil.showJavaSingleFileChooser("Specify(or create) the destination file",
				"Any file", true, root.getScene().getWindow(), null);
		if (file != null) {
			destinationTextField.setText(file.getAbsolutePath());
		}
	}

	@FXML
	void browseSourcePath(ActionEvent event) {
		// NOTE: The button of this action will be enabled only if dstExtension != null, so it's OK.
		File file = GUIUtil.showJavaSingleFileChooser("Specify the source file", "File to copy",
				false, root.getScene().getWindow(), new String[] { dstExtension });
		if (file != null) {
			sourceTextField.setText(file.getAbsolutePath());
		}
	}
    
//    private boolean pathsAreNotEmptyAndHaveDifferentExtensions(String path1, String path2) {
//    	if (path1.isEmpty() || path2.isEmpty())
//    		return false;
//    	return !Util.checkIfExtensionsOfFilesAreSame(path1, path2);
//    }

}
