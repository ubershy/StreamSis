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
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.actions.DelayedActions;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.helperclasses.IntegerTextField;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * DelayedActionsController, the controller that allows to edit {@link DelayedActions} actions in a
 * panel.
 */
public class DelayedActionsController extends AbstractCuteController
		implements CuteElementController {

    /** The root node. */
    @FXML
    private GridPane root;

    /** The container for {@link #delayIntegerTextField}. */
    @FXML
    private Pane delayFieldContainer;

    /** The IntegerTextField for editing {@link DelayedActions#delayProperty()}. */
    private IntegerTextField delayIntegerTextField = new IntegerTextField(100000, false);
	
    /** The {@link DelayedActions} to edit. */
    private DelayedActions delayedActions;
    
	/** The original {@link DelayedActions} to compare values with {@link #delayedActions}. */
	private DelayedActions origDelayedActions;

    /** The Validation Support for this controller. */
    private ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		delayFieldContainer.getChildren().add(delayIntegerTextField);
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
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		delayedActions = (DelayedActions) editableCopyOfCE;
		origDelayedActions = (DelayedActions) origCE;
		// Bind to the new Actor.
		bindBidirectionalAndRemember(delayIntegerTextField.numberProperty(),
				delayedActions.delayProperty());
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
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		Validator<String> delayValidator = (c, newValue) -> {
			IntegerTextField tf = (IntegerTextField) c;
			int number = tf.numberProperty().get();
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"This field can't be empty.", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(origDelayedActions.getDelay(), number, c,
					emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(delayIntegerTextField, delayValidator);
	}

}
