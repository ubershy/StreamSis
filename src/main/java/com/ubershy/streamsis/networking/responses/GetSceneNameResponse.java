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

/**
 * The response that contains the name of the current scene of the Streaming Program.
 */
public class GetSceneNameResponse extends Response {
	
	private final String sceneName;
	
	/**
	 * Gets the name of the current scene of the Streaming Program.
	 *
	 * @return The name of the current scene of the Streaming Program.
	 */
	public String getSceneName() {return sceneName;}

	public GetSceneNameResponse(String errorText, JsonObject rawData, String sceneName) {
		super(errorText, rawData);
		this.sceneName = sceneName;
	}

}
