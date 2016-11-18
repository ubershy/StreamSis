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

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.checkers.VariableChecker;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.UserVars;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class VariableCheckerController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;

	@FXML
	private CustomTextField variableNameTextField;

	@FXML
	private TextField valueTextField;

	/** The {@link VariableChecker} to edit. */
	protected VariableChecker varChecker;

	/** The original {@link VariableChecker} to compare values with {@link #varChecker}. */
	protected VariableChecker origVarChecker;

	protected ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Do nothing.
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		varChecker = (VariableChecker) editableCopyOfCE;
		origVarChecker = (VariableChecker) origCE;
		bindBidirectionalAndRemember(variableNameTextField.textProperty(),
				varChecker.keyProperty());
		TextFields.bindAutoCompletion(variableNameTextField,
				UserVars.getCopyOfCurrentVariables().keySet());
		bindBidirectionalAndRemember(valueTextField.textProperty(),
				varChecker.expectedValueProperty());
		valueTextField.textProperty().addListener((o, oldVal, newVal) -> {
			buttonStateManager.reportNewValueOfControl(origVarChecker.getExpectedValue(), newVal,
					valueTextField, null);
		});
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
		this.validationSupport = validationSupport;
		Validator<String> variableNameTextFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify Variable name", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(origVarChecker.getKey(), newValue, c,
					emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(variableNameTextField,
				variableNameTextFieldValidator);
	}

}
