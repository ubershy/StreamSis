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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.helperclasses.CuteTreeCell;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteNodeContainer;
import com.ubershy.streamsis.project.CuteNode.AddableChildrenTypeInfo;
import com.ubershy.streamsis.project.CuteNode.ContainerCreationParams;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.TreeItem;

/**
 * This class can build {@link ContextMenu}s for StreamSis's trees and tree items inside trees.
 */
public class TreeContextMenuBuilder {

	static final Logger logger = LoggerFactory.getLogger(TreeContextMenuBuilder.class);

	private static List<Class<? extends Action>> allAvailableNewActions = getAvailableCuteNodes(
			"com.ubershy.streamsis.actions", Action.class);
	private static List<Class<? extends Checker>> allAvailableNewCheckers = getAvailableCuteNodes(
			"com.ubershy.streamsis.checkers", Checker.class);
	private static List<Class<? extends Counter>> allAvailableNewCounters = getAvailableCuteNodes(
			"com.ubershy.streamsis.counters", Counter.class);

	/**
	 * Creates the {@link ContextMenu} for the chosen {@link CuteTreeCell}.
	 *
	 * @param treeCell
	 *            the CuteTreeCell for which ContextMenu will be created.
	 * @return the ContextMenu
	 */
	public static ContextMenu createCuteTreeCellContextMenu(CuteTreeCell treeCell) {
		CuteNode cuteNode = (CuteNode) treeCell.getItem();
		if (cuteNode == null)
			return null;
		ContextMenu cm = new ContextMenu();
		CustomMenuItem moveUpMenuItem = GUIUtil.createTooltipedMenuItem("Move Up",
				"This will move the thingy Up");
		moveUpMenuItem.setOnAction(createMovementEventHandler(treeCell, true));
		CustomMenuItem moveDownMenuItem = GUIUtil.createTooltipedMenuItem("Move Down",
				"This will move the thingy Down");
		moveDownMenuItem.setOnAction(createMovementEventHandler(treeCell, false));
		CustomMenuItem deleteMenuItem = GUIUtil.createTooltipedMenuItem("Delete",
				"Are you sure?\nAre you going to throw this little item away..?");
		deleteMenuItem.setOnAction((ActionEvent event) -> {
			ObservableList<? extends CuteNode> list = treeCell.getTreeItem().getParent().getValue()
					.getChildren();
			if (list != null) {
				if (!list.isEmpty()) {
					if (list.contains(cuteNode)) {
						list.remove(cuteNode);
					} else {
						logger.error("Nothing to remove from list");
					}
				} else {
					logger.error("list is empty");
				}
			} else {
				logger.error("list is null");
			}
		});

		// Let's determine where the Item can move.
		// By default let's have ability to move in both directions.
		PossibleMoves possibleMoves = PossibleMoves.UPORDOWN;
		TreeItem<CuteNode> treeItem = treeCell.getTreeItem();
		int treeItemIndex = treeItem.getParent().getChildren().indexOf(treeItem);
		int sizeOfList = treeItem.getParent().getChildren().size();
		if (sizeOfList != 1) {
			if (treeItemIndex == 0)
				possibleMoves = PossibleMoves.ONLYDOWN;
			if (treeItemIndex == sizeOfList - 1)
				possibleMoves = PossibleMoves.ONLYUP;
		} else {
			possibleMoves = PossibleMoves.NOWHERE;
		}

		// Let's add all MenuItems to Context Menu
		if (canChildrenBeAdded(cuteNode)) {
			cm.getItems().add(generateNewCuteNodeMenu(cuteNode));
		}
		if (!possibleMoves.equals(PossibleMoves.NOWHERE)) {
			if (possibleMoves.equals(PossibleMoves.UPORDOWN)) {
				cm.getItems().add(moveUpMenuItem);
				cm.getItems().add(moveDownMenuItem);
			} else {
				if (possibleMoves.equals(PossibleMoves.ONLYUP))
					cm.getItems().add(moveUpMenuItem);
				if (possibleMoves.equals(PossibleMoves.ONLYDOWN))
					cm.getItems().add(moveDownMenuItem);
			}
		}
		if (!CuteNode.MaxAddableChildrenCount.UNDEFINEDORZERO.equals(
				treeCell.getTreeItem().getParent().getValue().getMaxAddableChildrenCount())) {
			cm.getItems().add(deleteMenuItem);
		}
		cm.autoHideProperty().set(true);
		return cm;
	}

	/**
	 * Gets the available list of subtypes of specified type. <br>
	 * Convenient for finding subtypes of chosen {@link CuteNode} type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param packageName
	 *            the Package Name where CuteNode classes reside
	 * @param type
	 *            the preferred type of CuteNode
	 * @return the available list of subclasses
	 */
	private static <T> List<Class<? extends T>> getAvailableCuteNodes(String packageName,
			Class<T> type) {
		// Lets use reflections to find all subtypes of type
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends T>> subtypes = reflections.getSubTypesOf(type);
		// Lets convert our set of subtypes to a list and will further work on the list
		List<Class<? extends T>> resultList = new ArrayList<>(subtypes);
		// Lets get rid of abstract classes inside our list
		List<Class<? extends T>> thingsToGetRidOf = new ArrayList<>();
		for (Class<? extends T> subtype : subtypes) {
			if (Modifier.isAbstract(subtype.getModifiers())) {
				thingsToGetRidOf.add(subtype);
			}
		}
		resultList.removeAll(thingsToGetRidOf);
		// Lets sort our list of classes
		Collections.sort(resultList, new Comparator<Class<? extends T>>() {
			public int compare(Class<? extends T> o1, Class<? extends T> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			};
		});
		return resultList;
	}

