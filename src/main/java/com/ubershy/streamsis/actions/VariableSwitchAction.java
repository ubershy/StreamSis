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
package com.ubershy.streamsis.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteNodeContainer;
import com.ubershy.streamsis.project.UserVars;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Variable Switch Action. <br>
 * This {@link Action} checks the value of a user variable stored in {@link UserVars}. <br>
 * And based on what the value is, it executes associated {@link Action} stored inside it's
 * {@link #actionsMap}. <br>
 */
public class VariableSwitchAction extends AbstractCuteNode implements Action {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(VariableSwitchAction.class);

	/**
	 * List with cases (possible Variable's Values) ({@link CuteNodeContainer}) each one containing
	 * associated {@link Action Actions}.
	 */
	@JsonProperty("cases")
	ObservableList<CuteNodeContainer<Action>> cases = FXCollections.observableArrayList(); 

	/** The name of user Variable(key) to check for Values. */
	@JsonProperty("key")
	StringProperty key = new SimpleStringProperty("");

	/**
	 * Instantiates a new variable switch action.
	 */
	public VariableSwitchAction() {
	}

	/**
	 * Instantiates a new VariableSwitchAction. <br>
	 * On execution it will check the value of a user variable stored in {@link UserVars}. <br>
	 * And based on what the value is, it executes associated {@link Action}s stored inside it's
	 * {@link #cases}. <br>
	 *
	 * @param key
	 *            the name of the user variable(key) to set in {@link UserVars}
	 * @param actionsMap
	 *            the treeMap with values and corresponding {@link Action Actions}
	 */
	public VariableSwitchAction(String key, TreeMap<String, ArrayList<Action>> actionsMap) {
		this.key.set(key);
		cases = generateCasesBasedOnMap(actionsMap);
	}
	
	/**
	 * Instantiates a new VariableSwitchAction via deserialization. <br>
	 * On execution it will check the value of a user variable stored in {@link UserVars}. <br>
	 * And based on what the value is, it executes associated {@link Action}s stored inside it's
	 * {@link #cases}. <br>
	 *
	 * @param key            the name of the user variable(key) to set in {@link UserVars}
	 * @param cases the cases
	 */
	@JsonCreator
	public VariableSwitchAction(@JsonProperty("key") String key,
			@JsonProperty("cases") ArrayList<CuteNodeContainer<Action>> cases) {
		this.key.set(key);
		this.cases.setAll(cases);
		for (CuteNodeContainer<Action> container: cases) {
			container.getElementInfo().setEditable(true);
			container.getElementInfo().setEmptyNameAllowed(true);
		}
	}

	/**
	 * Generates list of {@link CuteNodeContainer}s containing Action from the map of string Values
	 * and corresponding Actions.
	 * 
	 * @param actionsMap
	 *            map which keys are Values and it's values are corresponding lists of Actions.
	 * @return ObservableList of CuteNodeContainers which contain lists of Actions. Names of
	 *         CuteNodeContainers are Values.
	 *
	 */
	private ObservableList<CuteNodeContainer<Action>> generateCasesBasedOnMap(
			TreeMap<String, ArrayList<Action>> actionsMap) {
		ObservableList<CuteNodeContainer<Action>> genCases = FXCollections.observableArrayList();
		for (Entry<String, ArrayList<Action>> entry : actionsMap.entrySet()) {
			CuteNodeContainer<Action> container = new CuteNodeContainer<Action>(entry.getValue(),
					entry.getKey(), AddableChildrenTypeInfo.ACTION,
					MaxAddableChildrenCount.INFINITY);
			container.getElementInfo().setEditable(true);
			container.getElementInfo().setEmptyNameAllowed(true);
			genCases.add(container);
		}
		return genCases;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			String aquiredValue = UserVars.get(key.get());
			if (aquiredValue == null) {
				elementInfo.setBooleanResult(false);
				logger.info(
						"VariableSwitchAction: Doing nothing. No string Value is found for Key: "
								+ key.get());
				return;
			}
			boolean foundCase = false;
			for (CuteNodeContainer<Action> container : cases) {
				String caseName = container.getElementInfo().getName();
				if (aquiredValue.equals(caseName)) {
					foundCase = true;
					logger.info(
							"Actions are found for Key: " + key.get() + " Value: " + aquiredValue);
					for (CuteNode node : container.getChildren()) {
						Action actionToExecute = (Action) node;
						logger.info("Executing '" + actionToExecute.getElementInfo().getName()
								+ "'(" + actionToExecute.getClass().getSimpleName() + ")");
						actionToExecute.execute();
					}
					elementInfo.setBooleanResult(true);
				}
			}
			if (foundCase == false) {
				elementInfo.setBooleanResult(false);
				logger.info("VariableSwitchAction: Doing nothing. No Action is found for Key: "
						+ key.get() + " Value: " + aquiredValue);
			}
		}
	}

	@Override
	public void init() {
		super.init();
		elementInfo.setAsReadyAndHealthy();
		if (key.get().isEmpty()) {
			elementInfo.setAsBroken("Variable name is empty");
		}
		if (cases.size() == 0) {
			elementInfo.setAsBroken(
					"At least one Variable's Value should be added inside VariableSwitchAction");
		}
		LinkedList<String> caseNamesForFindingDuplicates = new LinkedList<>();
		for (CuteNodeContainer<Action> container: cases) {
			String caseName = container.getElementInfo().getName();
			// Check duplicate names of Containers.
			if (caseNamesForFindingDuplicates.contains(caseName)) {
				// Already contains such a name added from previous iteration.
				elementInfo.setAsBroken("Duplicate Variable's Value detected inside - '" + caseName + "'");
			}
			caseNamesForFindingDuplicates.add(caseName);
			// Check if Container doesn't have Actions inside.
			if (container.getChildren().size() == 0) {
				elementInfo.setAsBroken("The Variable's Value '" + caseName
						+ "' don't have any Actions assigned to it.");
			}
			// Init each Action in this Container.
			for (CuteNode node : container.getChildren()) {
				node.init();
				if (node.getElementInfo().isBroken()) {
					elementInfo.setAsBroken("One or more Actions assigned to Variable's Value '"
							+ caseName + "' are broken");
				}
			}
		}
	}

	/**
	 * Gets the {@link #key}.
	 *
	 * @return The key.
	 */
	public String getKey() {
		return key.get();
	}
	
	/**
	 * Sets the {@link #key}.
	 *
	 * @param key The new key.
	 */
	public void setKey(String key) {
		this.key.set(key);
	}
	
	/** See {@link #key}. */
	public StringProperty keyProperty() {
		return key;
	}
	
	/**
	 * Gets {@link #cases}.
	 * 
	 * @return the {@link #cases}.
	 */
	@JsonIgnore
	public ObservableList<CuteNodeContainer<Action>> getCases() {
		return cases;
	}

	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return AddableChildrenTypeInfo.CONTAINER;
	}
	
	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.INFINITY;
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public ObservableList<CuteNodeContainer<Action>> getChildren() {
		return cases;
	}
	
	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public ContainerCreationParams getChildContainerCreationParams() {
		return new CuteNode.ContainerCreationParams(AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, true, true, "Variable's Value",
				"Variable's Value (replace it)",
				"A container of Actions which name(Value) is compared to Variable's current Value."
						+ "\nIf they are equal, the contained Actions are run.");
	}

}
