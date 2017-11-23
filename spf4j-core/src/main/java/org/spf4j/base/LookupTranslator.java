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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Objects;

/**
 * Translates a value using a lookup table.
 *
 * @since 3.0
 * @version $Id$
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("CLI_CONSTANT_LIST_INDEX")
public final class LookupTranslator extends CharSequenceTranslator {

    private final HashMap<String, CharSequence> lookupMap;
    private final int shortest;
    private final int longest;


    public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<>();
        int ashortest = Integer.MAX_VALUE;
        int alongest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                this.lookupMap.put(seq[0].toString(), seq[1]);
                final int sz = seq[0].length();
                if (sz < ashortest) {
                    ashortest = sz;
                }
                if (sz > alongest) {
                    alongest = sz;
                }
            }
        }
        shortest = ashortest;
        longest = alongest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        int max = longest;
        final int l = input.length();
        if (index + longest > l) {
            max = l - index;
        }
        // descend so as to get a greedy algorithm
        for (int i = max; i >= shortest; i--) {
            final CharSequence subSeq = input.subSequence(index, index + i);
            final CharSequence result = lookupMap.get(subSeq.toString());
            if (result != null) {
                out.write(result.toString());
                return i;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "LookupTranslator{" + "lookupMap=" + lookupMap
                + ", shortest=" + shortest + ", longest=" + longest + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.lookupMap);
        hash = 73 * hash + this.shortest;
        return 73 * hash + this.longest;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LookupTranslator other = (LookupTranslator) obj;
        if (this.shortest != other.shortest) {
            return false;
        }
        if (this.longest != other.longest) {
            return false;
        }
        return Objects.equals(this.lookupMap, other.lookupMap);
    }





}

