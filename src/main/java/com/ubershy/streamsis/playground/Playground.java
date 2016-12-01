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
package com.ubershy.streamsis.playground;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.LowLevel;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.actions.DelayedActions;
import com.ubershy.streamsis.elements.actions.MultiFileCopyAction;
import com.ubershy.streamsis.elements.actions.MultiSoundAction;
import com.ubershy.streamsis.elements.actions.RunProgramAction;
import com.ubershy.streamsis.elements.actions.SwitchSPSceneAction;
import com.ubershy.streamsis.elements.actions.SwitchSisSceneAction;
import com.ubershy.streamsis.elements.actions.VariableSetterAction;
import com.ubershy.streamsis.elements.actions.VariableSwitchAction;
import com.ubershy.streamsis.elements.actors.Actor;
import com.ubershy.streamsis.elements.actors.ActorBuilder;
import com.ubershy.streamsis.elements.actors.UniversalActor;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.checkers.Coordinates;
import com.ubershy.streamsis.elements.checkers.LogicalChecker;
import com.ubershy.streamsis.elements.checkers.RegionChecker;
import com.ubershy.streamsis.elements.checkers.RelationToPreviousNumberChecker;
import com.ubershy.streamsis.elements.checkers.AbstractRelationToNumberChecker.BooleanNumberOperator;
import com.ubershy.streamsis.elements.counters.TrueCheckerCounter;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectSerializator;

import javafx.collections.ObservableList;

/**
 * The playground for testing things and generating hardcoded Projects.<br>
 * Will be removed after StreamSis GUI will be finished.
 */
public final class Playground {

	static final Logger logger = LoggerFactory.getLogger(Playground.class);

