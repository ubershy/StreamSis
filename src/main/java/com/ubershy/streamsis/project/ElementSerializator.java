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
 * Element Serializator is a class for serializing/deserializing {@link CuteElement CuteElements}.
 * <br>
 * Can be useful for making full copies of CuteElements.
 */
public final class ElementSerializator {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(ElementSerializator.class);

	/**
	 * Serialize {@link CuteElement} to a string with all it's children.
	 *
	 * @param element
	 *            the {@link CuteElement} to serialize
	 * @return the string in JSON format
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String serializeToString(CuteElement element) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String serialized = null;
		try {
			serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(element);
		} catch (JsonGenerationException e) {
			logger.error("CuteElement serializing to string fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error("CuteElement serializing to string fail: Mapping error");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error("CuteElement serializing to string fail: IO error");
			throw e;
		}
		logger.debug("CuteElement serializing to string success");
		return serialized;
	}

	/**
	 * Deserialize {@link CuteElement} from a string with all it's children.
	 *
	 * @param serialized
	 *            the string in JSON format
	 * @return the CuteElement
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static CuteElement deserializeFromString(String serialized) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		CuteElement element = null;
		try {
			element = mapper.readValue(serialized, CuteElement.class);
		} catch (JsonGenerationException e) {
			logger.error("CuteElement deserialization fail: JsonGeneration error");
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			logger.error("CuteElement deserialization fail: mapping error. Different or outdated"
					+ "format");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			logger.error("CuteElement deserialization fail: IO error");
			throw e;
		}
		logger.info("CuteElement deserialization success");
		return element;
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
