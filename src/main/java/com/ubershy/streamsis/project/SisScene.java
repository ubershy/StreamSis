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
package com.ubershy.streamsis.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * {@link CuteProject} uses this class as a container of name references to Actors.
 * <p>
 * <i>I think this class is not really that cute. <br>
 * Maybe it can be substituted with a ObservableMap of SisScene names as keys and lists of Actor
 * names as values in {@link CuteProject}... <br>
 * The additional ObservableList will be required then to maintain the order of SisScene
 * names. </i>
 */
public class SisScene implements CuteElement {

	static final Logger logger = LoggerFactory.getLogger(SisScene.class);

	/** The SisScene's list of Actor names */
	@JsonProperty
	private ObservableList<String> actorNames = FXCollections.observableArrayList();

	/** The SisScene's {@link ElementInfo}. */
	@JsonProperty
	private ElementInfo elementInfo = new ElementInfo(this);

	/**
	 * Instantiates a new SisScene
	 *
	 * @param name
	 *            the SisScene's name
	 * @param actorNames
	 *            the List with Actor names
	 */
	@JsonCreator
	public SisScene(@JsonProperty("name") String name,
			@JsonProperty("actorNames") ArrayList<String> actorNames) {
		this.elementInfo.setName(name);
		this.actorNames.setAll(actorNames);
	}

	/**
	 * Instantiates a new SisScene, but uses Array parameter instead list for Actor names
	 *
	 * @param name
	 *            the SisScene's name
	 * @param actorNames
	 *            the Array with Actor names
	 */
	public SisScene(String name, String[] actorNames) {
		this(name, new ArrayList<String>(Arrays.asList(actorNames)));
	}

	/**
	 * Adds the name of Actor to SisScene Actor name list.
	 *
	 * @param actorName
	 *            the name of Actor
	 * @throws IllegalArgumentException
	 */
	public void addActorName(String actorName) {
		if (actorNames.contains(actorName)) {
			throw new IllegalArgumentException("Actor with name '" + actorName
					+ "' already exists in SisScene '" + elementInfo.getName() + "'");
		}
		actorNames.add(actorName);
	}

	/**
	 * Gets the SisScene's list of Actor names
	 *
	 * @return the list with Actor names
	 */
	public ObservableList<String> getActorNames() {
		return actorNames;
	}

	@Override
	public ElementInfo getElementInfo() {
		return elementInfo;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if ("".equals(elementInfo.getName())) {
			elementInfo.setAsBroken("SisScene's name cannot be empty");
			return;
		}
		if (actorNames.contains(null)) {
			elementInfo.setAsBroken("SisScene can't have null in the list of Actor names");
			return;
		}
		if (!actorNames.isEmpty()) {
			HashSet<String> uniqueNames = new HashSet<String>(actorNames);
			if (actorNames.size() != uniqueNames.size()) {
				elementInfo.setAsBroken("SisScene can't have duplicate Actor names");
			}
		}
	}

	/**
	 * Moves down Actor name in SisScene Actor name list. Only useful for GUI.
	 * 
	 * @param actorName
	 *            the name of Actor
	 */
	public void moveDownActorName(String actorName) {
		int index = actorNames.indexOf(actorName);
		if (actorNames.size() != index + 1)
			Collections.swap(actorNames, index, index + 1);
	}

	/**
	 * Moves up Actor name in SisScene Actor name list. Only useful for GUI.
	 *
	 * @param actorName
	 *            the name of Actor
	 */
	public void moveUpActorName(String actorName) {
		int index = actorNames.indexOf(actorName);
		if (index != 0)
			Collections.swap(actorNames, index, index - 1);
	}

	/**
	 * Gets the name property of SisScene.
	 *
	 * @return the string name property
	 */
	@Deprecated
	public ReadOnlyStringProperty nameProperty() {
		return elementInfo.nameProperty();
	}

	/**
	 * Removes the Actor name from SisScene Actor name list.
	 *
	 * @param actorName
	 *            the name of Actor
	 */
	public void removeActorName(String actorName) {
		actorNames.remove(actorName);
	}

	/**
	 * Rename Actor in SisScene
	 *
	 * @param oldName
	 *            the existing name of Actor in this SisScene
	 * @param newName
	 *            the new name for this Actor
	 * 
	 * @throws NullPointerException
	 *             if null parameter detected
	 * @throws IllegalArgumentException
	 *             if SisScene don't have existing Actor name <br>
	 *             or SisScene already have Actor with new name
	 */
	public void renameActor(String oldName, String newName) {
		if (oldName == null || newName == null) {
			throw new NullPointerException();
		}
		int indexToRename = actorNames.indexOf(oldName);
		if (indexToRename != -1) {
			if (!actorNames.contains(newName)) {
				actorNames.set(indexToRename, newName);
			} else {
				throw new IllegalArgumentException(
						"Can't apply new name. Such Actor name already exists in SisScene");
			}
		} else {
			throw new IllegalArgumentException("SisScene don't have such Actor name");
		}
	}

	@Override
	public String toString() {
		return elementInfo.getName();
	}

}
