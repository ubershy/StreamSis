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
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.UserVars;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.elements.actors.Actor;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

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
 * Note: CuteProject and SisScene classes might need some refactoring. I'm not sure.
 */
public class CuteProject implements Serializable {
	
	// This class is big. To make navigation easier, it is split into sections.

	// ♥ Serializable properties ♥
	
	/**
	 * The name of this CuteProject.
	 */
	@JsonProperty("name")
	private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper("");
	public ReadOnlyStringProperty nameProperty() {return name.getReadOnlyProperty();}
	public String getName() {return name.get();}
	public void setName(String name) {this.name.set(name);}

	/**
	 * The name of primary {@link SisScene}. <br>
	 * When CuteProject starts, it starts it's primary SisScene after finding it by it's name.
	 */
	@JsonProperty("primarySisSceneName")
	private final ReadOnlyStringWrapper primarySisSceneName = new ReadOnlyStringWrapper("");
	public ReadOnlyStringProperty primarySisSceneNameProperty() {
		return primarySisSceneName.getReadOnlyProperty();
		}
	public String getPrimarySisSceneName() {return primarySisSceneName.get();}
	public void setPrimarySisSceneName(String name) {
		logger.info("Changing primary SisScene to '" + name + "'");
		primarySisSceneName.set(name);
	}

	/** All CuteProject's SisScenes. */
	@JsonProperty("sisScenes")
	private final ObservableList<SisScene> sisScenes = FXCollections.observableArrayList();
	@JsonIgnore
	private final ObservableList<SisScene> readOnlySisScenes = FXCollections
			.unmodifiableObservableList(sisScenes);

	/**
	 * Gets the unmodifiable list of all CuteProject's {@link SisScene}s.
	 *
	 * @return The unmodifiable list of all CuteProject's {@link SisScene}s.
	 */
	@JsonIgnore
	public ObservableList<SisScene> getSisScenesUnmodifiable() {
		return readOnlySisScenes;
	}

	/**
	 * All CuteProject's Actors.
	 * <p>
	 * They are not really global. They exist only in CuteProject. <br>
	 * They are called global because they do not depend on SisScenes as a user may think. <br>
	 * SisScenes don't contain Actors, they just reference to Actors by names. <br>
	 * <b>So they all must have unique names</b>.
	 */
	@JsonProperty("globalActors")
	private final ObservableList<Actor> globalActors = FXCollections.observableArrayList();
	@JsonIgnore
	private final ObservableList<Actor> readOnlyGlobalActors = FXCollections
			.unmodifiableObservableList(globalActors);
	/**
	 * Gets unmodifiable list of all CuteProject's {@link Actor}s (Global Actors).
	 *
	 * @return The unmodifiable list of all CuteProject's {@link Actor}s (Global Actors).
	 */
	@JsonIgnore
	public ObservableList<Actor> getGlobalActorsUnmodifiable() {
		return readOnlyGlobalActors;
	}
	
