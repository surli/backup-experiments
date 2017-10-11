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

import org.spf4j.perf.MeasurementsInfo;
import java.util.Arrays;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author zoly
 */
@Immutable
public final class MeasurementsInfoImpl implements MeasurementsInfo {
    private final Object measuredEntity;
    private final String description;
    private final String[] measurementNames;
    private final String[] measurementUnits;



    public MeasurementsInfoImpl(final Object measuredEntity, final String description,
            final String[] measurementNames, final String[] measurementUnits) {
        this.measuredEntity = measuredEntity;
        this.description = description;
        this.measurementNames = measurementNames.clone();
        this.measurementUnits = measurementUnits.clone();
    }

    @Override
    public Object getMeasuredEntity() {
        return measuredEntity;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getMeasurementNames() {
        return measurementNames.clone();
    }

    @Override
    public int hashCode() {
        return measuredEntity.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MeasurementsInfoImpl other = (MeasurementsInfoImpl) obj;
        if (this.measuredEntity != other.measuredEntity
                && (this.measuredEntity == null || !this.measuredEntity.equals(other.measuredEntity))) {
            return false;
        }
        if ((this.description == null)
                ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        return Arrays.equals(this.measurementNames, other.measurementNames);
    }

    @Override
    public String toString() {
        return "EntityMeasurementsInfoImpl{" + "measuredEntity=" + measuredEntity
                + ", description=" + description + ", measurementNames="
                + Arrays.toString(measurementNames) + ", measurementUnits="
                + Arrays.toString(measurementUnits) + '}';
    }


    @Override
    public int getNumberOfMeasurements() {
        return measurementNames.length;
    }

    @Override
    public String[] getMeasurementUnits() {
        return measurementUnits.clone();
    }

    @Override
    public String getMeasurementName(final int measurementNr) {
        return measurementNames[measurementNr];
    }

    @Override
    public String getMeasurementUnit(final int measurementNr) {
        return measurementUnits[measurementNr];
    }


}
