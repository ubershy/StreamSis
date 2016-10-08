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
package com.ubershy.streamsis.gui.controllers.edit.littlethings;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.MultiSourceFileChooser;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.gui.contextmenu.PossibleMoves;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.helperclasses.FileNameCell;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/**
 * MultiSourceFileChooserController, the controller that allows to edit
 * {@link MultiSourceFileChooser} in Element Editor panel.
 */
public class MultiSourceFileChooserController extends AbstractCuteController {

	@FXML
	private GridPane root;

	@FXML
	private TextField srcPathTextField;

	@FXML
	private Button browseSourceDirButton;

	@FXML
	private Label allowedExtensionsLabel;

	@FXML
	private CheckBox chooseFilesRandomlyCheckBox;

	@FXML
	private TitledPane manualTitledPane;

	@FXML
	private TableView<File> fileTable;

	@FXML
	private TableColumn<File, String> numberColumn;

	@FXML
	private TableColumn<File, String> fileNameColumn;

	@FXML
	private Button addButton;

	@FXML
	private Button moveUpButton;

	@FXML
	private Button moveDownButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button clearAllButton;

	/** The {@link MultiSourceFileChooser} to edit. */
	protected MultiSourceFileChooser chooser;

	/** The original {@link MultiSourceFileChooser} to compare values with {@link #chooser}. */
	protected MultiSourceFileChooser origChooser;

	protected ValidationSupport validationSupport;
	
	protected ThreeDotsAnimation manualPaneDotsAnima;
	
	protected ChangeListener<? super Boolean> paneExpansionListener;

	private String fileTypeName;
	
	private final String addToExtensionsText = "Allowed extensions are: ";
	
	/**
	 * The fake animation that just executes {@link #setValidationForFileTable()} with delay. Delay
	 * is needed because {@link #fileTable} is inside {@link #manualTitledPane} and when the pane
	 * expands, validation should be applied to fileTable. If apply validation without delay, the
	 * validation graphics will not be in the right place, because the pane will still be expanding
	 * at this moment.
	 */
	Timeline setValidationForFileTableWithDelay = new Timeline(
			new KeyFrame(Duration.millis(500), event -> {
				setValidationForFileTable();
			}));
	
