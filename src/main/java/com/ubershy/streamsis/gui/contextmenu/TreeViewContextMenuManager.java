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

import java.util.HashMap;
import java.util.Map;

import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class TreeViewContextMenuManager {

	private TreeView<CuteElement> treeView;

	private Map<TreeItem<CuteElement>, ListChangeListener<TreeItem<CuteElement>>> childrenListeners = new HashMap<TreeItem<CuteElement>, ListChangeListener<TreeItem<CuteElement>>>();

	public TreeViewContextMenuManager(TreeView<CuteElement> treeView) {
		this.treeView = treeView;
		startManaging();
	}

	public void startManaging() {
		// Lets generate initial context menu for the TreeView
		treeView.setContextMenu(
				TreeContextMenuBuilder.createRootTreeItemContextMenu(treeView.getRoot()));
		// Lets start reacting to root TreeItem's children list changes
		subscribeToRootTreeItemChildren(treeView.getRoot());
		ChangeListener<TreeItem<CuteElement>> rootChangeListener = new ChangeListener<TreeItem<CuteElement>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<CuteElement>> observable,
					TreeItem<CuteElement> oldValue, TreeItem<CuteElement> newValue) {
				if (newValue != null) {
					// Lets generate new context menu
					treeView.setContextMenu(
							TreeContextMenuBuilder.createRootTreeItemContextMenu(newValue));
					// Also lets stop reacting to changes of previous root TreeItem's children list
					unsubscribeToRootTreeItemChildren(oldValue);
					// And lets start reacting to new root TreeItem's children list changes
					subscribeToRootTreeItemChildren(newValue);
				} else {
					treeView.setContextMenu(null);
					// Lets stop reacting to changes of previous root TreeItem's children list
					unsubscribeToRootTreeItemChildren(oldValue);
				}
			}
		};
		// Lets react when root TreeItem changes
		treeView.rootProperty().addListener(rootChangeListener);
	}

	protected void unsubscribeToRootTreeItemChildren(TreeItem<CuteElement> rootTreeItem) {
		if (rootTreeItem != null) {
			ListChangeListener<TreeItem<CuteElement>> listenerToRemove = childrenListeners
					.get(rootTreeItem);
			rootTreeItem.getChildren().removeListener(listenerToRemove);
			childrenListeners.remove(rootTreeItem);
		}
	}

	private void subscribeToRootTreeItemChildren(TreeItem<CuteElement> rootTreeItem) {
		if (rootTreeItem != null) {
			ListChangeListener<TreeItem<CuteElement>> childrenChangeListener = new ListChangeListener<TreeItem<CuteElement>>() {
				@Override
				public void onChanged(ListChangeListener.Change<? extends TreeItem<CuteElement>> c) {
					// Lets generate new context menu
					treeView.setContextMenu(
							TreeContextMenuBuilder.createRootTreeItemContextMenu(rootTreeItem));
				}
			};
			// Lets remember the listener so we can unsubscribe later
			childrenListeners.put(rootTreeItem, childrenChangeListener);
			// Lets react to root TreeItem's children list changes
			rootTreeItem.getChildren().addListener(childrenChangeListener);
		}
	}

}
