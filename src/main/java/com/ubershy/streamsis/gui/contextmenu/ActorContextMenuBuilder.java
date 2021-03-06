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

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.actors.Actor;
import com.ubershy.streamsis.elements.actors.UniversalActor;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;

public final class ActorContextMenuBuilder {

	static final Logger logger = LoggerFactory.getLogger(ActorContextMenuBuilder.class);
	
	private static final String aboutActor = "About Actor:\n"
			+ CuteElement.getDescriptionOfType(UniversalActor.class);
	
	private static void addNewActor() {
		String genericName = "New Actor";
		String alteredName = genericName;
		int counter = 0;
		while (ProjectManager.getProject().getActorByName(alteredName) != null) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		Actor newActor = new UniversalActor(alteredName, 1000, 1000, false, false);
		ProjectManager.getProject().addActorToGlobalActors(newActor);
		ProjectManager.getProject().addExistingActorToCurrentSisScene(newActor);
		// Initialize whole project
		ProjectManager.initProjectFromGUI();
	}

	public static ContextMenu createCMForStructureViewList(ListView<Actor> listView) {
		ContextMenu cm = new ContextMenu();
		CustomMenuItem addActorMenuItem = GUIUtil.createTooltipedMenuItem("Add new",
				"Create new global Actor and add it to currently selected SisScene.\n\n"
						+ aboutActor);
		Menu addExistingActorMenuItem = generateAddExistingActorMenu(false);
		addActorMenuItem.setOnAction((ActionEvent event) -> {
			addNewActor();
		});
		cm.getItems().addAll(addActorMenuItem);
		if (addExistingActorMenuItem.getItems().size() != 0) {
			cm.getItems().add(addExistingActorMenuItem);
		}
		cm.autoHideProperty().set(true);
		return cm;
	}

	public static ContextMenu createCMForStructureViewItem(Actor actor,
			PossibleMoves possibleMoves) {
		CuteProject project = ProjectManager.getProject();
		ContextMenu cm = new ContextMenu();
		Menu addExistingActorMenuItem = generateAddExistingActorMenu(true);
		CustomMenuItem addActorMenuItem = GUIUtil.createTooltipedMenuItem(
				"Add new Actor below",
				"Add new Actor to the end of the list.\n\n" + aboutActor);
		CustomMenuItem deleteActorMenuItem = GUIUtil.createTooltipedMenuItem("Delete from SisScene",
				"Delete the Actor from the SisScene."
						+ "\nActor will be still accessible for reuse in other SisScenes.");
		CustomMenuItem deleteActorGloballyMenuItem = GUIUtil
				.createTooltipedMenuItem("Delete globally", "Delete the Actor completely.\nActor "
						+ "will be deleted globally from all SisScenes and without recovery.");
		CustomMenuItem moveUpActorMenuItem = GUIUtil.createTooltipedMenuItem("Move Up",
				"The order of Actors doesn't matter."
						+ "\nBut you can still move them around if you want. ;)");
		CustomMenuItem moveDownActorMenuItem = GUIUtil.createTooltipedMenuItem("Move Down",
				"The order of Actors doesn't matter."
						+ "\nBut you can still move them around if you want. ;)");
		addActorMenuItem.setOnAction((ActionEvent event) -> {
			addNewActor();
		});
		deleteActorMenuItem.setOnAction((ActionEvent event) -> {
			project.removeActorFromCurrentSisScene(actor);
			// Initialize whole project
			ProjectManager.initProjectFromGUI();
		});
		deleteActorGloballyMenuItem.setOnAction((ActionEvent event) -> {
			project.removeActorGlobally(actor);
			// Initialize whole project
			ProjectManager.initProjectFromGUI();
		});
		moveUpActorMenuItem.setOnAction((ActionEvent event) -> {
			project.moveUpActorInSisScene(actor);
		});
		moveDownActorMenuItem.setOnAction((ActionEvent event) -> {
			project.moveDownActorInSisScene(actor);
		});
		switch (possibleMoves) {
		case NOWHERE:
			break;
		case ONLYDOWN:
			cm.getItems().add(moveDownActorMenuItem);
			break;
		case ONLYUP:
			cm.getItems().add(moveUpActorMenuItem);
			break;
		case UPORDOWN:
			cm.getItems().add(moveUpActorMenuItem);
			cm.getItems().add(moveDownActorMenuItem);
			break;
		}
		cm.getItems().addAll(deleteActorGloballyMenuItem, deleteActorMenuItem, addActorMenuItem,
				addExistingActorMenuItem);
		cm.autoHideProperty().set(true);
		return cm;
	}

