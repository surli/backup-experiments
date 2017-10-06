/*
 *
 * MariaDB Client for Java
 *
 * Copyright (c) 2012-2014 Monty Program Ab.
 * Copyright (c) 2015-2017 MariaDB Ab.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to Monty Program Ab info@montyprogram.com.
 *
 */

package org.mariadb.jdbc.internal.util.pool;

import org.mariadb.jdbc.UrlParser;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Pools {

    private static final AtomicInteger poolIndex = new AtomicInteger();
    private static final Map<UrlParser, Pool> poolMap = new ConcurrentHashMap<>();

    /**
     * Get existing pool for a configuration. Create it if doesn't exists.
     *
     * @param urlParser configuration parser
     * @return pool
     * @throws SQLException if any error occur
     */
    public static Pool retrievePool(UrlParser urlParser) throws SQLException {
        if (!poolMap.containsKey(urlParser)) {
            synchronized (poolMap) {
                if (!poolMap.containsKey(urlParser)) {
                    Pool pool = new Pool(urlParser, poolIndex.incrementAndGet());
                    poolMap.put(urlParser, pool);
                    return pool;
                }
            }
        }
        return poolMap.get(urlParser);
    }

    public static void remove(Pool pool) {
        poolMap.remove(pool.getUrlParser());
    }

    /**
     * Close all pools.
     */
    public static void close() {
        for (Pool pool : poolMap.values()) {
            try {
                pool.close();
            } catch (InterruptedException exception) {
                //eat
            }
        }
    }

    /**
     * Closing a pool with name defined in url.
     *
     * @param poolName the option "poolName" value
     */
    public static void close(String poolName) {
        if (poolName == null) return;
        for (Pool pool : poolMap.values()) {
            if (poolName.equals(pool.getUrlParser().getOptions().poolName)) {
                try {
                    pool.close();
                } catch (InterruptedException exception) {
                    //eat
                }
                return;
            }
        }
    }

}
