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
package com.ubershy.streamsis.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.actors.AbstractActor;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.gui.contextmenu.ActorListContextMenuManager;
import com.ubershy.streamsis.gui.contextmenu.SisSceneContextMenuBuilder;
import com.ubershy.streamsis.gui.contextmenu.TreeViewContextMenuManager;
import com.ubershy.streamsis.gui.controllers.CompactModeController;
import com.ubershy.streamsis.gui.controllers.CuteController;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.gui.controllers.FullModeController;
import com.ubershy.streamsis.gui.controllers.MainController;
import com.ubershy.streamsis.gui.controllers.editspecific.NoSuchElementController;
import com.ubershy.streamsis.gui.helperclasses.ActorCell;
import com.ubershy.streamsis.gui.helperclasses.AutoTreeItem;
import com.ubershy.streamsis.gui.helperclasses.CuteTreeCell;
import com.ubershy.streamsis.gui.helperclasses.SisSceneCell;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteNodeContainer;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectManager;
import com.ubershy.streamsis.project.SisScene;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * A factory for creating {@link StreamSis} GUI objects.
 */
public class StreamSisAppFactory {

	static final Logger logger = LoggerFactory.getLogger(StreamSisAppFactory.class);

	/**
	 * Builds the {@link MainController} (A presenter that contains the view we want to pass to the
	 * top of view hierarchy of {@link StreamSis} GUI).
	 *
	 * @return the {@link MainController}
	 */
	public static MainController buildMainController() {
		return (MainController) buildControllerByRelativePath("Main.fxml");
	}

	/**
	 * Builds the {@link FullModeController}.
	 *
	 * @return the {@link FullModeController}
	 */
	public static FullModeController buildFullModeController() {
		return (FullModeController) buildControllerByRelativePath("FullMode.fxml");
	}

	/**
	 * Builds the {@link CompactModeController}.
	 *
	 * @return the {@link CompactModeController}
	 */
	public static CompactModeController buildCompactModeController() {
		return (CompactModeController) buildControllerByRelativePath("CompactMode.fxml");
	}

	/**
	 * Builds the {@link ElementEditorController}.
	 *
	 * @return the {@link ElementEditorController}
	 */
	public static ElementEditorController buildElementEditorController() {
		return (ElementEditorController) buildControllerByRelativePath("ElementEditor.fxml");
	}

