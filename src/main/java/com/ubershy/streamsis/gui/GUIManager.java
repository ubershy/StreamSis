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

import org.controlsfx.control.NotificationPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.controllers.CompactModeController;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.gui.controllers.FullModeController;
import com.ubershy.streamsis.gui.controllers.MainController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public final class GUIManager {
	static final Logger logger = LoggerFactory.getLogger(GUIManager.class);
	public static ListView<Actor> actorList;
	public static TreeView<CuteElement> checkerTree;
	public static ListView<SisScene> sisSceneList;
	public static MainController mainController;
	private static Scene mainScene;
	public static TreeView<CuteElement> offActionsTree;
	public static TreeView<CuteElement> onActionsTree;
	public static ElementEditorController elementEditor;
	private static Stage primaryStage;
	private static NotificationPane fullModeNotificationPane;
	private static NotificationPane compactModeNotificationPane;

	public static void buildGui(CuteProject project) {
		if (primaryStage == null) {
			throw new RuntimeException("You need to setPrimaryStage() first.");
		}
		logger.debug("GUI. Initializing...");

		mainController = StreamSisAppFactory.buildMainController();
		CompactModeController compactModeController = StreamSisAppFactory
				.buildCompactModeController();
		FullModeController fullModeController = StreamSisAppFactory.buildFullModeController();

		// Size will be overridden later by showLastMode()
		mainScene = new Scene((Parent) mainController.getView(), 777, 777);

		// mainScene.getStylesheets().add("/com/ubershy/streamsis/gui/css/progressIndicators.css");
		primaryStage.setScene(mainScene);
		
		mainController.init(primaryStage, fullModeController.getView(),
				compactModeController.getView());

		mainController.useLastMode();

		sisSceneList = StreamSisAppFactory.buildSisSceneListView(project);
		actorList = StreamSisAppFactory.buildActorListView(project);
		checkerTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.CHECKER_TREE);
		onActionsTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.ON_ACTIONS_TREE);
		offActionsTree = StreamSisAppFactory.buildTreeView(actorList,
				StreamSisAppFactory.CuteTreeViewType.OFF_ACTIONS_TREE);
		elementEditor = StreamSisAppFactory.buildElementEditorController();

		fullModeController.bindToProject(project);
		compactModeController.bindToProject(project);
	}

	// GUI classes must use this method
	public static void createNewProject() {
		GUIManager.saveCoordinatesOfAllWindows();
		CuteProject project = ProjectManager.createAndSetNewProject();
		buildGui(project);
		ProjectManager.initProjectOutsideJavaFXThread();
	}

	public static Scene getMainScene() {
		return mainScene;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void loadProject(String path, boolean start) {
		GUIManager.saveCoordinatesOfAllWindows();
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
			logger.error("GUI. Creating new empty project.");
			project = ProjectManager.createAndSetNewProject();
		}
		buildGui(project);
		if (start) {
			ProjectManager.startProjectOutsideJavaFXThread();
		} else {
			ProjectManager.initProjectOutsideJavaFXThread();
		}
	}

	public static void setPrimaryStage(Stage mainStage) {
		primaryStage = mainStage;
	}

	public static void showLoadingError(IOException e) {
		Alert alert = new Alert(AlertType.ERROR);
		GUIUtil.cutifyAlert(alert);
		alert.setTitle("Can't load the Project");
		alert.setHeaderText("Dear user, StreamSis can't load the Project.");
		alert.setContentText(Util.whyProjectCantBeLoaded(e));
		GUIUtil.showAlertInPrimaryStageCenter(alert);
	}

	public static void showNotification(Node graphic, String text) {
		NotificationPane paneToUse;
		if (mainController.isCurrentModeFull()) {
			paneToUse = fullModeNotificationPane;
		} else {
			paneToUse = compactModeNotificationPane;
		}
		paneToUse.setText(text);
		paneToUse.setGraphic(graphic);
		paneToUse.show();
	}
	
	/**
	 * @param Sets the {@link NotificationPane} to use when StreamSis in Full Mode.
	 */
	public static void setFullModeNotificationPane(NotificationPane notificationPane) {
		fullModeNotificationPane = notificationPane;
	}
	
	/**
	 * @param Sets the {@link NotificationPane} to use when StreamSis in Compact Mode.
	 */
	public static void setCompactModeNotificationPane(NotificationPane notificationPane) {
		compactModeNotificationPane = notificationPane;
	}

	/** Save the coordinates of all windows of {@link GUIManager} to config. */
	public static void saveCoordinatesOfAllWindows() {
		// Save primary stage coordinates.
		if (primaryStage != null && primaryStage.isShowing()) {
			String currentMode = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode") + "Mode";
			GUIUtil.saveWindowCoordinates(primaryStage, currentMode);
		}
	}

}
