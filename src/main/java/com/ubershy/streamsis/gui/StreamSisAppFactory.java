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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.MultiSourceFilePicker;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.checkers.Coordinates;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.contextmenu.ActorListContextMenuManager;
import com.ubershy.streamsis.gui.contextmenu.SisSceneContextMenuBuilder;
import com.ubershy.streamsis.gui.contextmenu.TreeViewContextMenuManager;
import com.ubershy.streamsis.gui.controllers.CompactModeController;
import com.ubershy.streamsis.gui.controllers.CuteController;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.gui.controllers.FullModeController;
import com.ubershy.streamsis.gui.controllers.MainController;
import com.ubershy.streamsis.gui.controllers.edit.NoSuchElementController;
import com.ubershy.streamsis.gui.helperclasses.ActorCell;
import com.ubershy.streamsis.gui.helperclasses.AutoTreeItem;
import com.ubershy.streamsis.gui.helperclasses.CuteTreeCell;
import com.ubershy.streamsis.gui.helperclasses.SisSceneCell;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.CuteElementContainer;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A factory for creating {@link StreamSis} GUI objects.
 */
public class StreamSisAppFactory {

	static final Logger logger = LoggerFactory.getLogger(StreamSisAppFactory.class);

	/**
	 * The Enum with special {@link CuteElementController}s like "No Such Element" or
	 * "None Selected".
	 */
	public enum SpecialCuteElementControllerType {

		/**
		 * The {@link CuteElementController} that tells the user/programmer that the editing of the
		 * selected {@link CuteElement} is not supported for the current moment.
		 */
		NOSUCHELEMENT("NoSuchElement.fxml"),

		/**
		 * The {@link CuteElementController} that tells the user that no {@link CuteElement} is
		 * selected at the current moment so nothing to edit.
		 */
		NONESELECTED("NoneSelected.fxml");

		/** The file name of CuteController. */
		private final String fileName;

		/**
		 * Instantiates a new SpecialCuteController.
		 *
		 * @param fileName
		 *            The file name of CuteElementController {@link #fileName}.
		 */
		private SpecialCuteElementControllerType(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public String toString() {
			return fileName;
		}
	}
	
	/**
	 * The Enum with little {@link CuteController}s that are parts of bigger CuteControllers like 
	 * {@link CuteElementController}. They can't be on their own and require CuteController that
	 * wraps them. Preferably gently.
	 */
	public enum LittleCuteControllerType {

		/**
		 * The {@link CuteController} for editing {@link MultiSourceFilePicker}.
		 */
		MULTISOURCEFILEPICKER("MultiSourceFilePicker.fxml"),
		
		/**
		 * The {@link CuteController} for editing {@link MultiSourceFileLister}.
		 */
		MULTISOURCEFILELISTER("MultiSourceFileLister.fxml"),
		
		/**
		 * The {@link CuteController} for editing Similarity parameter.
		 */
		SIMILARITY("Similarity.fxml"),
		
		/**
		 * The {@link CuteController} for editing {@link Coordinates}.
		 */
		COORDINATES("Coordinates.fxml");

		/** The file name of CuteController. */
		private final String fileName;

		/**
		 * Instantiates a new LittleCuteController.
		 *
		 * @param fileName
		 *            The file name of CuteController {@link #fileName}.
		 */
		private LittleCuteControllerType(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public String toString() {
			return fileName;
		}
	}
	
	/**
	 * The Enum which specifies type of TreeView to create in StreamSis.
	 */
	public enum CuteTreeViewType {
		/** TreeView with {@link Checker} as root node. */
		CHECKER_TREE,
		
		/** TreeView with "On" {@link Actions}. */
		ON_ACTIONS_TREE,

