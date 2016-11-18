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
package com.ubershy.streamsis.gui.controllers.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * The AbstractCuteController. Implements partially {@link CuteElementController}.
 */
public abstract class AbstractCuteController implements CuteController {

	/** The button states manager. */
	protected CuteButtonsStatesManager buttonStateManager;

	/** The remembered bidirectional binds. */
	private Map<Property<?>, Property<?>> rememberedBidirectionalBinds = new HashMap<Property<?>, 
			Property<?>>();
	
	/** The remembered normal binds. */
	private List<Property<?>> rememberedNormalBinds = new ArrayList<Property<?>>();

	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		this.buttonStateManager = buttonStateManager;
	}

	/**
	 * Binds properties bidirectionally and remembers these binds, so all of them can be simply
	 * unbound lated by {@link #unbindAllRememberedBinds()} method.
	 *
	 * @param <T>
	 *            The type of wrapped value of property.
	 * @param a
	 *            The property "A" to bind with "B".
	 * @param b
	 *            The property "B" to bind with "A".
	 */
	protected <T> void bindBidirectionalAndRemember(Property<T> a, Property<T> b) {
		T value = a.getValue();
		if (!(value instanceof String || value instanceof Integer || value instanceof Double
				|| value instanceof Boolean || value instanceof Float)) {
			throw new RuntimeException("Eww. Unsupported property type.");
		}
		a.bindBidirectional(b);
		rememberedBidirectionalBinds.put(a, b);
	}
	
	/**
	 * Binds properties normally and remembers these binds, so all of them can be simply
	 * unbound lated by {@link #unbindAllRememberedBinds()} method.
	 *
	 * @param <T>
	 *            The type of wrapped value of property.
	 * @param a
	 *            The property "A" to bind to "B".
	 * @param b
	 *            The ObservableValue "B".
	 */
	protected <T> void bindNormalAndRemember(Property<T> a, ObservableValue<T> b) {
		T value = a.getValue();
		if (!(value instanceof String || value instanceof Integer || value instanceof Double
				|| value instanceof Boolean || value instanceof Float)) {
			throw new RuntimeException("Eww. Unsupported property type.");
		}
		a.bind(b);
		rememberedNormalBinds.add(a);
	}

	/**
	 * Unbinds all remembered binds. <br>
	 * Normal binds can be set and remembered by
	 * {@link #bindNormalAndRemember(Property, Property)} method. <br>
	 * Bidirectional binds can be set and remembered by
	 * {@link #bindBidirectionalAndRemember(Property, Property)} method.
	 */
	@SuppressWarnings("unchecked")
	protected void unbindAllRememberedBinds() {
		// Cleanup normal binds
		for (Property<?> propa : rememberedNormalBinds) {
			propa.unbind();
		}
		rememberedNormalBinds.clear();
		// Cleanup bidirectional binds
		for (Entry<Property<?>, Property<?>> entry : rememberedBidirectionalBinds.entrySet()) {
			Property<?> a = entry.getKey();
			Property<?> b = entry.getValue();
			Class<? extends Object> type = a.getValue().getClass();
			// TODO: somehow make this universal, not only for Strings, Integers and Doubles.
			if (type.equals(String.class)) {
				Property<String> aString = (Property<String>) a;
				Property<String> bString = (Property<String>) b;
				aString.unbindBidirectional(bString);
			} else if (type.equals(Integer.class)) {
				Property<Integer> aInteger = (Property<Integer>) a;
				Property<Integer> bInteger = (Property<Integer>) b;
				aInteger.unbindBidirectional(bInteger);
			} else if (type.equals(Double.class)) {
				Property<Double> aDouble = (Property<Double>) a;
				Property<Double> bDouble = (Property<Double>) b;
				aDouble.unbindBidirectional(bDouble);
			} else if (type.equals(Boolean.class)) {
				Property<Boolean> aBoolean = (Property<Boolean>) a;
				Property<Boolean> bBoolean = (Property<Boolean>) b;
				aBoolean.unbindBidirectional(bBoolean);
			} else if (type.equals(Float.class)) {
				Property<Float> aFloat = (Property<Float>) a;
				Property<Float> bFloat = (Property<Float>) b;
				aFloat.unbindBidirectional(bFloat);
			} else {
				throw new RuntimeException("Eww. Unsupported property type.");
			}
		}
		rememberedBidirectionalBinds.clear();
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setInputAllowed(boolean allowInput) {
		this.getView().setDisable(!allowInput); // default behavior
	}

}
