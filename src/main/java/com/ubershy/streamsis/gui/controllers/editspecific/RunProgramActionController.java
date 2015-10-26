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

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.RunProgramAction;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
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

	protected RunProgramAction action;

	protected StringProperty argumentsProperty = new SimpleStringProperty("");

	@FXML
	private TextField argumentsTextField;

	@FXML
	private Button browsePathButton;

	@FXML
	private Button browseWorkingDirButton;

	protected BooleanProperty killIfStillRunningProperty = new SimpleBooleanProperty(false);

	protected StringProperty pathProperty = new SimpleStringProperty("");

	@FXML
	private TextField pathTextField;
	@FXML
	private GridPane root;
	@FXML
	private CheckBox terminationCheckBox;
	protected ValidationSupport validationSupport;

	protected StringProperty workingDirProperty = new SimpleStringProperty("");

	@FXML
	private TextField workingDirTextField;

	/*
	 * @inheritDoc
	 */
	@Override
	public void apply() {
		action.setPath(pathTextField.getText());
		action.setArguments(argumentsTextField.getText());
		action.setWorkingDir(workingDirTextField.getText());
		action.setkillIfStillRunning(terminationCheckBox.selectedProperty().get());
		action.init();
	}

	private void BindProperties(RunProgramAction actn) {
		pathProperty.bind(actn.pathProperty());
		argumentsProperty.bind(actn.argumentsProperty());
		killIfStillRunningProperty.bind(actn.killIfStillRunningProperty());
		workingDirProperty.bind(actn.workingDirProperty());
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement element) {
		action = (RunProgramAction) element;
		reset();
		BindProperties(action);
		pathProperty.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				pathTextField.setText(newValue);
			});
		});
		argumentsProperty.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				System.out.println("jaja");
				argumentsTextField.setText(newValue);
			});
		});
		workingDirProperty
				.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
					Platform.runLater(() -> {
						workingDirTextField.setText(newValue);
					});
				});
		killIfStillRunningProperty
				.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
					Platform.runLater(() -> {
						terminationCheckBox.setSelected(newValue);
					});
				});
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
	public void reset() {
		pathTextField.setText(action.getPath());
		argumentsTextField.setText(action.getArguments());
		workingDirTextField.setText(action.getWorkingDir());
		terminationCheckBox.setSelected(action.getkillIfStillRunning());
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
			GUIUtil.reportToButtonStateManager(action.getPath(), newValue, c, finalResult,
					buttonStateManager);
			workingDirTextField.setText("");
			return finalResult;
		};
		Validator<String> argumentsFieldValidator = (c, newValue) -> {
			ValidationResult alwaysSuccessfulResult = ValidationResult.fromErrorIf(c,
					"The user will not see this message", false);
			GUIUtil.reportToButtonStateManager(action.getArguments(), newValue, c,
					alwaysSuccessfulResult, buttonStateManager);
			return alwaysSuccessfulResult;
		};
		Validator<String> workingDirFieldValidator = (c, newValue) -> {
			ValidationResult validWorkingDirResult = ValidationResult.fromErrorIf(c,
					"Please select a \"working directory\"",
					!(Util.checkDirectory(newValue) || newValue.isEmpty()));
			ValidationResult finalResult = ValidationResult.fromResults(validWorkingDirResult);
			GUIUtil.reportToButtonStateManager(action.getWorkingDir(), newValue, c, finalResult,
					buttonStateManager);
			return finalResult;
		};
		Validator<Boolean> terminationCheckBoxValidator = (c, newValue) -> {
			ValidationResult alwaysSuccessfulResult = ValidationResult.fromErrorIf(c,
					"The user will not see this message", false);
			GUIUtil.reportToButtonStateManager(action.getkillIfStillRunning(), newValue, c,
					alwaysSuccessfulResult, buttonStateManager);
			return alwaysSuccessfulResult;
		};
		this.validationSupport.registerValidator(pathTextField, pathFieldValidator);
		this.validationSupport.registerValidator(argumentsTextField, argumentsFieldValidator);
		this.validationSupport.registerValidator(workingDirTextField, workingDirFieldValidator);
		this.validationSupport.registerValidator(terminationCheckBox, terminationCheckBoxValidator);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		pathProperty.unbind();
		argumentsProperty.unbind();
		killIfStillRunningProperty.unbind();
		workingDirProperty.unbind();
	}

}
