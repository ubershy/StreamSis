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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.playground.Playground;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

	/**
	 * The main method.
	 * <p>
	 * You can use any combinations of these available arguments:<br>
	 * <ul>
	 * <li>"quiet". <br>
	 * Defines if StreamSis must run in quiet mode (non-GUI). <br>
	 * Overrides "ProjectAutoLoad" and "ProjectAutoStart" settings. <br>
	 * Automatically starts Project.</li>
	 * <li>Path of project to load. <br>
	 * Defines if StreamSis must load a project from specified path. <br>
	 * Overrides "LastProjectLocation" setting.</li>
	 * </ul>
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		logger.info("StreamSis has started =)");

		// Start of developer stuff

		// Here we can disable actual Checking and/or Acting
		// This helps to measure the performance of different parts of the program
		// ConstsAndVars.performChecking = false;
		// ConstsAndVars.performActing = false;

		// Lets generate hardcoded Projects for testing new features
		// Because there's no way to construct Projects in GUI now
		// Also it automatically serialize them to files, so we can check later, how they will
		// deserialize
		Playground.generateHardcodedDefaultProject();
		Playground.generateHardcodedTestProject();

		// End of developer stuff

		// Lets parse passed command-line parameters
		boolean quietMode = false;
		final Parameters params = getParameters();
		List<String> parameters = new ArrayList<String>(params.getRaw());
		if (parameters.contains("quiet")) {
			quietMode = true;
			// After this operation, only path parameter will be left in parameters array, if
			// existed
			parameters.remove("quiet");
		}

		// If path parameter exists, lets try to load Project at this path. If not, lets take the
		// last Project location from configuration file
		final String projectToLoad = !parameters.isEmpty() ? parameters.get(0)
				: CuteConfig.getString(CuteConfig.CUTE, "LastProjectLocation");

		// Lets automatically load Project from the variable above if StreamSis is in Quiet Mode or
		// is set to AutoLoad last Project
		// Else lets create a new Project
		if (CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoLoad") || quietMode) {
			try {
				ProjectManager.loadProjectAndSet(projectToLoad);
			} catch (IOException e) {
				ProjectManager.createAndSetNewProject();
			}
		} else {
			ProjectManager.createAndSetNewProject();
		}

		// Now lets automatically start Project if StreamSis is in Quiet Mode or is set to
		// AutoStart
		if (CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoStart") || quietMode) {
			// Lets check if ProjectManager knows current Project path, because if not, it's
			// probably a new empty CuteProject that is worthless of starting
			if (ProjectManager.getProjectFilePath() != null) {
				try {
					ProjectManager.getProject().startProject();
				} catch (RuntimeException e) {
					// Lets find out why we can't start Project.
					logger.info(e.getMessage());
				}
			}
		}

		// Lets start GUI if StreamSis is not in Quiet Mode
		if (!quietMode) {
			CuteProject project = ProjectManager.getProject();
			try {
				primaryStage.setTitle("StreamSis");
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						GUIManager.closeApplicatonSafely();
					}
				});
				GUIManager.setPrimaryStage(primaryStage);
				GUIManager.buildGui(project);
				primaryStage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
