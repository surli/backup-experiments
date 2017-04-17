package org.testcontainers.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.command.DockerCmd;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;

import java.util.List;

/**
 * Logger for tracking potentially destructive actions, intended for usage in a shared Docker environment where
 * traceability is needed. This class uses SLF4J, logging at TRACE level and capturing common fields as MDC fields.
 * <p>
 * Users should configure their test logging to apply appropriate filters/storage so that these logs are
 * captured appropriately.
 */
@Slf4j
@UtilityClass
public class AuditLogger {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String MDC_PREFIX = AuditLogger.class.getCanonicalName();

    public static void doLog(String action, String image, String containerId, DockerCmd<?> cmd) {
        doLog(action, image, containerId, cmd, null);
    }

    public static void doLog(String action, String image, String containerId, DockerCmd<?> cmd, @Nullable Exception e) {
        MDC.put(MDC_PREFIX + ".Action", action);
        MDC.put(MDC_PREFIX + ".Image", image);
        MDC.put(MDC_PREFIX + ".ContainerId", containerId);
        try {
            MDC.put(MDC_PREFIX + ".Command", objectMapper.writeValueAsString(cmd));
        } catch (JsonProcessingException ignored) {
        }

        if (e != null) {
            MDC.put(MDC_PREFIX + ".Exception", e.getLocalizedMessage());
            log.trace("{} action with image: {}, containerId: {}", action, image, containerId, e);
        } else {
            log.trace("{} action with image: {}, containerId: {}", action, image, containerId);
        }

        MDC.remove(MDC_PREFIX + ".Action");
        MDC.remove(MDC_PREFIX + ".Image");
        MDC.remove(MDC_PREFIX + ".ContainerId");
        MDC.remove(MDC_PREFIX + ".Command");
        MDC.remove(MDC_PREFIX + ".Exception");
    }

    public static void doComposeLog(String[] commandParts, List<String> env) {
        MDC.put(MDC_PREFIX + ".Action", "COMPOSE");
        final String command = StringUtils.join(commandParts, ' ');
        MDC.put(MDC_PREFIX + ".Compose.Command", command);
        MDC.put(MDC_PREFIX + ".Compose.Env", env.toString());

        log.trace("COMPOSE action with command: {}, env: {}", command, env);

        MDC.remove(MDC_PREFIX + ".Action");
        MDC.remove(MDC_PREFIX + ".Compose.Command");
        MDC.remove(MDC_PREFIX + ".Compose.Env");
    }
}
