/*
 * Created on 01/mag/2010
 *
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.core.context;

import java.util.Map;
import java.util.Map.Entry;

import org.sejda.core.Sejda;
import org.sejda.core.notification.strategy.NotificationStrategy;
import org.sejda.model.exception.ConfigurationException;
import org.sejda.model.exception.SejdaRuntimeException;
import org.sejda.model.parameter.base.TaskParameters;
import org.sejda.model.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global configuration singleton.
 * <p>
 * A user can submit a configuration including a file named "sejda.xml" in the classpath or using the system property sejda.config.file where the value of the property is the name
 * of the configuration file available in the classpath. If Both are specified then system property has precedence.
 * </p>
 * 
 * @author Andrea Vacondio
 * 
 */
final class GlobalConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfiguration.class);

    private Class<? extends NotificationStrategy> notificationStrategy;
    private TasksRegistry tasksRegistry;
    private boolean validation;
    private boolean ignoreXmlConfiguration;

    private GlobalConfiguration() {
        LOG.info("Configuring Sejda {}", Sejda.VERSION);
        initialize();
        if (LOG.isDebugEnabled()) {
            logConfiguredTasks();
        }
    }

    private void logConfiguredTasks() {
        LOG.trace("Configured tasks:");
        for (@SuppressWarnings("rawtypes")
        Entry<Class<? extends TaskParameters>, Class<? extends Task>> entry : tasksRegistry.getTasks().entrySet()) {
            LOG.trace(String.format("%s executed by -> %s", entry.getKey(), entry.getValue()));
        }
    }

    @SuppressWarnings("rawtypes")
    private void initialize() {
        tasksRegistry = new DefaultTasksRegistry();
        ConfigurationStrategy configStrategy;
        try {
            configStrategy = XmlConfigurationStrategy.newInstance(new XmlConfigurationStreamProvider());
        } catch (ConfigurationException e) {
            throw new SejdaRuntimeException("Unable to complete Sejda configuration ", e);
        }
        notificationStrategy = configStrategy.getNotificationStrategy();
        LOG.trace("Notification strategy: {}", notificationStrategy);
        validation = configStrategy.isValidation();
        LOG.trace("Validation: {}", validation);
        ignoreXmlConfiguration = configStrategy.isIgnoreXmlConfiguration();
        LOG.trace("Validation, ignore xml configuration: {}", ignoreXmlConfiguration);
        Map<Class<? extends TaskParameters>, Class<? extends Task>> userTasks = configStrategy.getTasksMap();
        for (Entry<Class<? extends TaskParameters>, Class<? extends Task>> entry : userTasks.entrySet()) {
            tasksRegistry.addTask(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return the global configuration instance
     * @throws SejdaRuntimeException
     *             if an error occur during the configuration loading
     */
    static GlobalConfiguration getInstance() {
        return GlobalConfigurationHolder.CONFIGURATION;
    }

    /**
     * @return the configured {@link NotificationStrategy}
     */
    Class<? extends NotificationStrategy> getNotificationStrategy() {
        return notificationStrategy;
    }

    TasksRegistry getTasksRegistry() {
        return tasksRegistry;
    }

    /**
     * @return true if validation should be performed or false if incoming parameters instances are already validate externally.
     */
    boolean isValidation() {
        return validation;
    }

    /**
     * @return true if the validator should ignore <i>META-INF/validation.xml</i>.
     */
    boolean isIgnoreXmlConfiguration() {
        return ignoreXmlConfiguration;
    }

    /**
     * Lazy initialization holder class idiom (Joshua Bloch, Effective Java second edition, item 71).
     * 
     * @author Andrea Vacondio
     * 
     */
    private static final class GlobalConfigurationHolder {

        private GlobalConfigurationHolder() {
            // hide constructor
        }

        static final GlobalConfiguration CONFIGURATION = new GlobalConfiguration();
    }
}
