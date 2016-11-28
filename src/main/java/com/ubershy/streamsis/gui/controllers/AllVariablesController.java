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
package com.ubershy.streamsis.gui.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import com.ubershy.streamsis.UserVars;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

/**
 * The controller for showing all {@link UserVars}.
 */
public class AllVariablesController implements Initializable {
    @FXML
    private VBox root;
    @FXML
    private TableView<Entry<String, String>> initialVarsTableView;
    @FXML
    private TableColumn<Entry<String, String>, String> initVarNameColumn;
    @FXML
    private TableColumn<Entry<String, String>, String> initVarValueColumn;
    @FXML
    private TableView<Entry<String, String>> currentVarsTableView;
    @FXML
    private TableColumn<Entry<String, String>, String> currVarNameColumn;
    @FXML
    private TableColumn<Entry<String, String>, String> currVarValueColumn;

	/**
	 * Listens to {@link UserVars} and populates {@link #readOnlyCurrentVarsList}.
	 */
	@SuppressWarnings("unchecked")
	MapChangeListener<String, String> currentVarsListener = change -> {
		readOnlyCurrentVarsList
				.setAll(((MapChangeListener.Change<String, String>) (change)).getMap().entrySet());
	};

	/**
	 * Listens to {@link CuteProject#initialUserVars} map and populates
	 * {@link #readOnlyInitialVarsList} .
	 */
	@SuppressWarnings("unchecked")
	MapChangeListener<String, String> initialVarsListener = change -> {
		readOnlyInitialVarsList
				.setAll(((MapChangeListener.Change<String, String>) (change)).getMap().entrySet());
	};

	/**
	 * This list is populated with entries from {@link UserVars} map so this list can be used to
	 * populate {@link #currentVarsTableView} which doesn't accept maps, but accepts lists.
	 */
	private static ObservableList<Map.Entry<String, String>> readOnlyCurrentVarsList = FXCollections
			.observableArrayList();

	/**
	 * This list is populated with entries from {@link CuteProject#initialUserVars} map so this list
	 * can be used to populate {@link #initialVarsTableView} which doesn't accept maps, but accepts
	 * lists.
	 */
	private static ObservableList<Map.Entry<String, String>> readOnlyInitialVarsList = FXCollections
			.observableArrayList();

	private CuteProject project;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set style.
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				// Set scene style.
				String url = AllVariablesController.class.getResource("/css/streamsis.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		// Set readOnlyCurrentVarsList to UserVars entry set.
		readOnlyCurrentVarsList.setAll(UserVars.getUserVarsMap().entrySet());
		// Subscribe to UserVars changes.
		UserVars.getUserVarsMap()
				.addListener(new WeakMapChangeListener<String, String>(currentVarsListener));
		// Initialize tables.
		initializeInitialVarsTable();
		initializeCurrentVarsTable();
	}

	private void initializeInitialVarsTable() {
		Label lbl = new Label(
				"You can add here Variables that will be set at the start of the project.");
		lbl.setWrapText(true);
		lbl.setTextAlignment(TextAlignment.CENTER);
		initialVarsTableView.setPlaceholder(lbl);
		initialVarsTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// Make columns always have equal size and covering whole table width.
		GUIUtil.maintainEqualWidthOfTableColumns(initialVarsTableView);
		// Initialize context menu of table.
		initialVarsTableView.setContextMenu(new ContextMenu(generateNewInitVarMenuItem()));
		// Initialize context menu of table rows.
		initialVarsTableView.setRowFactory(
				(Callback<TableView<Entry<String, String>>, TableRow<Entry<String, String>>>) c -> {
					TableRow<Map.Entry<String, String>> row = new TableRow<>();
					row.itemProperty().addListener((o, oldVal, newVal) -> {
						if (newVal == null) {
							row.setContextMenu(null);
							return;
						}
						MenuItem deleteVarMenuItem = new MenuItem("Delete Variable");
						deleteVarMenuItem.setOnAction(e -> {
							project.getInitialUserVars().remove(newVal.getKey());
						});
						row.setContextMenu(
								new ContextMenu(generateNewInitVarMenuItem(), deleteVarMenuItem));
					});
					return row;
				});
		// Initialize table columns.
		initVarNameColumn
				.setCellValueFactory(p -> new ReadOnlyObjectWrapper<String>(p.getValue().getKey()));
		initVarNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		initVarValueColumn.setCellValueFactory(
				p -> new ReadOnlyObjectWrapper<String>(p.getValue().getValue()));
		initVarValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		// Initialize editing capability of table columns.
		initVarNameColumn.setOnEditCommit(t -> {
			String newKey = t.getNewValue();
			String oldKey = t.getOldValue();
			if (newKey.equals(oldKey)) {
				// Do nothing if input is same.
				return;
			}
			ObservableMap<String, String> initVars = project.getInitialUserVars();
			// Validating input.
			// TODO: implement custom cell "VarCell" that contains validation for existing key
			// and restricts hitting enter key if validation fails.
			if (initVars.containsKey(newKey)) {
				// Show message and revert input on attempt to input existing key.
				GUIManager.showNotification(null,
						"Not renaming: such Initial Variable already exists");
				// Doing fake update of initialUserVars to update the view and replace current text
				// input in the cell.
				String oldValue = initVars.get(oldKey);
				initVars.remove(oldKey);
				initVars.put(oldKey, oldValue);
				return;
			}
			// Validation passed. Setting new key, removing old one.
			Entry<String, String> entry = t.getRowValue();
			initVars.put(newKey, entry.getValue());
			initVars.remove(oldKey);
		});
		initVarValueColumn.setOnEditCommit(t -> {
			String newValue = t.getNewValue();
			String oldValue = t.getOldValue();
			if (newValue.equals(oldValue)) {
				// Do nothing if input is same.
				return;
			}
			Entry<String, String> entry = t.getRowValue();
			project.getInitialUserVars().put(entry.getKey(), newValue);
		});
	}

