/*
 * Created on Aug 22, 2011
 * Copyright 2010 by Eduard Weissmann (edi.weissmann@gmail.com).
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
package org.sejda.cli.model;

import org.sejda.conversion.DirectoryOutputAdapter;
import org.sejda.conversion.ExistingOutputPolicyAdapter;

import com.lexicalscope.jewel.cli.Option;

/**
 * 
 * Base interface for specifying of the command line interface for tasks that have output configured as a directory
 * 
 * @author Eduard Weissmann
 * 
 */
public interface CliArgumentsWithDirectoryOutput extends TaskCliArguments {

    @Option(shortName = "o", description = "output directory (required)")
    DirectoryOutputAdapter getOutput();

    @Option(shortName = "j", description = "policy to use when an output file with the same name already exists. {overwrite, skip, fail, rename}. Default is 'fail' (optional)", defaultValue = "fail")
    ExistingOutputPolicyAdapter getExistingOutput();

    @Option(description = "overwrite existing output files. (shorthand for -j overwrite) (optional)")
    boolean getOverwrite();
}
