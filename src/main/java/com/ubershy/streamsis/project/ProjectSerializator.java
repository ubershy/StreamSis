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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubershy.streamsis.Util;

/**
 * Project Serializator is a class for saving and loading {@link CuteProject CuteProjects}. <br>
 * Files it can produce or load are in JSON format.
 */
public final class ProjectSerializator {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(ProjectSerializator.class);

	/**
	 * Serialize {@link CuteProject} to string.
	 *
	 * @param project
	 *            the {@link CuteProject} to serialize
	 * @return the string in JSON format
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String serializeToString(CuteProject project) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String serialized = null;
		try {
			serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(project);
		} catch (JsonGenerationException e) {
			logger.error("CuteProject serializing to string fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error("CuteProject serializing to string fail: Mapping error");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error("CuteProject serializing to string fail: IO error");
			throw e;
		}
		logger.debug("CuteProject serializing to string success");
		return serialized;
	}

	/**
	 * Serialize(save) {@link CuteProject} to file.
	 *
	 * @param project
	 *            the {@link CuteProject} to serialize
	 * @param path
	 *            the path where file will be saved
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void serializeToFile(CuteProject project, String path) throws IOException {
		logger.info("Saving Project file: " + path);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Util.createFileAndDirectoriesAtPath(path);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(new File(path)), project);
		} catch (JsonGenerationException e) {
			logger.error("CuteProject saving fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error("CuteProject saving fail: Mapping error");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error("CuteProject saving fail: IO error");
			throw e;
		}
		logger.info("CuteProject saving success");
	}

	/**
	 * Deserialize(load) {@link CuteProject} from file.
	 *
	 * @param path
	 *            the path of the file to deserialize
	 * @return {@link CuteProject}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static CuteProject deSerializeFromFile(String path) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		CuteProject project = null;
		try {
			project = mapper.readValue(Files.readAllBytes(Paths.get(path)), CuteProject.class);
		} catch (JsonGenerationException e) {
			logger.error("CuteProject opening fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error("CuteProject opening fail: mapping error. Can't deserialize Project file. It has different or outdated format " + path);
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error("CuteProject opening fail: IO error " + path);
			throw e;
		}
		logger.info("CuteProject opening success");
		return project;
	}

	/**
	 * Tells if {@link CuteProject} class is written nicely by programmer and can be serialized. <br>
	 *
	 * @return true, if {@link CuteProject} can be serialized
	 */
	public static boolean canSerializeProjectClass() {
		ObjectMapper mapper = new ObjectMapper();
		boolean bool = mapper.writerWithDefaultPrettyPrinter().canSerialize(CuteProject.class);
		return bool;
	}
}
