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
package com.ubershy.streamsis.gui.helperclasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * The Class AutoTreeItem.<br>
 * This is the View. And it's generic type is used as Model.<br>
 * AutoTreeItem is based on {@link TreeItem}, but builds/removes it's children items automatically.
 * <p>
 * And to build the whole Tree you just need to instantiate a single AutoTreeItem with the model passed as argument.<br>
 * This Tree that consists of AutoTreeItems will automatically reflect changes made to the Model.<br>
 * <b>Note</b>: please use Collections.swap() to move items inside the model.
 *
 * @param <T>
 *            the generic type (<b>T</b> extends {@link RecursiveParent})
 */
@SuppressWarnings("rawtypes")
public class AutoTreeItem<T extends RecursiveParent> extends TreeItem<T> {

	static final Logger logger = LoggerFactory.getLogger(AutoTreeItem.class);

	/**
	 * Instantiates a new AutoTreeItem.
	 * You will need to {@link #setValue(Object)} later.
	 */
	public AutoTreeItem() {
		this(null);
	}

	/**
	 * Instantiates a new AutoTreeItem and sets it's Value.
	 *
	 * @param value
	 *            the Value
	 */
	public AutoTreeItem(T value) {
		this(value, (Node) null);
	}

	/**
	 * Instantiates a new AutoTreeItem, sets it's Value to passed value and it's Graphic to passed node.
	 *
	 * @param value
	 *            the Value
	 * @param node
	 *            the Graphic
	 */
	@SuppressWarnings("unchecked")
	public AutoTreeItem(T value, Node node) {
		super(value, node);
		if (value != null) {
			ObservableList<T> valueChildren = value.getChildren();
			if (valueChildren != null) {
				for (T valueChild : valueChildren) {
					AutoTreeItem<T> treeItemChild = new AutoTreeItem<T>(valueChild);
					treeItemChild.setExpanded(true);
					getChildren().add(treeItemChild);
				}
				value.getChildren().addListener(new WeakListChangeListener<>(childrenListener));
			}
		}
	}

	/** Listens to the Model's children when they need it most. */
	private ListChangeListener<T> childrenListener = new ListChangeListener<T>() {
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends T> c) {
			while (c.next()) {
				if (c.wasPermutated()) {
					for (int i = c.getFrom(); i < c.getTo(); ++i) {
						// permutation is not needed, when using Collections.swap()
					}
				} else if (c.wasUpdated()) {
					// updating is not needed
				} else {
					if (c.wasRemoved()) {
						for (int i = c.getRemovedSize() - 1; i >= 0; i--) {
							int index = c.getFrom() + i;
							int size = getChildren().size();
							if (index < size) {
								getChildren().get(index).setValue(null);
								getChildren().remove(index);
								logger.debug("Deleted AutoTreeItem with index: " + index);
							} else {
								logger.error("Attempt to delete unexisting AutoTreeItem in treeItemChildren");
							}
						}
					}
					if (c.wasAdded()) {
						for (int i = c.getFrom(); i < c.getTo(); i++) {
							AutoTreeItem<T> newChildTreeItem = new AutoTreeItem<T>(c.getList().get(i));
							if (expandedProperty().getValue()) {
								newChildTreeItem.setExpanded(true);
							}
							getChildren().add(i, newChildTreeItem);
							logger.debug("Added new AutoTreeItem to index position: " + i);
						}
					}
				}
			}
		}
	};
}