	public static void testNewMTRegionChecker(int numOfTimes, long delay) {
		// The path where structurized resources needed for project generation are located.
		String resourcesLocation = "D:\\1SRC\\StreamSisProjects\\CSGO\\";
		ActorBuilder actorBuilder = new ActorBuilder(resourcesLocation);
		Checker checker = actorBuilder.createChecker("Kill", new Coordinates(1400, 103, 338, 33),
				false, 0.65f);
		checker.init();

		long startTime = System.currentTimeMillis();
		for (int i = 0; i <= numOfTimes; i++) {
			logger.info("iteration: " + i + " of " + numOfTimes);
			checker.check();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long wholeDeltaTime = System.currentTimeMillis() - startTime;

		logger.info(numOfTimes + " iterations processed " + wholeDeltaTime + " ms");
		logger.info("Average time of execution = " + (wholeDeltaTime / numOfTimes));
	}

	/**
	 * Generate small hardcoded 'TestProject' project and save it to project file.
	 */
	public static void generateHardcodedTestProject() {

		// The path where structurized resources needed for project generation are located.
		String resourcesLocation = "D:\\1SRC\\StreamSisProjects\\CSGO\\";

		if (!Util.checkDirectory(resourcesLocation)) {
			logger.error("Programmer. You can't generate 'TestProject' hardcoded project.\n"
					+ "The directory with resource files not found:\n" + resourcesLocation);
			return;
		}

		logger.info("Generating hardcoded project 'TestProject' based on resources located in:\n"
				+ resourcesLocation);

		CuteProject project = new CuteProject("TestProject");

		UniversalActor uniTestActor = new UniversalActor("UniTestActor", 1000, 500, false, false);

		// String closeWindow = new KeyCodeCombination(KeyCode.F4,
		// KeyCombination.ALT_DOWN).getName();

		// uniTestActor.addOnAction(new SwitchSisSceneAction("testSisScene"));
		uniTestActor.addOnAction(
				new MultiSoundAction(resourcesLocation + "UniTest\\Sounds\\", 1.0, true));
		// uniTestActor.addOnAction(new HotkeyAction(closeWindow));
		uniTestActor.addOnAction(new RunProgramAction("C:\\Windows\\System32\\cmd.exe",
				"/C \"" + resourcesLocation + "Scripts\\curl_lamp_set_hue.bat\" " + "307", "",
				false));
		uniTestActor.addOffAction(new RunProgramAction("C:\\Windows\\System32\\cmd.exe",
				"/C \"" + resourcesLocation + "Scripts\\curl_lamp_set_hue.bat\" " + "180", "",
				false));
		Checker checker1 = new RegionChecker(new Coordinates(1320, 480, 600, 600),
				resourcesLocation + "UniTest\\Targets\\transparencytest.png", 0.999f);
		// LogicalChecker uniChecker = LogicalChecker.createOr(new Checker[] { checker1 });
		uniTestActor.setChecker(checker1);

		project.addActorToGlobalActors(uniTestActor);
		
		setRandomNamesForCheckersActionsCountersInProject(project);

		String[] testActors = new String[] { "UniTestActor" };
		SisScene testSisScene = new SisScene("testSisScene", testActors);
		project.addSisScene(testSisScene);
		project.setPrimarySisSceneName("testSisScene");

		String pathWhereToSave = LowLevel.getAppDataPath() + "generatedTestProjects"
				+ File.separator + "testProjectSerialized.streamsis";

		try {
			ProjectSerializator.serializeToFile(project, pathWhereToSave);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate big hardcoded test project for CS:GO and save it to project file.
	 * <p>
	 * Internally it uses {@link ActorBuilder} to generate Actors, Checkers and Actions. <br>
	 * So if resources in {@link #resourcesLocation} are missing, some essential Checkers/Actions
	 * will not be created.
	 */
	public static void generateHardcodedDefaultProject() {
		
		// The path where structurized resources needed for project generation are located.
		String resourcesLocation = "D:\\1SRC\\StreamSisProjects\\CSGO\\";

		if (!Util.checkDirectory(resourcesLocation)) {
			logger.error("Programmer. You can't generate 'Default' hardcoded project.\n"
					+ "The directory with resource files not found:\n" + resourcesLocation);
			return;
		}

		CuteProject project = new CuteProject("Default");

		int defaultRepeatInterval = 1000;

		ActorBuilder actorBuilder = new ActorBuilder(resourcesLocation);
		logger.info("Generating hardcoded project 'Default' based on resources located in:\n"
				+ resourcesLocation
				+ "\nIf you see too many messages about something broken in console (>35) then"
				+ " it probably means you don't have required resource files for generating."
				+ "\nIf you don't see so many, everything is okay. ;)");

		// These are names of scenes in streaming program.
		String SPMenuSceneName = "CSGO Menu Scene";
		String SPLobbySceneName = "CSGO Lobby Scene";
		String SPMatchSceneName = "CSGO Match Scene";
		String SPDeathMatchSceneName = "CSGO DeathMatch Scene";
		String SPSniperSceneName = "CSGO SniperScope Scene";
		String SPIkaMusumeSceneName = "CSGO IkaMusume Scene";
		String SPMessingAroundSceneName = "MessingAround Scene";

		UniversalActor mvpActor = actorBuilder.createUniversalActor("MVP", 2000,
				defaultRepeatInterval, false, false, new Coordinates(563, 158, 80, 80), 0.7f, true,
				false, true);
		UniversalActor deathActor = actorBuilder.createUniversalActor("Death", 1000,
				defaultRepeatInterval, false, false, new Coordinates(1740, 72, 178, 176), 0.75f,
				false, false, true);
		UniversalActor winTActor = actorBuilder.createUniversalActor("WinT", 2000,
				defaultRepeatInterval, false, false, new Coordinates(1280, 165, 70, 70), 0.75f,
				true, false, true);
		UniversalActor winCTActor = actorBuilder.createUniversalActor("WinCT", 2000,
				defaultRepeatInterval, false, false, new Coordinates(1280, 165, 70, 70), 0.75f,
				true, false, true);
		UniversalActor lostTActor = actorBuilder.createUniversalActor("LostT", 2000,
				defaultRepeatInterval, false, false, new Coordinates(1280, 165, 70, 70), 0.75f,
				true, false, true);
		UniversalActor lostCTActor = actorBuilder.createUniversalActor("LostCT", 2000,
				defaultRepeatInterval, false, false, new Coordinates(1280, 165, 70, 70), 0.75f,
				true, false, true);

		UniversalActor smokeActor = actorBuilder.createUniversalActor("Smoke", 150,
				defaultRepeatInterval, false, false, new Coordinates(1740, 72, 178, 176), 0.85f,
				false, false, true);
		float smokePrecision = 0.970f;
		Checker[] smokeCheckers = new Checker[] {
				new RegionChecker(new Coordinates(53, 1026, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(413, 453, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(950, 540, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(1356, 356, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision) };
		Checker smokeChecker = LogicalChecker.createAnd(smokeCheckers);
		smokeActor.setChecker(smokeChecker);

		UniversalActor sniperActor = actorBuilder.createUniversalActor("Sniper", 150,
				defaultRepeatInterval, false, false, new Coordinates(5, 5, 1, 1), 0.9999f, false,
				false, true);
		Checker[] sniperCheckers = new Checker[] {
				new RegionChecker(new Coordinates(1919, 1079, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 1.0f),
				LogicalChecker.createNot(new RegionChecker(new Coordinates(800, 350, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 1.0f)),
				new RegionChecker(new Coordinates(1919, 0, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 1.0f), };
		Checker sniperChecker = LogicalChecker.createAnd(sniperCheckers);
		sniperActor.setChecker(sniperChecker);
		sniperActor.clearOnActions();
		// sniperActor.addOnAction(new ConfirmedDelayedActions(
		// Util.singleItemAsList(new OBSHotkeyAction(hkSniper)), 750, sniperChecker));
		// decided not to use delayed action since hotkeys are working good now =)
		sniperActor.addOnAction(new SwitchSPSceneAction(SPSniperSceneName));
		TreeMap<String, ArrayList<Action>> sniperUndoSPSwitchActions = new TreeMap<String, ArrayList<Action>>();
		sniperUndoSPSwitchActions.put("Match",
				Util.singleItemAsList(new SwitchSPSceneAction(SPMatchSceneName)));
		sniperUndoSPSwitchActions.put("Deathmatch",
				Util.singleItemAsList(new SwitchSPSceneAction(SPDeathMatchSceneName)));
		sniperActor.clearOffActions();
		sniperActor
				.addOffAction(new VariableSwitchAction("currentMode", sniperUndoSPSwitchActions));

		UniversalActor physicalActor = new UniversalActor("Physical", 150, defaultRepeatInterval,
				false, false, new RegionChecker(new Coordinates(2755, 1185, 80, 80, 0),
						resourcesLocation + "Physical\\Targets\\toFind.png", 0.8f),
				null, null);
		physicalActor.addOnAction(new SwitchSPSceneAction(SPMessingAroundSceneName));
		TreeMap<String, ArrayList<Action>> physicalUndoHotkey = new TreeMap<String, ArrayList<Action>>();
		physicalUndoHotkey.put("Match", Util.singleItemAsList(new SwitchSPSceneAction(SPMatchSceneName)));
		physicalUndoHotkey.put("Deathmatch",
				Util.singleItemAsList(new SwitchSPSceneAction(SPDeathMatchSceneName)));
		physicalUndoHotkey.put("CSGOMenu", Util.singleItemAsList(new SwitchSPSceneAction(SPMenuSceneName)));
		physicalActor.addOffAction(new VariableSwitchAction("currentMode", physicalUndoHotkey));

		UniversalActor killActor = actorBuilder.createUniversalActor("Kill", 500,
				defaultRepeatInterval, false, false, new Coordinates(1400, 72, 344, 176), 0.75f,
				false, false, true);
		ArrayList<Checker> killminiCheckers = new ArrayList<Checker>();
		killminiCheckers.add(actorBuilder.createChecker("Kill", new Coordinates(1400, 64, 338, 33),
				false, 0.65f));
		killminiCheckers.add(actorBuilder.createChecker("Kill", new Coordinates(1400, 103, 338, 33),
				false, 0.65f));
		killminiCheckers.add(actorBuilder.createChecker("Kill", new Coordinates(1400, 141, 338, 33),
				false, 0.65f));
		killminiCheckers.add(actorBuilder.createChecker("Kill", new Coordinates(1400, 179, 338, 33),
				false, 0.65f));
		killminiCheckers.add(actorBuilder.createChecker("Kill", new Coordinates(1400, 216, 338, 33),
				false, 0.65f));
		TrueCheckerCounter killCounter = new TrueCheckerCounter(killminiCheckers);
		RelationToPreviousNumberChecker killChecker = new RelationToPreviousNumberChecker(
				killCounter, BooleanNumberOperator.GREATER, 0);
		// killActor.setChecker(killminiCheckers[0]);
		killActor.setChecker(killChecker);

		UniversalActor lobbyActor = new UniversalActor("Lobby", 150, defaultRepeatInterval, false,
				false,
				new RegionChecker(new Coordinates(707, 160, 53, 23),
						resourcesLocation + "Lobby\\Targets\\lobby.png", 0.7f),
				new Action[] { new SwitchSPSceneAction(SPLobbySceneName) }, null);
		lobbyActor.addOnAction(new RunProgramAction("C:\\Windows\\System32\\cmd.exe",
				"/C \"" + resourcesLocation + "Scripts\\curl_lamp_set_hue.bat\" " + "307", "",
				false));

		UniversalActor menuActor = new UniversalActor("Menu", 1000, defaultRepeatInterval, false,
				false,
				new RegionChecker(new Coordinates(704, 364, 63, 33),
						resourcesLocation + "Menu\\Targets\\menu.png", 0.7f),
				new Action[] { new SwitchSPSceneAction(SPMenuSceneName) }, null);
		menuActor.addOnAction(new VariableSetterAction("currentMode", "CSGOMenu"));
		menuActor.addOnAction(new RunProgramAction("C:\\Windows\\System32\\cmd.exe",
				"/C \"" + resourcesLocation + "Scripts\\curl_lamp_set_hue.bat\" " + "180", "",
				false));

		UniversalActor competitiveHotkeyActor = new UniversalActor("Competitive", 300,
				defaultRepeatInterval, false, false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\competitive.png", 0.4f),
				new Action[] { new SwitchSPSceneAction(SPMatchSceneName) }, null);

		competitiveHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Match"));

		UniversalActor deathmatchHotkeyActor = new UniversalActor("Deathmatch", 300,
				defaultRepeatInterval, false, false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\deathmatch.png", 0.4f),
				new Action[] { new SwitchSPSceneAction(SPDeathMatchSceneName) }, null);
		deathmatchHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Deathmatch"));

		UniversalActor casualHotkeyActor = new UniversalActor("Casual", 300, defaultRepeatInterval,
				false, false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\casual.png", 0.4f),
				new Action[] { new SwitchSPSceneAction(SPMatchSceneName) }, null);
		casualHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Match"));

		float musumePrecision = 0.965f;
		Checker[] musumeCheckers = new Checker[] {
				new RegionChecker(new Coordinates(53, 1026, 1, 1),
						resourcesLocation + "IkaMusume\\Targets\\musume.png", musumePrecision),
				// new RegionChecker(new Coordinates(413, 453, 1, 1),
				// resourcesLocation + "IkaMusume\\Targets\\musume.png", musumePrecision),
				new RegionChecker(new Coordinates(1356, 1060, 1, 1),
						resourcesLocation + "IkaMusume\\Targets\\musume.png", musumePrecision) };
		Checker musumeChecker = LogicalChecker.createAnd(musumeCheckers);
		UniversalActor IkaMusumeActor = new UniversalActor("IkaMusume", 100, defaultRepeatInterval,
				false, false, musumeChecker, null,
				new Action[] { new SwitchSPSceneAction(SPMatchSceneName) });

		// IkaMusumeActor.addOnAction(
		// new MultiSoundAction(resourcesLocation + "IkaMusume\\Sounds", 0.3, true));
		IkaMusumeActor.addOnAction(new SwitchSPSceneAction(SPIkaMusumeSceneName));
		IkaMusumeActor
				.addOnAction(new DelayedActions(
						Util.singleItemAsList(
								new MultiFileCopyAction(resourcesLocation + "IkaMusume\\Images",
										resourcesLocation + "IkaMusume\\current.png", true)),
						2500));

		UniversalActor changeMenuActor = new UniversalActor("ChangeMenu", 3000,
				defaultRepeatInterval, false, false,
				actorBuilder.createChecker("ChangeMenu", new Coordinates(319, 12, 32, 32), false,
						0.7f),
				new Action[] { new SwitchSisSceneAction("changeTeamSisScene") },
				new Action[] { new SwitchSisSceneAction("changeTeamSisScene") });

		UniversalActor changeLoadingActor = actorBuilder.createUniversalActor("ChangeLoading", 200,
				defaultRepeatInterval, false, false, new Coordinates(313, 136, 130, 130), 0.7f,
				false, false, true);

		UniversalActor changeTeamActor = actorBuilder.createUniversalActor("ChangeTeam", 1000,
				defaultRepeatInterval, false, false, new Coordinates(483, 0, 82, 377), 0.95f, false,
				false, false);

		UniversalActor changeTActor = actorBuilder.createUniversalActor("ChangeT", 1000,
				defaultRepeatInterval, false, false, new Coordinates(567, 571, 35, 394), 0.65f,
				false, false, false);

		UniversalActor changeCTActor = actorBuilder.createUniversalActor("ChangeCT", 1000,
				defaultRepeatInterval, false, false, new Coordinates(567, 125, 35, 380), 0.65f,
				false, false, false);

		UniversalActor poorActor = actorBuilder.createUniversalActor("Poor Actor", 1000,
				defaultRepeatInterval, false, false, new Coordinates(567, 125, 1, 1), 0.65f, false,
				false, false);

		// We don't need some actions generated by actorBuilder. Let's wipe them.
		changeMenuActor.clearOnActions();
		changeMenuActor.clearOffActions();
		changeLoadingActor.clearOnActions();
		changeLoadingActor.clearOffActions();
		changeTActor.clearOnActions();
		changeCTActor.clearOnActions();
		changeTeamActor.clearOnActions();

		changeMenuActor.addOnAction(new SwitchSisSceneAction("menuSisScene"));
		changeMenuActor.addOffAction(new SwitchSisSceneAction("loadingSisScene"));
		changeLoadingActor.addOnAction(new SwitchSisSceneAction("modeSelectionSisScene"));
		changeLoadingActor.addOffAction(new SwitchSisSceneAction("menuSisScene"));
		changeTActor.addOnAction(new SwitchSisSceneAction("matchTSisScene"));
		changeTActor.addOnAction(new VariableSetterAction("currentTeam", "T"));
		changeCTActor.addOnAction(new SwitchSisSceneAction("matchCTSisScene"));
		changeCTActor.addOnAction(new VariableSetterAction("currentTeam", "CT"));
		changeTeamActor.addOnAction(new SwitchSisSceneAction("changeTeamSisScene"));

		project.addActorToGlobalActors(mvpActor);
		project.addActorToGlobalActors(deathActor);
		project.addActorToGlobalActors(winTActor);
		project.addActorToGlobalActors(winCTActor);
		project.addActorToGlobalActors(lostTActor);
		project.addActorToGlobalActors(lostCTActor);
		project.addActorToGlobalActors(smokeActor);
		project.addActorToGlobalActors(sniperActor);
		project.addActorToGlobalActors(killActor);
		project.addActorToGlobalActors(lobbyActor);
		project.addActorToGlobalActors(menuActor);
		project.addActorToGlobalActors(competitiveHotkeyActor);
		project.addActorToGlobalActors(deathmatchHotkeyActor);
		project.addActorToGlobalActors(casualHotkeyActor);
		project.addActorToGlobalActors(IkaMusumeActor);
		project.addActorToGlobalActors(changeMenuActor);
		project.addActorToGlobalActors(changeLoadingActor);
		project.addActorToGlobalActors(changeTeamActor);
		project.addActorToGlobalActors(changeTActor);
		project.addActorToGlobalActors(changeCTActor);
		project.addActorToGlobalActors(poorActor);
		project.addActorToGlobalActors(physicalActor);
		
		setRandomNamesForCheckersActionsCountersInProject(project);

		// String[] testActors = new String[] { "UniTest"};
		String[] menuActors = new String[] { "Lobby", "Menu", "ChangeMenu", "ChangeT", "Poor Actor",
				"ChangeCT", "Physical"};
		String[] loadingActors = new String[] { "ChangeLoading", "ChangeCT", "ChangeT",
				"ChangeMenu", "Menu" };
		String[] modeSelectionActors = new String[] { "Competitive", "Casual", "Deathmatch",
				"ChangeTeam", "ChangeMenu" };
		String[] matchTActors = new String[] { "ChangeCT", "MVP", "IkaMusume", "WinT", "LostT",
				"Kill", "Death", "ChangeMenu", "Smoke", "Sniper", "Physical" };
		String[] matchCTActors = new String[] { "ChangeT", "MVP", "IkaMusume", "WinCT", "LostCT",
				"Kill", "Death", "ChangeMenu", "Smoke", "Sniper", "Physical" };
		String[] changeTeamActors = new String[] { "ChangeT", "ChangeCT", "ChangeMenu" };
		String[] testActors = new String[] { "Physical" };

		// SisScene testSisScene = new SisScene("testSisScene", testActors);
		SisScene menuSisScene = new SisScene("menuSisScene", menuActors);
		SisScene loadingSisScene = new SisScene("loadingSisScene", loadingActors);
		SisScene modeSelectionSisScene = new SisScene("modeSelectionSisScene", modeSelectionActors);
		SisScene matchTSisScene = new SisScene("matchTSisScene", matchTActors);
		SisScene matchCTSisScene = new SisScene("matchCTSisScene", matchCTActors);
		SisScene changeTeamSisScene = new SisScene("changeTeamSisScene", changeTeamActors);
		SisScene testingStuffSisScene = new SisScene("testingStuff", testActors);

		project.addSisScene(menuSisScene);
		project.addSisScene(loadingSisScene);
		project.addSisScene(modeSelectionSisScene);
		project.addSisScene(matchTSisScene);
		project.addSisScene(matchCTSisScene);
		project.addSisScene(changeTeamSisScene);
		project.addSisScene(testingStuffSisScene);

		project.setPrimarySisSceneName("menuSisScene");

		String pathWhereToSave = LowLevel.getAppDataPath() + "generatedTestProjects"
				+ File.separator + "uniDefaultProjectSerialized.streamsis";

		try {
			ProjectSerializator.serializeToFile(project, pathWhereToSave);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void setRandomNamesForCheckersActionsCountersInProject(CuteProject project) {
		for (Actor actor : project.getGlobalActorsUnmodifiable()) {
			setRandomNameForCuteElementRecursively(actor.getChecker());
			for (Action action : actor.getOnActions()) {
				setRandomNameForCuteElementRecursively(action);
			}
			for (Action action : actor.getOffActions()) {
				setRandomNameForCuteElementRecursively(action);
			}
		}
	}
	
	private static void setRandomNameForCuteElementRecursively(CuteElement element) {
		if (element != null) {
			setRandomNameForCuteElement(element);
			ObservableList<? extends CuteElement> children = element.getChildren();
			if (children != null) {
				for(CuteElement subNode : children) {
					setRandomNameForCuteElementRecursively(subNode);
				}
			}
		}
	}

	private static void setRandomNameForCuteElement(CuteElement element) {
		if (element != null) {
			// Some CuteElementContainers have names that are important in context. Let's not modify
			// their names.
			if (element.getClass() != CuteElementContainer.class) { 
				String firstPart = element.getClass().getSimpleName();
				String secondPart = String.valueOf((new Random().nextInt(10000)));
				element.getElementInfo().setName(firstPart+secondPart);
			}
		}
	}
	
}
