/*
 * Created on 09/set/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.model.parameter.base;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sejda.model.output.SingleTaskOutput;
import org.sejda.model.validation.constraint.ValidSingleOutput;

/**
 * Provides a skeletal implementation for parameter classes having a single pdf source as input and producing a {@link SingleTaskOutput}.
 * 
 * @author Andrea Vacondio
 * 
 */
@ValidSingleOutput
public abstract class SinglePdfSourceSingleOutputParameters extends SinglePdfSourceParameters implements
        SingleOutputTaskParameters {

    private String outputName;
    @Valid
    @NotNull
    private SingleTaskOutput output;

    @Override
    public String getOutputName() {
        return outputName;
    }

    @Override
    public SingleTaskOutput getOutput() {
        return output;
    }

    @Override
    public void setOutput(SingleTaskOutput output) {
        this.output = output;
    }

    /**
     * @param outputName
     *            the outputName to be used when the output is not a file destination
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(outputName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SinglePdfSourceSingleOutputParameters)) {
            return false;
        }
        SinglePdfSourceSingleOutputParameters parameter = (SinglePdfSourceSingleOutputParameters) other;
        return new EqualsBuilder().appendSuper(super.equals(other)).append(outputName, parameter.getOutputName())
                .isEquals();
    }
}
