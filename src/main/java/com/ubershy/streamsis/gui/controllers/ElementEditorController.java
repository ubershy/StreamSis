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
import org.controlsfx.control.MasterDetailPane;

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.SpecialCuteController;
import com.ubershy.streamsis.gui.animations.HorizontalShadowAnimation;
import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ElementSerializator;
import com.ubershy.streamsis.project.ProjectManager;
import com.ubershy.streamsis.project.ElementInfo.ElementHealth;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

// Under construction. Not working. Please unsee.
public class ElementEditorController implements Initializable {

	@FXML
	private TitledPane root;

	@FXML
	private ScrollPane propertiesPane;
	
	@FXML
    private GridPane buttonsGridPane;

	@FXML
	private Button applyButton;

	private Button performTestButton = new Button("Perform Test");
	
	private String testResultText = "Eww!";
	
	private Color testResultColor;
	
	private Button performTestStatusFakeButton = new Button(testResultText);
	
	private MasterDetailPane performTestPane = new MasterDetailPane(Side.RIGHT);

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
	
	private Timeline hideTestResultAnimation = new Timeline();

	private ThreeDotsAnimation tPaneDotsAnima;
	
	private ThreeDotsAnimation buttonsBeforeInitDotsAnima;
	
	private StringProperty awaitingForInputTextProperty = new SimpleStringProperty();
	
	private CuteElement elementWorkingCopy = null;
	