	/**
	 * Creates the {@link EventHandler} for moving {@link CuteTreeCell} up or down in the
	 * {@link TreeView}.
	 *
	 * @param treeCell
	 *            the CuteTreeCell to move
	 * @param up
	 *            move item up? (down otherwise)
	 * @return the EventHandler
	 */
	private static EventHandler<ActionEvent> createMovementEventHandler(CuteTreeCell treeCell,
			boolean up) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				CuteNode cuteNode = (CuteNode) treeCell.getItem();
				ObservableList<? extends CuteNode> list = treeCell.getTreeItem().getParent()
						.getValue().getChildren();
				if (list != null) {
					if (!list.isEmpty()) {
						if (list.contains(cuteNode)) {
							int index = list.indexOf(cuteNode);
							if (up) {
								Collections.swap(list, index, index - 1);
							} else {
								Collections.swap(list, index, index + 1);
							}
						} else {
							logger.error("No such thingy to move");
						}
					} else {
						logger.error("List is empty");
					}
				} else {
					logger.error("List is null");
				}
			}
		};
	}

	/**
	 * Generate {@link Menu} for adding new {@link CuteNode}s as children to the chosen CuteNode.
	 *
	 * @param whereToAdd
	 *            the CuteNode where to add new child using generated Menu
	 * @return the Menu
	 */
	private static Menu generateNewCuteNodeMenu(CuteNode whereToAdd) {
		AddableChildrenTypeInfo typeInfo = whereToAdd.getAddableChildrenTypeInfo();
		Class<? extends CuteNode> type = typeInfo.getType();
		List<Class<? extends CuteNode>> listWithCuteNodeClasses;
		switch (typeInfo) {
		case ACTION:
			listWithCuteNodeClasses = new ArrayList<Class<? extends CuteNode>>(
					allAvailableNewActions);
			break;
		case CHECKER:
			listWithCuteNodeClasses = new ArrayList<Class<? extends CuteNode>>(
					allAvailableNewCheckers);
			break;
		case COUNTER:
			listWithCuteNodeClasses = new ArrayList<Class<? extends CuteNode>>(
					allAvailableNewCounters);
			break;
		case CONTAINER:
			// This case is very specific. Handle it in another method.
			Menu addNewContainerMenu = generateMenuForAddingNewContainer(whereToAdd);
			return addNewContainerMenu;
		default:
			throw new RuntimeException("Programmer. Context menu for " + typeInfo.name()
					+ " is not implemented. You can fix it =)");
		}
		
		Menu addNewCuteNodeMenu = new Menu("Add new " + type.getSimpleName() + "...");
		
		for (Class<? extends CuteNode> cuteNodeClass : listWithCuteNodeClasses) {
			// TODO: Set good description of CuteNodes instead of smile :3
			CustomMenuItem newCuteNodeMenuItem = GUIUtil
					.createTooltipedMenuItem(cuteNodeClass.getSimpleName(), ":3");
			newCuteNodeMenuItem.setOnAction((ActionEvent event) -> {
				
				// This list will later help to generate name for the new CuteNode.
				List<String> existingChildrenNames = new ArrayList<String>();
				for (CuteNode node : whereToAdd.getChildren()) {
					existingChildrenNames.add(node.getElementInfo().getName());
				}
				
				CuteNode nodeToAdd = null;
				try {
					// Instantiate CuteNode
					nodeToAdd = cuteNodeClass.newInstance();
					// Set Appropriate name for CuteNode
					nodeToAdd.getElementInfo().setName(generateUniqueNameForCuteNode(
							cuteNodeClass.getSimpleName(), existingChildrenNames));
				} catch (Exception e) {
					throw new RuntimeException(
							"Can't instantiate element: " + cuteNodeClass.getSimpleName()
									+ " inside " + whereToAdd.getClass().getSimpleName(),
							e);
				}
				if (nodeToAdd != null) {
					@SuppressWarnings("unchecked")
					ObservableList<CuteNode> whereToAddCasted = (ObservableList<CuteNode>) whereToAdd
							.getChildren();
					whereToAddCasted.add(nodeToAdd);
					// Initialize CuteNode to highlight that it is still not configured
					GUIUtil.reinitElementAndWholeProject(nodeToAdd);
					// Initialize also parent as method above does not guarantee that parent will
					// be reinitialized, but it's needed because it might be broken because of
					// missing children.
					whereToAdd.init();
				}
			});
			addNewCuteNodeMenu.getItems().add(newCuteNodeMenuItem);
		}
		return addNewCuteNodeMenu;
	}

	/**
	 * Generate {@link Menu} for adding new {@link CuteNodeContainer}s as children to the chosen
	 * CuteNode.
	 *
	 * @param whereToAdd
	 *            the CuteNode where to add new child using generated Menu
	 * @return the Menu
	 */
	private static Menu generateMenuForAddingNewContainer(CuteNode whereToAdd) {
		ContainerCreationParams params = whereToAdd.getChildContainerCreationParams();
		Menu menu = new Menu("Add new " + params.GUIItemMenuName + "...");
		
		CustomMenuItem newContainerMenuItem = GUIUtil
				.createTooltipedMenuItem(params.GUIItemMenuName, params.GUIDescription);
		newContainerMenuItem.setOnAction((ActionEvent event) -> {
			CuteNode cuteNodeContainerToAdd = null;
			
			// This list will later help to generate name for the new Container.
			List<String> existingChildrenNames = new ArrayList<String>();
			for (CuteNode node : whereToAdd.getChildren()) {
				existingChildrenNames.add(node.getElementInfo().getName());
			}
			
			try {
				// Determine proper name for new container
				String name = generateUniqueNameForCuteNode(params.creationBaseName,
						existingChildrenNames);
				// Instantiate Container
				cuteNodeContainerToAdd = CuteNodeContainer.createEmptyCuteNodeContainer(name,
						params.childrenType, params.childrenMaxCount);
				cuteNodeContainerToAdd.getElementInfo().setEditable(params.editable);
				cuteNodeContainerToAdd.getElementInfo()
						.setEmptyNameAllowed(params.emptyNameAllowed);

			} catch (Exception e) {
				throw new RuntimeException("Can't instantiate Container inside "
						+ whereToAdd.getClass().getSimpleName(), e);
			}
			if (cuteNodeContainerToAdd != null) {
				@SuppressWarnings("unchecked")
				ObservableList<CuteNode> whereToAddCasted = (ObservableList<CuteNode>) whereToAdd
						.getChildren();
				whereToAddCasted.add(cuteNodeContainerToAdd);
				// Initialize cuteNodeContainerToAdd though currently it does nothing.
				// But reinitializing the whole project still needed.
				GUIUtil.reinitElementAndWholeProject(cuteNodeContainerToAdd);
				// Initialize also parent as method above does not guarantee that parent will
				// be reinitialized, but it's needed because parent might be broken because of
				// missing children.
				whereToAdd.init();
			}
		});
		menu.getItems().add(newContainerMenuItem);
		return menu;
	}

	/**
	 * Generates possible name for {@link CuteNode} which will be created inside parent CuteNode as
	 * a child. <br>
	 * The name will be unique only in parent CuteNode's scope, not project scope. <br>
	 * The name will look like this: "New SomeCuteNodeClassName". <br>
	 * An incrementing number in parentheses might be added to ensure this name is unique.
	 * 
	 * @param baseName
	 *            Base name to use. It is usually CuteNode class's simple name.
	 * @param existingChildrenNames
	 *            A list with the names of parent CuteNode's existing children.
	 * 
	 * @return A Name which unique in parent CuteNode's scope.
	 */
	private static String generateUniqueNameForCuteNode(String baseName,
			List<String> existingChildrenNames) {
		String genericName = "New " + baseName;
		String alteredName = genericName;
		int counter = 0;
		while (existingChildrenNames.contains(alteredName)) {
			counter++;
			alteredName = String.format("%s(%d)", genericName, counter);
		}
		return alteredName;
	}

	/**
	 * Creates the special {@link ContextMenu} for the root {@link TreeItem} in {@link TreeView}
	 * which contains {@link CuteNodes}.
	 *
	 * @param treeItem
	 *            the TreeItem for which Context Menu will be created
	 * @return Context Menu
	 */
	public static ContextMenu createRootTreeItemContextMenu(TreeItem<CuteNode> treeItem) {
		ContextMenu cm = null;
		if (treeItem != null) {
			cm = new ContextMenu();
			CuteNode rootCuteNode = treeItem.getValue();
			if (rootCuteNode != null) {
				if (canChildrenBeAdded(rootCuteNode)) {
					cm.getItems().add(generateNewCuteNodeMenu(rootCuteNode));
				}
			}
			cm.autoHideProperty().set(true);
		}
		return cm;
	}

	/**
	 * Determines if children can be added to the chosen {@link CuteNode}.
	 *
	 * @param cuteNode
	 *            the CuteNode to check
	 * @return true, if children can be added to this CuteNode
	 */
	private static boolean canChildrenBeAdded(CuteNode cuteNode) {
		if (cuteNode.getAddableChildrenTypeInfo() == null)
			return false;
		if (cuteNode.getMaxAddableChildrenCount() == null)
			return false;
		CuteNode.MaxAddableChildrenCount maxChildren = cuteNode.getMaxAddableChildrenCount();
		switch (maxChildren) {
		case INFINITY:
			break;
		case ONE:
			if (cuteNode.getChildren().size() >= 1) {
				return false;
			}
			break;
		case UNDEFINEDORZERO:
			// We can't add or remove children if we don't know how many there can be
			return false;
		default:
			return false;
		}
		return true;
	}
}
