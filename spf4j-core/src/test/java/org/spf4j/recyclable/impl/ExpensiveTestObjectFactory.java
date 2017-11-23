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
package org.spf4j.recyclable.impl;

import org.spf4j.recyclable.ObjectDisposeException;
import org.spf4j.recyclable.RecyclingSupplier;
import java.io.IOException;
import org.apache.http.ConnectionClosedException;

/**
 *
 * @author zoly
 */
public final class ExpensiveTestObjectFactory implements RecyclingSupplier.Factory<ExpensiveTestObject> {

    private final long maxIdleMillis;
    private final int nrUsesToFailAfter;
    private final long  minOperationMillis;
    private final long maxOperationMillis;

    public ExpensiveTestObjectFactory(final long maxIdleMillis, final int nrUsesToFailAfter,
            final long minOperationMillis, final long maxOperationMillis) {
        this.maxIdleMillis = maxIdleMillis;
        this.nrUsesToFailAfter = nrUsesToFailAfter;
        this.minOperationMillis = minOperationMillis;
        this.maxOperationMillis = maxOperationMillis;
    }

    public ExpensiveTestObjectFactory() {
        this(100, 10, 1, 20);
    }



    @Override
    public ExpensiveTestObject create() {
        return new ExpensiveTestObject(maxIdleMillis, nrUsesToFailAfter, minOperationMillis, maxOperationMillis);
    }

    @Override
    public void dispose(final ExpensiveTestObject object) throws ObjectDisposeException {
        try {
            object.close();
        } catch (ConnectionClosedException ex) {
          // connection is already closed.
        } catch (IOException ex) {
            throw new ObjectDisposeException(ex);
        }
    }

    @Override
    public boolean validate(final ExpensiveTestObject object, final Exception e) throws IOException {
       if (e instanceof IOException) {
           return false;
       } else {
            object.testObject();
            return true;
       }
    }

  @Override
  public String toString() {
    return "ExpensiveTestObjectFactory{" + "maxIdleMillis=" + maxIdleMillis
            + ", nrUsesToFailAfter=" + nrUsesToFailAfter + ", minOperationMillis="
            + minOperationMillis + ", maxOperationMillis=" + maxOperationMillis + '}';
  }



}
