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
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.LowLevel;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.actions.DelayedActions;
import com.ubershy.streamsis.actions.MultiFileCopyAction;
import com.ubershy.streamsis.actions.MultiSoundAction;
import com.ubershy.streamsis.actions.OBSHotkeyAction;
import com.ubershy.streamsis.actions.SwitchSisSceneAction;
import com.ubershy.streamsis.actions.VariableSetterAction;
import com.ubershy.streamsis.actions.VariableSwitchAction;
import com.ubershy.streamsis.actors.ActorBuilder;
import com.ubershy.streamsis.actors.UniversalActor;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.checkers.Coordinates;
import com.ubershy.streamsis.checkers.LogicalChecker;
import com.ubershy.streamsis.checkers.RegionChecker;
import com.ubershy.streamsis.checkers.RelationToPreviousNumberChecker;
import com.ubershy.streamsis.checkers.AbstractRelationToNumberChecker.BooleanNumberOperator;
import com.ubershy.streamsis.counters.TrueCheckerCounter;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectSerializator;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;

/**
 * The playground for testing things.
 */
public final class Playground {

	static final Logger logger = LoggerFactory.getLogger(Playground.class);

	/** The path where structurized resources needed for project generation are located. */
	static String resourcesLocation = "D:\\1SRC\\My Pictures\\3PHOTOSHOP\\twitch\\SEresources\\";

