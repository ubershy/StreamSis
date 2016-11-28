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

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.CuteElement.AddableChildrenTypeInfo;
import com.ubershy.streamsis.elements.CuteElement.ContainerCreationParams;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.counters.Counter;
import com.ubershy.streamsis.gui.cells.CuteTreeCell;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.ProjectManager;

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

	private static List<Class<? extends Action>> allAvailableNewActions = getAvailableCuteElements(
			"com.ubershy.streamsis.elements.actions", Action.class);
	private static List<Class<? extends Checker>> allAvailableNewCheckers = getAvailableCuteElements(
			"com.ubershy.streamsis.elements.checkers", Checker.class);
	private static List<Class<? extends Counter>> allAvailableNewCounters = getAvailableCuteElements(
			"com.ubershy.streamsis.elements.counters", Counter.class);

	/**
	 * Creates the {@link ContextMenu} for the chosen {@link CuteTreeCell}.
	 *
	 * @param treeCell
	 *            the CuteTreeCell for which ContextMenu will be created.
	 * @return the ContextMenu
	 */
	public static ContextMenu createCuteTreeCellContextMenu(CuteTreeCell treeCell) {
		CuteElement cuteElement = (CuteElement) treeCell.getItem();
		if (cuteElement == null)
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
			ObservableList<? extends CuteElement> list = treeCell.getTreeItem().getParent()
					.getValue().getChildren();
			if (list != null) {
				if (!list.isEmpty()) {
					if (list.contains(cuteElement)) {
						list.remove(cuteElement);
						// Initialize whole project to reinitialize any parents that might get
						// broken.
						ProjectManager.initProjectOutsideJavaFXThread();
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
		TreeItem<CuteElement> treeItem = treeCell.getTreeItem();
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
		if (canChildrenBeAdded(cuteElement)) {
			cm.getItems().add(generateNewCuteElementMenu(cuteElement));
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
		if (!CuteElement.MaxAddableChildrenCount.UNDEFINEDORZERO.equals(
				treeCell.getTreeItem().getParent().getValue().getMaxAddableChildrenCount())) {
			cm.getItems().add(deleteMenuItem);
		}
		cm.autoHideProperty().set(true);
		return cm;
	}

	/**
	 * Gets the available list of subtypes of specified type. <br>
	 * Convenient for finding subtypes of chosen {@link CuteElement} type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param packageName
	 *            the Package Name where CuteElement classes reside
	 * @param type
	 *            the preferred type of CuteElement
	 * @return the available list of subclasses
	 */
	private static <T> List<Class<? extends T>> getAvailableCuteElements(String packageName,
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
				CuteElement cuteElement = (CuteElement) treeCell.getItem();
				ObservableList<? extends CuteElement> list = treeCell.getTreeItem().getParent()
						.getValue().getChildren();
				if (list != null) {
					if (!list.isEmpty()) {
						if (list.contains(cuteElement)) {
							int index = list.indexOf(cuteElement);
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
	 * Generate {@link Menu} for adding new {@link CuteElement}s as children to the chosen
	 * CuteElement.
	 *
	 * @param whereToAdd
	 *            the CuteElement where to add new child using generated Menu
	 * @return the Menu
	 */
	private static Menu generateNewCuteElementMenu(CuteElement whereToAdd) {
		AddableChildrenTypeInfo typeInfo = whereToAdd.getAddableChildrenTypeInfo();
		Class<? extends CuteElement> type = typeInfo.getType();
		List<Class<? extends CuteElement>> listWithCuteElementClasses;
		switch (typeInfo) {
		case ACTION:
			listWithCuteElementClasses = new ArrayList<Class<? extends CuteElement>>(
					allAvailableNewActions);
			break;
		case CHECKER:
			listWithCuteElementClasses = new ArrayList<Class<? extends CuteElement>>(
					allAvailableNewCheckers);
			break;
		case COUNTER:
			listWithCuteElementClasses = new ArrayList<Class<? extends CuteElement>>(
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
		
		Menu addNewCuteElementMenu = new Menu("Add new " + type.getSimpleName() + "...");
		
		for (Class<? extends CuteElement> cuteElementClass : listWithCuteElementClasses) {
			// TODO: Set good description of CuteElements instead of smile :3
			CustomMenuItem newCuteElementMenuItem = GUIUtil
					.createTooltipedMenuItem(cuteElementClass.getSimpleName(), ":3");
			newCuteElementMenuItem.setOnAction((ActionEvent event) -> {
				
				// This list will later help to generate name for the new CuteElement.
				List<String> existingChildrenNames = new ArrayList<String>();
				for (CuteElement node : whereToAdd.getChildren()) {
					existingChildrenNames.add(node.getElementInfo().getName());
				}
				
				CuteElement elemToAdd = null;
				try {
					// Instantiate CuteElement
					elemToAdd = cuteElementClass.newInstance();
					// Set Appropriate name for CuteElement
					elemToAdd.getElementInfo().setName(GUIUtil.generateUniqueNameForNewItem(
							cuteElementClass.getSimpleName(), existingChildrenNames));
				} catch (Exception e) {
					throw new RuntimeException(
							"Can't instantiate element: " + cuteElementClass.getSimpleName()
									+ " inside " + whereToAdd.getClass().getSimpleName(),
							e);
				}
				if (elemToAdd != null) {
					@SuppressWarnings("unchecked")
					ObservableList<CuteElement> whereToAddCasted = (ObservableList<CuteElement>) whereToAdd
							.getChildren();
					whereToAddCasted.add(elemToAdd);
					// Initialize whole project, it may highlight unconfigured CuteElement.
					ProjectManager.initProjectOutsideJavaFXThread();
				}
			});
			addNewCuteElementMenu.getItems().add(newCuteElementMenuItem);
		}
		return addNewCuteElementMenu;
	}

	/**
	 * Generate {@link Menu} for adding new {@link CuteElementContainer}s as children to the chosen
	 * CuteElement.
	 *
	 * @param whereToAdd
	 *            the CuteElement where to add new child using generated Menu
	 * @return the Menu
	 */
	private static Menu generateMenuForAddingNewContainer(CuteElement whereToAdd) {
		ContainerCreationParams params = whereToAdd.getChildContainerCreationParams();
		Menu menu = new Menu("Add new " + params.containerFakeTypeName + "...");
		
		CustomMenuItem newContainerMenuItem = GUIUtil
				.createTooltipedMenuItem(params.containerFakeTypeName, params.GUIDescription);
		newContainerMenuItem.setOnAction((ActionEvent event) -> {
			CuteElement cuteElementContainerToAdd = null;
			
			// This list will later help to generate name for the new Container.
			List<String> existingChildrenNames = new ArrayList<String>();
			for (CuteElement element : whereToAdd.getChildren()) {
				existingChildrenNames.add(element.getElementInfo().getName());
			}
			
			try {
				// Determine proper name for new container
				String name = GUIUtil.generateUniqueNameForNewItem(params.creationBaseName,
						existingChildrenNames);
				// Instantiate Container
				cuteElementContainerToAdd = CuteElementContainer.createEmptyCuteElementContainer(
						name, params.childrenType, params.childrenMaxCount,
						params.childrenMinCount, params.containerFakeTypeName);
				cuteElementContainerToAdd.getElementInfo().setEditable(params.editable);
				cuteElementContainerToAdd.getElementInfo()
						.setEmptyNameAllowed(params.emptyNameAllowed);

			} catch (Exception e) {
				throw new RuntimeException("Can't instantiate Container inside "
						+ whereToAdd.getClass().getSimpleName(), e);
			}
			if (cuteElementContainerToAdd != null) {
				@SuppressWarnings("unchecked")
				ObservableList<CuteElement> whereToAddCasted = (ObservableList<CuteElement>) whereToAdd
						.getChildren();
				whereToAddCasted.add(cuteElementContainerToAdd);
				// Initialize whole project to reinitialize any parents that might get broken.
				ProjectManager.initProjectOutsideJavaFXThread();
			}
		});
		menu.getItems().add(newContainerMenuItem);
		return menu;
	}

	/**
	 * Creates the special {@link ContextMenu} for the root {@link TreeItem} in {@link TreeView}
	 * which contains {@link CuteElement}s.
	 *
	 * @param treeItem
	 *            the TreeItem for which Context Menu will be created
	 * @return Context Menu
	 */
	public static ContextMenu createRootTreeItemContextMenu(TreeItem<CuteElement> treeItem) {
		ContextMenu cm = null;
		if (treeItem != null) {
			cm = new ContextMenu();
			CuteElement rootCuteElement = treeItem.getValue();
			if (rootCuteElement != null) {
				if (canChildrenBeAdded(rootCuteElement)) {
					cm.getItems().add(generateNewCuteElementMenu(rootCuteElement));
				}
			}
			cm.autoHideProperty().set(true);
		}
		return cm;
	}

	/**
	 * Determines if children can be added to the chosen {@link CuteElement}.
	 *
	 * @param cuteElement
	 *            the CuteElement to check
	 * @return true, if children can be added to this CuteElement
	 */
	private static boolean canChildrenBeAdded(CuteElement cuteElement) {
		if (cuteElement.getAddableChildrenTypeInfo() == null)
			return false;
		if (cuteElement.getMaxAddableChildrenCount() == null)
			return false;
		CuteElement.MaxAddableChildrenCount maxChildren = cuteElement.getMaxAddableChildrenCount();
		switch (maxChildren) {
		case INFINITY:
			break;
		case ONE:
			if (cuteElement.getChildren().size() >= 1) {
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
