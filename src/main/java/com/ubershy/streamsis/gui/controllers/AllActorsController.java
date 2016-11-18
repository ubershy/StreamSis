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
import java.util.ResourceBundle;

import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.ActorCell;
import com.ubershy.streamsis.gui.helperclasses.ActorCell.ActorCellType;
import com.ubershy.streamsis.project.CuteProject;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/**
 * The controller for showing all {@link CuteProject}'s Actors (global Actors) even the ones that
 * are not currently active in a separate window.
 */
public class AllActorsController implements Initializable {
	@FXML
	private VBox root;
    @FXML
    private ListView<Actor> AllActorsList;

	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				// Set scene style.
				String url = AllActorsController.class.getResource("/css/streamsis.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		AllActorsList.setCellFactory(view -> new ActorCell(ActorCellType.ALLACTORSVIEWCELL));
	}
	
	public void bindToProject(CuteProject project) {
		// Set initial list. We don't set items directly to avoid ObservableList binding.
		AllActorsList.setItems(FXCollections.observableArrayList(project.getGlobalActors()));
		// Subscribe for changes
		project.getGlobalActors().addListener((ListChangeListener.Change<? extends Actor> c) -> {
			@SuppressWarnings("unchecked")
			ObservableList<Actor> list = (ObservableList<Actor>) c.getList();
			Platform.runLater(() -> {
				AllActorsList.getItems().setAll(list);
			});
		});
		AllActorsList.getFocusModel().focusedItemProperty()
				.addListener((ChangeListener<Actor>) (observable, oldValue, newValue) -> {
					if (AllActorsList.isFocused()) {
						GUIManager.elementEditor.setCurrentElement(newValue);
					}
				});
	}

	public double getWidth() {
		return 200.0;
	}

	public double getHeight() {
		return 200.0;
	}

}
