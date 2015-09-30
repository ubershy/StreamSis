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

import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;

public class SisSceneContextMenuBuilder {

	public static ContextMenu createSisSceneListContextMenu() {
		ContextMenu cm = new ContextMenu();
		CustomMenuItem addSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Add new", "Add new SisScene in which you can add Actors later.");
		addSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			GUIUtil.addNewSisScene();
		});
		cm.getItems().add(addSisSceneMenuItem);
		cm.autoHideProperty().set(true);
		return cm;
	}

	public static ContextMenu createSisSceneItemContextMenu(int upDownOption) {
		CuteProject project = ProjectManager.getProject();
		ListView<SisScene> sisSceneList = GUIManager.sisSceneList;
		ContextMenu cm = new ContextMenu();
		CustomMenuItem setDefaultSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Set as Default SisScene",
				"Default SisScene starts first when you start Project.");
		CustomMenuItem deleteSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Delete",
				"Delete the SisScene. Actors that were inside of it will be still accessible for reuse.");
		CustomMenuItem deleteSisSceneWithActorsMenuItem = GUIUtil.createTooltipedMenuItem("Delete with Actors",
				"Delete the SisScene and Actors.\nActors will be deleted globally from all SisScenes and without recovery.");
		CustomMenuItem moveUpSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Move up",
				"The order of SisScenes doesn't matter.\nBut you can still move them around if you want. ;)");
		CustomMenuItem moveDownSisSceneMenuItem = GUIUtil.createTooltipedMenuItem("Move down",
				"The order of SisScenes doesn't matter.\nBut you can still move them around if you want. ;)");
		deleteSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.removeSisScene(sisSceneList.getSelectionModel().getSelectedItem());
		});
		deleteSisSceneWithActorsMenuItem.setOnAction((ActionEvent event) -> {
			project.removeSisSceneWithActors(sisSceneList.getSelectionModel().getSelectedItem());
		});
		setDefaultSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.setDefaultSisSceneName(sisSceneList.getSelectionModel().getSelectedItem().getElementInfo().getName());
		});
		moveUpSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.moveUpSisScene(sisSceneList.getSelectionModel().getSelectedItem());
		});
		moveDownSisSceneMenuItem.setOnAction((ActionEvent event) -> {
			project.moveDownSisScene(sisSceneList.getSelectionModel().getSelectedItem());
		});
		if (upDownOption != 3) {
			if (upDownOption != 1)
				cm.getItems().add(moveUpSisSceneMenuItem);
			if (upDownOption != 2)
				cm.getItems().add(moveDownSisSceneMenuItem);
		}
		cm.getItems().addAll(setDefaultSisSceneMenuItem, deleteSisSceneWithActorsMenuItem, deleteSisSceneMenuItem);
		cm.autoHideProperty().set(true);
		return cm;
	}

}
