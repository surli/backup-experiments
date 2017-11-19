/*
 * Copyright 2005-2017 Dozer Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dozer.builder.xml;

import java.io.StringReader;

import org.w3c.dom.ls.LSInput;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class InputStreamLSInputTest {

    @Test
    public void canCreate() {
        LSInput input = new InputStreamLSInput("", "http://dozermapper.github.io/schema/bean-mapping.xsd", "", new StringReader("value"));

        assertNotNull(input.getSystemId());
        assertNotNull(input.getByteStream());
        assertNotNull(input.getCharacterStream());
    }
}
