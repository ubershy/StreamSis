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
package com.ubershy.streamsis.networking.responses;

import javax.json.JsonObject;

import com.ubershy.streamsis.networking.clients.TypicalClient;

/**
 * Just a typical Response from the {@link TypicalClient}.
 */
public class Response {
	
	/** The text describing the error. Can be null, if there's no error. */
	private final String errorText;
	
	/**
	 * Contains raw response in JSON format. JSON is one of the most popular formats. Can be null,
	 * if no actual response came from the Streaming Program. In this case the {@link #errorText}
	 * will have the error message.
	 */
	private final JsonObject rawData;
	
	/**
	 * Instantiates a new response.
	 *
	 * @param errorText
	 *            The text describing the error. Can be null, if there's no error.
	 * @param rawData
	 *            The raw response in JSON format, if possible. If not, response from the client
	 *            needs to be converted to {@link JsonObject}. Can be null.
	 * @throws IllegalArgumentException
	 *             If both parameters are null at the same time.
	 */
	public Response(String errorText, JsonObject rawData){
		if ((rawData == null) && (errorText == null))
			throw new IllegalArgumentException("Both arguments can't be null.");
		this.errorText = errorText;
		this.rawData = rawData;
	}
	
	/**
	 * Gets the text describing the error. Can be null, if there's no error.
	 *
	 * @return The text describing the error. Can be null, if there's no error.
	 */
	public String getErrorText() {
		return errorText;
	}
	
	/**
	 * Gets the raw response in JSON format.
	 *
	 * @return The raw response in JSON format.
	 * @see {@link #rawData} for more information.
	 */
	public JsonObject getRawData() {
		return rawData;
	}

}