/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Unit test for {@link TypeOf}
 * @author thierry.bouvet@mpsa.com
 *
 */
public class TypeOfTest {

	/**
	 * Test method for {@link TypeOf#getType()}.
	 */
	@Test
	public void testResult() {
		TypeOf<List<String>> typeOf = new TypeOf<List<String>>() {
		};
		Assertions.assertThat(typeOf.getType().toString()).isEqualTo("java.util.List<java.lang.String>");
		Assertions.assertThat(typeOf.getRawType()).isEqualTo(List.class);

		TypeOf<Long> typeOf2 = new TypeOf<Long>() {
		};
		Assertions.assertThat(typeOf2.getType()).isEqualTo(Long.class);
		Assertions.assertThat(typeOf2.getRawType()).isEqualTo(Long.class);

	}

	/**
	 * Test method for {@link TypeOf#getType()}.
	 * Test a {@link SeedException} if no generic parameter.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testGetTypeWithoutParameterized() {
        StringWriter stringWriter = new StringWriter();
		try {
			new TypeOf() {};
			Assertions.fail("Should throw a SeedException");
		} catch (SeedException e) {
			e.printStackTrace(new PrintWriter(stringWriter));
	        String text = stringWriter.toString();
	        Assertions.assertThat(text).contains("Missing generic parameter");
	        Assertions.assertThat(text).contains("Check that class has generic parameter");
		}
	}

}
