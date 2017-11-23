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
package org.spf4j.perf.impl.ms.tsdb;

import com.google.common.base.Charsets;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.spf4j.perf.MeasurementsInfo;
import org.spf4j.perf.MeasurementStore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.ThreadSafe;
import org.spf4j.io.Csv;
import org.spf4j.jmx.JmxExport;
import org.spf4j.perf.impl.ms.Id2Info;

/**
 * File based store implementation.
 *
 * @author zoly
 */
@ThreadSafe
public final class TSDBTxtMeasurementStore
        implements MeasurementStore {

  private static final Interner<String> INTERNER = Interners.newStrongInterner();

  private final BufferedWriter writer;

  private final String fileName;

  public TSDBTxtMeasurementStore(final File file) throws IOException {
    this.writer = new BufferedWriter(new OutputStreamWriter(
            Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND),
            Charsets.UTF_8));
    this.fileName = INTERNER.intern(file.getPath());
  }

  @Override
  public long alocateMeasurements(final MeasurementsInfo measurement,
          final int sampleTimeMillis) {
    return Id2Info.getId(measurement);
  }

  @Override
  public void saveMeasurements(final long tableId,
          final long timeStampMillis, final long... measurements)
          throws IOException {
    MeasurementsInfo measurementInfo = Id2Info.getInfo(tableId);
    String groupName = measurementInfo.getMeasuredEntity().toString();
    synchronized (fileName) {
      Csv.writeCsvElement(groupName, writer);
      writer.write(',');
      writer.write(Long.toString(timeStampMillis));
//            writer.write(',');
//            writer.write(Integer.toString(sampleTimeMillis));
      for (int i = 0; i < measurements.length; i++) {
        String measurementName = measurementInfo.getMeasurementName(i);
        writer.write(',');
        Csv.writeCsvElement(measurementName, writer);
        writer.write(',');
        writer.write(Long.toString(measurements[i]));
      }
      writer.write('\n');
    }
  }

  @PreDestroy
  @Override
  public void close() throws IOException {
    writer.close();
  }

  @JmxExport(description = "flush out buffers")
  @Override
  public void flush() throws IOException {
    writer.flush();
  }

  @Override
  public String toString() {
    return "TSDBTxtMeasurementStore{" + "fileName=" + fileName + '}';
  }

}