	/** Tells in which directions the selected items in {@link #fileTable} can be moved. */
	private ObjectProperty<PossibleMoves> possibleMovesForSelection = new SimpleObjectProperty<>();

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		browseSourceDirButton.disableProperty().bind(manualTitledPane.expandedProperty());
		srcPathTextField.disableProperty().bind(manualTitledPane.expandedProperty());
		fileTable.setPlaceholder(new Label("Let's add some files!"));
		fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		fileNameColumn.setCellValueFactory(
				p -> new ReadOnlyObjectWrapper<String>(p.getValue().getPath()));
		fileNameColumn.setCellFactory(view -> new FileNameCell());
		// indexOf operation might not be very fast. Also the code should ensure that the list with
		// items contains only unique values.
		numberColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<String>(
				String.valueOf(p.getTableView().getItems().indexOf(p.getValue()) + 1)));
		manualPaneDotsAnima = new ThreeDotsAnimation(
				"Editing list of <filetype> files", '.',
				manualTitledPane.textProperty(), 1, 500, Timeline.INDEFINITE);
		setValidationForFileTableWithDelay.setCycleCount(1);
		initializeListeners();
	}
	
	private void initializeListeners() {
		// This listener starts and stops three dots animation on manualTitledPane based if it's
		// open or not. Meant to be added later to manualTitledPane.expandedProperty() when
		// connecting to MultiSourceFileChooser.
		paneExpansionListener = (o, oldValue, newValue) -> {
			if (newValue) { // Expanded. Chooser will not find sources in source path directory.
				chooser.setFindingSourcesInSrcPath(false);
				buttonStateManager.reportNewValueOfControl(origChooser.isFindingSourcesInSrcPath(),
						!newValue, manualTitledPane, null);
				setAnimationAndValidationAccordingToManualTitledPaneExpansion(newValue);
			}
			else { // Collapsed. Chooser will find sources in source path directory.
				chooser.setFindingSourcesInSrcPath(true);
				buttonStateManager.reportNewValueOfControl(origChooser.isFindingSourcesInSrcPath(),
						!newValue, manualTitledPane, null);
				setAnimationAndValidationAccordingToManualTitledPaneExpansion(newValue);
			}
		};
		// This listener updates possibleMovesForSelection value when selection in fileTable
		// changes, also it disables or enabled "Delete" button.
		fileTable.getSelectionModel().getSelectedItems()
				.addListener((ListChangeListener.Change<? extends File> c) -> {
					@SuppressWarnings("unchecked")
					ObservableList<File> selectedItems = (ObservableList<File>) c.getList();
					int selectedItemsSize = selectedItems.size();
					if (fileTable.getItems().size() > 1) {
						File upperItem = selectedItems.get(0);
						int upperIndexInTable = fileTable.getItems().indexOf(upperItem);
						int lowerIndexInTable = upperIndexInTable + selectedItemsSize - 1;
						possibleMovesForSelection.set(PossibleMoves
								.generateMovesForMultipleItemsInList(fileTable.getItems(),
										upperIndexInTable, lowerIndexInTable));
					} else {
						possibleMovesForSelection.set(PossibleMoves.NOWHERE);
					}
					if (selectedItemsSize == 0) {
						deleteButton.setDisable(true);
					} else {
						deleteButton.setDisable(false);
					}
				});
		// This listener disables or enables "Up" or "Down" buttons based on
		// possibleMovesForSelection value
		possibleMovesForSelection.addListener((o, oldVal, newVal) -> {
			switch (newVal) {
			case NOWHERE:
				moveUpButton.setDisable(true);
				moveDownButton.setDisable(true);
				break;
			case ONLYDOWN:
				moveUpButton.setDisable(true);
				moveDownButton.setDisable(false);
				break;
			case ONLYUP:
				moveUpButton.setDisable(false);
				moveDownButton.setDisable(true);
				break;
			case UPORDOWN:
				moveUpButton.setDisable(false);
				moveDownButton.setDisable(false);
				break;
			default:
				break;
			}
		});
	}

	/**
	 * @param expanded
	 *            Is {@link #manualTitledPane} currently expanded?
	 */
	private void setAnimationAndValidationAccordingToManualTitledPaneExpansion(Boolean expanded) {
		if (expanded) {
			manualPaneDotsAnima.play();
			setValidationForFileTableWithDelay.playFromStart();
		} else {
			if (manualPaneDotsAnima.isRunning()) {
				manualPaneDotsAnima.stop();
			}
			setValidationForSrcPathTextField();
		}
	}

	/**
	 * Sets the {@link MultiSourceFileChooser} to work with and binds view's controls to
	 * MultiSourceFileChooser's properties. Before using this method you should first invoke
	 * {@link #replaceFileTypeNameTagInLabeledControls(String)} method.
	 *
	 * @param editableCopyOfChooser
	 *            The MultiSourceFileChooser to edit. (Actually a copy of the MultiSourceFileChooser
	 *            the user wishes to edit. The changes made in the copy will be transferred to
	 *            original MultiSourceFileChooser once the user hit "Apply" or "OK" button).
	 * @param origChooser
	 *            The Original MultiSourceFileChooser to use as storage of original values of
	 *            CuteElement's attributes. Should not be edited in controllers.
	 * @throws RuntimeException
	 *             In case {@link #replaceFileTypeNameTagInLabeledControls(String)} wasn't used
	 *             before this method.
	 */
	public void bindToMultiSourceFileChooser(MultiSourceFileChooser editableCopyOfChooser,
			MultiSourceFileChooser origChooser) {
		if (fileTypeName == null) {
			// refer to replaceFileTypeNameTagInLabeledControls() method.
			throw new RuntimeException("File type name tag needs to be replaced before"
					+ " using this view.");
		}
		this.chooser = editableCopyOfChooser;
		this.origChooser = origChooser;
		// Set up bidirectional bindings.
		bindBidirectionalAndRemember(chooseFilesRandomlyCheckBox.selectedProperty(),
				editableCopyOfChooser.chooseFilesRandomlyProperty());
		bindBidirectionalAndRemember(srcPathTextField.textProperty(),
				editableCopyOfChooser.srcPathProperty());
		fileTable.itemsProperty()
		.bindBidirectional(editableCopyOfChooser.persistentSourceFileListProperty());
		// Set up listeners
		// Let the manualTitledPane expanded status control if file chooser should search for
		// files in manually set list or in "source" directory. So there's no need for additional
		// CheckBox. Instead of bindings use listener because... reasons.
		manualTitledPane.setExpanded(!editableCopyOfChooser.isFindingSourcesInSrcPath());
		manualTitledPane.expandedProperty().addListener(paneExpansionListener);
		chooseFilesRandomlyCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
			buttonStateManager.reportNewValueOfControl(origChooser.isChoosingFilesRandomly(),
					newVal, chooseFilesRandomlyCheckBox, null);
		});
		// Set up bindings to read-only properties.
		allowedExtensionsLabel.setText(
				addToExtensionsText + editableCopyOfChooser.getAcceptableExtensions().toString());
		bindNormalAndRemember(allowedExtensionsLabel.textProperty(),
				Bindings.concat(addToExtensionsText)
						.concat(this.chooser.acceptableExtensionsProperty().asString()));
	}

	public void unbindFromMultiSourceFileChooser() {
		unbindAllRememberedBinds();
		manualTitledPane.expandedProperty().removeListener(paneExpansionListener);
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
		if (chooser.isFindingSourcesInSrcPath()) {
			setAnimationAndValidationAccordingToManualTitledPaneExpansion(false);
		} else {
			setAnimationAndValidationAccordingToManualTitledPaneExpansion(true);
		}
	}
	
	private void setValidationForFileTable() {
		if (buttonStateManager != null) {
			if (validationSupport != null) {
				if (validationSupport.getRegisteredControls().contains(srcPathTextField)) {
					disableValidationForSrcPathTextField();
				}
				Validator<ObservableList<File>> fileTableValidator = (c, newValue) -> {
					if (newValue.size() == 0) {
						clearAllButton.setDisable(true);
					} else {
						clearAllButton.setDisable(false);
					}
					ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
							"No files selected", newValue.size() == 0);
					buttonStateManager.reportNewValueOfControl(
							origChooser.getPersistentSourceFileList(), newValue, c, emptyResult);
					return emptyResult;
				};
				validationSupport.registerValidator(fileTable, true, fileTableValidator);
				disableValidationForSrcPathTextField();
			}
		} else {
			throw new RuntimeException(
					"Check if CuteButtonsStatesManager is set to this controller.");
		}
	}

	private void setValidationForSrcPathTextField() {
		if (buttonStateManager != null) {
			if (validationSupport != null) {
				if (validationSupport.getRegisteredControls().contains(fileTable)) {
					disableValidationForFileTable();
				}
				Validator<String> srcPathFieldValidator = (c, newValue) -> {
					ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
							"Select a path to directory with files", newValue.isEmpty());
					ValidationResult invalidPathResult = ValidationResult.fromErrorIf(c,
							"The path seems slightly... invalid",
							!Util.checkIfAbsolutePathSeemsValid(newValue));
					ValidationResult notAbsolutePathResult = ValidationResult.fromErrorIf(c,
							"The path should be absolute. No exceptions.",
							!(new File(newValue).isAbsolute()));
					ValidationResult notDirectoryResult = ValidationResult.fromErrorIf(c,
							"The path should be to an existing directory",
							!(new File(newValue).isDirectory()));
					ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
							notDirectoryResult, invalidPathResult, notAbsolutePathResult);
					buttonStateManager.reportNewValueOfControl(origChooser.getSrcPath(), newValue,
							c, finalResult);
					return finalResult;
				};
				validationSupport.registerValidator(srcPathTextField, true,
						srcPathFieldValidator);
			}
		} else {
			throw new RuntimeException(
					"Check if CuteButtonsStatesManager is set to this controller.");
		}
	}
	
	private void disableValidationForFileTable() {
		// Register fileTable to fake validator, so it will not affect validation anymore.
		this.validationSupport.registerValidator(fileTable, false, GUIUtil.createFakeAlwaysSuccessfulValidator());
		// Tell buttonStateManager that no errors exist now in fileTable.
		buttonStateManager.reportNewValueOfControl(origChooser.getPersistentSourceFileList(),
				chooser.getPersistentSourceFileList(), fileTable,
				GUIUtil.fakeSuccessfulValidationResult(fileTable));
	}
	
	private void disableValidationForSrcPathTextField() {
		// Register srcPathTextField to fake validator, so it will not affect validation anymore.
		this.validationSupport.registerValidator(srcPathTextField, false, GUIUtil.createFakeAlwaysSuccessfulValidator());
		// Tell buttonStateManager that no errors exist now in srcPathTextField.
		buttonStateManager.reportNewValueOfControl(origChooser.getSrcPath(), chooser.getSrcPath(),
				srcPathTextField, GUIUtil.fakeSuccessfulValidationResult(srcPathTextField));
	}

	/**
	 * Replaces &lt;filetype&gt tags in this {@link MultiSourceFileChooserController}'s view to a
	 * specified text. Should be invoked before
	 * {@link #bindToMultiSourceFileChooser(MultiSourceFileChooser, MultiSourceFileChooser)} method.
	 *
	 * @param fileTypeName
	 *            The text to use instead of &lt;filetype&gt tags in texts of {@link Label}s. Should
	 *            describe in one or two words the type of files this
	 *            MultiSourceFileChooserController accepts (not extension).
	 * @throws RuntimeException
	 *             if it was already invoked and replaced the text in Labels.
	 */
	public void replaceFileTypeNameTagInLabeledControls(String fileTypeName) {
		if (this.fileTypeName == null) {
			this.fileTypeName = fileTypeName;
			for (Node node : ((GridPane) getView()).getChildren()) {
				if (node instanceof Labeled) {
					Labeled labeled = (Labeled) node;
					labeled.setText(labeled.getText().replace("<filetype>", fileTypeName));
				}
			}
			manualPaneDotsAnima.setAdditionalText(
					manualPaneDotsAnima.getAdditionalText().replace("<filetype>", fileTypeName));
		} else {
			throw new RuntimeException("File type name tag is already replaced.");
		}
	}
	
    @FXML
    void browseSourceDir(ActionEvent event) {
		GUIUtil.showJavaSingleDirectoryChooser(
				"Specify directory containing files to use as sources", srcPathTextField);
    }

    @FXML
    void onAdd(ActionEvent event) {
		List<File> unmodifiableListOfChosenFiles = GUIUtil.showJavaMultiFileChooser(
				"Add " + fileTypeName + " files from which to choose", "Files to choose from",
				getView().getScene().getWindow(),
				chooser.getAcceptableExtensions().toArray(new String[0]));
		// If null, the operation was cancelled by the user.
		if (unmodifiableListOfChosenFiles != null) {
			ArrayList<File> chosenFiles = new ArrayList<File>(unmodifiableListOfChosenFiles);
			int howManyFilesAreChosen = chosenFiles.size();
			// Let's remove all duplicates of files.
			chosenFiles.removeAll(fileTable.getItems());
			int howManyFilesWillNotBeAdded = howManyFilesAreChosen - chosenFiles.size();
			// Let's apply changes if after removing duplicates something left in the list of chosen
			// files.
			if (chosenFiles.size() != 0) {
				// Instead of adding files to TableView.getItems(), we need to create new
				// ObservableList and pass it to TableView.setItems(), so the TableView's property
				// itemsProperty() will be updated, thus triggering validation.
				ObservableList<File> result = FXCollections
						.observableArrayList(fileTable.getItems());
				result.addAll(chosenFiles);
				fileTable.setItems(result);
			}
			if (howManyFilesWillNotBeAdded != 0) {
				GUIManager.showNotification(null, "Some (" + howManyFilesWillNotBeAdded
						+ ") duplicate files were not added.");
			}
		}
    }

    @FXML
    void onClearAll(ActionEvent event) {
    	// Instead of clearing TableView.getItems(), we need to pass new empty list to
    	// TableView.setItems() to trigger validation.
		fileTable.setItems(FXCollections.observableArrayList());
    }

    @FXML
    void onDelete(ActionEvent event) {
    	ObservableList<File> selectedItems = fileTable.getSelectionModel().getSelectedItems();
		// Instead of removing items from TableView.getItems(), we need to pass new list to
		// TableView.setItems() to trigger validation.
    	ObservableList<File> result = FXCollections.observableArrayList(fileTable.getItems());
    	result.removeAll(selectedItems);
    	fileTable.setItems(result);
    }

    @FXML
    void onMoveDown(ActionEvent event) {
    	onMove(false);
    }

	@FXML
    void onMoveUp(ActionEvent event) {
		onMove(true);
    }
	
	private void onMove(boolean moveUp) {
		PossibleMoves movesAvailable = possibleMovesForSelection.get();
		int shifter;
		if (moveUp) { // Move Up button was clicked
			if (movesAvailable.equals(PossibleMoves.ONLYUP)
					|| movesAvailable.equals(PossibleMoves.UPORDOWN)) {
				shifter = -1; // shift items up in list
			} else {
				throw new RuntimeException("Move Up button should be disabled because "
						+ "possibleMovesForSelection don't allow moving up.");
			}
		} else { // Move Down button was clicked
			if (movesAvailable.equals(PossibleMoves.ONLYDOWN)
					|| movesAvailable.equals(PossibleMoves.UPORDOWN)) {
				shifter = 1; // shift items down in list
			} else {
				throw new RuntimeException("Move Down button should be disabled because "
						+ "possibleMovesForSelection don't allow moving down.");
			}
		}
		ObservableList<File> selectedItems = fileTable.getSelectionModel().getSelectedItems();
    	
    	File upperItemInSelection = selectedItems.get(0);
    	int upperIndexInTable = fileTable.getItems().indexOf(upperItemInSelection);
    	int lowerIndexInTable = upperIndexInTable + selectedItems.size() - 1;
		
    	// Instead of removing and adding items from/in TableView.getItems(), we need to pass new
    	// list to TableView.setItems() to trigger validation.
		ObservableList<File> result = FXCollections.observableArrayList(fileTable.getItems());
		result.removeAll(selectedItems);
		result.addAll(upperIndexInTable + shifter, selectedItems);
		fileTable.setItems(result);
		
		// Reselect items
		fileTable.getSelectionModel().clearSelection();
		if (upperIndexInTable == lowerIndexInTable) { // single file to reselect
			fileTable.getSelectionModel().select(lowerIndexInTable + shifter);
		} else { // multiple files to reselect
			fileTable.getSelectionModel().selectRange(upperIndexInTable + shifter,
					lowerIndexInTable + 1 + shifter);
		}
	}

}