	public static void testNewMTRegionChecker(int numOfTimes, long delay) {
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

		if (!Util.checkDirectory(resourcesLocation)) {
			logger.error("Programmer. You can't generate 'TestProject' hardcoded project.\n"
					+ "Directory with resource files not found:\n" + resourcesLocation);
			return;
		}

		logger.info("Generating hardcoded project 'TestProject' based on resources located in "
				+ resourcesLocation);

		CuteProject project = new CuteProject("TestProject");

		UniversalActor uniTestActor = new UniversalActor("UniTestActor", 1000, 500, false, false);

		// String closeWindow = new KeyCodeCombination(KeyCode.F4,
		// KeyCombination.ALT_DOWN).getName();

		// uniTestActor.addOnAction(new SwitchSisSceneAction("testSisScene"));
		uniTestActor.addOnAction(
				new MultiSoundAction(resourcesLocation + "UniTest\\Sounds\\", 1.0, true));
		// uniTestActor.addOnAction(new HotkeyAction(closeWindow));

		Checker checker1 = new RegionChecker(new Coordinates(1320, 480, 600, 600),
				resourcesLocation + "UniTest\\Targets\\transparencytest.png", 0.99f);
		// LogicalChecker uniChecker = LogicalChecker.createOr(new Checker[] { checker1 });
		uniTestActor.setChecker(checker1);

		project.addActorToGlobalActors(uniTestActor);

		String[] testActors = new String[] { "UniTestActor" };
		SisScene testSisScene = new SisScene("testSisScene", testActors);
		project.addSisScene(testSisScene);
		project.setDefaultSisSceneName("testSisScene");

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

		if (!Util.checkDirectory(resourcesLocation)) {
			logger.error("Programmer. You can't generate 'Default' hardcoded project.\n"
					+ "Directory with resource files not found:\n" + resourcesLocation);
			return;
		}

		CuteProject project = new CuteProject("Default");

		String resourcesLocation = "D:\\1SRC\\My Pictures\\3PHOTOSHOP\\twitch\\SEresources\\";

		ActorBuilder actorBuilder = new ActorBuilder(resourcesLocation);
		logger.info("Generating hardcoded project 'Default' based on resources located in "
				+ resourcesLocation
				+ "\nIf you see too many messages about something broken in console (>30) then"
				+ " it probably means you don't have required resource files for generating."
				+ "\nIf you don't see so many, everything is okay. ;)");

		Modifier altMod = KeyCombination.ALT_DOWN;
		String hkMenu = new KeyCodeCombination(KeyCode.F5, altMod).getName();
		String hkLobby = new KeyCodeCombination(KeyCode.F6, altMod).getName();
		String hkMatch = new KeyCodeCombination(KeyCode.PAGE_UP, altMod).getName();
		String hkDeathMatch = new KeyCodeCombination(KeyCode.PAGE_DOWN, altMod).getName();
		String hkIkaMusume = new KeyCodeCombination(KeyCode.F1, altMod).getName();
		String hkSniper = new KeyCodeCombination(KeyCode.F3, altMod).getName();

		// lets make mvpActor intentionally broken
		UniversalActor mvpActor = actorBuilder.createUniversalActor("MVP", -1, 0, false, false,
				new Coordinates(563, 158, 80, 80), 0.7f, true, false, true);
		UniversalActor deathActor = actorBuilder.createUniversalActor("Death", 1000, 0, false,
				false, new Coordinates(1740, 72, 178, 176), 0.75f, false, false, true);
		UniversalActor winTActor = actorBuilder.createUniversalActor("WinT", 2000, 0, false, false,
				new Coordinates(1280, 165, 70, 70), 0.75f, true, false, true);
		UniversalActor winCTActor = actorBuilder.createUniversalActor("WinCT", 2000, 0, false,
				false, new Coordinates(1280, 165, 70, 70), 0.75f, true, false, true);
		UniversalActor lostTActor = actorBuilder.createUniversalActor("LostT", 2000, 0, false,
				false, new Coordinates(1280, 165, 70, 70), 0.75f, true, false, true);
		UniversalActor lostCTActor = actorBuilder.createUniversalActor("LostCT", 2000, 0, false,
				false, new Coordinates(1280, 165, 70, 70), 0.75f, true, false, true);

		UniversalActor smokeActor = actorBuilder.createUniversalActor("Smoke", 150, 0, false, false,
				new Coordinates(1740, 72, 178, 176), 0.85f, false, false, true);
		float smokePrecision = 0.965f;
		Checker[] smokeCheckers = new Checker[] {
				new RegionChecker(new Coordinates(53, 1026, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(413, 453, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(582, 383, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision),
				new RegionChecker(new Coordinates(1356, 356, 1, 1),
						resourcesLocation + "Smoke\\Targets\\1.png", smokePrecision) };
		Checker smokeChecker = LogicalChecker.createAnd(smokeCheckers);
		smokeActor.setChecker(smokeChecker);

		UniversalActor sniperActor = actorBuilder.createUniversalActor("Sniper", 50, 0, false,
				false, new Coordinates(5, 5, 1, 1), 0.9999f, false, false, true);
		Checker[] sniperCheckers = new Checker[] {
				new RegionChecker(new Coordinates(1800, 600, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 0.999f),
				LogicalChecker.createNot(new RegionChecker(new Coordinates(800, 350, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 0.999f)),
				new RegionChecker(new Coordinates(444, 400, 1, 1),
						resourcesLocation + "Sniper\\Targets\\sniper.png", 0.999f) };
		Checker sniperChecker = LogicalChecker.createAnd(sniperCheckers);
		sniperActor.setChecker(sniperChecker);
		sniperActor.clearOnActions();
		// sniperActor.addOnAction(new ConfirmedDelayedActions(
		// Util.singleItemAsList(new OBSHotkeyAction(hkSniper)), 750, sniperChecker));
		// decided not to use delayed action since hotkeys are working good now =)
		sniperActor.addOnAction(new OBSHotkeyAction(hkSniper));
		TreeMap<String, Action> sniperUndoHotkey = new TreeMap<String, Action>();
		sniperUndoHotkey.put("Match", new OBSHotkeyAction(hkMatch));
		sniperUndoHotkey.put("Deathmatch", new OBSHotkeyAction(hkDeathMatch));
		sniperActor.clearOffActions();
		sniperActor.addOffAction(new VariableSwitchAction("currentMode", sniperUndoHotkey));

		UniversalActor killActor = actorBuilder.createUniversalActor("Kill", 500, 0, false, false,
				new Coordinates(1400, 72, 344, 176), 0.75f, false, false, true);
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

		UniversalActor lobbyHotkeyActor = new UniversalActor("Lobby", 1000, 0, false, false,
				new RegionChecker(new Coordinates(707, 160, 53, 23),
						resourcesLocation + "Lobby\\Targets\\lobby.png", 0.7f),
				new Action[] { new OBSHotkeyAction(hkLobby) }, null);

		UniversalActor menuHotkeyActor = new UniversalActor("Menu", 1000, 0, false, false,
				new RegionChecker(new Coordinates(704, 364, 63, 33),
						resourcesLocation + "Menu\\Targets\\menu.png", 0.7f),
				new Action[] { new OBSHotkeyAction(hkMenu) }, null);

		UniversalActor competitiveHotkeyActor = new UniversalActor("Competitive", 300, 0, false,
				false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\competitive.png", 0.4f),
				new Action[] { new OBSHotkeyAction(hkMatch) }, null);

		competitiveHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Match"));

		UniversalActor deathmatchHotkeyActor = new UniversalActor("Deathmatch", 300, 0, false,
				false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\deathmatch.png", 0.4f),
				new Action[] { new OBSHotkeyAction(hkDeathMatch) }, null);
		deathmatchHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Deathmatch"));

		UniversalActor casualHotkeyActor = new UniversalActor("Casual", 300, 0, false, false,
				new RegionChecker(new Coordinates(1065, 327, 154, 19),
						resourcesLocation + "Modes\\casual.png", 0.4f),
				new Action[] { new OBSHotkeyAction(hkMatch) }, null);
		casualHotkeyActor.addOnAction(new VariableSetterAction("currentMode", "Match"));

		UniversalActor IkaMusumeActor = new UniversalActor("IkaMusume", 100, 0, false, false,
				new RegionChecker(new Coordinates(0, 500, 1, 1),
						resourcesLocation + "IkaMusume\\Targets\\musume.png", 0.99f),
				null, new Action[] { new OBSHotkeyAction(hkMatch) });

		IkaMusumeActor.addOnAction(
				new MultiSoundAction(resourcesLocation + "IkaMusume\\Sounds", 0.3, true));
		IkaMusumeActor.addOnAction(new OBSHotkeyAction(hkIkaMusume));
		IkaMusumeActor
				.addOnAction(new DelayedActions(
						Util.singleItemAsList(
								new MultiFileCopyAction(resourcesLocation + "IkaMusume\\Images",
										resourcesLocation + "IkaMusume\\current.png", true)),
						2500));

		UniversalActor changeMenuActor = new UniversalActor("ChangeMenu", 3000, 0, false, false,
				actorBuilder.createChecker("ChangeMenu", new Coordinates(319, 12, 32, 32), false,
						0.7f),
				new Action[] { new SwitchSisSceneAction("changeTeamSisScene") },
				new Action[] { new SwitchSisSceneAction("changeTeamSisScene") });

		UniversalActor changeLoadingActor = actorBuilder.createUniversalActor("ChangeLoading", 200,
				0, false, false, new Coordinates(313, 136, 130, 130), 0.7f, false, false, true);

		UniversalActor changeTeamActor = actorBuilder.createUniversalActor("ChangeTeam", 1000, 0,
				false, false, new Coordinates(483, 0, 82, 377), 0.95f, false, false, false);

		UniversalActor changeTActor = actorBuilder.createUniversalActor("ChangeT", 1000, 0, false,
				false, new Coordinates(567, 571, 35, 394), 0.65f, false, false, false);

		UniversalActor changeCTActor = actorBuilder.createUniversalActor("ChangeCT", 1000, 0, false,
				false, new Coordinates(567, 125, 35, 380), 0.65f, false, false, false);

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
		project.addActorToGlobalActors(lobbyHotkeyActor);
		project.addActorToGlobalActors(menuHotkeyActor);
		project.addActorToGlobalActors(competitiveHotkeyActor);
		project.addActorToGlobalActors(deathmatchHotkeyActor);
		project.addActorToGlobalActors(casualHotkeyActor);
		project.addActorToGlobalActors(IkaMusumeActor);
		project.addActorToGlobalActors(changeMenuActor);
		project.addActorToGlobalActors(changeLoadingActor);
		project.addActorToGlobalActors(changeTeamActor);
		project.addActorToGlobalActors(changeTActor);
		project.addActorToGlobalActors(changeCTActor);

		// String[] testActors = new String[] { "UniTest"};
		String[] menuActors = new String[] { "Lobby", "Menu", "ChangeMenu", "ChangeT", "ChangeCT" };
		String[] loadingActors = new String[] { "ChangeLoading", "ChangeCT", "ChangeT",
				"ChangeMenu", "Menu" };
		String[] modeSelectionActors = new String[] { "Competitive", "Casual", "Deathmatch",
				"ChangeTeam", "ChangeMenu" };
		String[] matchTActors = new String[] { "ChangeCT", "MVP", "IkaMusume", "WinT", "LostT",
				"Kill", "Death", "ChangeMenu", "Smoke", "Sniper" };
		String[] matchCTActors = new String[] { "ChangeT", "MVP", "IkaMusume", "WinCT", "LostCT",
				"Kill", "Death", "ChangeMenu", "Smoke", "Sniper" };
		String[] changeTeamActors = new String[] { "ChangeT", "ChangeCT", "ChangeMenu" };
		String[] testActors = new String[] { "Kill" };

		// SisScene testSisScene = new SisScene("testSisScene", testActors);
		SisScene menuSisScene = new SisScene("menuSisScene", menuActors);
		SisScene loadingSisScene = new SisScene("loadingSisScene", loadingActors);
		SisScene modeSelectionSisScene = new SisScene("modeSelectionSisScene",
				modeSelectionActors);
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

		project.setDefaultSisSceneName("menuSisScene");

		String pathWhereToSave = LowLevel.getAppDataPath() + "generatedTestProjects"
				+ File.separator + "uniDefaultProjectSerialized.streamsis";

		try {
			ProjectSerializator.serializeToFile(project, pathWhereToSave);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
