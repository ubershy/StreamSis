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

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.StreamSis;

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
	
	/** The current CuteProject's file path. */
	private static String currentProjectFilePath;
	public static String getProjectFilePath() {return currentProjectFilePath;}
	public static void setProjectFilePath(String path) {currentProjectFilePath = path;}

	/** The current CuteProject. */
	static CuteProject currentProject;

	/**
	 * Gets the current CuteProject.
	 *
	 * @return the current CuteProject
	 */
	public static CuteProject getProject() {
		return currentProject;
	}

	/**
	 * Sets the current CuteProject and initializes it.
	 *
	 * @param currentProject
	 *            CuteProject you want to make current
	 * @param path
	 *            file path of this project <br>
	 *            can be null, if project is not yet saved.
	 */
	public static void setProject(CuteProject currentProject, String path) {
		ProjectManager.currentProject = currentProject;
		ProjectManager.currentProjectFilePath = path;
		if (path != null && !path.isEmpty()) {
			CuteConfig.setStringValue(CuteConfig.CUTE, "LastProjectLocation", path);
		}
		currentProject.init();
	}

	/**
	 * Loads CuteProject from path, sets it as current and initializes it.
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
			logger.error("Can't load Project.");
			throw e;
		}
		if (project != null) {
			logger.info("Successfully loaded project file: " + path);
			ProjectManager.setProject(project, path);
		}
		return project;
	}

	/**
	 * Creates new CuteProject with one empty SisScene and sets it as current.
	 */
	public static CuteProject createAndSetNewProject() {
		logger.info("Creating new empty Project");
		CuteProject emptyProject = new CuteProject("Empty Project");
		emptyProject.addSisScene(new SisScene("New SisScene", new ArrayList<String>()));
		ProjectManager.setProject(emptyProject, null);
		return emptyProject;
	}

}
