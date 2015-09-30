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
package com.ubershy.streamsis;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends {@link Random}. It ensures that a new generated value is not same as the previous one.
 */
public class NonRepeatingRandom extends Random {

	static final Logger logger = LoggerFactory.getLogger(NonRepeatingRandom.class);

	private static final long serialVersionUID = 660787213049452838L;

	/** The value which we compare with new value to be sure that the new value is different */
	protected int lastInt = -1;

	@Override
	public int nextInt(int bound) {
		int result;
		do {
			result = super.nextInt(bound);
		} while (result == lastInt && bound > 1);
		lastInt = result;
		return result;
	}

	@Override
	public int nextInt() {
		int result;
		do {
			result = super.nextInt();
		} while (result != lastInt);
		lastInt = result;
		return result;
	}
}
