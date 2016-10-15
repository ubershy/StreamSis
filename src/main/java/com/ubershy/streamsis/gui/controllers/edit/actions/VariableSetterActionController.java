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

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.actions.VariableSetterAction;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.UserVars;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class VariableSetterActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;

	@FXML
	private CustomTextField variableNameTextField;

	@FXML
	private TextField valueTextField;

	/** The {@link VariableSetterAction} to edit. */
	protected VariableSetterAction vsAction;

	/** The original {@link VariableSetterAction} to compare values with {@link #vsAction}. */
	protected VariableSetterAction origVsAction;

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
		vsAction = (VariableSetterAction) editableCopyOfCE;
		origVsAction = (VariableSetterAction) origCE;
		bindBidirectionalAndRemember(variableNameTextField.textProperty(), vsAction.keyProperty());
		TextFields.bindAutoCompletion(variableNameTextField,
				UserVars.getCopyOfCurrentVariables().keySet());
		bindBidirectionalAndRemember(valueTextField.textProperty(), vsAction.valueProperty());
		valueTextField.textProperty().addListener((o, oldVal, newVal) -> {
			buttonStateManager.reportNewValueOfControl(origVsAction.getValue(), newVal,
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
			buttonStateManager.reportNewValueOfControl(origVsAction.getKey(), newValue, c,
					emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(variableNameTextField,
				variableNameTextFieldValidator);
	}

}
