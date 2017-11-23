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
package org.spf4j.zel.vm;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.spf4j.base.Pair;
import org.spf4j.zel.instr.CALLA;
import org.spf4j.zel.instr.Instruction;
import org.spf4j.zel.vm.ParsingContext.Location;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public final class ProgramBuilder {

    private static final int DEFAULT_SIZE = 16;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private Instruction[] instructions;

    private final List<Location> debugInfo;

    private int instrNumber;

    private Program.Type type;

    private Program.ExecutionType execType;

    private final Interner<String> stringInterner;

    private final MemoryBuilder staticMemBuilder;

    public static int generateID() {
        return COUNTER.getAndIncrement();
    }
    /**
     * initializes the program
     */
    public ProgramBuilder(final MemoryBuilder staticMemBuilder) {
        this.staticMemBuilder = staticMemBuilder;
        instructions = new Instruction[DEFAULT_SIZE];
        instrNumber = 0;
        type = Program.Type.NONDETERMINISTIC;
        execType = null; //Program.ExecutionType.ASYNC;
        stringInterner = Interners.newStrongInterner();
        debugInfo = new ArrayList<>();
    }

    public void intern(final Object[] array) {
        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];
            if (obj instanceof String) {
                    array[i] = stringInterner.intern((String) obj);
            }
        }
    }

    /**
     * @return the type
     */
    public Program.Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public ProgramBuilder setType(final Program.Type ptype) {
        this.type = ptype;
        return this;
    }

    public ProgramBuilder setExecType(final Program.ExecutionType pexecType) {
        this.execType = pexecType;
        return this;
    }



    public ProgramBuilder add(final Instruction object, final Location loc) {
        ensureCapacity(instrNumber + 1);
        instructions[instrNumber++] = object;
        debugInfo.add(loc);
        return this;
    }

    public boolean contains(final Class<? extends Instruction> instr) {
        Boolean res = itterate(new Program.HasClass(instr));
        if (res == null) {
            return false;
        }
        return res;
    }

    public <T> T itterate(final Function<Object, T> func) {
         for (int i = 0; i < instrNumber; i++) {
            Instruction code = instructions[i];
            T res = func.apply(code);
            if (res != null) {
                return res;
            }
            for (Object param : code.getParameters()) {
                res = func.apply(param);
                if (res != null) {
                   return res;
                }
                if (param instanceof Program) {
                    res = ((Program) param).itterate(func);
                }
                if (res != null) {
                   return res;
                }

            }
        }
        return null;
    }



    public ProgramBuilder set(final int idx, final Instruction object) {
        ensureCapacity(idx + 1);
        instructions[idx] = object;
        instrNumber = Math.max(idx + 1, instrNumber);
        return this;
    }

    public ProgramBuilder addAll(final Instruction[] objects, final List<Location> debug) {
        ensureCapacity(instrNumber + objects.length);
        System.arraycopy(objects, 0, instructions, instrNumber, objects.length);
        instrNumber += objects.length;
        this.debugInfo.addAll(debug);
        return this;
    }

//    public ProgramBuilder setAll(final int idx, final Object[] objects) {
//        ensureCapacity(idx + objects.length);
//        System.arraycopy(objects, 0, instructions, idx, objects.length);
//        instrNumber = Math.max(objects.length + idx, instrNumber);
//        return this;
//    }

    public ProgramBuilder addAll(final ProgramBuilder opb) {
        ensureCapacity(instrNumber + opb.instrNumber);
        System.arraycopy(opb.instructions, 0, instructions, instrNumber, opb.instrNumber);
        instrNumber += opb.instrNumber;
        this.debugInfo.addAll(opb.debugInfo);
        return this;
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary, to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void ensureCapacity(final int minCapacity) {
        int oldCapacity = instructions.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            instructions = Arrays.copyOf(instructions, newCapacity);
        }
    }

    public int size() {
        return instrNumber;
    }

    public Object[] toArray() {
        return Arrays.copyOf(instructions, instrNumber);
    }

    public boolean hasDeterministicFunctions() {
        Boolean hasDetFuncs = itterate(new HasDeterministicFunc());
        if (hasDetFuncs == null) {
            hasDetFuncs = Boolean.FALSE;
        }
        return hasDetFuncs;
    }

    public boolean hasAsyncCalls() {
         Boolean hasAsyncCalls = itterate(new HasAsyncFunc());
        if (hasAsyncCalls == null) {

            return false;
        }
        return hasAsyncCalls;
    }

    public Program toProgram(final String name, final String source, final String[] parameterNames)
            throws CompileException {
       return toProgram(name, source, parameterNames, Collections.EMPTY_MAP);
    }

    public Program toProgram(final String name, final String source, final String[] parameterNames,
            final Map<String, Integer> localTable)
            throws CompileException {
        intern(instructions);
        intern(parameterNames);
        Pair<Object[], Map<String, Integer>> build = staticMemBuilder.build();
        boolean hasAsyncPrograms = false;
        for (Object obj : build.getFirst()) {
            if (obj instanceof Program) {
                Program prog = (Program) obj;
                if (prog.getExecType() == Program.ExecutionType.ASYNC) {
                    hasAsyncPrograms = true;
                    break;
                }
            }
        }
        return new Program(name, build.getSecond(), build.getFirst(), localTable, instructions,
                debugInfo.toArray(new Location[debugInfo.size()]), source,  0, instrNumber, type,
                hasAsyncPrograms || this.execType == Program.ExecutionType.ASYNC || hasAsyncCalls()
                        ? (this.execType == null ? Program.ExecutionType.ASYNC : this.execType)
                        : Program.ExecutionType.SYNC,
                hasDeterministicFunctions(), parameterNames);
    }



    public Program toProgram(final String name, final String source, final List<String> parameterNames)
            throws CompileException {
        return toProgram(name, source, parameterNames.toArray(new String[parameterNames.size()]));
    }

    private static final class HasDeterministicFunc implements Function<Object, Boolean> {

        @Override
        @SuppressFBWarnings({ "TBP_TRISTATE_BOOLEAN_PATTERN", "NP_BOOLEAN_RETURN_NULL" })
        public Boolean apply(final Object input) {
            if (input instanceof Program) {
                Program prog = (Program) input;
                if (prog.getType() == Program.Type.DETERMINISTIC
                        || prog.hasDeterministicFunctions()) {
                    return Boolean.TRUE;
                }
            }
            return null;
        }
    }

    private static final class HasAsyncFunc implements Function<Object, Boolean> {

        @Override
        @SuppressFBWarnings({ "TBP_TRISTATE_BOOLEAN_PATTERN",
          "ITC_INHERITANCE_TYPE_CHECKING", "NP_BOOLEAN_RETURN_NULL" })
        public Boolean apply(final Object input) {
            if (input instanceof Program) {
                Program prog = (Program) input;
                if (prog.getExecType() == Program.ExecutionType.ASYNC) {
                    return Boolean.TRUE;
                }
            } else if (input instanceof CALLA) {
                return Boolean.TRUE;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "ProgramBuilder{" + "instructions=" + Arrays.toString(instructions) + ", debugInfo=" + debugInfo
                + ", instrNumber=" + instrNumber + ", type=" + type + ", execType=" + execType
                + ", stringInterner=" + stringInterner + ", staticMemBuilder=" + staticMemBuilder + '}';
    }



}
