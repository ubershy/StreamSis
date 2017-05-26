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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.ElementInfo.ElementHealth;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.counters.Counter;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.SpecialCuteElementControllerType;
import com.ubershy.streamsis.gui.animations.HorizontalShadowAnimation;
import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.gui.controllers.editor.CommonElementFieldsController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.project.StuffSerializator;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import com.ubershy.streamsis.project.ProjectManager;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.control.Tooltip;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * The panel for editing {@link CuteElement}s. <br>
 * Has "OK", "Apply", "Cancel" and "Perform Test" buttons and view for showing CuteElement's name,
 * type and status. <br>
 * It's view contains {@link CommonElementFieldsController}'s view inside.
 */
public class ElementEditorController implements Initializable {
	
	static final Logger logger = LoggerFactory.getLogger(ElementEditorController.class);

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
    private Label typeDescriptionLabel;

    @FXML
    private Text typeDescriptionIcon;

	@FXML
	private Label statusLabel;

	@FXML
	private ScrollPane unhealthyPane;

	@FXML
	private Label whyUnhealthyLabel;

	@FXML
	private Label propertiesPaneDots;

	private CommonElementFieldsController commonElementFieldsController;
	
	private CuteElementController noneSelectedController = StreamSisAppFactory
			.buildSpecialCuteElementController(SpecialCuteElementControllerType.NONESELECTED);

	private CuteButtonsStatesManager buttonStateManager = new CuteButtonsStatesManager();

	/** The property of last selected CuteElement */
	private ObjectProperty<CuteElement> currentElement = new SimpleObjectProperty<>();
	
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
	
	private final Color typeDescriptionActiveColor = Color.GREY;
	
	private final Color typeDescriptionInactiveColor = Color.GAINSBORO;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		commonElementFieldsController = (CommonElementFieldsController) StreamSisAppFactory
				.buildControllerByRelativePath("editor/CommonElementFields.fxml");
		propertiesPane.setContent(noneSelectedController.getView());
		hsShadowAnima = new HorizontalShadowAnimation(statusLabel);
		nameShadowAnima = new HorizontalShadowAnimation(nameLabel);
		typeShadowAnima = new HorizontalShadowAnimation(typeLabel);
		whyShadowAnima = new HorizontalShadowAnimation(whyUnhealthyLabel);
		tPaneDotsAnima = new ThreeDotsAnimation("Editing", '.', root.textProperty(), 1, 1000,
				Timeline.INDEFINITE);
		buttonsBeforeInitDotsAnima = new ThreeDotsAnimation("", '❤', awaitingForInputTextProperty,
				1, 300, 1);
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
		
		// Replace non-awesome description icon with awesome one.
		FontAwesomeIconView newDescriptionIcon = new FontAwesomeIconView(
				FontAwesomeIcon.QUESTION_CIRCLE);
		newDescriptionIcon.setGlyphSize(14);
		newDescriptionIcon.setFill(typeDescriptionInactiveColor);
		typeDescriptionLabel.setGraphic(newDescriptionIcon);
		typeDescriptionIcon = newDescriptionIcon;
		
