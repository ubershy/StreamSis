/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Stuff Serializator is a class for serializing/deserializing stuff.
 * <br>
 * Can be useful for making full copies of {@link CuteElement}s. And much more!
 */
public final class StuffSerializator {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(StuffSerializator.class);

	/**
	 * Serialize Object to a string.
	 *
	 * @param object
	 *            The object to serialize.
	 * @return The string in JSON format
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String serializeToString(Object object, boolean pretty) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String serialized = null;
		try {
			if (pretty)
				serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			else
				serialized = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			logger.error(object + "object serializing to string fail: JsonGeneration error.");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error(object + "object serializing to string fail: Mapping error.");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error(object + "object serializing to string fail: IO error.");
			throw e;
		}
		logger.debug(object + "object serializing to string success.");
		return serialized;
	}

	/**
	 * Deserialize Object from a string with all it's children.
	 *
	 * @param serialized
	 *            the string in JSON format
	 * @param clazz
	 *            the expected class of deserialized object
	 * @return the CuteElement
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Object deserializeFromString(String serialized, Class<?> clazz)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		Object object = null;
		try {
			object = mapper.readValue(serialized, clazz);
		} catch (JsonGenerationException e) {
			logger.error(clazz.getSimpleName() + " deserialization fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error(clazz.getSimpleName()
					+ " deserialization fail: mapping error. Different or outdated format");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error(clazz.getSimpleName() + " deserialization fail: IO error");
			throw e;
		}
		logger.info(clazz.getSimpleName() + " deserialization success");
		return object;
	}
	
	public static Object makeACopyOfObjectUsingSerialization(Object object) throws IOException {
		String serialized = serializeToString(object, false);
		Object copy = deserializeFromString(serialized, object.getClass());
		return copy;
	}

	/**
	 * Tells if {@link CuteElement} class is written nicely by programmer and can be serialized.
	 *
	 * @return true, if {@link CuteElement} can be serialized
	 */
	public static boolean canSerializeCuteElementClass() {
		ObjectMapper mapper = new ObjectMapper();
		boolean bool = mapper.writerWithDefaultPrettyPrinter().canSerialize(CuteElement.class);
		return bool;
	}
}
