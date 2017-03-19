/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;


/**
 * {@link TxAndTimerDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxAndTimerDrivenModule}s with no configuration. Singleton.
 */
public final class NullTxAndTimerDrivenModuleConfiguration implements TxAndTimerDrivenModuleConfiguration {

    static {
        Serializer.register(NullTxAndTimerDrivenModuleConfiguration.class, new SingletonSerializer(), 1020);
    }

    private static final NullTxAndTimerDrivenModuleConfiguration INSTANCE = new NullTxAndTimerDrivenModuleConfiguration();

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static NullTxAndTimerDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxAndTimerDrivenModuleConfiguration() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstanceRolePolicy getInstanceRolePolicy() {
        return NullTimerDrivenModuleConfiguration.getInstance().getInstanceRolePolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InclusionPolicies getInclusionPolicies() {
        return NullTxDrivenModuleConfiguration.getInstance().getInclusionPolicies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long initializeUntil() {
        return NullTxDrivenModuleConfiguration.getInstance().initializeUntil();
    }
}
