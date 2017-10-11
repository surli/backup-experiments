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
package org.spf4j.perf.tsdb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.spf4j.base.MutableHolder;
import org.spf4j.concurrent.DefaultExecutor;
import org.spf4j.tsdb2.TimeSeries;

/**
 *
 * @author zoly
 */
@Ignore
@SuppressFBWarnings({ "MDM_THREAD_YIELD", "COMMAND_INJECTION", "PATH_TRAVERSAL_IN" })
public final class TSDBTailerTest {

    private static final String FILE_NAME = System.getProperty("java.io.tmpdir") + "/testdb.tsdb";

    private volatile boolean finish = false;

    /**
     * Test of close method, of class TimeSeriesDatabase.
     */
    @Test
    public void testWriteTSDB() throws Exception {
        System.out.println("testWriteTSDB");
        if (new File(FILE_NAME).delete()) {
            System.out.println("existing tsdb file deleted");
        }
        final TimeSeriesDatabase instance = new TimeSeriesDatabase(new File(FILE_NAME), new byte[]{});
        Future<Integer> tailFut = DefaultExecutor.INSTANCE.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return doTail(instance);
            }

        });

        instance.addTSTable("gr1", new byte[]{}, 5, new String[]{"a", "b"}, new byte[][]{});
        instance.write(System.currentTimeMillis(), "gr1", new long[]{0, 1});
        Thread.sleep(5);
        instance.write(System.currentTimeMillis(), "gr1", new long[]{1, 2});
        Thread.sleep(5);
        instance.write(System.currentTimeMillis(), "gr1", new long[]{3, 4});
        Thread.sleep(5);
        instance.addTSTable("gr2", new byte[]{}, 5, new String[]{"a", "b"}, new byte[][]{});
        instance.write(System.currentTimeMillis(), "gr2", new long[]{7, 8});
        instance.flush();

        instance.addTSTable("gr3", new byte[]{}, 5, new String[]{"a", "b"}, new byte[][]{});
        instance.write(System.currentTimeMillis(), "gr3", new long[]{7, 8});
        Thread.sleep(5);
        instance.write(System.currentTimeMillis(), "gr3", new long[]{9, 10});
        instance.flush();
        Thread.sleep(1000);
        finish = true;
        int result = tailFut.get();
        Assert.assertEquals(6, result);

        final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        final String jvmPath = System.getProperty("java.home")
                + File.separatorChar + "bin" + File.separatorChar + "java";

        Future<Integer> result2 = DefaultExecutor.INSTANCE.submit(() -> {
          String[] command = new String[]{jvmPath, "-cp", classPath, TSDBTailerTest.class.getName(),
            FILE_NAME};
          System.out.println("Running " + Arrays.toString(command));
          Process proc = Runtime.getRuntime().exec(command);
          return proc.waitFor();
        });
        Assert.assertEquals(6, (int) result2.get());

    }

    private Integer doTail(final TimeSeriesDatabase instance) throws IOException {
        final MutableHolder<Integer> counter = new MutableHolder<>(0);
        instance.tail(1, 0, new TSDataHandler() {

            @Override
            public void newTable(final String tableName, final String[] columnNames) {
                System.out.println("New Table: " + tableName
                        + " columns: " + Arrays.toString(columnNames));
            }

            @Override
            public void newData(final String tableName, final TimeSeries data) {
                System.out.println("Table " + tableName + " - " + data);
                counter.setValue(counter.getValue() + data.getTimeStamps().length);
            }

            @Override
            public boolean finish() {
                return finish;
            }
        });
        return counter.getValue();
    }

    public static void main(final String[] parameters) throws IOException {
        final MutableHolder<Integer> counter = new MutableHolder<>(0);
        TimeSeriesDatabase tsdb = new TimeSeriesDatabase(new File(parameters[0]));
        tsdb.tail(10, 0, new TSDataHandler() {

            private int count = 0;

            @Override
            public void newTable(final String tableName, final String[] columnNames) {
                System.out.println("New Table: " + tableName
                        + " columns: " + Arrays.toString(columnNames));
            }

            @Override
            public void newData(final String tableName, final TimeSeries data) {
                System.out.println("Table " + tableName + " - " + data);
                counter.setValue(counter.getValue() + data.getTimeStamps().length);
            }

            @Override
            public boolean finish() {
                return count++ > 400;
            }
        });
        System.exit(counter.getValue());
    }

}
