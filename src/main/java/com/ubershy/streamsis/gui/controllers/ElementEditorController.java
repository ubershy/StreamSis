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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.PropertyUtils;

import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.animations.HorizontalShadowAnimation;
import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ElementSerializator;
import com.ubershy.streamsis.project.ProjectManager;
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

	private CuteButtonsStatesManager buttonStateManager = new CuteButtonsStatesManager();

	/** The property of last selected CuteElement */
	public ObjectProperty<CuteElement> lastFocusedProperty = new SimpleObjectProperty<>();

	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>();

	private StringProperty whyUnhealthyProperty = new SimpleStringProperty();

	private HorizontalShadowAnimation hsShadowAnima;

	private HorizontalShadowAnimation nameShadowAnima;

	private HorizontalShadowAnimation typeShadowAnima;

	private HorizontalShadowAnimation whyShadowAnima;

	private ThreeDotsAnimation tPaneDotsAnima;
	
	private CuteElement elementCopy = null;
	
	private CuteElement currentElement = null;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		CuteController noneController = StreamSisAppFactory
				.buildSpecificControllerByCuteElementName("NoneSelected");
		propsWithNameController = (PropsWithNameController) StreamSisAppFactory
				.buildControllerByRelativePath("PropsWithName.fxml");
		propsWithNameController.setCuteButtonsStatesManager(buttonStateManager);
		propertiesPane.setContent(noneController.getView());
		hsShadowAnima = new HorizontalShadowAnimation(statusLabel);
		nameShadowAnima = new HorizontalShadowAnimation(nameLabel);
		typeShadowAnima = new HorizontalShadowAnimation(typeLabel);
		whyShadowAnima = new HorizontalShadowAnimation(whyUnhealthyLabel);
		tPaneDotsAnima = new ThreeDotsAnimation("Editing", root, 1);
		initializeAllListeners();
		bindButtons();
	}

	private void bindButtons() {
		applyButton.disableProperty().bind((buttonStateManager.applyButtonOnProperty().not()));
		OKButton.disableProperty()
				.bind(buttonStateManager.okAndPerformTestButtonsOnProperty().not());
		performTestButton.disableProperty()
				.bind(buttonStateManager.okAndPerformTestButtonsOnProperty().not());
	}

	private void initializeAllListeners() {
		lastFocusedProperty
				.addListener((ChangeListener<CuteElement>) (observable, oldValue, newValue) -> {
					if (!propertiesPane.getContent().equals(propsWithNameController.getView())) {
						propertiesPane.setContent(propsWithNameController.getView());
					}
					connectToNewElement(newValue);
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
		elementHealthProperty
				.addListener((ChangeListener<ElementHealth>) (observable, oldValue, newValue) -> {
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

	private void connectToNewElement(CuteElement currentElement) {
		// Let's clean up after previous CuteElement
		nameLabel.textProperty().unbind();
		elementHealthProperty.unbind();
		whyUnhealthyProperty.unbind();
		
		this.currentElement = currentElement;

		// CuteElements of higher hierarchy levels are referring to current element, and it's hard
		// to substitute this reference. It's easier to retain this reference.
		// Let's create a copy of the current CuteElement and work on the copy. After the user will
		// finish editing this copy and press "Ok" or "Apply" buttons, we can transfer changes back
		// to the original CuteElement.
		String serializedCurrentElement = null;
		try {
			serializedCurrentElement = ElementSerializator.serializeToString(currentElement);
			elementCopy = ElementSerializator.deserializeFromString(serializedCurrentElement);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		elementCopy.init();
		
		// Let's set up initial values of visible textLabels describing the CuteElement
		String simpleClassName = elementCopy.getClass().getSimpleName();
		ElementInfo infoOfCopyElement = elementCopy.getElementInfo();
		typeLabel.setText(simpleClassName);
		defineElementHealthStyle(infoOfCopyElement.elementHealthProperty().get());
		whyUnhealthyLabel.setText(infoOfCopyElement.getUnhealthyMessage());

		// Now let's bind all properties to visible textLabels describing the CuteElement
		nameLabel.textProperty().bind(infoOfCopyElement.nameProperty());
		elementHealthProperty.bind(infoOfCopyElement.elementHealthProperty());
		whyUnhealthyProperty.bind(infoOfCopyElement.whyUnhealthyProperty());

		// Let's set up propertiesPane according to CuteElement
		propsWithNameController.setPropertiesViewByCuteElement(elementCopy);
		
		// Let's reset state manager of buttons
		buttonStateManager.reset();
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
			statusLabel.setTextFill(Color.GOLDENROD);
			break;
		default:
			break;
		}
	}

	private void applyChanges() {
		// Current CuteElement's copy at this moment was modified by user, we need to transfer
		// changes to the original CuteElement.
		String NameOfCopyElement = elementCopy.getElementInfo().getName();
		String NameOfCurrentElement = currentElement.getElementInfo().getName();
		// setCuteElementNameSafely() will throw IllegalArgument exception if "name" field
		// validation in propsWithNameController is not implemented correctly.
		if (!NameOfCopyElement.equals(NameOfCurrentElement))
			ProjectManager.getProject().setCuteElementNameSafely(currentElement, NameOfCopyElement);
		// Let's apply changes from the copy to original CuteElement by copying properties
		try {
			PropertyUtils.copyProperties(currentElement, elementCopy);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException("For some reason can't transfer changes from copy of the "
					+ "current CuteElement to the current CuteElement");
		}
		currentElement.init();
		connectToNewElement(currentElement);
	}
	
	@FXML
	void hitApplyButton(ActionEvent event) {
		applyChanges();
	}

	@FXML
	void hitOKButton(ActionEvent event) {
		root.expandedProperty().set(false);
		applyChanges();
	}

	@FXML
	void hitCancelButton(ActionEvent event) {
		root.expandedProperty().set(false);
		currentElement.init();
		connectToNewElement(currentElement);
	}

}
