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

import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.controllers.CompactModeController;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.gui.controllers.FullModeController;
import com.ubershy.streamsis.gui.controllers.MainController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public final class GUIManager {
	public static ListView<Actor> actorList;
	public static TreeView<CuteNode> checkerTree;
	public static ListView<SisScene> sisSceneList;
	private static Actor lastSelectedActor;

	static final Logger logger = LoggerFactory.getLogger(GUIManager.class);
	public static MainController mainController;
	private static Scene mainScene;
	public static TreeView<CuteNode> offActionsTree;
	public static TreeView<CuteNode> onActionsTree;
	public static ElementEditorController elementEditor;
	private static Stage primaryStage;

	// First method to call
	public static void buildGui(CuteProject project) {
		logger.debug("GUI. Initializing...");

		clearLocalProjectSpecificVariables();

		mainController = StreamSisAppFactory.buildMainController();
		CompactModeController compactModeController = StreamSisAppFactory
				.buildCompactModeController();
		FullModeController fullModeController = StreamSisAppFactory.buildFullModeController();

		sisSceneList = StreamSisAppFactory.buildSisSceneListView(project);
		actorList = StreamSisAppFactory.buildActorListView(project);
		checkerTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.CHECKER_TREE);
		onActionsTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.ON_ACTIONS_TREE);
		offActionsTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.OFF_ACTIONS_TREE);
		elementEditor = StreamSisAppFactory.buildElementEditorController();

		fullModeController.postInit(project);
		compactModeController.postInit(project);

		// Size will be overridden later by showLastMode()
		mainScene = new Scene((Parent) mainController.getView(), 777, 777);

		// mainScene.getStylesheets().add("/com/ubershy/streamsis/gui/css/progressIndicators.css");
		primaryStage.setScene(mainScene);

		mainController.init(primaryStage, fullModeController.getView(),
				compactModeController.getView());
		mainController.showLastMode();
	}

	private static void clearLocalProjectSpecificVariables() {
		setLastSelectedActor(null);
	}

	public static void closeApplicatonSafely() {
		if (primaryStage != null) {
			GUIUtil.saveCurrentModeWindowStateAndEverything();
			Platform.exit();
			// FIXME
			System.exit(0);
		}
	}

	// GUI classes must use this method
	public static void createNewProject() {
		CuteProject project = ProjectManager.createAndSetNewProject();
		GUIUtil.saveCurrentModeWindowStateAndEverything();
		buildGui(project);
	}

	public static Actor getLastSelectedActor() {
		return lastSelectedActor;
	}

	public static Scene getMainScene() {
		return mainScene;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void loadProject(String path) {
		if (path == null) {
			logger.error("GUI. Attempt to load Project with NULL path");
			return;
		}
		if (path.isEmpty()) {
			logger.error("GUI. Attempt to load project with empty path");
			return;
		}

		// Stop previous project if exist
		CuteProject project = ProjectManager.getProject();
		if (project != null) {
			project.stopProject();
		}

		// Try load new project
		try {
			project = ProjectManager.loadProjectAndSet(path);
			logger.info("GUI. Project loading SUCCESS");
		} catch (IOException e) {
			logger.error("GUI. Can't load project: " + path);
			showLoadingError(e);
			return;
		}
		if (project == null) {
			logger.debug("Current project is Null. Creating new");
			project = ProjectManager.createAndSetNewProject();
		}
		GUIUtil.saveCurrentModeWindowStateAndEverything();
		buildGui(project);
	}

	public static void setLastSelectedActor(Actor lastSelectedActor) {
		GUIManager.lastSelectedActor = lastSelectedActor;
	}

	public static void setPrimaryStage(Stage mainStage) {
		primaryStage = mainStage;
	}

	public static void showLoadingError(IOException e) {
		Alert alert = new Alert(AlertType.ERROR);
		GUIUtil.cutifyAlert(alert);
		alert.setTitle("Can't load the Project");
		alert.setHeaderText("Dear user, StreamSis can't load the Project.");
		if ("IOException".equals(e.getClass().getSimpleName())) { // Means not IOException subtype
			alert.setContentText(
					"Project file is inaccessible.\nMaybe you don't have system rights to read this file.");
		} else {
			// Means we got one of the IOException subtypes: JsonMappingException and
			// JsonGenerationException
			alert.setContentText(
					"Project file is corrupted or incompatible with this version of StreamSis.\nOr maybe the programmer needs to be spanked.");
		}
		GUIUtil.showAlertInStageCenter(alert);
	}

}