	/**
	 * Map with Initial Variables and their Values to set to {@link UserVars} on each CuteProject's
	 * start.
	 */
	@JsonProperty("initialUserVars")
	private final ObservableMap<String, String> initialUserVars = FXCollections
			.observableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER));
	/**
	 * Gets {@link #initialUserVars} of this CuteProject.
	 *
	 * @return {@link #initialUserVars} of this CuteProject.
	 */
	public ObservableMap<String, String> getInitialUserVars() {
		return initialUserVars;
	}
	
	// ♥ Runtime variables and properties ♥

	/**
	 * The current Actors. <br>
	 * This list updates every time current SisScene changes to another. <br>
	 * GUI shows them in the list of current Actors.
	 */
	@JsonIgnore
	private final ObservableList<Actor> currentActors = FXCollections.observableArrayList();
	@JsonIgnore
	private final ObservableList<Actor> readOnlyCurrentActors = FXCollections
			.unmodifiableObservableList(currentActors);
	/**
	 * Gets the unmodifiable list of current CuteProject's {@link Actor}s.
	 *
	 * @return The unmodifiable list of current CuteProject's {@link Actor}s.
	 */
	@JsonIgnore
	public ObservableList<Actor> getCurrentActorsUnmodifiable() {return readOnlyCurrentActors;}

	/**
	 * Current SisScene name <br>
	 * GUI shows only Actors of CuteProject's current SisScene.
	 */
	@JsonIgnore
	private final ReadOnlyStringWrapper currentSisSceneName = new ReadOnlyStringWrapper("");
	public ReadOnlyStringProperty currentSisSceneNameProperty() {
		return currentSisSceneName.getReadOnlyProperty();
	}
	@JsonIgnore
	public String getCurrentSisSceneName() {return currentSisSceneName.get();}

	/**
	 * A property telling if CuteProject is started or stopped.
	 */
	@JsonIgnore
	private final ReadOnlyBooleanWrapper started = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty startedProperty() {return started.getReadOnlyProperty();}
	@JsonIgnore
	public boolean isStarted() {
		return started.get();
	}

	/**
	 * The current CuteProject's initializing (successfully or not) {@link CuteElement}. When there
	 * are no mistakes in the code, after {@link CuteProject#init()} this number should equal to the
	 * quantity of CuteElements in the whole project.
	 */
	@JsonIgnore
	private final ReadOnlyIntegerWrapper initElementsNumber = new ReadOnlyIntegerWrapper(0);
	public ReadOnlyIntegerProperty initElementsNumberProperty() {
		return initElementsNumber.getReadOnlyProperty();
	}
	@JsonIgnore
	public int getInitElementsNumber() {return initElementsNumber.get();}
	
	/**
	 * The current CuteProject's number of {@link CuteElement}s.
	 */
	@JsonIgnore
	private final ReadOnlyIntegerWrapper allElementsNumber = new ReadOnlyIntegerWrapper(0);
	public ReadOnlyIntegerProperty allElementsNumberProperty() {
		return allElementsNumber.getReadOnlyProperty();
	}
	@JsonIgnore
	public int getAllElementsNumber() {return allElementsNumber.get();}
	
	/** Tells if current CuteProject is currently initializing. */
	@JsonIgnore
	private final ReadOnlyBooleanWrapper initializing = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty initializingProperty() {
		return initializing.getReadOnlyProperty();
	}

	private static final long serialVersionUID = 0x0626132d0b091a17L;
	static final Logger logger = LoggerFactory.getLogger(CuteProject.class);

	// ♥ Constructors ♥

	/**
	 * Instantiates a new CuteProject with empty {@link #globalActors Global Actors} and
	 * {@link #sisScenes SisScenes} lists.
	 *
	 * @param name
	 *            The name of the CuteProject.
	 */
	public CuteProject(String name) {
		this(name, "New SisScene", new ArrayList<SisScene>(), new ArrayList<Actor>(),
				new TreeMap<String, String>());
	}

	/**
	 * Instantiates a new cute project the hard way. Mainly used by deserializator.
	 *
	 * @param name
	 *            The name of CuteProject
	 * @param primarySisSceneName
	 *            The primary SisScene name
	 * @param sisScenes
	 *            The list of SisScenes
	 * @param globalActors
	 *            The list of Actors
	 */
	@JsonCreator
	private CuteProject(@JsonProperty("name") String name,
			@JsonProperty("primarySisSceneName") String primarySisSceneName,
			@JsonProperty("sisScenes") ArrayList<SisScene> sisScenes,
			@JsonProperty("globalActors") ArrayList<Actor> globalActors,
			@JsonProperty("initialUserVars") Map<String, String> initialUserVars) {
		this.name.set(name);
		this.sisScenes.setAll(sisScenes);
		this.globalActors.setAll(globalActors);
		this.primarySisSceneName.set(primarySisSceneName);
		this.initialUserVars.putAll(initialUserVars);

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

	// ♥ Public methods ♥

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
	 * Initializes the CuteProject. This will also initialize all of it's Actors and SisScenes.
	 */
	public synchronized void init() {
		setStartedCountingElements();
		int count = countElementsInProject();
		setFinishedCountingElements(count);
		if (isStarted())
			stopProject();
		logger.info("Initializing Project...");
		setProjectAsInitializing();
		
		UserVars.clear();

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
		setProjectAsInitialized();
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
	public synchronized void startProject() {
		if (!isStarted()) {
			if (!sisScenes.isEmpty()) {
				if (!globalActors.isEmpty()) {
					// Lets initialize everything before starting
					init();
					UserVars.setAll(initialUserVars);
					started.set(true);
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
			UserVars.clear();
			started.set(false);
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
			if (getGlobalActorsUnmodifiable().contains(element)) {
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
	
	/**
	 * Increase the quantity of initialized (successful or not) {@link CuteElement}s during whole
	 * {@link CuteProject#init()}.
	 */
	public void incrementInitNumberOfElements() {
		if (initializing.get()) {
			initElementsNumber.set(initElementsNumber.get() + 1);
		}
	}

	/**
	 * Moves Actor up in list of {@link #globalActors}.
	 * 
	 * @param actor
	 *            The Actor to move up.
	 */
	public void moveUpActorInGlobalActors(Actor actor) {
		int index = globalActors.indexOf(actor);
		if (index != 0)
			Collections.swap(globalActors, index, index - 1);
	}

	/**
	 * Moves Actor up down list of {@link #globalActors}.
	 * 
	 * @param actor
	 *            The Actor to move down.
	 */
	public void moveDownActorInGlobalActors(Actor actor) {
		int index = globalActors.indexOf(actor);
		if (globalActors.size() != index + 1)
			Collections.swap(globalActors, index, index + 1);
	}
	
	// ♥ Private methods ♥

	/**
	 * Sets the current {@link CuteProject} as currently counting {@link CuteElement}s inside. <br>
	 * Internally it resets {@link #allElementsNumber} to "0".
	 */
	private void setStartedCountingElements() {
		allElementsNumber.set(0);
	}

	/**
	 * Sets the current {@link CuteProject} as finished counting {@link CuteElement}s inside. <br>
	 *
	 * @param count The number of counted CuteElements.
	 */
	private void setFinishedCountingElements(int count) {
		allElementsNumber.set(count);
	}
	
	/**
	 * Sets the current {@link CuteProject} as currently initializing.
	 */
	private void setProjectAsInitializing() {
		initializing.set(true);
		initElementsNumber.set(0);
	}

	/**
	 * Sets the current {@link CuteProject} as initialized.
	 */
	private void setProjectAsInitialized() {
		initializing.set(false);
		int all = allElementsNumber.get();
		int inited = initElementsNumber.get();
		if (inited != all) {
			throw new RuntimeException("The number of initialized Elements(" + inited + ") doesn't "
					+ "match with the " + "number of all Elements(" + all + ") in the Project.");
		}
		logger.info("Project Initialized");
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
	 * Counts the {@link CuteElement}s in CuteProject.
	 *
	 * @return The number of CuteElements in this CuteProject.
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
	 * Checks and fixes current and primary {@link SisScene}s:<br>
	 * 1. If primary SisScene is missing or undefined, sets primary SisScene to the first SisScene
	 * in {@link #sisScenes}. <br>
	 * 2. If current SisScene is missing or undefined, sets it to the primary SisScene.
	 */
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

}
