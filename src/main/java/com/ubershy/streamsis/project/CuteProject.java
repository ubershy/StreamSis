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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.actors.Actor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * CuteProject aka CProject is a Project class for StreamSis program.
 * <p>
 * It has lists of {@link #globalActors Global Actors} and {@link #sisScenes SisScenes}. <br>
 * <p>
 * CuteProject can be started by {@link #startProject()} method. <br>
 * When CuteProject starts working, it sets it's {@link #currentSisSceneName Current SisScene} to
 * {@link #primarySisSceneName Primary SisScene}. <br>
 * Then it starts Current SisScene's Actors.
 * <p>
 * Only Actors from Current SisScene are showing in GUI main window and can be edited. <br>
 * Current SisScene and associated Actors can be switched in runtime by
 * {@link #switchSisSceneTo(String)} method. <br>
 * If Project is working while switching Current SisScene, associated Actors will start
 * automatically.
 * <p>
 * Note: CuteProject and SisScene classes might need some refactoring. I don't sure.
 * 
 */
public class CuteProject implements Serializable {

	private static final long serialVersionUID = 0x0626132d0b091a17L;

	static final Logger logger = LoggerFactory.getLogger(CuteProject.class);

	/**
	 * The current Actors. <br>
	 * This list updates every time current SisScene changes. <br>
	 * GUI shows them in list of current Actors.
	 */
	@JsonIgnore
	private ObservableList<Actor> currentActors = FXCollections.observableArrayList();

	/**
	 * Current SisScene name <br>
	 * GUI shows only Actors of CuteProject's current SisScene.
	 */
	@JsonIgnore
	private StringProperty currentSisSceneName = new SimpleStringProperty();

	/** All CuteProject's SisScenes. */
	@JsonProperty
	private ObservableList<SisScene> sisScenes = FXCollections.observableArrayList();;

	/** The name of primary SisScene. */
	@JsonIgnore
	private StringProperty primarySisSceneName = new SimpleStringProperty();

	/**
	 * All CuteProject's Actors.
	 * <p>
	 * They are not really global. They exist only in CuteProject. <br>
	 * They are called global because they do not depend on SisScenes as a user may think. <br>
	 * SisScenes don't contain Actors, they just reference to Actors by names. <br>
	 * <b>So they all must have unique names</b>.
	 */
	@JsonProperty
	private ObservableList<Actor> globalActors = FXCollections.observableArrayList();

	/** A property telling if CuteProject is started or stopped. */
	@JsonIgnore
	private BooleanProperty isStarted = new SimpleBooleanProperty(false);

	/** The name of CuteProject */
	@JsonIgnore
	private StringProperty name = new SimpleStringProperty();

	/**
	 * Instantiates a new CuteProject with empty {@link #globalActors Global Actors} and
	 * {@link #sisScenes SisScenes} lists.
	 *
	 * @param name
	 *            the name of CuteProject
	 */
	public CuteProject(String name) {
		this(name, "New SisScene", new ArrayList<SisScene>(), new ArrayList<Actor>());
	}

	/**
	 * Instantiates a new cute project the hard way. Mainly used by deserializator.
	 *
	 * @param name
	 *            the name of CuteProject
	 * @param primarySisSceneName
	 *            the primary SisScene name
	 * @param sisScenes
	 *            the list of SisScenes
	 * @param globalActors
	 *            the list of Actors
	 */
	@JsonCreator
	private CuteProject(@JsonProperty("name") String name,
			@JsonProperty("primarySisSceneName") String primarySisSceneName,
			@JsonProperty("sisScenes") ArrayList<SisScene> sisScenes,
			@JsonProperty("globalActors") ArrayList<Actor> globalActors) {
		this.name.set(name);
		this.sisScenes.setAll(sisScenes);
		this.globalActors.setAll(globalActors);
		this.primarySisSceneName.set(primarySisSceneName);

		this.sisScenes.addListener((ListChangeListener.Change<? extends SisScene> c) -> {
			// For safety lets stop project.
			if (isStarted())
				stopProject();
			checkAndFixCurrentAndPrimarySisScenes();
		});
		this.globalActors.addListener((ListChangeListener.Change<? extends Actor> c) -> {
			// For safety lets stop project on each change
			if (isStarted())
				stopProject();
		});
	}

	private void checkAndFixCurrentAndPrimarySisScenes() {
		// if some SisScenes still left in list
		if (sisScenes.size() != 0) {
			// If we can't find primary SisScene, we will set as primary the first SisScene in
			// list
			if (getSisSceneByName(getPrimarySisSceneName()) == null) {
				if (sisScenes.get(0) != null)
					setPrimarySisSceneName(sisScenes.get(0).getElementInfo().getName());
			}
			// If there's no current SisScene we will switch to the primary SisScene
			if (currentSisSceneName.get() != null) {
				if (getSisSceneByName(currentSisSceneName.get()) == null) {
					switchSisSceneTo(getPrimarySisSceneName());
				}
			} else {
				switchSisSceneTo(getPrimarySisSceneName());
			}
		} else {
			currentActors.clear();
		}
	}

	/**
	 * Add the Actor to the list of CuteProject's Global Actors.
	 *
	 * @param actor
	 *            the Actor to add
	 */
	public void addActorToGlobalActors(Actor actor) {
		logger.info("Adding Actor '" + actor.getElementInfo().getName() + "' to Global Actors");
		globalActors.add(actor);
	}

	/**
	 * Add the SisScene to this CuteProject.
	 *
	 * @param sisScene
	 *            the SisScene to add
	 */
	public void addSisScene(SisScene sisScene) {
		sisScenes.add(sisScene);
		logger.info("Added SisScene '" + sisScene.getElementInfo().getName() + "'");
	}

	/**
	 * Adds the Actor existing in CuteProject's Global Actors to the current SisScene.
	 *
	 * @param actor
	 *            the Actor to add
	 */
	public void addExistingActorToCurrentSisScene(Actor actor) {
		if (isStarted())
			stopProject();
		logger.info("Adding existing Actor '" + actor.getElementInfo().getName()
				+ "' to current SisScene");
		if (globalActors.contains(actor)) {
			SisScene currentSisScene = getSisSceneByName(getCurrentSisSceneName());
			if (currentSisScene != null) {
				currentSisScene.addActorName(actor.getElementInfo().getName());
				currentActors.add(actor);
			} else {
				logger.error("Current SisScene is null o_O");
			}
		} else {
			logger.error("There's no such Actor '" + actor.getElementInfo().getName()
					+ "' in Global Actors. Not adding to current SisScene.");
		}
	}

	/**
	 * Gets the current SisScene's name property.
	 *
	 * @return current SisScene's name property
	 */
	public StringProperty currentSisSceneNameProperty() {
		return currentSisSceneName;
	}

	/**
	 * Primary SisScene name property. <br>
	 * When CuteProject starts, it starts it's primary SisScene after finding it by it's name.
	 *
	 * @return the string property
	 */
	public StringProperty primarySisSceneNameProperty() {
		return primarySisSceneName;
	}

	/**
	 * Gets the Actor by name.
	 *
	 * @param name
	 *            the Actor's name
	 * @return Actor, <br>
	 *         null if name parameter is null or Actor not found by name
	 */
	public Actor getActorByName(String name) {
		if (name == null)
			return null;
		Actor result = null;
		for (Actor t : globalActors) {
			if (t.getElementInfo().getName().equals(name)) {
				result = t;
			}
		}
		return result;
	}

	/**
	 * Gets the list of current Actors.
	 *
	 * @return the list of current Actors
	 */
	public ObservableList<Actor> getCurrentActors() {
		return currentActors;
	}

	/**
	 * Gets the current SisScene's name.
	 *
	 * @return current SisScene's name
	 */
	public String getCurrentSisSceneName() {
		return currentSisSceneName.get();
	}

	/**
	 * Gets the SisScene by name.
	 *
	 * @param name
	 *            the SisScene's name
	 * @return the SisScene, <br>
	 *         null if name parameter is null or SisScene not found by name
	 */
	public SisScene getSisSceneByName(String name) {
		if (name == null)
			return null;
		SisScene result = null;
		for (SisScene t : sisScenes) {
			if (t.getElementInfo().getName().equals(name)) {
				result = t;
			}
		}
		return result;
	}

	/**
	 * Gets CuteProject's SisScenes.
	 *
	 * @return the list of SisScenes
	 */
	public ObservableList<SisScene> getSisScenes() {
		return sisScenes;
	}

	/**
	 * Gets the name of CuteProject's primary SisScene. <br>
	 * When CuteProject starts, it starts it's primary SisScene after finding it by it's name.
	 *
	 * @return the name of SisScene
	 */
	@JsonProperty("primarySisSceneName")
	public String getPrimarySisSceneName() {
		return primarySisSceneName.get();
	}

	/**
	 * Gets this CuteProject's Global Actors.
	 *
	 * @return the list of Actors
	 */
	public ObservableList<Actor> getGlobalActors() {
		return globalActors;
	}

	/**
	 * Gets this CuteProject's name.
	 *
	 * @return name of the CuteProject
	 * 
	 */
	@JsonProperty("name")
	public String getName() {
		return name.get();
	}

	/**
	 * Initializes the CuteProject. This will also initialize all of it's Actors and SisScenes.
	 */
	public void init() {
		int count = countElementsInProject();
		ProjectManager.setAllElementsNumber(count);
		logger.info("Initializing Project...");
		ProjectManager.setCurrentProjectAsInitializing();
		if (isStarted())
			stopProject();

		checkAndFixCurrentAndPrimarySisScenes();

		validateListOfActorsOrSisScenes(globalActors);
		validateListOfActorsOrSisScenes(sisScenes);

		// Lets init all SisScenes
		for (SisScene scene : sisScenes) {
			scene.init();
			if (scene.getElementInfo().isBroken()) {
				throw new RuntimeException("CuteProject can't have broken SisScene:\n"
						+ scene.getElementInfo().getUnhealthyMessage());
			}
		}

		// Lets init all Actors
		for (Actor actor : globalActors) {
			actor.init();
		}
		if (getSisSceneByName(getPrimarySisSceneName()) == null) {
			String firstSisSceneName = this.sisScenes.get(0).getElementInfo().getName();
			setPrimarySisSceneName(firstSisSceneName);
		}
		logger.info("Project Initialized");
		ProjectManager.setCurrentProjectAsInitialized();
	}

	/**
	 * @return The number of CuteElements in this Project.
	 */
	private int countElementsInProject() {
		logger.info("Counting all Elements in Project...");
		int count = 0;
		for (SisScene scene : sisScenes) {
			if (scene.getChildren() != null) {
				// For the future...
				throw new RuntimeException("As scene can have children now, the counting code "
						+ "needs be changed.");
			}
			count++;
		}
		for (Actor actor : globalActors) {
			count+=actor.countActorAndChildrenRecursivelyWithoutContainersOnTop();
		}
		logger.info("Counted all Elements in Project: " + count);
		return count;
	}
	
	/**
	 * Validates list with Actors/SisScenes. <br>
	 * Throws exceptions if something is wrong telling programmer about errors he made.
	 *
	 * @param list
	 *            the list with Actors or SisScenes
	 */
	private <T> void validateListOfActorsOrSisScenes(ObservableList<T> list) {
		if (list.isEmpty()) {
			return;
		}
		if (list.contains(null)) {
			throw new RuntimeException("One of the elements in list is null");
		}
		String elementType = null;
		Object firstElement = list.get(0);
		if (firstElement instanceof Actor) {
			elementType = "Actor";
		}
		if (firstElement instanceof SisScene) {
			elementType = "SisScene";
		}
		if (elementType == null) {
			throw new RuntimeException("This method only checks lists of Actors or SisScenes");
		}

		ArrayList<String> names = new ArrayList<String>();
		for (T element : list) {
			String name = ((CuteElement) element).getElementInfo().getName();
			if (name != null) {
				names.add(name);
			} else {
				throw new RuntimeException(elementType + " has null name");
			}
		}
		HashSet<String> uniqueNames = new HashSet<String>(names);
		if (names.size() != uniqueNames.size()) {
			throw new RuntimeException("Some of " + elementType + "s don't have unique names");
		}
	}

	/**
	 * Checks if CuteProject is started.
	 *
	 * @return true, if CuteProject is started
	 */
	@JsonIgnore
	public boolean isStarted() {
		return isStarted.get();
	}

	/**
	 * Gets the CuteProject isStarted property.
	 *
	 * @return the boolean property isStarted
	 */
	@JsonIgnore
	public BooleanProperty isStartedProperty() {
		return isStarted;
	}

	/**
	 * Moves down the Actor in CuteProject's current SisScene. <br>
	 * Can be useful only for GUI.
	 *
	 * @param actor
	 *            the Actor to move
	 */
	public void moveDownActorInSisScene(Actor actor) {
		int index = currentActors.indexOf(actor);
		if (currentActors.size() != index + 1)
			Collections.swap(currentActors, index, index + 1);
		getSisSceneByName(currentSisSceneName.get())
				.moveDownActorName(actor.getElementInfo().getName());
	}

	/**
	 * Moves down the SisScene in CuteProject's list of SisScenes. <br>
	 * Can be useful only for GUI.
	 *
	 * @param sisScene
	 *            the SisScene to move
	 */
	public void moveDownSisScene(SisScene sisScene) {
		int index = sisScenes.indexOf(sisScene);
		if (sisScenes.size() != index + 1)
			Collections.swap(sisScenes, index, index + 1);
	}

	/**
	 * Moves up the Actor in CuteProject's current SisScene. <br>
	 * Can be useful only for GUI.
	 *
	 * @param actor
	 *            the Actor to move
	 */
	public void moveUpActorInSisScene(Actor actor) {
		int index = currentActors.indexOf(actor);
		if (index != 0)
			Collections.swap(currentActors, index, index - 1);
		getSisSceneByName(currentSisSceneName.get())
				.moveUpActorName(actor.getElementInfo().getName());
	}

	/**
	 * Moves up the SisScene in CuteProject's list of SisScenes. <br>
	 * Can be useful only for GUI.
	 *
	 * @param sisScene
	 *            the SisScene to move
	 */
	public void moveUpSisScene(SisScene sisScene) {
		int index = sisScenes.indexOf(sisScene);
		if (index != 0)
			Collections.swap(sisScenes, index, index - 1);
	}

	/**
	 * Prints the list of CuteProject's SisScenes to logger.
	 */
	public void printAllSisScenes() {
		logger.info("All project's SisScenes: ");
		for (SisScene t : sisScenes) {
			logger.info("SisScene: " + t.getElementInfo().getName());
		}
	}

	/**
	 * Removes the Actor only from current SisScene. <br>
	 * Actor remains in Global Actors.
	 *
	 * @param actor
	 *            the Actor to remove from current SisScene
	 */
	public void removeActorFromCurrentSisScene(Actor actor) {
		logger.info("Removing actor: '" + actor.getElementInfo().getName() + "' from SisScene: '"
				+ getCurrentSisSceneName() + "'");
		if (isStarted())
			stopProject();
		SisScene currentSisScene = getSisSceneByName(getCurrentSisSceneName());
		if (currentSisScene != null) {
			currentSisScene.removeActorName(actor.getElementInfo().getName());
		}
		if (currentActors.contains(actor)) {
			currentActors.remove(actor);
		}
	}

	/**
	 * Removes Actor globally. <br>
	 * I.e. from list of current SisScene, list of current Actors and list of Global Actors.
	 * <p>
	 * Actor names remain in all other SisScenes, but they will eventually clean up by
	 * {@link #switchSisSceneTo(String)} method.
	 *
	 * @param actor
	 *            the Actor to banish forever
	 */
	public void removeActorGlobally(Actor actor) {
		logger.info("Removing actor: '" + actor.getElementInfo().getName() + "' globally");
		SisScene currentSisScene = getSisSceneByName(getCurrentSisSceneName());
		if (currentSisScene != null) {
			currentSisScene.removeActorName(actor.getElementInfo().getName());
		}
		if (currentActors.contains(actor)) {
			currentActors.remove(actor);
		}
		if (globalActors.contains(actor)) {
			globalActors.remove(actor);
		}
	}

	/**
	 * Removes the SisScene from CuteProject.
	 *
	 * @param sisScene
	 *            the SisScene to remove
	 */
	public void removeSisScene(SisScene sisScene) {
		logger.info("Removing SisScene: '" + sisScene.getElementInfo().getName() + "'");
		sisScenes.remove(sisScene);
	}

	/**
	 * Removes the SisScene from CuteProject and also globally removes Actors associated with it.
	 *
	 * @param sisScene
	 *            the SisScene to purge
	 */
	public void removeSisSceneWithActors(SisScene sisScene) {
		// Lets create a new list to avoid ConcurrentModificationException()
		ArrayList<String> actorsToRemove = new ArrayList<String>(sisScene.getActorNames());
		for (String actorName : actorsToRemove) {
			removeActorGlobally(getActorByName(actorName));
		}
		removeSisScene(sisScene);
		switchSisSceneTo(getPrimarySisSceneName());
	}

	/**
	 * Sets the CuteProject primary SisScene by it's name. <br>
	 * When CuteProject starts, it starts it's primary SisScene after finding it by it's name.
	 *
	 * @param name
	 *            the name of SisScene you want to make primary
	 */
	public void setPrimarySisSceneName(String name) {
		logger.info("Changing primary SisScene to '" + name + "'");
		primarySisSceneName.set(name);
	}

	/**
	 * Sets this CuteProject's name.
	 *
	 * @param name
	 *            the new CuteProject's name
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * Starts the chosen Actor. <br>
	 * If Actor is already started, nothing will happen.
	 *
	 * @param actor
	 *            the Actor by himself
	 */
	public void startActor(Actor actor) {
		actor.start();
		logger.info("\tActor working: '" + actor.getElementInfo().getName() + "'");
	}

	/**
	 * Starts this CuteProject.
	 * <p>
	 * Also automatically performs {@link #init()}
	 */
	public void startProject() {
		if (!isStarted()) {
			if (!sisScenes.isEmpty()) {
				if (!globalActors.isEmpty()) {
					// Lets initialize everything before starting
					init();
					isStarted.set(true);
					logger.info("Project '" + getName() + "' started");
					switchSisSceneTo(getPrimarySisSceneName());
				} else {
					logger.error("Can't start Project: the list of global Actors is empty");
					throw new RuntimeException("globalActors are empty");
				}
			} else {
				logger.error("Can't start Project: the list of SisScenes is empty");
				throw new RuntimeException("sisScenes are empty");
			}
		}
	}

	/**
	 * Stops the chosen Actor. <br>
	 * If Actor is already stopped, nothing will happen.
	 * 
	 * @param actor
	 *            the Actor by himself
	 */
	public void stopActor(Actor actor) {
		actor.stop();
	}

	/**
	 * Stops all Actors in the currentActors list.
	 */
	public void stopCurrentActors() {
		stopChosenActors(currentActors);
	}

	/**
	 * Stops chosen Actors in the provided list.
	 */
	public void stopChosenActors(List<Actor> list) {
		if (!list.isEmpty()) {
			logger.info("Stopping Actors... ");
			for (Actor actor : list) {
				stopActor(actor);
				logger.info("\tActor stopped: '" + actor.getElementInfo().getName() + "'");
			}
		}
	}

	/**
	 * Stops this CuteProject.
	 */
	public void stopProject() {
		if (isStarted()) {
			stopCurrentActors();
			isStarted.set(false);
			logger.info("Project '" + getName() + "' stopped");
		}
	}

	/**
	 * Sets the current SisScene and starts corresponding Actors if Project is running. <br>
	 * Also cleans SisScenes from non-existing Actors.
	 *
	 * @param switchToSisSceneName
	 *            the name of SisScene to <i>which</i> we want to <i>switch</i>
	 */
	public final void switchSisSceneTo(String switchToSisSceneName) {

		SisScene switchToSisScene = getSisSceneByName(switchToSisSceneName);
		if (switchToSisScene == null || switchToSisSceneName.isEmpty())
			return;
		boolean switched = false;
		if (switchToSisSceneName.equals(getCurrentSisSceneName())) {
			logger.info("Not switching to the same SisScene");
			switched = false;
		} else {
			logger.info("Switching SisScene from: '" + getCurrentSisSceneName() + "' to: '"
					+ switchToSisSceneName + "'");
			currentSisSceneName.set(switchToSisSceneName);
			switched = true;
		}

		synchronized (currentActors) {
			if (switched) {
				// We will clean current SisScene from non-existing Actors
				// Also here we are filling the array of Actors to run. Working directly with
				// currentActors is bad idea.
				ArrayList<Actor> actorsThatNeedToBeRunning = new ArrayList<Actor>();
				ArrayList<String> actorsToDeleteFromSisScene = new ArrayList<String>();
				for (String actorName : switchToSisScene.getActorNames()) {
					Actor actor = getActorByName(actorName);
					if (actor == null) {
						actorsToDeleteFromSisScene.add(actorName);
					} else {
						actorsThatNeedToBeRunning.add(actor);
					}
				}
				for (String actorName : actorsToDeleteFromSisScene) {
					switchToSisScene.getActorNames().remove(actorName);
					logger.debug("Deleting from SisScene '" + switchToSisSceneName
							+ "' the non-existing Actor name: '" + actorName + "'");
				}

				// Lets find all Actors that need to be stopped
				// Firstly lets get list of currentActors
				ArrayList<Actor> actorsThatNeedToBeStopped = new ArrayList<Actor>(currentActors);
				// Finally subtraction will help us to find Actors to stop
				actorsThatNeedToBeStopped.removeAll(actorsThatNeedToBeRunning);

				if (isStarted())
					stopChosenActors(actorsThatNeedToBeStopped);
				currentActors.setAll(actorsThatNeedToBeRunning);
			}

			if (isStarted()) {
				logger.info("Executing SisScene '" + switchToSisSceneName + "' ("
						+ currentActors.size() + " Actors):");
				for (Actor actor : currentActors) {
					startActor(actor);
				}
			}
		}
	}

	/**
	 * Rename {@link CuteElement} safely. <br>
	 * Some CuteElements such as Actors and SisScenes must have non-null and unique names inside
	 * CuteProject. <br>
	 * Also renaming Actor that is inside CuteProject is complicated thing: SisScene that contains
	 * name of Actor must be edited too to reflect changes. <br>
	 * This method will respect such things.
	 *
	 * @param element
	 *            the CuteElement which name to set
	 * @param newName
	 *            the new name for CuteElement
	 * 
	 * @throws IllegalArgumentException
	 *             if CuteElement already has such name <br>
	 *             <b>or</b> type of CuteElement is Actor or SisScene <b>and</b> it's
	 *             null/empty/non-unique
	 * @throws NullPointerException
	 *             if CuteElement is null
	 * 
	 */
	public void setCuteElementNameSafely(CuteElement element, String newName) {
		logger.debug("Safely setting name for CuteElement: " + newName);
		if (element == null) {
			throw new NullPointerException();
		}
		String currentElementName = element.getElementInfo().getName();
		if (currentElementName.equals(newName)) {
			// Nothing to do
			return;
		}
		if (element instanceof Actor) {
			if (newName == null || newName.isEmpty()) {
				throw new IllegalArgumentException("Actor's new name can't be null or empty");
			}
			if (getGlobalActors().contains(element)) {
				if (getActorByName(newName) != null) {
					throw new IllegalArgumentException("Can't set new name for Actor. "
							+ "The Actor with such name already exists in CuteProject");
				}
				for (SisScene scene : sisScenes) {
					if (scene.getActorNames().contains(currentElementName)) {
						scene.renameActor(currentElementName, newName);
					}
				}
			}
		}
		if (element instanceof SisScene) {
			if (newName == null || newName.isEmpty()) {
				throw new IllegalArgumentException("SisScene's new name can't be null or empty");
			}
			if (sisScenes.contains(element)) {
				if (getSisSceneByName(newName) != null) {
					throw new IllegalArgumentException("Can't set new name for SisScene. "
							+ "The SisScene with such name already exists in CuteProject");
				}
			}
		}
		// Finally lets edit ElementInfo
		element.getElementInfo().setName(newName);
	}

}
