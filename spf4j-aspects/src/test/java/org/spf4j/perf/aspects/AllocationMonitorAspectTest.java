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
package org.spf4j.perf.aspects;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import org.spf4j.perf.impl.RecorderFactory;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.spf4j.perf.impl.ms.tsdb.TSDBMeasurementStore;
import org.spf4j.perf.io.OpenFilesSampler;
import org.spf4j.perf.memory.MemoryUsageSampler;
import org.spf4j.perf.memory.TestClass;
import org.spf4j.tsdb2.TSDBQuery;
import org.spf4j.tsdb2.TSDBWriter;
import org.spf4j.tsdb2.avro.TableDef;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("MDM_THREAD_YIELD")
public final class AllocationMonitorAspectTest {

    private static void testAllocInStaticContext() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            System.err.println("S" + i + Strings.repeat("A", i % 2 * 2));
            if (i % 100 == 0) {
                Thread.sleep(100);
            }
        }
    }

    /**
     * Test of afterAllocation method, of class AllocationMonitorAspect.
     */
    @Test
    public void testAfterAllocation() throws InterruptedException, IOException {
        System.setProperty("spf4j.perf.allocations.sampleTimeMillis", "1000");
        MemoryUsageSampler.start(500);
        OpenFilesSampler.start(500, 512, 1000, false);
        for (int i = 0; i < 1000; i++) {
            System.err.println("T" + i);
            if (i % 100 == 0) {
                Thread.sleep(500);
            }
        }
        testAllocInStaticContext();
        TestClass.testAllocInStaticContext();
        final TSDBWriter dbWriter = ((TSDBMeasurementStore) RecorderFactory.MEASUREMENT_STORE).getDBWriter();
        dbWriter.flush();
        File file = dbWriter.getFile();
        List<TableDef> tableDef = TSDBQuery.getTableDef(file, "heap-used");
        Assert.assertFalse(tableDef.isEmpty());
        MemoryUsageSampler.stop();
        OpenFilesSampler.stop();
    }
}