	/**
	 * Builds a {@link ListView} with {@link SisScene SisScenes}.
	 *
	 * @param project
	 *            the {@link CuteProject}
	 * @return the {@link ListView}
	 */
	public static ListView<SisScene> buildSisSceneListView(CuteProject project) {
		ListView<SisScene> sisSceneList = new ListView<SisScene>();
		// There are some problems with editable list with ability
		// to rename items... I can't handle them.
		sisSceneList.setEditable(false);
		if (project != null) {
			sisSceneList.setCellFactory(View -> new SisSceneCell());
			// Set initial list. We don't set items directly to avoid ObservableList binding.
			sisSceneList.setItems(FXCollections.observableArrayList(project.getSisScenes()));
			sisSceneList.getSelectionModel()
					.select(project.getSisSceneByName(project.getCurrentSisSceneName()));
			// Subscribe for changes
			project.getSisScenes()
					.addListener((ListChangeListener.Change<? extends SisScene> c) -> {
						@SuppressWarnings("unchecked")
						ObservableList<SisScene> list = (ObservableList<SisScene>) c.getList();
						Platform.runLater(() -> {
							sisSceneList.getItems().setAll(list);
							// Lets not select cell if we know that one of SisScenes needs rename.
							// Because selecting action will cancel cell edit mode.
							sisSceneList.getSelectionModel().select(
									project.getSisSceneByName(project.getCurrentSisSceneName()));
						});
					});
			project.currentSisSceneNameProperty().addListener((listener) -> {
				Platform.runLater(() -> {
					sisSceneList.getSelectionModel()
							.select(project.getSisSceneByName(project.getCurrentSisSceneName()));
				});
			});
			sisSceneList
					.setContextMenu(SisSceneContextMenuBuilder.createSisSceneListContextMenu());
			sisSceneList.getSelectionModel().selectedItemProperty()
					.addListener((ChangeListener<SisScene>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							String currentSisSceneName = project.getCurrentSisSceneName();
							String selectedSisSceneName = newValue.getElementInfo().getName();
							if (!currentSisSceneName.equals(selectedSisSceneName)) {
								project.switchSisSceneTo(newValue.getElementInfo().getName());
							}
						}
					});
			sisSceneList.getFocusModel().focusedItemProperty()
					.addListener((ChangeListener<SisScene>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							if (sisSceneList.isFocused()) {
								GUIManager.elementEditor.lastFocusedProperty.set(newValue);
							}
						}
					});

		}
		return sisSceneList;
	}

	/**
	 * Builds a {@link ListView} with {@link Actor Actors}.
	 *
	 * @param project
	 *            the {@link CuteProject}
	 * @return the {@link ListView}
	 */
	@SuppressWarnings("unused")
	public static ListView<Actor> buildActorListView(CuteProject project) {
		ListView<Actor> actorList = new ListView<Actor>();
		// There are some problems with editable list(with ability to rename items)... I can't
		// handle them.
		actorList.setEditable(false);
		if (project != null) {
			actorList.setCellFactory(view -> new ActorCell());
			// Set initial list. We don't set items directly to avoid ObservableList binding.
			actorList.setItems(FXCollections.observableArrayList(project.getCurrentActors()));
			if (actorList.getItems().size() != 0) {
				actorList.getSelectionModel().select(0);
			}
			ActorListContextMenuManager contextMenuManager = new ActorListContextMenuManager(
					actorList);
			// Subscribe for changes
			project.getCurrentActors()
					.addListener((ListChangeListener.Change<? extends Actor> c) -> {
						@SuppressWarnings("unchecked")
						ObservableList<Actor> list = (ObservableList<Actor>) c.getList();
						Platform.runLater(() -> {
							actorList.getItems().setAll(list);
							// Lets try to select the last selected actor
							if (actorList.getItems().size() != 0) {
								Actor actorToSelect = GUIManager.getLastSelectedActor();
								if (actorToSelect != null) {
									int indexToSelect = actorList.getItems().indexOf(actorToSelect);
									if (indexToSelect != -1) {
										actorList.getSelectionModel().select(indexToSelect);
									} else {
										actorList.getSelectionModel().select(0);
									}
								} else {
									actorList.getSelectionModel().select(0);
								}
							}
						});
					});
			actorList.getSelectionModel().selectedItemProperty()
					.addListener((ChangeListener<Actor>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							GUIManager.setLastSelectedActor(newValue);
						}
					});
			actorList.getFocusModel().focusedItemProperty()
					.addListener((ChangeListener<Actor>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							if (actorList.isFocused()) {
								GUIManager.elementEditor.lastFocusedProperty.set(newValue);
							}
						}
					});
		}
		return actorList;
	}

	/**
	 * Builds a {@link TreeViewWithItems TreeView} with single {@link Checker Checker} in top of
	 * hierarchy based on {@link ListView} with {@link Actor Actors}.
	 *
	 * @param actorsView
	 *            the {@link ListView} with {@link Actor Actors}
	 * @return the {@link TreeViewWithItems TreeView} with single {@link Checker Checker} in top of
	 *         hierarchy
	 */
	public static TreeView<CuteNode> buildCheckerTreeView(ListView<Actor> actorsView) {
		return buildTreeView(actorsView, 0);
	}

	/**
	 * Builds a {@link TreeViewWithItems TreeView} with {@link AbstractActor#onActions} elements in
	 * top of hierarchy based on the {@link ListView} with {@link Actor Actors}.
	 *
	 * @param actorsView
	 *            the {@link ListView} with {@link Actor Actors}
	 * @return the {@link TreeViewWithItems TreeView} with {@link AbstractActor#onActions} elements
	 *         in top of hierarchy
	 */
	public static TreeView<CuteNode> buildOnActionsTreeView(ListView<Actor> actorsView) {
		return buildTreeView(actorsView, 1);
	}

	/**
	 * Builds a {@link TreeViewWithItems TreeView} with {@link AbstractActor#offActions} elements in
	 * top of hierarchy based on the {@link ListView} with {@link Actor Actors}.
	 *
	 * @param actorsView
	 *            the {@link ListView} with {@link Actor Actors}
	 * @return the {@link TreeViewWithItems TreeView} with {@link AbstractActor#offActions} elements
	 *         in top of hierarchy
	 */
	public static TreeView<CuteNode> buildOffActionsTreeView(ListView<Actor> actorsView) {
		return buildTreeView(actorsView, 2);
	}

	/**
	 * Creates a new {@link TreeViewWithItems TreeView} with elements specified by passed index.
	 * <br>
	 * Index 0: Create TreeView with Checker. <br>
	 * Index 1: Create TreeView with "On Actions". <br>
	 * Index 2: Create TreeView with "Off Actions" <br>
	 *
	 * @param actorsView
	 *            the {@link ListView} with {@link Actor Actors}
	 * @param actorTreeIndex
	 *            the index from 0 to 2 that specifies the type of TreeView to create.
	 * @return the tree view with CuteNode items
	 */
	private static TreeView<CuteNode> buildTreeView(ListView<Actor> actorsView,
			int actorTreeIndex) {
		CuteProject project = ProjectManager.getProject();
		TreeView<CuteNode> resultTreeView = new TreeView<CuteNode>();
		AutoTreeItem<CuteNode> emptyRoot = new AutoTreeItem<CuteNode>();
		resultTreeView.setCellFactory(p -> new CuteTreeCell());
		resultTreeView.setShowRoot(false);
		resultTreeView.setRoot(emptyRoot);
		// Something bad happens when we traverse focus on the tree by arrow keys on keyboard.
		// So lets not allow it.
		resultTreeView.setFocusTraversable(false);
		// This thingy will listen to TreeView's rootProperty and rootProperty's children.
		// And apply ContextMenu accordingly
		@SuppressWarnings("unused")
		TreeViewContextMenuManager contextMenuManager = new TreeViewContextMenuManager(
				resultTreeView);
		if (project != null) {
			// Set initial tree
			ObservableList<Actor> actors = actorsView.getItems();
			if (actors.size() != 0) {
				Actor firstActor = actors.get(0);
				if (firstActor != null) {
					// Actor(CuteNode) returns 3 CuteNodeContainer children. One with Checker
					// inside, one with OnActions inside, one with OffActions inside.
					// And here we select the child we want by using index.
					CuteNodeContainer childrenContainer = (CuteNodeContainer) firstActor
							.getChildren().get(actorTreeIndex);
					AutoTreeItem<CuteNode> treeItemRoot = new AutoTreeItem<CuteNode>(
							childrenContainer);
					treeItemRoot.setExpanded(true);
					resultTreeView.setRoot(treeItemRoot);
				}
			}
			// Subscribe for changes
			actorsView.getSelectionModel().selectedItemProperty()
					.addListener((ChangeListener<Actor>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							// Actor(CuteNode) returns 3 CuteNodeContainer children. One with
							// Checker inside, one with OnActions inside, one with OffActions
							// inside.
							// And here we select the child we want by using index.
							CuteNodeContainer childrenContainer = (CuteNodeContainer) newValue
									.getChildren().get(actorTreeIndex);
							AutoTreeItem<CuteNode> treeItemRoot = new AutoTreeItem<CuteNode>(
									childrenContainer);
							treeItemRoot.setExpanded(true);
							resultTreeView.setRoot(treeItemRoot);
						} else {
							resultTreeView.setRoot(emptyRoot);
							resultTreeView.setContextMenu(null);
						}
					});
			resultTreeView.getFocusModel().focusedItemProperty()
					.addListener((ChangeListener<? super TreeItem<CuteNode>>) (observable, oldValue,
							newValue) -> {
						if (newValue != null) {
							if (resultTreeView.isFocused()) {
								GUIManager.elementEditor.lastFocusedProperty
										.set(newValue.getValue());
							}
						}
					});
		}
		return resultTreeView;
	}

	/**
	 * Builds the FXML controller specific to provided simpleClassName of {@link CuteElement}).<br>
	 * Such controller controls a view for {@link CuteElement} that is intended to be used in
	 * Element Editor panel. <br>
	 * <p>
	 * Building is <b>only</b> possible when these three conditions are met: <br>
	 * 1. in "/com/ubershy/streamsis/gui/editspecific" directory exists
	 * "<b>SimpleClassName</b>.fxml" file <br>
	 * 2. The controller for such FXML file exists at the right path <br>
	 * 3. Such controller implements {@link CuteController} interface.
	 * <p>
	 * <b>If something is wrong, the method returns {@link NoSuchElementController}</b>.
	 *
	 * @param simpleClassName
	 *            the Simple Class Name of one of the {@link CuteElement CuteElements}
	 * @return the {@link CuteController} specific to chosen simpleClassName <br>
	 *         In the case of <b>fail</b>, returns {@link NoSuchElementController}
	 */
	public static CuteController buildSpecificControllerByCuteElementName(String elementSimpleClassName) {
		CuteController controller = null;
		final String subDir = "editspecific/";
		try {
			controller = (CuteController) buildControllerByRelativePath(
					subDir + elementSimpleClassName + ".fxml");
			if (!(controller instanceof CuteController) || controller == null) {
				logger.debug(elementSimpleClassName
						+ " controller cannot be loaded.\nLoading controller for NoSuchElement.fxml");
				controller = (CuteController) buildControllerByRelativePath(
						subDir + "NoSuchElement.fxml");
			}
		} catch (IllegalStateException e) {
			controller = (CuteController) buildControllerByRelativePath(
					subDir + "NoSuchElement.fxml");
		}
		return controller;
	}
	

	/**
	 * Builds the FXML controller by the path relative to "src/main/resources/fxml/".
	 * <p>
	 * Building is <b>only</b> possible when these two conditions are met: <br>
	 * 1. in "src/main/resources/fxml/" directory exists "<b>relativePath</b>" FXML file
	 * <br>
	 * 2. The controller for such FXML file exists at the right path
	 * 
	 * @param relativePath
	 *            the relative path to "src/main/resources/fxml/" where FXML file is stored
	 * @return the corresponding FXML controller <br>
	 *         In the case of <b>fail</b>, returns NULL
	 */
	public static Object buildControllerByRelativePath(String relativePath) {
		Object controller = null;
		String toBuild = "/fxml/" + relativePath;
		try {
			FXMLLoader loader = new FXMLLoader(StreamSisAppFactory.class
					.getResource(toBuild));
			loader.load();
			controller = loader.getController();
		} catch (Exception e) {
			throw new RuntimeException("Unable to build controller :" + toBuild, e);
		}
		return controller;
	}
}
