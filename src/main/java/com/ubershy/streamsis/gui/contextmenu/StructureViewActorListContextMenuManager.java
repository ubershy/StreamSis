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

import com.ubershy.streamsis.actors.Actor;

import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;

public class StructureViewActorListContextMenuManager {
	
	private ListView<Actor> listView;
	
	public StructureViewActorListContextMenuManager(ListView<Actor> listView) {
		this.listView = listView;
	}
	
	public void startManaging() {
		// Lets generate initial context menu for the Actor ListView
		listView.setContextMenu(ActorContextMenuBuilder.createCMForStructureViewList(listView));
		ListChangeListener<Actor> childrenChangeListener = new ListChangeListener<Actor>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Actor> c) {
				// Lets generate new context menu
				listView.setContextMenu(ActorContextMenuBuilder.createCMForStructureViewList(listView));
			}
		};
		// Lets react when ListView children list changes
		listView.getItems().addListener(childrenChangeListener);
	}

}
