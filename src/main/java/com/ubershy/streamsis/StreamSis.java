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
package com.ubershy.streamsis;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.networking.StreamingProgramManager;
import com.ubershy.streamsis.playground.Playground;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * StreamSis.
 * <p>
 * The main class.
 * <p>
 * Note: some classes have *cute prefix in their names. <br>
 * In most cases it is made to conveniently avoid name conflicts with classes from another packages.
 */
public class StreamSis extends Application {

	static final Logger logger = LoggerFactory.getLogger(StreamSis.class);
	
	static final UncaughtExceptionHandler eHandler = new SneakyExceptionHandler();

	/**
	 * The main method.
	 * <p>
	 * You can use any combinations of these available arguments:<br>
	 * <ul>
	 * <li>"--quiet". <br>
	 * Defines if StreamSis must run in quiet mode (non-GUI). <br>
	 * Overrides "ProjectAutoLoad" and "ProjectAutoStart" settings. <br>
	 * Automatically starts Project.</li>
	 * <li>Path of project to load. <br>
	 * Defines if StreamSis must load a project from specified path. <br>
	 * Overrides "LastProjectLocation" setting.</li>
	 * </ul>
	 *
	 * @param args
	 *            The arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		logger.info("StreamSis has started =)");
		
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			logger.error("Unexpected error occured in " + thread.getName() + " thread.", throwable);
			System.exit(1);
		});
		
		// Start of developer stuff

		// Here we can disable actual Checking and/or Acting
		// This helps to measure the performance of different parts of the program
		// ConstsAndVars.performChecking = false;
		// ConstsAndVars.performActing = false;
		
		// ConstsAndVars.slowDownInitForMs = 50;

		// Let's generate hardcoded Projects for testing new features
		// Because there's no way to construct Projects in GUI now
		// Also it automatically serializes them to files, so we can check later, how they will
		// deserialize.
		Playground.generateHardcodedDefaultProject();
		Playground.generateHardcodedTestProject();

		// End of developer stuff
		
		// Let's get configuration parameters related to application start.
		boolean projectAutoLoad = CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoLoad");
		boolean projectAutoStart = CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoStart");
		String projectToLoadPath = CuteConfig.getString(CuteConfig.CUTE, "LastProjectLocation");

		// Let's parse CLI arguments.
		boolean quietMode = false;
		List<String> arguments = new ArrayList<String>(getParameters().getUnnamed());
		if (arguments.contains("--quiet")) {
			quietMode = true;
			// After this operation, only path argument will be left in arguments array, if
			// existed.
			arguments.remove("--quiet");
		}
		// If CLI project path argument exists, let's override configuration's project path.
		if (!arguments.isEmpty()) {
			projectToLoadPath = arguments.get(0);
			arguments.remove(0);
			boolean valid = Util.checkSingleFileExistanceAndExtension(projectToLoadPath,
					new String[] { "*.streamsis" });
			if (!valid) {
				logger.error("Invalid project file path command-line parameter, exiting...");
				System.exit(1);
			}
		}
		if (arguments.size() != 0) { // At this point, the list of CLI arguments must be empty.
			logger.error("Unknown command-line parameters provided, exiting...");
			System.exit(1);
		}
		
		// Initialize network connection to Streaming Program.
		StreamingProgramManager.start();
		
		if (quietMode) { // Non-GUI mode.
			try {
				ProjectManager.loadProjectAndSet(projectToLoadPath);
			} catch (IOException e) {
				logger.error(Util.whyProjectCantBeLoaded(e));
				System.exit(1);
			}
			try {
				ProjectManager.getProject().startProject();
			} catch (Exception e) {
				logger.error("Error during Project running", e);
				System.exit(1);
			}
		} else { // GUI mode.
			primaryStage.setTitle("StreamSis");
			primaryStage.setOnCloseRequest(event -> stop());
			GUIManager.setPrimaryStageAndCreateOtherWindows(primaryStage);
			// Change exception handler to special GUI-specific handler.
			Thread.setDefaultUncaughtExceptionHandler(eHandler);
	        try {
	        	// Always start with showing an empty project instead of loading screen or something
				// like that.
				GUIManager.createNewProject();
				primaryStage.show();
				if (projectAutoLoad) {
					GUIManager.loadProject(projectToLoadPath, projectAutoStart);
				}
				HotkeyManager.startListeningToHotkeys();
	        } catch (Exception e) {
	        	eHandler.uncaughtException(Thread.currentThread(), e);
	        }
		}
	}

	@Override
	public void stop() {
		logger.info("Safely exiting StreamSis...");
		if (GUIManager.getPrimaryStage() != null)
			GUIManager.saveCoordinatesOfAllWindows();
		StreamingProgramManager.stopForever();
		System.exit(0);
    }

}
