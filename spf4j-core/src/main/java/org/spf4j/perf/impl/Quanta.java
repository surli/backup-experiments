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
package org.spf4j.perf.impl;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * this class ordering is based on start Interval ordering
 */
@Immutable
public final class Quanta implements Comparable<Quanta>, Serializable {

    private static final long serialVersionUID = 1L;

    private final long intervalStart;
    private final long intervalEnd;

    public Quanta(final long intervalStart, final long intervalEnd) {
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public Quanta(@Nonnull final String stringVariant) {
        int undLocation = stringVariant.indexOf('_');
        if (undLocation < 0) {
            throw new IllegalArgumentException("Invalid Quanta DataSource " + stringVariant);
        }
        String startStr = stringVariant.substring(1, undLocation);
        String endStr = stringVariant.substring(undLocation + 1);
        if ("NI".equals(startStr)) {
            this.intervalStart = Long.MIN_VALUE;
        } else {
            this.intervalStart = Long.parseLong(startStr);
        }
        if ("PI".equals(endStr)) {
            this.intervalEnd = Long.MAX_VALUE;
        } else {
            this.intervalEnd = Long.parseLong(endStr);
        }
    }

    public long getIntervalEnd() {
        return intervalEnd;
    }

    public long getIntervalStart() {
        return intervalStart;
    }

    public long getClosestToZero() {
        return (intervalStart < 0) ? intervalEnd : intervalStart;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(16);
        result.append('Q');
        if (intervalStart == Long.MIN_VALUE) {
            result.append("NI");
        } else {
            result.append(intervalStart);
        }
        result.append('_');
        if (intervalEnd == Long.MAX_VALUE) {
            result.append("PI");
        } else {
            result.append(intervalEnd);
        }
        return result.toString();
    }

    @Override
    public int compareTo(final Quanta o) {
        if (this.intervalStart < o.intervalStart) {
            return -1;
        } else if (this.intervalStart > o.intervalStart) {
            return 1;
        } else {
            if (this.intervalEnd < o.intervalEnd) {
                return -1;
            } else if (this.intervalEnd > o.intervalEnd) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.intervalStart ^ (this.intervalStart >>> 32));
        return 89 * hash + (int) (this.intervalEnd ^ (this.intervalEnd >>> 32));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Quanta other = (Quanta) obj;
        return this.compareTo(other) == 0;
    }

}
