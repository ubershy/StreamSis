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
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.actions.SwitchSisSceneAction;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.ProjectManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class SwitchSisSceneActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
    
    @FXML
    private CustomTextField ssNameTextField;

    /** The {@link SwitchSisSceneAction} to edit. */
	protected SwitchSisSceneAction sssAction;
	
	/** The original {@link SwitchSisSceneAction} to compare values with {@link #sssAction}. */
	protected SwitchSisSceneAction origSssAction;
	
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
		sssAction = (SwitchSisSceneAction) editableCopyOfCE;
		origSssAction = (SwitchSisSceneAction) origCE;
		bindBidirectionalAndRemember(ssNameTextField.textProperty(),
				sssAction.sisSceneNameProperty());
		TextFields.bindAutoCompletion(ssNameTextField, ProjectManager.getProject().getSisScenes());
		
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
		Validator<String> ssNameTextFieldValidator = (c, newValue) -> {
			boolean sisExist = ProjectManager.getProject().getSisSceneByName(newValue)!= null;
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please specify existing SisScene name", newValue.isEmpty());
			ValidationResult unexistingSisScene = ValidationResult.fromErrorIf(c,
					"SisScene with such name does not exist", !sisExist);
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					unexistingSisScene);
			buttonStateManager.reportNewValueOfControl(origSssAction.getSisSceneName(),
					newValue, c, finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(ssNameTextField, ssNameTextFieldValidator);
	}

}
