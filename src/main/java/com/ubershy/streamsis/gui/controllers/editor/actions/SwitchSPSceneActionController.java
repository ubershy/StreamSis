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

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.actions.networking.SwitchSPSceneAction;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class SwitchSPSceneActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
    
    @FXML
    private CustomTextField sceneNameTextField;

    /** The {@link SwitchSPSceneAction} to edit. */
	protected SwitchSPSceneAction sspsAction;
	
	/** The original {@link SwitchSPSceneAction} to compare values with {@link #sspsAction}. */
	protected SwitchSPSceneAction origSspsAction;
	
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
		sspsAction = (SwitchSPSceneAction) editableCopyOfCE;
		origSspsAction = (SwitchSPSceneAction) origCE;
		bindBidirectionalAndRemember(sceneNameTextField.textProperty(),
				sspsAction.sceneNameProperty());
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
		Validator<String> sceneNameTextFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Specify existing scene name in Streaming Program", newValue.isEmpty());
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult);
			buttonStateManager.reportNewValueOfControl(origSspsAction.getSceneName(), newValue, c,
					finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(sceneNameTextField, sceneNameTextFieldValidator);
	}

}
