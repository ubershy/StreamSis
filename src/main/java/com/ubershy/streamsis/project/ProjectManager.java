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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;

/**
 * This class manages current StreamSis's CuteProject. <br>
 * {@link StreamSis} can work with only one {@link CuteProject} at the same time. <br>
 * <p>
 * What it can do? <br>
 * ProjectManager can load CuteProject from path and set it as current, for example. <br>
 * It also provides static {@link #getProject()} method which makes current CuteProject accessible from all parts of program. <br>
 * Also it automatically changes LastProjectLocation variable in {@link CuteConfig}.
 */
public final class ProjectManager {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(ProjectManager.class);
	
	/** The current {@link CuteProject}'s file path. */
	private static final ReadOnlyStringWrapper projectFilePath = new ReadOnlyStringWrapper("");
	public static ReadOnlyStringProperty projectFilePathProperty() {
		return projectFilePath.getReadOnlyProperty();
	}
	public static String getProjectFilePath() {return projectFilePath.get();}
	public static void setProjectFilePath(String path) {projectFilePath.set(path);}

	/** The current CuteProject. */
	private static CuteProject currentProject;
	public static CuteProject getProject() {return currentProject;}
	
	private static CountDownLatch changeProjectLatch;
	
	/**
	 * Sets the current CuteProject.
	 *
	 * @param currentProject
	 *            CuteProject you want to make current
	 * @param path
	 *            file path of this project <br>
	 *            can be null, if project is not yet saved.
	 */
	public static void setProject(CuteProject currentProject, String path) {
		if (changeProjectLatch != null) {
			try {
				changeProjectLatch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException("I don't like when someone interrupts me.");
			}
			changeProjectLatch = null;
		}
		ProjectManager.currentProject = currentProject;
		ProjectManager.projectFilePath.set(path);
		if (path != null && !path.isEmpty()) {
			CuteConfig.setString(CuteConfig.CUTE, "LastProjectLocation", path);
		}
		logger.info(
				"Project is set to: '" + currentProject.getName() + "' with path: '" + path + "'.");
	}

	/**
	 * Loads CuteProject from path, sets it as current.
	 *
	 * @param path
	 *            path from where CuteProject will be loaded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static CuteProject loadProjectAndSet(String path) throws IOException {
		CuteProject project = null;
		try {
			project = ProjectSerializator.deSerializeFromFile(path);
		} catch (IOException e) {
			logger.error("Can't load project file: " + path);
			throw e;
		}
		logger.info("Successfully loaded project file: " + path);
		ProjectManager.setProject(project, path);
		return project;
	}

	/**
	 * Creates new CuteProject with one empty SisScene and sets it as current.
	 */
	public static CuteProject createAndSetNewProject() {
		logger.info("Creating new empty Project");
		CuteProject emptyProject = new CuteProject("New Project");
		emptyProject.addSisScene(new SisScene("New SisScene", new ArrayList<String>()));
		ProjectManager.setProject(emptyProject, null);
		return emptyProject;
	}

	public synchronized static void initProjectFromGUI() {
		CuteProject toInit = currentProject;
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				if (changeProjectLatch != null) {
					changeProjectLatch.await();
				}
				changeProjectLatch = new CountDownLatch(1);
				logger.info("Initializing project from GUI...");
				toInit.init();
				logger.info("Project was initialized from GUI");
				changeProjectLatch.countDown();
				return null;
			}
		};
		task.setOnFailed((e) -> {
			// Fail harshly.
			if (task.getException() == null) {
				throw new RuntimeException("Project initialization failed for some reason.");
			} else {
				throw new RuntimeException("Project can't be initialized", task.getException());
			}
		});
		new Thread(task).start();
	}

	public synchronized static void startProjectFromGUI() {
		CuteProject toStart = currentProject;
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				toStart.startProject();
				return null;
			}
		};
		task.setOnFailed((e) -> {
			// Fail gently and show the error to the user. The user will be able to save project.
			currentProject.stopProject();
			Throwable ex = task.getException();
			String errorText;
			if (ex == null) {
				errorText = "Project failed after start for unknown reason.";
			} else {
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				ex.printStackTrace(new PrintStream(ostream));
				errorText = ostream.toString();
			}
			Alert alert = new Alert(AlertType.ERROR);
			GUIUtil.cutifyAlert(alert);
			alert.setTitle("Project failed after start");
			alert.setHeaderText("A scary internal error occured while running the project.");
			TextArea stackTraceArea = new TextArea(errorText);
			stackTraceArea.setEditable(false);
			stackTraceArea.setMinSize(800, 450);
			alert.getDialogPane().setExpandableContent(stackTraceArea);
			GUIUtil.showAlertInPrimaryStageCenter(alert);
		});
		new Thread(task).start();
	}

}
