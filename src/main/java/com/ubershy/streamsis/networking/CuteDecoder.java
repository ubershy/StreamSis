package com.ubershy.streamsis.networking;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class CuteDecoder implements Decoder.Text<JsonObject> {
	
	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public JsonObject decode(String s) throws DecodeException {
		JsonObject json = null;
		try {
			json = Json.createReader(new StringReader(s)).readObject();
		} catch (Exception e) {
			throw new DecodeException(s, "Can't decode payload");
		}
		return json;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
