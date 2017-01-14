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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.networking.ConnectionStatus;
import com.ubershy.streamsis.networking.responses.GetSceneNameResponse;
import com.ubershy.streamsis.networking.responses.Response;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * NoneClient is a stub class that is used when networking client that is not set.
 */
public class NoneClient implements TypicalClient {
	
	static final Logger logger = LoggerFactory.getLogger(NoneClient.class);
	
	private static final String errorText = "Streaming Program is not set. Doing nothing.";
	
	private ReadOnlyObjectWrapper<ConnectionStatus> status = new ReadOnlyObjectWrapper<ConnectionStatus>(
			ConnectionStatus.OFFLINE);

	public ReadOnlyObjectProperty<ConnectionStatus> statusProperty() {
		return status.getReadOnlyProperty();
	}

	@Override
	public GetSceneNameResponse getSceneName() {
		return new GetSceneNameResponse(errorText, null, null);
	}

	@Override
	public Response setSceneName(String name) {
		return new Response(errorText, null);
	}

	@Override
	public void connect() {
		logger.info("Got request to connect. " + errorText);
	}

	@Override
	public void disconnect() {
		logger.info("Got request to disconnect. " + errorText);
	}

}