	private void initializeCurrentVarsTable() {
		// Initialize TableView settings.
		Label lbl = new Label("No Variables are currently set. You can add some here,"
				+ " but they are wiped on Project stop");
		lbl.setWrapText(true);
		lbl.setTextAlignment(TextAlignment.CENTER);
		currentVarsTableView.setPlaceholder(lbl);
		// Make columns always have equal size and covering whole table width.
		GUIUtil.maintainEqualWidthOfTableColumns(currentVarsTableView);
		currentVarsTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// Initialize context menu of table.
		currentVarsTableView.setContextMenu(new ContextMenu(generateNewUserVarMenuItem()));
		// Initialize context menu of table rows.
		currentVarsTableView.setRowFactory(
				(Callback<TableView<Entry<String, String>>, TableRow<Entry<String, String>>>) c -> {
					TableRow<Map.Entry<String, String>> row = new TableRow<>();
					row.itemProperty().addListener((o, oldVal, newVal) -> {
						if (newVal == null) {
							row.setContextMenu(null);
							return;
						}
						MenuItem deleteVarMenuItem = new MenuItem("Delete Variable");
						deleteVarMenuItem.setOnAction(e -> {
							UserVars.remove(newVal.getKey());
						});
						row.setContextMenu(
								new ContextMenu(generateNewUserVarMenuItem(), deleteVarMenuItem));
					});
					return row;
				});
		// Initialize table columns.
		currVarNameColumn
				.setCellValueFactory(p -> new ReadOnlyObjectWrapper<String>(p.getValue().getKey()));
		currVarNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		currVarValueColumn.setCellValueFactory(
				p -> new ReadOnlyObjectWrapper<String>(p.getValue().getValue()));
		currVarValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		// Initialize editing capability of table columns.
		currVarNameColumn.setOnEditCommit(t -> {
			String newKey = t.getNewValue();
			String oldKey = t.getOldValue();
			if (newKey.equals(oldKey)) {
				// Do nothing if input is same.
				return;
			}
			// Validating input.
			// TODO: implement custom cell "VarCell" that contains validation for existing key
			// and restricts hitting enter key if validation fails.
			if (UserVars.getUserVarsMap().containsKey(newKey)) {
				// Show message and revert input on attempt to input existing key.
				GUIManager.showNotification(null, "Not renaming: such Variable already exists");
				// Doing fake update of UserVars to update the view and replace current text input
				// in the cell.
				String oldValue = UserVars.get(oldKey);
				UserVars.remove(oldKey);
				UserVars.put(oldKey, oldValue);
				return;
			}
			// Validation passed. Setting new key, removing old one.
			Entry<String, String> entry = t.getRowValue();
			UserVars.put(newKey, entry.getValue());
			UserVars.remove(oldKey);
		});
		currVarValueColumn.setOnEditCommit(t -> {
			String newValue = t.getNewValue();
			String oldValue = t.getOldValue();
			if (newValue.equals(oldValue)) {
				// Do nothing if input is same.
				return;
			}
			Entry<String, String> entry = t.getRowValue();
			UserVars.put(entry.getKey(), newValue);
		});
		// Finally set items to table.
		currentVarsTableView.setItems(readOnlyCurrentVarsList);
	}

	protected MenuItem generateNewUserVarMenuItem() {
		MenuItem addNewVarMenuItem = new MenuItem("Add new Variable to the end of list");
		addNewVarMenuItem.setOnAction(e -> {
			ArrayList<String> existingVarsNames = new ArrayList<>(
					UserVars.getUserVarsMap().keySet());
			String nameOfNewVar = GUIUtil.generateUniqueNameForNewItem("Variable",
					existingVarsNames);
			UserVars.put(nameOfNewVar, "Some value");
		});
		return addNewVarMenuItem;
	}

	private MenuItem generateNewInitVarMenuItem() {
		MenuItem addNewVarMenuItem = new MenuItem("Add new Variable to the end of list");
		addNewVarMenuItem.setOnAction(e -> {
			ArrayList<String> existingVarsNames = new ArrayList<>(
					project.getInitialUserVars().keySet());
			String nameOfNewVar = GUIUtil.generateUniqueNameForNewItem("Variable",
					existingVarsNames);
			project.getInitialUserVars().put(nameOfNewVar, "Some value");
		});
		return addNewVarMenuItem;
	}

	public void bindToProject(CuteProject project) {
		this.project = project;
		// Set readOnlyInitialVarsList to project's initial UserVars entry set.
		readOnlyInitialVarsList.setAll(project.getInitialUserVars().entrySet());
		// Subscribe to project initial UserVars changes.
		project.getInitialUserVars()
				.addListener(new WeakMapChangeListener<String, String>(initialVarsListener));
		initialVarsTableView.setItems(readOnlyInitialVarsList);
		currentVarsTableView.disableProperty().bind(project.startedProperty().not());
		initialVarsTableView.disableProperty().bind(project.startedProperty());
	}

	public double getWidth() {
		return 200.0;
	}

	public double getHeight() {
		return 200.0;
	}

}
