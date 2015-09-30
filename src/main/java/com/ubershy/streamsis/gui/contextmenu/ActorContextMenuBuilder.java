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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;

public final class ActorContextMenuBuilder {

	static final Logger logger = LoggerFactory.getLogger(ActorContextMenuBuilder.class);

	public static ContextMenu createActorListContextMenu(ListView<Actor> listView) {
		ContextMenu cm = new ContextMenu();
		CustomMenuItem addActorMenuItem = GUIUtil.createTooltipedMenuItem("Add new", "Create new global Actor and add to currently selected SisScene.");
		Menu addExistingActorMenuItem = generateAddExistingActorMenu();
		addActorMenuItem.setOnAction((ActionEvent event) -> {
			GUIUtil.addNewActor();
		});
		cm.getItems().addAll(addActorMenuItem);
		if (addExistingActorMenuItem.getItems().size() != 0) {
			cm.getItems().add(addExistingActorMenuItem);
		}
		cm.autoHideProperty().set(true);
		return cm;
	}

	public static ContextMenu createActorItemContextMenu(int upDownOption) {
		CuteProject project = ProjectManager.getProject();
		ListView<Actor> actorList = GUIManager.actorList;
		ContextMenu cm = new ContextMenu();
		CustomMenuItem deleteActorMenuItem = GUIUtil.createTooltipedMenuItem("Delete from SisScene",
				"Delete the Actor from the SisScene.\nActor will be still accessible for reuse in other SisScenes.");
		CustomMenuItem deleteActorGloballyMenuItem = GUIUtil.createTooltipedMenuItem("Delete globally",
				"Delete the Actor completely.\nActor will be deleted globally from all SisScenes and without recovery.");
		CustomMenuItem moveUpActorMenuItem = GUIUtil.createTooltipedMenuItem("Move up",
				"The order of Actors doesn't matter.\nBut you can still move them around if you want. ;)");
		CustomMenuItem moveDownActorMenuItem = GUIUtil.createTooltipedMenuItem("Move down",
				"The order of Actors doesn't matter.\nBut you can still move them around if you want. ;)");
		deleteActorMenuItem.setOnAction((ActionEvent event) -> {
			Actor actor = actorList.getSelectionModel().getSelectedItem();
			project.removeActorFromCurrentSisScene(actor);
		});
		deleteActorGloballyMenuItem.setOnAction((ActionEvent event) -> {
			Actor actor = actorList.getSelectionModel().getSelectedItem();
			project.removeActorGlobally(actor);
		});
		moveUpActorMenuItem.setOnAction((ActionEvent event) -> {
			Actor actor = actorList.getSelectionModel().getSelectedItem();
			project.moveUpActorInSisScene(actor);
		});
		moveDownActorMenuItem.setOnAction((ActionEvent event) -> {
			Actor actor = actorList.getSelectionModel().getSelectedItem();
			project.moveDownActorInSisScene(actor);
		});
		if (upDownOption != 3) {
			if (upDownOption != 1)
				cm.getItems().add(moveUpActorMenuItem);
			if (upDownOption != 2)
				cm.getItems().add(moveDownActorMenuItem);
		}
		cm.getItems().addAll(deleteActorGloballyMenuItem, deleteActorMenuItem);
		cm.autoHideProperty().set(true);
		return cm;
	}

	public static Menu generateAddExistingActorMenu() {
		Menu addExistingActorMenu = new Menu("Add existing...");
		ArrayList<Actor> existingActors = new ArrayList<Actor>(ProjectManager.getProject().getGlobalActors());
		ObservableList<Actor> currentActors = ProjectManager.getProject().getCurrentActors();
		existingActors.removeAll(currentActors);
		for (Actor actor : existingActors) {
			ElementInfo info = actor.getElementInfo();
			String params = "Name: " + info.getName() + "\nCheck Interval: " + actor.getCheckInterval() +
					" ms\nRepeat Interval: " + actor.getRepeatInterval() + " ms\nRepeat On Actions: " +
					actor.isDoOnRepeat() + "\nRepeat Off Actions: " + actor.isDoOffRepeat() + "\nIs broken: "
					+ actor.getElementInfo().isBroken();
			CustomMenuItem existingActorMenuItem = GUIUtil.createTooltipedMenuItem(actor.getElementInfo().getName(), params);
			existingActorMenuItem.setOnAction((ActionEvent event) -> {
				ProjectManager.getProject().addExistingActorToCurrentSisScene(actor);
			});
			addExistingActorMenu.getItems().add(existingActorMenuItem);
		}
		return addExistingActorMenu;
	}
}