		KeyFrame hideTestResultFrame = new KeyFrame(Duration.seconds(2), event -> {
			performTestPane.showDetailNodeProperty().set(false);
		});
		hideTestResultAnimation.getKeyFrames().add(hideTestResultFrame);
        buttonsGridPane.add(performTestPane, 0, 3);
		initializeListeners();
		recreateButtonStateManager();
	}
	
	private void recreateButtonStateManager() {
		buttonStateManager = new CuteButtonsStatesManager();
		unbindTextsOfButtons();
		restoreDefaultTextsOfButtons();
		bindButtonsToButtonStateManager(buttonStateManager);
	}

	private void initializeListeners() {
		root.expandedProperty()
				.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
					if (newValue) {
						if (getCurrentElement() != null) {
							connectToCuteElement(getCurrentElement());
						}
						tPaneDotsAnima.play();
					} else {
						if (getCurrentElement() != null) {
							// This will disconnect commonElementFieldsController's view from scene.
							// Some buttons will notice that they are not inside the scene and
							// deactivate Hotkeys associated with them.
							setViewAsNoneSelected();
						}
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
						defineStyleByElementHealth(newValue);
						hsShadowAnima.play();
					});
				});
		whyUnhealthyProperty
				.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
					Platform.runLater(() -> {
						whyUnhealthyLabel.setText(newValue);
						whyShadowAnima.play();
					});
				});
		OKButton.disabledProperty().addListener((o, oldValue, newValue) -> {
			// "OK" button is disabled only when field validation fails, see to which property it's
			// bound in bindButtons() method. While field validation is failed let's set
			// CuteElement's health as unknown. Once field validation will pass, CuteElement will be
			// reinitialized and get new health.
			if (newValue) {
				if (elementWorkingCopy != null) {
					elementWorkingCopy.getElementInfo().setInvalidInputHealth();
				}
			}
		});
		ProjectManager.getProject().initializingProperty().addListener((o, oldVal, newVal) -> {
			Platform.runLater(() -> {
				// After structure changes are made to the project via user in Structure View,
				// the project initializes.
				// It's better to reconnect CuteElement, because if the user will hit Apply button,
				// the structure changes will be discarded. Also if CuteElement will get broken
				// because
				// of structure changes, without reconnecting to CuteElement the Element Editor
				// panel
				// will continue to show that CuteElement is still healthy.
				if (!newVal) {// Finished initializing
					// Let's reconnect to CuteElement
					if (root.isExpanded()) {
						connectToCuteElement(getCurrentElement());
					}
				}
			});
		});
	}
	
	private void bindButtonsToButtonStateManager(CuteButtonsStatesManager cbsm) {
		applyButton.disableProperty().bind((cbsm.applyButtonOnProperty().not()));
		OKButton.disableProperty().bind(cbsm.okButtonOnProperty().not());
		performTestButton.disableProperty().bind(cbsm.performTestButtonOnProperty().not());
		
		// TODO: gain some wisdom and beautify the code below.
		// <Start of a very ugly code that hardly tries to be thread safe>
		cbsm.needToScheduleCuteElementReinitProperty().addListener(o -> {
			if (cbsm.needToScheduleCuteElementReinitProperty().get()) {
				logger.debug("Start of reinitialization animation is requested.");
				// CuteElement initialization is a resource hungry operation. Let's not initialize
				// instantly, because the user might not finished typing into fields. Instead let's
				// play a text animation and when the text animation will finish playing itself to
				// the end, let's do initialization. The animation can be interrupted by new user
				// input and started over.
				OKButton.textProperty().bind(awaitingForInputTextProperty);
				applyButton.textProperty().bind(awaitingForInputTextProperty);
				performTestButton.textProperty().bind(awaitingForInputTextProperty);
				logger.debug("Text properties of buttons are bound to animation.");
				// This animation will internally animate awaitingForInputTextProperty's text.
				buttonsBeforeInitDotsAnima.play();
				logger.debug("Animation started.");
			}
		});
		cbsm.needToCancelScheduledCuteElementReinitProperty().addListener(o -> {
			if (cbsm.needToCancelScheduledCuteElementReinitProperty().get()) {
				logger.debug("Cancel of reinitialization animation is requested.");
				buttonsBeforeInitDotsAnima.stop();
				cbsm.setCuteElementReinitSuccessfullyCancelled();
				logger.debug("Animation stopped by cancel request.");
				unbindTextsOfButtons();
				restoreDefaultTextsOfButtons();
			}
		});
		buttonsBeforeInitDotsAnima.setOnFinished(evt -> {
			logger.debug("Animation finished.");
			boolean reinitWasCancelled = cbsm
					.needToCancelScheduledCuteElementReinitProperty().get();
			// After animation is done or was interrupted, let's unbind button text properties from
			// the property which is animated - buttonsBeforeInitDotsAnima.
			unbindTextsOfButtons();
			if (reinitWasCancelled) {
				logger.debug("Reinitialization is cancelled. Doing nothing with the CuteElement.");
				cbsm.setCuteElementReinitSuccessfullyCancelled();
			} else {
				logger.debug("Reinitializing the CuteElement...");
				String initText = "Initializing...";
				OKButton.setText(initText);
				applyButton.setText(initText);
				performTestButton.setText(initText);
				// TODO: run init() in another thread, as JavaFX thread may hang.
				if (elementWorkingCopy != null)
					elementWorkingCopy.init();
				logger.debug("Reinitialization of the CuteElement finished.");
			}
			cbsm.setCuteElementAsInitialized();
			logger.debug("Element set as initialized.");
			// Restore texts of buttons
			restoreDefaultTextsOfButtons();
			logger.debug("Texts are restored");
		});
		// </End of a very ugly code that hardly tries to be thread safe>
	}
	
	private void unbindTextsOfButtons() {
		OKButton.textProperty().unbind();
		applyButton.textProperty().unbind();
		performTestButton.textProperty().unbind();
		logger.debug("Unbound text properties of buttons.");
	}
	
	private void restoreDefaultTextsOfButtons() {
		OKButton.setText("OK");
		applyButton.setText("Apply");
		performTestButton.setText("Perform Test");
		logger.debug("Texts of buttons are restored.");
	}

	private void setViewAsNoneSelected() {
		propertiesPane.setContent(noneSelectedController.getView());
		disconnectFromConnectedCuteElement();
		nameLabel.setText("-");
		typeLabel.setText("-");
		typeDescriptionLabel.setTooltip(null);
		typeDescriptionIcon.setFill(typeDescriptionInactiveColor);
		statusLabel.setText("-");
		statusLabel.setTextFill(Color.BLACK);
		whyUnhealthyLabel.setText("-");
		unhealthyPane.setVisible(false);
		unhealthyPane.setManaged(false);
		buttonStateManager.reset();
	}

	private synchronized void connectToCuteElement(CuteElement currentElement) {
		if (currentElement == null) {
			setViewAsNoneSelected();
			return;
		}
		disconnectFromConnectedCuteElement();
		
		recreateButtonStateManager();
		
		commonElementFieldsController.setCuteButtonsStatesManager(buttonStateManager);
		
		// Set description of CuteElement type in a tooltip.
		typeDescriptionLabel.setTooltip(
				new Tooltip(CuteElement.getDescriptionOfType(currentElement.getClass())));
		typeDescriptionIcon.setFill(typeDescriptionActiveColor);
		
		// propertiesPane can currently show noneSelectedController's view.
		// If so, let's show commonElementFieldsController instead.
		if (!propertiesPane.getContent().equals(commonElementFieldsController.getView())) {
			propertiesPane.setContent(commonElementFieldsController.getView());
		}
		
		// CuteElements of higher hierarchy levels are referring to current element, and it's hard
		// to substitute this reference. It's easier to retain this reference.
		// Let's create a copy of the current CuteElement and work on the copy. After the user will
		// finish editing this copy and press "Ok" or "Apply" buttons, we can transfer changes back
		// to the original CuteElement.
		if (!currentElement.getElementInfo().isEditable()) {
			// The element is not editable.
			// No need to make copy. So let's make elementWorkingCopy have same reference.
			elementWorkingCopy = currentElement;
			// Restrict user input.
			commonElementFieldsController.setInputAllowed(false);
		} else {
			// Other CuteElements can be edited and serialized.
			try {
				elementWorkingCopy = (CuteElement) StuffSerializator
						.makeACopyOfObjectUsingSerialization(currentElement);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			// Allow user input.
			commonElementFieldsController.setInputAllowed(true);
			commonElementFieldsController
					.setEmptyNameAllowed(currentElement.getElementInfo().isEmptyNameAllowed());
		}
		
		// Initialize a copy
		elementWorkingCopy.init();
		
		ElementInfo infoOfCopyElement = elementWorkingCopy.getElementInfo();
		// In some situations Element might get "sick" while working and it's "whyUnhealthy" message
		// will contain the information why it became sick. We need to show this message to the
		// user. To accomplish that let's transfer the original health to the copy.
		infoOfCopyElement.setHealthFromAnotherInstance(currentElement.getElementInfo());
		
		// Let's set up initial values of visible Labels describing the CuteElement
		String simpleClassName = elementWorkingCopy.getClass().getSimpleName();
		typeLabel.setText(simpleClassName);
		defineStyleByElementHealth(infoOfCopyElement.elementHealthProperty().get());
		whyUnhealthyLabel.setText(infoOfCopyElement.getUnhealthyMessage());

		// Now let's bind all properties to visible textLabels describing the CuteElement
		nameLabel.textProperty().bind(infoOfCopyElement.nameProperty());
		elementHealthProperty.bind(infoOfCopyElement.elementHealthProperty());
		whyUnhealthyProperty.bind(infoOfCopyElement.whyUnhealthyProperty());
		
		// Let's set up propertiesPane according to CuteElement
		commonElementFieldsController.connectToCuteElement(elementWorkingCopy, currentElement);
		
//		// Let's reset state manager of buttons
//		buttonStateManager.reset();
		
		buttonStateManager.allowOrNotPerformTestButtonBasedOnElementClass(elementWorkingCopy);
	}

	private void disconnectFromConnectedCuteElement() {
		// Let's clean up after previous CuteElement
		nameLabel.textProperty().unbind();
		elementHealthProperty.unbind();
		whyUnhealthyProperty.unbind();
		elementWorkingCopy = null;
		buttonStateManager.allowOrNotPerformTestButtonBasedOnElementClass(null);
	}

	protected void defineStyleByElementHealth(ElementHealth elementHealth) {
		statusLabel.setText(elementHealth.toString());
		switch (elementHealth) {
		case BROKEN:
			unhealthyPane.setVisible(true);
			unhealthyPane.setManaged(true);
			statusLabel.setTextFill(Color.RED);
			OKButton.setStyle("-fx-effect: dropshadow(three-pass-box, plum, 5, 0.35, 0, 0);");
			applyButton.setStyle("-fx-effect: dropshadow(three-pass-box, plum, 5, 0.35, 0, 0);");
			break;
		case HEALTHY:
			unhealthyPane.setVisible(false);
			unhealthyPane.setManaged(false);
			statusLabel.setTextFill(Color.GREEN);
			OKButton.setStyle("");
			applyButton.setStyle("");
			break;
		case SICK:
			unhealthyPane.setVisible(true);
			unhealthyPane.setManaged(true);
			statusLabel.setTextFill(Color.GOLDENROD);
			OKButton.setStyle(
					"-fx-effect: dropshadow(three-pass-box, palegoldenrod, 5, 0.35, 0, 0);");
			applyButton.setStyle(
					"-fx-effect: dropshadow(three-pass-box, palegoldenrod, 5, 0.35, 0, 0);");
			break;
		case INVALIDINPUT:
			unhealthyPane.setVisible(true);
			unhealthyPane.setManaged(true);
			statusLabel.setTextFill(Color.DIMGRAY);
			OKButton.setStyle("");
			applyButton.setStyle("");
			break;
		default:
			break;
		}
	}

	private void applyChanges() {
		// Current CuteElement's copy at this moment was modified by user, we need to transfer
		// changes to the original CuteElement.
		String NameOfCopyElement = elementWorkingCopy.getElementInfo().getName();
		String NameOfCurrentElement = getCurrentElement().getElementInfo().getName();
		// setCuteElementNameSafely() will throw IllegalArgument exception if "name" field
		// validation in commonElementFieldsController is not implemented correctly.
		if (!NameOfCopyElement.equals(NameOfCurrentElement))
			ProjectManager.getProject().setCuteElementNameSafely(getCurrentElement(),
					NameOfCopyElement);
		// Let's apply changes from the copy to original CuteElement by copying properties
		try {
			// Note: if you notice copyProperties() works without throwing an exception, but doesn't
			// transfer all needed changes, CuteElement's properties and getters and setters might
			// not be set up properly.
			int childrenListIdentifierBefore = System
					.identityHashCode(getCurrentElement().getChildren());
			PropertyUtils.copyProperties(getCurrentElement(), elementWorkingCopy);
			if (System.identityHashCode(
					getCurrentElement().getChildren()) != childrenListIdentifierBefore) {
				throw new RuntimeException("Programmer. You are not allowed to substitute list "
						+ "of children of original CuteElement.");
			}
			
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("For some reason can't transfer changes from copy of the "
					+ "current CuteElement to the current CuteElement", e);
		}
		// Initialize whole project, it may fix parents and it may highlight problems in this
		// CuteElement.
		ProjectManager.initProjectFromGUI();
		connectToCuteElement(getCurrentElement());
	}
	
	@FXML
	void hitApplyButton(ActionEvent event) {
		applyChanges();
	}

	@FXML
	void hitOKButton(ActionEvent event) {
		root.expandedProperty().set(false);
		if (buttonStateManager.areChangesMade())
			applyChanges();
	}

	@FXML
	void hitCancelButton(ActionEvent event) {
		root.expandedProperty().set(false);
		if (getCurrentElement() != null) {
			getCurrentElement().init();
			connectToCuteElement(getCurrentElement());
		}
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
	            } else if (newValue==Worker.State.FAILED){
					if (task.getException() == null) {
						throw new RuntimeException("GUI Testing task failed for some reason for "
								+ elementWorkingCopy.getClass().getSimpleName());
					} else {
						throw new RuntimeException(task.getException());
					}
	            }
			}
	    });
	    new Thread(task).start();
	}
	
	public void setCurrentElement(CuteElement element) {
		if (element != currentElement.get()) {
			currentElement.set(element);
			if (root.isExpanded()) {
				connectToCuteElement(element);
			}
		}
	}
	
	private CuteElement getCurrentElement() {
		return currentElement.get();
	}
	
}
