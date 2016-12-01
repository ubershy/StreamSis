package com.ubershy.streamsis.networking.requests;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Request {
	@JsonProperty("Command")
	private String command;
	
	@JsonProperty("Data")
	protected Map<String, String> data = new TreeMap<>();

	public Request(String command) {
		this.command = command;
	}
}
