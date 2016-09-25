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
package com.ubershy.streamsis.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.CompoundValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.CuteGraphicValidationDecoration;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.CuteNodeContainer;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

public class PropsWithNameController implements Initializable {

	private CuteButtonsStatesManager buttonStateManager;

	private CuteController currentCuteController;

	private CuteElement elementWorkingCopy;

	@FXML
	private TextField nameTextField;

	private String originalName;
	
	@FXML
	private GridPane root;

	private ValidationSupport validationSupport;
	
	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				String url = PropsWithNameController.class.getResource("/css/validation.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		recreateNameTextField();
	}

	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		this.buttonStateManager = buttonStateManager;
	}

	public void connectToCuteElement(CuteElement newElementCopy) {
		disconnectFromConnectedCuteElement();
		// nameTextField should be recreated before invoking recreateValidationSupport() method.
		recreateNameTextField();
		// Let's remember the reference to the CuteElement's copy
		elementWorkingCopy = newElementCopy;
		// Let's remember original name of the CuteElement
		ElementInfo newInfo = newElementCopy.getElementInfo();
		originalName = newInfo.getName();
		// Let's get the new controller specific to CuteElement's type
		currentCuteController = StreamSisAppFactory
				.buildCuteControllerBasedOnCuteElement(newElementCopy);
		// Let's set the new controller and view for the cell
		currentCuteController.setCuteButtonsStatesManager(buttonStateManager);
		// Bind element to CuteController's View from which the user can edit subtype-specific
		// variables of the CuteElement's copy. Modified CuteElement's copy will be used to spread
		// changes to original CuteElement.
		currentCuteController.bindToCuteElement(newElementCopy);
		// Let's bind nameTextField to CuteElement's name before invoking
		// recreateValidationSupport() method. 
		nameTextField.setText(newInfo.getName());
		nameTextField.textProperty().bindBidirectional(newInfo.nameProperty());
		// This will update validationSupport and validationListener variables.
		recreateValidationSupport();
		// Give new validationSupport to currentCuteController, so it can validate input in fields.
		currentCuteController.setValidationSupport(validationSupport);
		validationSupport.initInitialDecoration();
		root.add(currentCuteController.getView(), 0, 1);
		// CuteNodeContainer should not be edited.
		if (newElementCopy instanceof CuteNodeContainer) {
			nameTextField.setDisable(true);
		} else {
			nameTextField.setDisable(false);
		}
		if (buttonStateManager.needToReinitCuteElementProperty().get())
			newElementCopy.init();
			buttonStateManager.setCuteElementAsInitialized();
	}

	/**
	 * Replaces {@link #nameTextField} responsible for editing name with new one. <br>
	 * It's needed because when we do {@link #recreateValidationSupport()}, the old
	 * ValidationSupport object still have registered validator to nameTextField. So to garbage
	 * collect old ValidationSupport object also we have to dump nameTextField with it.
	 */
	private void recreateNameTextField() {
		TextField newTextField = new TextField();
		GUIUtil.replaceChildInPane(nameTextField, newTextField);
		nameTextField = newTextField;
		// Let's not allow very long names
		UnaryOperator<Change> filter = c -> {
			if (c.getControlNewText().length() > 30) {
				return null;
			}
			return c;
		};
		nameTextField.setTextFormatter(new TextFormatter<>(filter));
		// Reset nameTextField on ESC key
		nameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					nameTextField.setText(originalName);
					// When root's parent is focused, second hit to Escape key will close panel
					root.getParent().requestFocus();
				}
			}
		});
	}

	private void disconnectFromConnectedCuteElement() {
		// Let's unbind name text field from previous CuteElement's name
		if (elementWorkingCopy != null) {
			nameTextField.textProperty()
					.unbindBidirectional(elementWorkingCopy.getElementInfo().nameProperty());
		}
		// Let's unbind currentCuteController's fields from previous CuteElement's properties
		if (currentCuteController != null) {
			currentCuteController.unbindFromCuteElement();
		}
		// Let's clean the cell from the previous view, if it exists
		if (currentCuteController != null) {
			root.getChildren().remove(currentCuteController.getView());
		}
	}

	private boolean validateNameEmptiness(Control c, String name) {
		return name.equals("");
	}

	private boolean validateNameAlreadyExistence(Control c, String newValue) {
		if (newValue.equals(originalName))
			return false;
		if (elementWorkingCopy instanceof SisScene) {
			SisScene existingSisScene = ProjectManager.getProject().getSisSceneByName(newValue);
			if (existingSisScene != null) {
				if (existingSisScene != elementWorkingCopy) {
					// So SisScene with such name already exists, it's bad
					return true;
				}
			}
		}
		if (elementWorkingCopy instanceof Actor) {
			Actor existingActor = ProjectManager.getProject().getActorByName(newValue);
			if (existingActor != null) {
				if (existingActor != elementWorkingCopy) {
					// So Actor with such name already exists, it's bad
					return true;
				}
			}
		}
		// Elements of other types can have same names, so no error
		return false;
	}
	
	/**
	 * Recreates {@link #validationSupport}, so the old validationSupport which is bound to unwanted
	 * fields can be garbage collected.
	 */
	private void recreateValidationSupport() {
		validationSupport = new ValidationSupport();
		CompoundValidationDecoration validationDecor = new CompoundValidationDecoration(
				// These styles are located in /StreamSis/src/main/resources/css/validation.css
				new StyleClassValidationDecoration("cuteerror", "cutewarning"),
				new CuteGraphicValidationDecoration());
		validationSupport.setValidationDecorator(validationDecor);
		Validator<String> nameTextFieldValidator = (c, newValue) -> {
			ValidationResult alreadyExistanceResult = ValidationResult.fromErrorIf(c,
					"The name is already taken. Please choose another one",
					validateNameAlreadyExistence(c, newValue));
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please choose a name for the element", validateNameEmptiness(c, newValue));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					alreadyExistanceResult);
			buttonStateManager.reportNewValueOfControl(originalName, newValue, c, finalResult);
			return finalResult;
		};
		validationSupport.registerValidator(nameTextField, nameTextFieldValidator);
	}

}
