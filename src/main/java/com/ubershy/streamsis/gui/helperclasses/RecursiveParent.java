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
package com.ubershy.streamsis.gui.helperclasses;

import javafx.collections.ObservableList;

/**
 * The interface that might have children of interfaces that inherit from this interface. <br>
 * Whatever that means.
 * <p>
 * RecursiveParent can be useful when building TreeViews in GUI.
 * <p>
 * 
 * It's {@link #getChildren()} method must return <b>null</b> if the particular type <b>can't</b> have children. <br>
 * And if the particular type is <b>fertile</b>, but currently don't have children, the {@link #getChildren()} <br>
 * method must return an <b>empty list</b>.
 * 
 * @param <T>
 *            the generic type that extends from RecursiveParent
 */
public interface RecursiveParent<T extends RecursiveParent<?>> {

	/**
	 * Gets the children from school.
	 *
	 * @return <b>the children</b> to home <br>
	 *         <b>empty list</b> if it don't have children currently. <br>
	 *         <b>null</b> if it can't have children.
	 */
	ObservableList<? extends T> getChildren();
}