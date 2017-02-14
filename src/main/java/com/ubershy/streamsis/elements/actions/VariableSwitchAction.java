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
package com.ubershy.streamsis.elements.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.UserVars;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;

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
public class VariableSwitchAction extends AbstractCuteElement implements Action {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(VariableSwitchAction.class);
	
	/** The description of this CuteElement type. */
	public final static String description = VariableSwitchAction.class.getSimpleName()
			+ " on execution checks the specified Variable for expected Values and runs the"
			+ " associated Actions with one of the Values.";

	/**
	 * List with cases (possible Variable's Values) ({@link CuteElementContainer}) each one
	 * containing associated {@link Action Actions}.
	 */
	@JsonProperty("cases")
	ObservableList<CuteElementContainer<Action>> cases = FXCollections.observableArrayList(); 

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
			@JsonProperty("cases") ArrayList<CuteElementContainer<Action>> cases) {
		this.key.set(key);
		this.cases.setAll(cases);
		for (CuteElementContainer<Action> container: cases) {
			container.getElementInfo().setEditable(true);
			container.getElementInfo().setEmptyNameAllowed(true);
		}
	}

	/**
	 * Generates list of {@link CuteElementContainer}s containing Action from the map of string
	 * Values and corresponding Actions.
	 * 
	 * @param actionsMap
	 *            map which keys are Values and it's values are corresponding lists of Actions.
	 * @return ObservableList of CuteElementContainers which contain lists of Actions. Names of
	 *         CuteElementContainers are Values.
	 *
	 */
	private ObservableList<CuteElementContainer<Action>> generateCasesBasedOnMap(
			TreeMap<String, ArrayList<Action>> actionsMap) {
		ObservableList<CuteElementContainer<Action>> genCases = FXCollections.observableArrayList();
		for (Entry<String, ArrayList<Action>> entry : actionsMap.entrySet()) {
			CuteElementContainer<Action> container = new CuteElementContainer<Action>(
					entry.getValue(), entry.getKey(), AddableChildrenTypeInfo.ACTION,
					MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.ONE,
					"Variable's Value");
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
			for (CuteElementContainer<Action> container : cases) {
				String caseName = container.getElementInfo().getName();
				if (aquiredValue.equalsIgnoreCase(caseName)) {
					foundCase = true;
					logger.info(
							"Actions are found for Key: " + key.get() + " Value: " + aquiredValue);
					for (CuteElement element : container.getChildren()) {
						Action actionToExecute = (Action) element;
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
		if (key.get().isEmpty()) {
			elementInfo.setAsBroken("Variable name is empty");
		}
		LinkedList<String> caseNamesForFindingDuplicates = new LinkedList<>();
		for (CuteElementContainer<Action> container: cases) {
			String caseName = container.getElementInfo().getName();
			// Check duplicate names of Containers.
			if (caseNamesForFindingDuplicates.contains(caseName)) {
				// Already contains such a name added from previous iteration.
				elementInfo.setAsBroken(
						"Duplicate Variable's Value detected inside - '" + caseName + "'");
			}
			caseNamesForFindingDuplicates.add(caseName);
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
	public ObservableList<CuteElementContainer<Action>> getCases() {
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
	
	@JsonIgnore
	@Override
	public MinAddableChildrenCount getMinAddableChildrenCount() {
		return MinAddableChildrenCount.ONE;
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public ObservableList<CuteElementContainer<Action>> getChildren() {
		return cases;
	}
	
	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public ContainerCreationParams getChildContainerCreationParams() {
		return new CuteElement.ContainerCreationParams(AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.ONE, true, true,
				"Variable's Value", "Variable's Value (replace it)",
				"A container of Actions which name(Value) is compared to Variable's current Value."
						+ "\nIf they are equal, the contained Actions are run.");
	}

}
