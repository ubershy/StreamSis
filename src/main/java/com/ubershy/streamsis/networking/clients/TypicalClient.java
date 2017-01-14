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
package com.ubershy.streamsis.networking.clients;

import com.ubershy.streamsis.networking.ConnectionMaintainer;
import com.ubershy.streamsis.networking.ConnectionStatus;
import com.ubershy.streamsis.networking.responses.GetSceneNameResponse;
import com.ubershy.streamsis.networking.responses.Response;

import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Just a TypicalClient for connecting to web interfaces of Streaming Programs. Contains only common
 * methods shared across such programs like scene change method, for example, because most of the
 * Streaming Programs have "scenes".
 */
public interface TypicalClient {

	/**
	 * Gets the current scene name in the Streaming Program.
	 *
	 * @return The current scene name in the Streaming Program.
	 */
	GetSceneNameResponse getSceneName();

	/**
	 * Sets the current scene name in the Streaming Program to the one specified by name.
	 *
	 * @param name
	 *            The name of the scene to set as current in the Streaming Program.
	 * @return The response containing the information, was it successful or not.
	 */
	Response setSceneName(String name);
	
	/**
	 * The read-only {@link ConnectionStatus} property. Used by {@link ConnectionMaintainer} and
	 * also shown in GUI.
	 *
	 * @return The read-only {@link ConnectionStatus} property.
	 */
	ReadOnlyObjectProperty<ConnectionStatus> statusProperty();

	/**
	 * Connects to the Streaming Program.
	 */
	void connect();
	
	/**
	 * Disconnects from the the Streaming Program.
	 */
	void disconnect();

}