	private static Menu generateAddExistingActorMenu(boolean onItem) {
		String menuText;
		if (onItem) {
			menuText = "Add existing Actor below...";
		} else {
			menuText = "Add existing...";
		}
		Menu addExistingActorMenu = new Menu(menuText);
		ArrayList<Actor> existingActors = new ArrayList<Actor>(
				ProjectManager.getProject().getGlobalActorsUnmodifiable());
		ObservableList<Actor> currentActors = ProjectManager.getProject().getCurrentActorsUnmodifiable();
		existingActors.removeAll(currentActors);
		for (Actor actor : existingActors) {
			ElementInfo info = actor.getElementInfo();
			String params = "Name: " + info.getName() + "\nCheck Interval: "
					+ actor.getCheckInterval() + " ms\nRepeat Interval: "
					+ actor.getRepeatInterval() + " ms\nRepeat On Actions: " + actor.getDoOnRepeat()
					+ "\nRepeat Off Actions: " + actor.getDoOffRepeat() + "\nIs broken: "
					+ actor.getElementInfo().isBroken();
			CustomMenuItem existingActorMenuItem = GUIUtil
					.createTooltipedMenuItem(actor.getElementInfo().getName(), params);
			existingActorMenuItem.setOnAction((ActionEvent event) -> {
				ProjectManager.getProject().addExistingActorToCurrentSisScene(actor);
			});
			addExistingActorMenu.getItems().add(existingActorMenuItem);
		}
		return addExistingActorMenu;
	}

	public static ContextMenu createCMForAllActorsViewItem(Actor actor,
			PossibleMoves possibleMoves) {
		CuteProject project = ProjectManager.getProject();
		ContextMenu cm = new ContextMenu();
		CustomMenuItem deleteActorGloballyMenuItem = GUIUtil
				.createTooltipedMenuItem("Delete globally", "Delete the Actor completely.\nActor "
						+ "will be deleted globally from all SisScenes and without recovery.");
		CustomMenuItem moveUpActorMenuItem = GUIUtil.createTooltipedMenuItem("Move Up",
				"The order of Actors doesn't matter."
						+ "\nBut you can still move them around if you want. ;)");
		CustomMenuItem moveDownActorMenuItem = GUIUtil.createTooltipedMenuItem("Move Down",
				"The order of Actors doesn't matter."
						+ "\nBut you can still move them around if you want. ;)");
		deleteActorGloballyMenuItem.setOnAction((ActionEvent event) -> {
			project.removeActorGlobally(actor);
			// Initialize whole project
			ProjectManager.initProjectFromGUI();
		});
		moveUpActorMenuItem.setOnAction((ActionEvent event) -> {
			project.moveUpActorInGlobalActors(actor);
		});
		moveDownActorMenuItem.setOnAction((ActionEvent event) -> {
			project.moveDownActorInGlobalActors(actor);
		});
		switch (possibleMoves) {
		case NOWHERE:
			break;
		case ONLYDOWN:
			cm.getItems().add(moveDownActorMenuItem);
			break;
		case ONLYUP:
			cm.getItems().add(moveUpActorMenuItem);
			break;
		case UPORDOWN:
			cm.getItems().add(moveUpActorMenuItem);
			cm.getItems().add(moveDownActorMenuItem);
			break;
		}
		cm.getItems().add(deleteActorGloballyMenuItem);
		return cm;
	}

}