	private CuteElement currentElement = null;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		CuteController noneController = StreamSisAppFactory
				.buildSpecialCuteController(SpecialCuteController.NONESELECTED);
		propsWithNameController = (PropsWithNameController) StreamSisAppFactory
				.buildControllerByRelativePath("PropsWithName.fxml");
		propsWithNameController.setCuteButtonsStatesManager(buttonStateManager);
		propertiesPane.setContent(noneController.getView());
		hsShadowAnima = new HorizontalShadowAnimation(statusLabel);
		nameShadowAnima = new HorizontalShadowAnimation(nameLabel);
		typeShadowAnima = new HorizontalShadowAnimation(typeLabel);
		whyShadowAnima = new HorizontalShadowAnimation(whyUnhealthyLabel);
		tPaneDotsAnima = new ThreeDotsAnimation("Editing", '.', root.textProperty(), 1, 1000,
				Timeline.INDEFINITE);
		buttonsBeforeInitDotsAnima = new ThreeDotsAnimation("", '❤', awaitingForInputTextProperty,
				1, 500, 1);
		performTestButton.setOnAction(this::hitPerformTestButton);
		performTestButton.wrapTextProperty().set(true);
		performTestButton.setTextAlignment(TextAlignment.CENTER);
        performTestStatusFakeButton.wrapTextProperty().set(true);
        performTestStatusFakeButton.setTextAlignment(TextAlignment.CENTER);
        performTestPane.setShowDetailNode(true);
        performTestPane.setMasterNode(performTestButton);
        performTestPane.setDetailNode(performTestStatusFakeButton);
		performTestPane.dividerPositionProperty().set(0.5);
		performTestPane.showDetailNodeProperty().set(false);
		KeyFrame hideTestResultFrame = new KeyFrame(Duration.seconds(2), event -> {
			performTestPane.showDetailNodeProperty().set(false);
		});
		hideTestResultAnimation.getKeyFrames().add(hideTestResultFrame);
        buttonsGridPane.add(performTestPane, 0, 3);
		initializeAllListeners();
		bindButtons();
	}

	private void bindButtons() {
		applyButton.disableProperty().bind((buttonStateManager.applyButtonOnProperty().not()));
		OKButton.disableProperty()
				.bind(buttonStateManager.okButtonOnProperty().not());
		performTestButton.disableProperty()
				.bind(buttonStateManager.performTestButtonOnProperty().not());
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
		this.buttonStateManager.needToReinitCuteElementProperty()
		.addListener((InvalidationListener) o -> {
			if (this.buttonStateManager.needToReinitCuteElementProperty().get()) {
			    // CuteElement initialization is a resource hungry operation. Let's not initialize
			    // instantly, because the user might not finished typing into fields. Instead let's
				// play a text animation and when the text animation will finish playing itself to
				// the end, let's do initialization. The animation can be interrupted by new user
				// input and started over.
				OKButton.textProperty().bind(awaitingForInputTextProperty);
				applyButton.textProperty().bind(awaitingForInputTextProperty);
				performTestButton.textProperty().bind(awaitingForInputTextProperty);
				// This animation will internally animate awaitingForInputTextProperty's text.
				buttonsBeforeInitDotsAnima.play();
			}
		});
		buttonsBeforeInitDotsAnima.setOnFinished(evt -> {
			// After animation is done, let's unbind button text properties from the property which
			// is animated - buttonsBeforeInitDotsAnima.
			OKButton.textProperty().unbind();
			applyButton.textProperty().unbind();
			performTestButton.textProperty().unbind();
			String initText = "Initializing...";
			OKButton.setText(initText);
			applyButton.setText(initText);
			performTestButton.setText(initText);
			// TODO: run init() in another thread, as JavaFX thread may hang.
			elementWorkingCopy.init();
			// Restore texts of buttons
			OKButton.setText("OK");
			applyButton.setText("Apply");
			performTestButton.setText("Perform Test");
			buttonStateManager.setCuteElementAsInitialized();
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
			elementWorkingCopy = ElementSerializator.deserializeFromString(serializedCurrentElement);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		elementWorkingCopy.init();
		
		// Let's set up initial values of visible textLabels describing the CuteElement
		String simpleClassName = elementWorkingCopy.getClass().getSimpleName();
		ElementInfo infoOfCopyElement = elementWorkingCopy.getElementInfo();
		typeLabel.setText(simpleClassName);
		defineElementHealthStyle(infoOfCopyElement.elementHealthProperty().get());
		whyUnhealthyLabel.setText(infoOfCopyElement.getUnhealthyMessage());

		// Now let's bind all properties to visible textLabels describing the CuteElement
		nameLabel.textProperty().bind(infoOfCopyElement.nameProperty());
		elementHealthProperty.bind(infoOfCopyElement.elementHealthProperty());
		whyUnhealthyProperty.bind(infoOfCopyElement.whyUnhealthyProperty());

		// Let's set up propertiesPane according to CuteElement
		propsWithNameController.setPropertiesViewByCuteElement(elementWorkingCopy);
		
		// Let's reset state manager of buttons
		buttonStateManager.reset();
		
		buttonStateManager.allowOrNotPerformTestButtonBasedOnElementClass(elementWorkingCopy);
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
		String NameOfCopyElement = elementWorkingCopy.getElementInfo().getName();
		String NameOfCurrentElement = currentElement.getElementInfo().getName();
		// setCuteElementNameSafely() will throw IllegalArgument exception if "name" field
		// validation in propsWithNameController is not implemented correctly.
		if (!NameOfCopyElement.equals(NameOfCurrentElement))
			ProjectManager.getProject().setCuteElementNameSafely(currentElement, NameOfCopyElement);
		// Let's apply changes from the copy to original CuteElement by copying properties
		try {
			// Note: if you notice this method doesn't transfer changes, CuteElement's properties
			// and getters and setters might not be set up properly.
			PropertyUtils.copyProperties(currentElement, elementWorkingCopy);
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
	
	@FXML
	void hitPerformTestButton(ActionEvent event) {
		buttonStateManager.reportStartOfTest();
		if (hideTestResultAnimation.getStatus().equals(Status.STOPPED))
			performTestPane.dividerPositionProperty().set(0.5);
		hideTestResultAnimation.stop();
		testResultText = "Testing...";
		testResultColor = Color.RED;
		performTestStatusFakeButton.setText(testResultText);
		InnerShadow shadow = new InnerShadow(10, testResultColor);
		shadow.setOffsetY(0f);
		shadow.setOffsetX(0f);
		performTestStatusFakeButton.setEffect(shadow);
		performTestPane.showDetailNodeProperty().set(true);
		Task<Void> task = new Task<Void>() {
	        @Override
	        protected Void call() throws Exception {
	        	if (elementWorkingCopy instanceof Action) {
	    		    Action action = (Action) elementWorkingCopy;
	    		    action.execute();
	    		    Platform.runLater(() -> {
						testResultText = "Done";
					    testResultColor = Color.PALEGREEN;
					});
	    		} else if (elementWorkingCopy instanceof Checker) {
	    			Checker checker = (Checker) elementWorkingCopy;
	    			boolean result = checker.check();
	    			Platform.runLater(() -> {
		    			if (result) {
		    				testResultText = "True";
		    				testResultColor = Color.DEEPSKYBLUE;
		    			} else {
		    				testResultText = "False";
		    				testResultColor = Color.HOTPINK;
		    			}
	    			});
	    		} else if (elementWorkingCopy instanceof Counter) {
	    			Counter counter = (Counter) elementWorkingCopy;
	    			int count = counter.count();
	    			Platform.runLater(() -> {
		    			testResultText = String.valueOf(count);
		    			testResultColor = Color.VIOLET;
	    			});
	    		}
	            return null;
	        }
	    };
	    new Thread(task).start();
	    task.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue,
					State newValue) {
				if(newValue==Worker.State.SUCCEEDED){
					shadow.setColor(testResultColor);
					performTestStatusFakeButton.setEffect(shadow);
					performTestStatusFakeButton.setText("Test result: " + testResultText + ".");
					hideTestResultAnimation.play();
					buttonStateManager.reportEndOfTest();
	            }
			}
	    });
	}
	
}
