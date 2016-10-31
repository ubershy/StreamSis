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
package com.ubershy.streamsis.gui.contextmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;

public class SisSceneContextMenuBuilder {

	public static ContextMenu createSisSceneListContextMenu() {
		ContextMenu cm = new ContextMenu();
		CustomMenuItem addSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Add new",
				"Add new SisScene in which you can add Actors later.");
		addSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			addSisScene();
		});
		cm.getItems().add(addSisSceneMenuItem);
		cm.autoHideProperty().set(true);
		return cm;
	}
	
	private static void addSisScene() {
		String genericName = "New SisScene";
		String alteredName = genericName;
		int counter = 0;
		while (ProjectManager.getProject().getSisSceneByName(alteredName) != null) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		SisScene newSisScene = new SisScene(alteredName, new String[] {});
		// Initialize SisScene to highlight that it is still not configured
		newSisScene.init();
		ProjectManager.getProject().addSisScene(newSisScene);
		// Initialize whole project
		ProjectManager.initProjectOutsideJavaFXThread();
	}

	public static ContextMenu createSisSceneItemContextMenu(PossibleMoves possibleMoves) {
		CuteProject project = ProjectManager.getProject();
		ListView<SisScene> sisSceneList = GUIManager.sisSceneList;
		ContextMenu cm = new ContextMenu();
		CustomMenuItem setPrimarySisSceneMenuItem = GUIUtil.createTooltipedMenuItem(
				"Set as Primary SisScene", "Primary SisScene starts first when you start Project.");
		CustomMenuItem deleteSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Delete",
				"Delete the SisScene."
				+ " Actors that were inside of it will be still accessible for reuse.");
		CustomMenuItem deleteSisSceneWithActorsMenuItem = GUIUtil.createTooltipedMenuItem(
				"Delete with Actors",
				"Delete the SisScene and Actors."
				+ "\nActors will be deleted globally from this and all others SisScenes without "
				+ "recovery.");
		CustomMenuItem moveUpSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Move Up",
				"The order of SisScenes doesn't matter."
				+ "\nBut you can still move them around if you want. ;)");
		CustomMenuItem moveDownSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Move Down",
				"The order of SisScenes doesn't matter."
				+ "\nBut you can still move them around if you want. ;)");
		CustomMenuItem addSisSceneMenuItem = GUIUtil.createTooltipedMenuItem(
				"Add new SisScene to the list", "Add new SisScene to the end of the list.");
		addSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			addSisScene();
		});
		deleteSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			SisScene scene = sisSceneList.getSelectionModel().getSelectedItem();
			project.removeSisScene(scene);
			// Initialize whole project
			ProjectManager.initProjectOutsideJavaFXThread();
		});
		deleteSisSceneWithActorsMenuItem.setOnAction((ActionEvent event) -> {
			SisScene scene = sisSceneList.getSelectionModel().getSelectedItem();
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete SisScene with Actors");
			alert.setHeaderText("This SisScene will be deleted."
					+ "\nAlso the Actors of this SisScene will be deleted globally and from all"
					+ " other SisScenes without recovery.");
			String poorActors = "";
			Random random = new Random();
			List<String> phrases = new ArrayList<String>(Arrays.asList(
					"I have a wife and kids", "I still have some tricks up my sleeve ;)",
					"Please no...", "You can't delete me just like that!",
					"Treat me bad, I need the reputation", "Maybe... we can make a deal?",
					"Don't delete me, I promise I will perform better next time!",
					"I don't care anymore...", "My Actions were that bad?",
					"If you will delete me, I will have no choice but to become a streamer..."));
			for (String actorName : scene.getActorNames()) {
				poorActors += "\t" + actorName;
				if (random.nextInt(7) == 0) {
					if (phrases.size() != 0) {
						int phraseIndex = random.nextInt(phrases.size());
						poorActors += "\t\t- \"" + phrases.get(phraseIndex) + "\"";
						phrases.remove(phraseIndex);
					}
				}
				poorActors += "\n";
			}
			String warning = "";
			if (!poorActors.isEmpty()) {
				warning = "Take a good look of these poor Actors:\n\n" + poorActors;
			}
			alert.setContentText(warning + "\n" + "Is it okay for you to delete them?");
			GUIUtil.cutifyAlert(alert);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				project.removeSisSceneWithActors(scene);
				// Initialize whole project
				ProjectManager.initProjectOutsideJavaFXThread();
			}
		});
		setPrimarySisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.setPrimarySisSceneName(
					sisSceneList.getSelectionModel().getSelectedItem().getElementInfo().getName());
		});
		moveUpSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.moveUpSisScene(sisSceneList.getSelectionModel().getSelectedItem());
		});
		moveDownSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.moveDownSisScene(sisSceneList.getSelectionModel().getSelectedItem());
		});
		cm.getItems().addAll(moveUpSisSceneMenuItem, moveDownSisSceneMenuItem,
				setPrimarySisSceneMenuItem, deleteSisSceneMenuItem,
				deleteSisSceneWithActorsMenuItem, addSisSceneMenuItem);
		switch (possibleMoves) {
		case NOWHERE:
			cm.getItems().clear();
			cm.getItems().add(addSisSceneMenuItem);
			break;
		case ONLYDOWN:
			cm.getItems().remove(moveUpSisSceneMenuItem);
			break;
		case ONLYUP:
			cm.getItems().remove(moveDownSisSceneMenuItem);
			break;
		case UPORDOWN:
			break;
		}
		cm.autoHideProperty().set(true);
		return cm;
	}

}
