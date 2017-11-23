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
package org.spf4j.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;

/**
 *
 * @author zoly
 */
public final class SequenceTest {


    @Test
    public void testSequence() throws InterruptedException, ExecutionException {
        if (org.spf4j.base.Runtime.NR_PROCESSORS <= 4) {
            return;
        }
        // warmup JVM
        testSeq(new AtomicSequence(0));
        testSeq(new ScalableSequence(0, 100));
        // measure;
        long timeAtomic = testSeq(new AtomicSequence(0));
        long timeScalable = testSeq(new ScalableSequence(0, 100));
//        Assert.assertTrue("condition " + timeAtomic + " > " + timeScalable, timeAtomic > timeScalable);
        System.out.println("Atomic time "  + timeAtomic);
        System.out.println("Scalable time "  + timeScalable);

    }

    public long testSeq(final Sequence sequence) throws InterruptedException, ExecutionException {
        long startTime = System.currentTimeMillis();
        Future<Long>[] futures = new Future[org.spf4j.base.Runtime.NR_PROCESSORS];
        for (int i = 0; i < org.spf4j.base.Runtime.NR_PROCESSORS; i++) {
            futures[i] = DefaultExecutor.INSTANCE.submit(() -> {
              long last = -1;
              for (int i1 = 0; i1 < 100000; i1++) {
                last = sequence.next();
              }
              return last;
            });
        }
        for (Future<Long> future : futures) {
            System.out.println("Seq" + sequence.getClass() + " " + future.get());
        }
        return System.currentTimeMillis() - startTime;
    }

}
