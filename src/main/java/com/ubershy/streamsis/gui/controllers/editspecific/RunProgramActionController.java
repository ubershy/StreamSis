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
package com.ubershy.streamsis.gui.controllers.editspecific;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.RunProgramAction;
import com.ubershy.streamsis.project.CuteElement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class RunProgramActionController extends AbstractCuteController {

	@FXML
	private TextField argumentsTextField;

	@FXML
	private Button browsePathButton;

	@FXML
	private Button browseWorkingDirButton;

	@FXML
	private TextField pathTextField;
	
	@FXML
	private GridPane root;
	
	@FXML
	private CheckBox terminationCheckBox;
	
	@FXML
	private TextField workingDirTextField;
	
	protected RunProgramAction action;

	protected String origArguments = "";
	
	protected String origPath = "";
	
	protected String origWorkingDir = "";
	
	protected boolean  origKillIfStillRunning = false;
	
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
		action = (RunProgramAction) element;
		origArguments = action.getArguments();
		origPath = action.getPath();
		origWorkingDir = action.getWorkingDir();
		origKillIfStillRunning = action.getkillIfStillRunning();
		argumentsTextField.textProperty().bindBidirectional(action.argumentsProperty());
		pathTextField.textProperty().bindBidirectional(action.pathProperty());
		workingDirTextField.textProperty().bindBidirectional(action.workingDirProperty());
		terminationCheckBox.selectedProperty()
				.bindBidirectional(action.killIfStillRunningProperty());
	}

	@FXML
	void browsePath(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose the executable file(program)");
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			fileChooser.setInitialDirectory(new File(lastDir));
		}
		File file = fileChooser.showOpenDialog(root.getScene().getWindow());
		if (file != null) {
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastFileDirectory",
					file.getParentFile().getAbsolutePath());
			pathTextField.setText(file.getAbsolutePath());
		}
	}

	@FXML
	void browseWorkingDir(ActionEvent event) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose the \"working directory\"");
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			dirChooser.setInitialDirectory(new File(lastDir));
		}
		File dir = dirChooser.showDialog(root.getScene().getWindow());
		if (dir != null) {
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastFileDirectory",
					dir.getParentFile().getAbsolutePath());
			workingDirTextField.setText(dir.getAbsolutePath());
		}
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
		Validator<String> pathFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please select a path to the program", newValue.isEmpty());
			ValidationResult validPathResult = ValidationResult.fromErrorIf(c,
					"Executable file is not found on this path",
					!action.checkIfPathIsAbsoluteAndFileExists(newValue));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					validPathResult);
			buttonStateManager.reportNewValueOfControl(origPath, newValue, c, finalResult);
			return finalResult;
		};
		Validator<String> argumentsFieldValidator = (c, newValue) -> {
			ValidationResult alwaysSuccessfulResult = ValidationResult.fromErrorIf(c,
					"The user will not see this message", false);
			buttonStateManager.reportNewValueOfControl(origArguments, newValue, c,
					alwaysSuccessfulResult);
			return alwaysSuccessfulResult;
		};
		Validator<String> workingDirFieldValidator = (c, newValue) -> {
			ValidationResult validWorkingDirResult = ValidationResult.fromErrorIf(c,
					"Please select an existing \"working directory\" or leave this field empty",
					!(Util.checkDirectory(newValue) || newValue.isEmpty()));
			ValidationResult finalResult = ValidationResult.fromResults(validWorkingDirResult);
			buttonStateManager.reportNewValueOfControl(origWorkingDir, newValue, c, finalResult);
			return finalResult;
		};
		Validator<Boolean> terminationCheckBoxValidator = (c, newValue) -> {
			ValidationResult alwaysSuccessfulResult = ValidationResult.fromErrorIf(c,
					"The user will not see this message", false);
			buttonStateManager.reportNewValueOfControl(origKillIfStillRunning, newValue, c,
					alwaysSuccessfulResult);
			return alwaysSuccessfulResult;
		};
		this.validationSupport.registerValidator(pathTextField, pathFieldValidator);
		this.validationSupport.registerValidator(argumentsTextField, argumentsFieldValidator);
		this.validationSupport.registerValidator(workingDirTextField, workingDirFieldValidator);
		this.validationSupport.registerValidator(terminationCheckBox, terminationCheckBoxValidator);
	}

}
