/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.data.redis.connection.jedis;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Builder;
import redis.clients.jedis.Client;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.Queable;
import redis.clients.jedis.Response;
import redis.clients.util.RedisOutputStream;
import redis.clients.util.SafeEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class to invoke arbitraty Jedis commands.
 * 
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.1
 */
@SuppressWarnings({ "unchecked", "ConstantConditions" })
class JedisClientUtils {

	private static final Field CLIENT_FIELD;
	private static final Method SEND_COMMAND;
	private static final Method GET_RESPONSE;
	private static final Method PROTOCOL_SEND_COMMAND;
	private static final Set<String> KNOWN_COMMANDS;
	private static final Builder<Object> OBJECT_BUILDER;

	static {

		CLIENT_FIELD = ReflectionUtils.findField(BinaryJedis.class, "client", Client.class);
		ReflectionUtils.makeAccessible(CLIENT_FIELD);

		PROTOCOL_SEND_COMMAND = ReflectionUtils.findMethod(Protocol.class, "sendCommand", RedisOutputStream.class,
				byte[].class, byte[][].class);
		ReflectionUtils.makeAccessible(PROTOCOL_SEND_COMMAND);

		try {

			Class<?> commandType = ClassUtils.isPresent("redis.clients.jedis.ProtocolCommand", null)
					? ClassUtils.forName("redis.clients.jedis.ProtocolCommand", null)
					: ClassUtils.forName("redis.clients.jedis.Protocol$Command", null);

			SEND_COMMAND = ReflectionUtils.findMethod(Connection.class, "sendCommand", commandType, byte[][].class);
		} catch (Exception e) {
			throw new NoClassDefFoundError(
					"Could not find required flavor of command required by 'redis.clients.jedis.Connection#sendCommand'.");
		}

		ReflectionUtils.makeAccessible(SEND_COMMAND);

		GET_RESPONSE = ReflectionUtils.findMethod(Queable.class, "getResponse", Builder.class);
		ReflectionUtils.makeAccessible(GET_RESPONSE);

		KNOWN_COMMANDS = Arrays.stream(Command.values()).map(Enum::name).collect(Collectors.toSet());

		OBJECT_BUILDER = new Builder<Object>() {
			public Object build(Object data) {
				return data;
			}

			public String toString() {
				return "Object";
			}
		};
	}

	/**
	 * Execute an arbitrary on the supplied {@link Jedis} instance.
	 * 
	 * @param command the command.
	 * @param keys must not be {@literal null}, may be empty.
	 * @param args must not be {@literal null}, may be empty.
	 * @param jedis must not be {@literal null}.
	 * @return the response, can be be {@literal null}.
	 */
	@Nullable // TODO: Change keys and args to byte[][]?
	static <T> T execute(String command, Collection<byte[]> keys, Collection<byte[]> args, Supplier<Jedis> jedis) {

		byte[][] commandArgs = getCommandArguments(keys, args);

		Client client = retrieveClient(jedis.get());
		sendCommand(client, command, commandArgs);

		return (T) client.getOne();
	}

	private static byte[][] getCommandArguments(Collection<byte[]> keys, Collection<byte[]> args) {

		byte[][] commandArgs = new byte[keys.size() + args.size()][];

		int index = 0;
		for (byte[] key : keys) {
			commandArgs[index++] = key;
		}

		for (byte[] arg : args) {
			commandArgs[index++] = arg;
		}

		return commandArgs;
	}

	/**
	 * Send a Redis command and retrieve the {@link Client} for response retrieval.
	 * 
	 * @param jedis
	 * @param command
	 * @param args
	 * @return
	 */
	static Client sendCommand(Jedis jedis, String command, byte[][] args) {

		Client client = retrieveClient(jedis);

		if (isKnownCommand(command)) {
			ReflectionUtils.invokeMethod(SEND_COMMAND, client, Command.valueOf(command.trim().toUpperCase()), args);
		} else {
			sendProtocolCommand(client, command, args);
		}

		return client;
	}

	private static void sendCommand(Client client, String command, byte[][] args) {

		if (isKnownCommand(command)) {
			ReflectionUtils.invokeMethod(SEND_COMMAND, client, Command.valueOf(command.trim().toUpperCase()), args);
		} else {
			sendProtocolCommand(client, command, args);
		}
	}

	private static void sendProtocolCommand(Client client, String command, byte[][] args) {

		// quite expensive to construct for each command invocation
		DirectFieldAccessor dfa = new DirectFieldAccessor(client);

		client.connect();

		RedisOutputStream os = (RedisOutputStream) dfa.getPropertyValue("outputStream");
		ReflectionUtils.invokeMethod(PROTOCOL_SEND_COMMAND, null, os, SafeEncoder.encode(command), args);

		Integer pipelinedCommands = (Integer) dfa.getPropertyValue("pipelinedCommands");
		dfa.setPropertyValue("pipelinedCommands", pipelinedCommands + 1);
	}

	/**
	 * @param jedis the client instance.
	 * @return {@literal true} if the connection has entered {@literal MULTI} state.
	 */
	static boolean isInMulti(Jedis jedis) {
		return retrieveClient(jedis).isInMulti();
	}

	private static boolean isKnownCommand(String command) {
		return KNOWN_COMMANDS.contains(command);
	}

	@SuppressWarnings({ "unchecked", "ConstantConditions" })
	static Response<Object> getGetResponse(Object target) {
		return (Response<Object>) ReflectionUtils.invokeMethod(GET_RESPONSE, target, OBJECT_BUILDER);
	}

	private static Client retrieveClient(Jedis jedis) {
		return (Client) ReflectionUtils.getField(CLIENT_FIELD, jedis);
	}
}
