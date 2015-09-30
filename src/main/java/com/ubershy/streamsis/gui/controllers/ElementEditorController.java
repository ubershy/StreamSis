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

import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.animations.HorizontalShadowAnimation;
import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.gui.helperclasses.ApplyAndOkButtonsStateManager;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ElementInfo.ElementHealth;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;

// Under construction. Not working. Please unsee.
public class ElementEditorController implements Initializable {

	@FXML
	private TitledPane root;

	@FXML
	private ScrollPane propertiesPane;

	@FXML
	private Button applyButton;

	@FXML
	private Button performTestButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Button OKButton;

	@FXML
	private Label nameLabel;

	@FXML
	private Label typeLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private ScrollPane unhealthyPane;

	@FXML
	private Label whyUnhealthyLabel;

	@FXML
	private Label propertiesPaneDots;

	private PropsWithNameController propsWithNameController;

	private ApplyAndOkButtonsStateManager buttonStateManager = new ApplyAndOkButtonsStateManager();

	/** The property of last selected CuteElement */
	public ObjectProperty<CuteElement> lastFocusedProperty = new SimpleObjectProperty<>();

	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>();

	private StringProperty whyUnhealthyProperty = new SimpleStringProperty();

	private HorizontalShadowAnimation hsShadowAnima;

	private HorizontalShadowAnimation nameShadowAnima;

	private HorizontalShadowAnimation typeShadowAnima;

	private HorizontalShadowAnimation whyShadowAnima;

	private ThreeDotsAnimation tPaneDotsAnima;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		CuteController noneController = StreamSisAppFactory
				.buildSpecificControllerByCuteElementName("NoneSelected");
		propsWithNameController = (PropsWithNameController) StreamSisAppFactory
				.buildControllerByRelativePath("PropsWithName.fxml");
		propsWithNameController.setApplyAndOkButtonsStateManager(buttonStateManager);
		propertiesPane.setContent(noneController.getView());
		hsShadowAnima = new HorizontalShadowAnimation(statusLabel);
		nameShadowAnima = new HorizontalShadowAnimation(nameLabel);
		typeShadowAnima = new HorizontalShadowAnimation(typeLabel);
		whyShadowAnima = new HorizontalShadowAnimation(whyUnhealthyLabel);
		tPaneDotsAnima = new ThreeDotsAnimation("Editing", root, 1);
		initializeAllListeners();
		bindThingies();
	}

	private void bindThingies() {
		applyButton.disableProperty().bind(buttonStateManager.canApplyProperty().not());
		OKButton.disableProperty().bind(buttonStateManager.errorsExistProperty());

	}

	private void initializeAllListeners() {
		lastFocusedProperty
				.addListener((ChangeListener<CuteElement>) (observable, oldValue, newValue) -> {
					if (!propertiesPane.getContent().equals(propsWithNameController.getView())) {
						propertiesPane.setContent(propsWithNameController.getView());
					}
					connectToNewElement(oldValue, newValue);
				});
		root.expandedProperty()
				.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
					if (newValue) {
						tPaneDotsAnima.play();
					} else {
						tPaneDotsAnima.stop();
					}
				});
		nameLabel.textProperty()
				.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
					nameShadowAnima.play();
				});
		typeLabel.textProperty()
				.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
					typeShadowAnima.play();
				});
		elementHealthProperty.addListener((ChangeListener<ElementHealth>) (observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				defineElementHealthStyle(newValue);
				hsShadowAnima.play();
			});
		});
		whyUnhealthyProperty
				.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
					whyUnhealthyLabel.setText(newValue);
					whyShadowAnima.play();
				});
	}

	private void connectToNewElement(CuteElement oldValue, CuteElement newValue) {
		// Lets clean up after previous element
		nameLabel.textProperty().unbind();
		elementHealthProperty.unbind();
		whyUnhealthyProperty.unbind();

		// Lets set up initial values
		String simpleClassName = newValue.getClass().getSimpleName();
		ElementInfo newInfo = newValue.getElementInfo();
		typeLabel.setText(simpleClassName);
		defineElementHealthStyle(newInfo.elementHealthProperty().get());
		whyUnhealthyLabel.setText(newInfo.getUnhealthyMessage());

		// Now let's bind all properties and set up the desired behavior
		nameLabel.textProperty().bind(newInfo.nameProperty());
		elementHealthProperty.bind(newInfo.elementHealthProperty());
		whyUnhealthyProperty.bind(newInfo.whyUnhealthyProperty());

		// Lets attach new view to propertiesPane
		propsWithNameController.setPropertiesViewByCuteElement(newValue);
	}

	protected void defineElementHealthStyle(ElementHealth elementHealth) {
		statusLabel.setText(elementHealth.toString());
		switch (elementHealth) {
		case BROKEN:
			unhealthyPane.setVisible(true);
			unhealthyPane.setManaged(true);
			statusLabel.setTextFill(Color.RED);
			break;
		case HEALTHY:
			unhealthyPane.setVisible(false);
			unhealthyPane.setManaged(false);
			statusLabel.setTextFill(Color.GREEN);
			break;
		case SICK:
			unhealthyPane.setVisible(true);
			unhealthyPane.setManaged(true);
			statusLabel.setTextFill(Color.YELLOW);
			break;
		default:
			break;
		}
	}

	@FXML
	void hitApplyButton(ActionEvent event) {
		propsWithNameController.apply();
	}

	@FXML
	void hitOKButton(ActionEvent event) {
		root.expandedProperty().set(false);
		propsWithNameController.apply();
	}

	@FXML
	void hitCancelButton(ActionEvent event) {
		root.expandedProperty().set(false);
		propsWithNameController.reset();
	}

}
