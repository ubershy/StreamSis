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
import java.util.List;
import java.util.ResourceBundle;

import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Callback;

// Under construction. Not working. Please unsee.
public class CompactModeController implements Initializable {
	@FXML
	private Node rootNode;
	@FXML
	private Label currentSisScene;
	@FXML
	private ListView<Actor> actorList;

	public void bindToProject(CuteProject project) {
		// TODO init views
	}

	public void initialize(URL url, ResourceBundle resourceBundle) {
		actorList.setCellFactory(new Callback<ListView<Actor>, ListCell<Actor>>() {
			public ListCell<Actor> call(ListView<Actor> actorListView) {
				final ListCell<Actor> cell = new ListCell<Actor>() {
					protected void updateItem(Actor actor, boolean empty) {
						super.updateItem(actor, empty);
						if (!empty) {
							setText(String.format("%s", actor.getElementInfo().getName()));
						} else {
							setText(null);
							setGraphic(null);
						}
					}
				};
				cell.setOnMouseClicked(new EventHandler<Event>() {
					public void handle(Event event) {
						Actor actor = cell.getItem();
						if (actor != null) {
							sisSceneSelected(actor.getElementInfo().getName());
						}
					}
				});
				return cell;
			}
		});
	}

	public void searchSisScenes(ActionEvent event) {
		final javafx.concurrent.Task<List<Actor>> searchTasksActor = new javafx.concurrent.Task<List<Actor>>() {
			protected List<Actor> call() throws Exception {
				return ProjectManager.getProject().getGlobalActors();
			}
		};

		searchTasksActor.stateProperty().addListener(new ChangeListener<Worker.State>() {
			public void changed(ObservableValue<? extends Worker.State> source, Worker.State oldState, Worker.State newState) {
				if (newState.equals(Worker.State.SUCCEEDED)) {
					actorList.getItems().setAll(searchTasksActor.getValue());
				}
			}
		});

		new Thread(searchTasksActor).start();
	}

	public Node getView() {
		return rootNode;
	}

	public void sisSceneSelected(String sisSceneName) {
		// mainController.showContactDetail(contactId);
		showActorsOfSisScene(sisSceneName);
	}

	public void showActorsOfSisScene(String sisSceneName) {

	}

	@FXML
	public void showFullMode() {
		GUIManager.mainController.useFullMode();
	}

	@FXML
	public void startProject() {
		CuteProject project = ProjectManager.getProject();
		if (project != null) {
			if (project.isStarted()) {
				project.stopProject();
			} else {
				try {
					ProjectManager.startProjectOutsideJavaFXThread();
				} catch (RuntimeException e) {
					Alert alert = new Alert(AlertType.ERROR);
					GUIUtil.cutifyAlert(alert);
					alert.setTitle("Can't start the Project");
					alert.setHeaderText("Dear user, StreamSis can't start the Project.");
					if (e.getMessage().equals("globalActors are empty")) {
						alert.setContentText("You have to add at least one Actor to this Project");
					}
					if (e.getMessage().equals("sisScenes are empty")) {
						alert.setContentText("You have to add at least one SisScene to this Project");
					}
					GUIUtil.showAlertInPrimaryStageCenter(alert);
				}
			}
		}
	}

}
