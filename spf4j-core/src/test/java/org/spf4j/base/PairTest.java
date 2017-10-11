/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
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
package org.spf4j.base;

import java.util.List;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zoly
 */
public final class PairTest {

    @Test
    public void testToList() {
        Pair<String, String> pair1 = Pair.of("5", ", adf\"klf ");
        Pair<String, String> pair2 = Pair.from(pair1.toString());
        Assert.assertEquals(pair1, pair2);
    }

    @Test
    public void testToMap() {
        Pair<String, String> pair1 = Pair.of("5", ", adf\"klf ");
        Pair<String, LocalDate> pair2 = Pair.of("bla", new LocalDate());
        Assert.assertEquals(2, Pair.asMap(pair1, pair2).size());
    }

    @Test
    public void testNull() {
        Pair<String, String> pair1 = Pair.of(null, null);
        Assert.assertEquals(",", pair1.toString());
         List<Object> toList = pair1.toList();
        Assert.assertNull(toList.get(0));
        Assert.assertNull(toList.get(1));
    }

}