		/** TreeView with "Off" {@link Actions}. */
		OFF_ACTIONS_TREE;
	}
	
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
						if (sisSceneList.isFocused()) {
							GUIManager.elementEditor.setCurrentElement(newValue);
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
						if (actorList.isFocused()) {
							GUIManager.elementEditor.setCurrentElement(newValue);
						}
					});
		}
		return actorList;
	}

	/**
	 * Creates a new {@link TreeView} of CuteTreeViewType.
	 * <br>
	 *
	 * @param actorsView
	 *            the {@link ListView} with {@link Actor Actors}
	 * @param typeOfTree
	 *            the type of TreeView to create
	 * @return the tree view with CuteElement items
	 */
	static TreeView<CuteElement> buildTreeView(ListView<Actor> actorsView,
			CuteTreeViewType typeOfTree) {
		CuteProject project = ProjectManager.getProject();
		TreeView<CuteElement> resultTreeView = new TreeView<CuteElement>();
		AutoTreeItem<CuteElement> emptyRoot = new AutoTreeItem<CuteElement>();
		resultTreeView.setCellFactory(p -> new CuteTreeCell());
		resultTreeView.setShowRoot(false);
		resultTreeView.setRoot(emptyRoot);
		// This EventFilter will prevent selecting the hidden root item. Still children will be able
		// to get collapsed.
		resultTreeView.addEventFilter(KeyEvent.ANY, e -> {
			if (e.getCode() == KeyCode.LEFT) {
				TreeItem<CuteElement> selectedItem = resultTreeView.getSelectionModel()
						.getSelectedItem();
				if (selectedItem.getParent() == resultTreeView.getRoot()) {
					// The item is a child of root. A single wrong move will select root item!
					if (selectedItem.getChildren().size() == 0) {
						// The item don't have children, so it can't be expanded or collapsed.
						// Hitting LEFT on keyboard will select root item, let's prevent it by
						// consuming the event.
						e.consume();
					} else {
						// The item has children, so it can be expanded or collapsed.
						if (!selectedItem.isExpanded()) {
							// The item is already collapsed. Hitting LEFT on keyboard will select
							// root item, let's prevent it by consuming the event.
							e.consume();
						}
					}
				}
			}
		});
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
					// Actor(CuteElement) returns 3 CuteElementContainer children. One with Checker
					// inside, one with OnActions inside, one with OffActions inside.
					// And here we select the child we want by using index.
					CuteElementContainer<?> childrenContainer = (CuteElementContainer<?>) firstActor
							.getChildren().get(typeOfTree.ordinal());
					AutoTreeItem<CuteElement> treeItemRoot = new AutoTreeItem<CuteElement>(
							childrenContainer);
					treeItemRoot.setExpanded(true);
					resultTreeView.setRoot(treeItemRoot);
				}
			}
			// Subscribe for changes
			actorsView.getSelectionModel().selectedItemProperty()
					.addListener((ChangeListener<Actor>) (observable, oldValue, newValue) -> {
						if (newValue != null) {
							// Actor(CuteElement) returns 3 CuteElementContainer children. One with
							// Checker inside, one with OnActions inside, one with OffActions
							// inside.
							// And here we select the child we want by using index.
							CuteElementContainer<?> childrenContainer = (CuteElementContainer<?>) newValue
									.getChildren().get(typeOfTree.ordinal());
							AutoTreeItem<CuteElement> treeItemRoot = new AutoTreeItem<CuteElement>(
									childrenContainer);
							treeItemRoot.setExpanded(true);
							resultTreeView.setRoot(treeItemRoot);
						} else {
							resultTreeView.setRoot(emptyRoot);
							resultTreeView.setContextMenu(null);
						}
					});
			resultTreeView.getFocusModel().focusedItemProperty()
					.addListener((ChangeListener<? super TreeItem<CuteElement>>) (observable, oldValue,
							newValue) -> {
						if (resultTreeView.isFocused()) {
							CuteElement elemToEdit = null;
							if (newValue != null) {
								elemToEdit = newValue.getValue();
							}
							GUIManager.elementEditor.setCurrentElement(elemToEdit);
						}
					});
		}
		return resultTreeView;
	}

	/**
	 * Builds the FXML controller specific to provided {@link CuteElement}). <br>
	 * Such controller controls a view for {@link CuteElement} that is intended to be used in
	 * Element Editor panel. <br>
	 * <p>
	 * Building is <b>only</b> possible when these three conditions are met: <br>
	 * 1. in "/com/ubershy/streamsis/gui/editspecific" directory exists
	 * "<b>SimpleClassName</b>.fxml" file <br>
	 * 2. The controller for such FXML file exists at the right path <br>
	 * 3. Such controller implements {@link CuteElementController} interface.
	 * <p>
	 * <b>If something is wrong, the method returns {@link NoSuchElementController}</b>.
	 *
	 * @param simpleClassName
	 *            the Simple Class Name of one of the {@link CuteElement CuteElements}
	 * @return the {@link CuteElementController} specific to chosen simpleClassName <br>
	 *         In the case of <b>fail</b>, returns {@link NoSuchElementController}
	 */
	public static CuteElementController buildCuteElementControllerBasedOnCuteElement(CuteElement element) {
		String editorControllersSubDir = "edit/";
		String specificSubDirName = "";
		if (element instanceof Checker) {
			specificSubDirName = "checkers/";
		} else if (element instanceof Action) {
			specificSubDirName = "actions/";
		} else if (element instanceof Counter) {
			specificSubDirName = "counters/";
		}
		String elementSimpleClassName = element.getClass().getSimpleName();
		CuteElementController controller = null;
		try {
			controller = (CuteElementController) buildControllerByRelativePath(
					editorControllersSubDir + specificSubDirName + elementSimpleClassName
							+ ".fxml");
			if (!(controller instanceof CuteElementController) || controller == null) {
				logger.debug(elementSimpleClassName + "'s controller cannot be loaded.\n"
						+ "Loading controller for NoSuchElement.fxml");
				controller = buildSpecialCuteElementController(
						SpecialCuteElementControllerType.NOSUCHELEMENT);
			}
		} catch (IllegalStateException e) {
			controller = buildSpecialCuteElementController(
					SpecialCuteElementControllerType.NOSUCHELEMENT);
		}
		return controller;
	}
	
	/**
	 * Builds {@link SpecialCuteElementControllerType} FXML controller for editor panel.
	 * 
	 * @return the {@link CuteElementController}.
	 */
	public static CuteElementController buildSpecialCuteElementController(
			SpecialCuteElementControllerType scc) {
		final String subDir = "edit/";
		CuteElementController controller = (CuteElementController) buildControllerByRelativePath(
				subDir + scc.toString());
		if (!(controller instanceof CuteElementController) || controller == null) {
			throw new RuntimeException(scc.toString() + " not found");
		}
		return controller;
	}
	
	/**
	 * Builds {@link LittleCuteControllerType} FXML controller for editor panel.
	 * 
	 * @return the {@link CuteController}.
	 */
	public static CuteController buildLittleCuteController(LittleCuteControllerType lil) {
		final String subDir = "edit/littlethings/";
		CuteController controller = (CuteController) buildControllerByRelativePath(
				subDir + lil.toString());
		if (!(controller instanceof CuteController) || controller == null) {
			throw new RuntimeException(lil.toString() + " not found");
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
			FXMLLoader loader = new FXMLLoader(StreamSisAppFactory.class.getResource(toBuild));
			loader.load();
			controller = loader.getController();
		} catch (IOException e) {
			throw new RuntimeException("Unable to build controller :" + toBuild, e);
		}
		return controller;
	}
}
