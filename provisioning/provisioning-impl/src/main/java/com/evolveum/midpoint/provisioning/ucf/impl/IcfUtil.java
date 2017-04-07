/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package com.evolveum.midpoint.provisioning.ucf.impl;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NoPermissionException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SchemaViolationException;

import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.RetryableException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.CompositeFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;

import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.provisioning.ucf.api.GenericFrameworkException;
import com.evolveum.midpoint.schema.processor.ObjectClassComplexTypeDefinition;
import com.evolveum.midpoint.schema.processor.ResourceAttribute;
import com.evolveum.midpoint.schema.processor.ResourceAttributeDefinition;
import com.evolveum.midpoint.schema.processor.ResourceSchema;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

import org.identityconnectors.framework.impl.api.remote.RemoteWrappedException;

/**
 * Set of utility methods that work around some of the ICF problems.
 * 
 * @author Radovan Semancik
 *
 */
class IcfUtil {
	
	private static final Trace LOGGER = TraceManager.getTrace(IcfUtil.class);
    private static final String DOT_NET_EXCEPTION_PACKAGE_PLUS_DOT = "Org.IdentityConnectors.Framework.Common.Exceptions.";
    private static final String JAVA_EXCEPTION_PACKAGE = AlreadyExistsException.class.getPackage().getName();
    private static final String DOT_NET_ARGUMENT_EXCEPTION = "System.ArgumentException";

    private static final String CONNECTIONS_EXCEPTION_CLASS_NAME = "CommunicationsException";

    static Throwable processIcfException(Throwable icfException, ConnectorInstanceIcfImpl conn,
			OperationResult icfResult) {
		return processIcfException(icfException, conn.getHumanReadableName(), icfResult);
	}
	
	/**
	 * Transform ICF exception to something more usable.
	 *
	 * ICF throws exceptions that contains inner exceptions that cannot be
	 * reached by current classloader. Such inner exceptions may cause a lot
	 * of problems in upper layers, such as attempt to serialize/deserialize
	 * them. Therefore we cannot pass such exceptions to the upper layers.
	 * As Throwable is immutable and there is no good way how to copy it, we
	 * just cannot remove the "bad" exceptions from the inner exception stack.
	 * We need to do the brutal thing: remove all the ICF exceptions and do
	 * not pass then to upper layers. Try to save at least some information
	 * and "compress" the class names and messages of the inner ICF exceptions.
	 * The full exception with a stack trace is logged here, so the details are
	 * still in the log.
	 * 
	 * WARNING: This is black magic. Really. Blame ICF interface design.
	 * 
	 * @param icfException
	 *            exception from the ICF
	 * @param connIdResult
	 *            OperationResult to record failure
	 * @return reasonable midPoint exception
	 */
	static Throwable processIcfException(Throwable icfException, String desc,
			OperationResult connIdResult) {
		// Whole exception handling in this case is a black magic.
		// ICF does not define any exceptions and there is no "best practice"
		// how to handle ICF errors
		// Therefore let's just guess what might have happened. That's the best
		// we can do.
		
		if (icfException == null) {
			connIdResult.recordFatalError("Null exception while processing ICF exception ");
			throw new IllegalArgumentException("Null exception while processing ICF exception ");
		}
		
		LOGGER.error("ICF Exception {} in {}: {}",new Object[]{icfException.getClass().getName(),
				desc, icfException.getMessage(),icfException});

        if (icfException instanceof RemoteWrappedException) {
            // brutal hack, for now
            RemoteWrappedException remoteWrappedException = (RemoteWrappedException) icfException;
            String className = remoteWrappedException.getExceptionClass();
            if (className == null) {
                LOGGER.error("Remote ICF exception without inner exception class name. Continuing with original one: {}", icfException);
            } else if (DOT_NET_ARGUMENT_EXCEPTION.equals(className) && remoteWrappedException.getMessage().contains("0x800708C5")) {       // password too weak
                icfException = new SecurityViolationException(icfException.getMessage(), icfException);
            } else {
                if (className.startsWith(DOT_NET_EXCEPTION_PACKAGE_PLUS_DOT)) {
                    className = JAVA_EXCEPTION_PACKAGE + "." + className.substring(DOT_NET_EXCEPTION_PACKAGE_PLUS_DOT.length());
                    LOGGER.trace("Translated exception class: {}", className);
                }
                try {
                    icfException = (Throwable) Class.forName(className).getConstructor(String.class, Throwable.class).newInstance(
                            remoteWrappedException.getMessage(), remoteWrappedException);
                } catch (InstantiationException|IllegalAccessException|ClassNotFoundException|NoSuchMethodException|InvocationTargetException e) {
                    LoggingUtils.logException(LOGGER, "Couldn't unwrap remote ICF exception, continuing with original one {}", e, icfException);
                }
            }
        }
		
		if (icfException instanceof NullPointerException && icfException.getMessage() != null) {
			// NPE with a message text is in fact not a NPE but an application exception
			// this usually means that some parameter is missing
			Exception newEx = new SchemaException(createMessageFromAllExceptions("Required attribute is missing",icfException));  
			connIdResult.recordFatalError("Required attribute is missing: "+icfException.getMessage(),newEx);
			return newEx;
		} else if (icfException instanceof IllegalArgumentException) {
			// Let's assume this must be a configuration problem
			Exception newEx = new com.evolveum.midpoint.util.exception.ConfigurationException(createMessageFromInnermostException("Configuration error", icfException));
			connIdResult.recordFatalError("Configuration error: "+icfException.getMessage(), newEx);
			return newEx;
		}
        //fix of MiD-2645
        //exception brought by the connector is java.lang.RuntimeException with cause=CommunicationsException
        //this exception is to be analyzed here before the following if clause
        if (icfException.getCause() != null){
            String exCauseClassName = icfException.getCause().getClass().getSimpleName();
            if (exCauseClassName.equals(CONNECTIONS_EXCEPTION_CLASS_NAME) ){
                Exception newEx = new CommunicationException(createMessageFromAllExceptions("Connect error", icfException));
                connIdResult.recordFatalError("Connect error: " + icfException.getMessage(), newEx);
                return newEx;
            }
        }
		if (icfException.getClass().getPackage().equals(NullPointerException.class.getPackage())) {
			// There are java.lang exceptions, they are safe to pass through
			connIdResult.recordFatalError(icfException);
			return icfException;
		}
		
		if (icfException.getClass().getPackage().equals(SchemaException.class.getPackage())) {
			// Common midPoint exceptions, pass through
			connIdResult.recordFatalError(icfException);
			return icfException;
		}
		
		if (connIdResult == null) {
			throw new IllegalArgumentException(createMessageFromAllExceptions("Null parent result while processing ICF exception",icfException));
		}

		// Introspect the inner exceptions and look for known causes
		Exception knownCause = lookForKnownCause(icfException, icfException, connIdResult);
		if (knownCause != null) {
			connIdResult.recordFatalError(knownCause);
			return knownCause;
		}

		
		// ########
		// TODO: handle javax.naming.NoPermissionException
		// relevant message directly in the exception ("javax.naming.NoPermissionException([LDAP: error code 50 - The entry uid=idm,ou=Administrators,dc=example,dc=com cannot be modified due to insufficient access rights])

        // Otherwise try few obvious things
		if (icfException instanceof IllegalArgumentException) {
			// This is most likely missing attribute or similar schema thing
			Exception newEx = new SchemaException(createMessageFromAllExceptions("Schema violation (most likely)", icfException));
			connIdResult.recordFatalError("Schema violation: "+icfException.getMessage(), newEx);
			return newEx;
			
		} else if (icfException instanceof ConfigurationException) {
			Exception newEx = new com.evolveum.midpoint.util.exception.ConfigurationException(createMessageFromInnermostException("Configuration error", icfException));
			connIdResult.recordFatalError("Configuration error: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof AlreadyExistsException) {
			Exception newEx = new ObjectAlreadyExistsException(createMessageFromAllExceptions(null, icfException));
			connIdResult.recordFatalError("Object already exists: "+icfException.getMessage(), newEx);
			return newEx;
			
		} else if (icfException instanceof PermissionDeniedException) {
			Exception newEx = new SecurityViolationException(createMessageFromAllExceptions(null, icfException));
			connIdResult.recordFatalError("Security violation: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof ConnectionBrokenException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Connection broken", icfException));
			connIdResult.recordFatalError("Connection broken: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof ConnectionFailedException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Connection failed", icfException));
			connIdResult.recordFatalError("Connection failed: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof UnknownHostException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Unknown host", icfException));
			connIdResult.recordFatalError("Unknown host: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof ConnectorIOException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("IO error", icfException));
			connIdResult.recordFatalError("IO error: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof InvalidCredentialException) {
			Exception newEx = new GenericFrameworkException(createMessageFromAllExceptions("Invalid credentials", icfException));
			connIdResult.recordFatalError("Invalid credentials: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof OperationTimeoutException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Operation timed out", icfException));
			connIdResult.recordFatalError("Operation timed out: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof UnknownUidException) {
			Exception newEx = new ObjectNotFoundException(createMessageFromAllExceptions(null, icfException));
			connIdResult.recordFatalError("Unknown UID: "+icfException.getMessage(), newEx);
			return newEx;
			
		} else if (icfException instanceof InvalidAttributeValueException) {
			Exception newEx = new SchemaException(createMessageFromAllExceptions(null, icfException));
			connIdResult.recordFatalError("Schema violation: "+icfException.getMessage(), newEx);
			return newEx;
			
		} else if (icfException instanceof RetryableException) {
			Exception newEx = new CommunicationException(createMessageFromAllExceptions(null, icfException));
			connIdResult.recordFatalError("Retryable errror: "+icfException.getMessage(), newEx);
			return newEx;

		} else if (icfException instanceof ConnectorSecurityException) {
			// Note: connection refused is also packed inside
			// ConnectorSecurityException. But that will get addressed by the
			// lookForKnownCause(..) before
			
			// Maybe we need special exception for security?
			Exception newEx =  new SecurityViolationException(createMessageFromAllExceptions("Security violation",icfException));
			connIdResult.recordFatalError(
					"Security violation: " + icfException.getMessage(), newEx);
			return newEx;
			
		}
		
		// Fallback
		Exception newEx = new GenericFrameworkException(createMessageFromAllExceptions(null,icfException)); 
		connIdResult.recordFatalError(newEx);
		return newEx;
	}

    private static Exception lookForKnownCause(Throwable ex,
			Throwable originalException, OperationResult parentResult) {
		if (ex instanceof FileNotFoundException) {
            //fix MID-2711 consider FileNotFoundException as CommunicationException
			Exception newEx = new com.evolveum.midpoint.util.exception.CommunicationException(createMessageFromAllExceptions(null, ex));
			parentResult.recordFatalError("File not found: "+ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof NameAlreadyBoundException) {
			// This is thrown by LDAP connector and may be also throw by similar
			// connectors
			Exception newEx = new ObjectAlreadyExistsException(createMessageFromAllExceptions(null, ex));
			parentResult.recordFatalError("Object already exists: "+ex.getMessage(), newEx);
			return newEx;		
		} else if (ex instanceof javax.naming.CommunicationException) {
			// This is thrown by LDAP connector and may be also throw by similar
			// connectors
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Communication error", ex));
			parentResult.recordFatalError("Communication error: "+ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof ServiceUnavailableException) {
            // In some cases (e.g. JDK 1.6.0_31) this is thrown by LDAP connector and may be also throw by similar
            // connectors
            Exception newEx = new CommunicationException(createMessageFromAllExceptions("Communication error", ex));
            parentResult.recordFatalError("Communication error: "+ex.getMessage(), newEx);
            return newEx;
           } else if (ex instanceof ConnectionBrokenException) {
                Exception newEx = new CommunicationException(createMessageFromAllExceptions("Communication error", ex));
                parentResult.recordFatalError("Communication error: "+ex.getMessage(), newEx);
                return newEx;  
            } else if (ex instanceof ConnectionFailedException) {
                Exception newEx = new CommunicationException(createMessageFromAllExceptions("Communication error", ex));
                parentResult.recordFatalError("Communication error: "+ex.getMessage(), newEx);
                return newEx;  
        } else if (ex instanceof SchemaViolationException) {
			// This is thrown by LDAP connector and may be also throw by similar
			// connectors
			Exception newEx = new SchemaException(createMessageFromAllExceptions("Schema violation", ex)); 
			parentResult.recordFatalError("Schema violation: "+ex.getMessage(), newEx);
			return newEx;
        } else if (ex instanceof org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException) {
            Exception newEx = new SchemaException(createMessageFromAllExceptions("Invalid attribute", ex));
            parentResult.recordFatalError("Invalid attribute: "+ex.getMessage(), newEx);
            return newEx;
		} else if (ex instanceof InvalidAttributeValueException) {
			// This is thrown by LDAP connector and may be also throw by similar
			// connectors
			InvalidAttributeValueException e = (InvalidAttributeValueException) ex;
			Exception newEx = null;
			if (e.getExplanation().contains("unique attribute conflict")){
				newEx = new ObjectAlreadyExistsException(createMessageFromAllExceptions("Invalid attribute", ex));
			} else{
				newEx = new SchemaException(createMessageFromAllExceptions("Invalid attribute", ex));
			}
			parentResult.recordFatalError("Invalid attribute: "+ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof ConnectException) {
			// Buried deep in many exceptions, usually connection refused or
			// similar errors
			// Note: needs to be after javax.naming.CommunicationException as the
			//   javax.naming exception has more info (e.g. hostname)
			Exception newEx = new CommunicationException(createMessageFromAllExceptions("Connect error", ex));
			parentResult.recordFatalError("Connect error: " + ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof SQLSyntaxErrorException) {
			// Buried deep in many exceptions, usually DB schema problems of
			// DB-based connectors
			Exception newEx = new SchemaException(createMessageFromAllExceptions("DB syntax error", ex));
			parentResult.recordFatalError("DB syntax error: " + ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof SQLException) {
			// Buried deep in many exceptions, usually DB connection problems
			Exception newEx = new GenericFrameworkException(createMessageFromAllExceptions("DB error", ex));
			parentResult.recordFatalError("DB error: " + ex.getMessage(), newEx);
			return newEx;		
		} else if (ex instanceof UnknownUidException) {
			// Object not found
			Exception newEx = new ObjectNotFoundException(createMessageFromAllExceptions(null,ex));
			parentResult.recordFatalError("Object not found: "+ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof NoPermissionException){
			Exception newEx = new SecurityViolationException(createMessageFromAllExceptions(null,ex));
			parentResult.recordFatalError("Object not found: "+ex.getMessage(), newEx);
			return newEx;
		} else if (ex instanceof AttributeInUseException) {
			Exception newEx = new SchemaException(createMessageFromAllExceptions(null, ex));
			parentResult.recordFatalError("Attribute in use: "+ex.getMessage(), newEx);
			return newEx;

		} else if (ex instanceof NoSuchAttributeException) {
			Exception newEx = new SchemaException(createMessageFromAllExceptions(null, ex));
			parentResult.recordFatalError("No such attribute: "+ex.getMessage(), newEx);
			return newEx;
		}
		if (ex.getCause() == null) {
			// found nothing
			return null;
		} else {
			// Otherwise go one level deeper ...
			return lookForKnownCause(ex.getCause(), originalException,
					parentResult);
		}
	}

	public static String dump(Set<Attribute> attributes) {
		StringBuilder sb = new StringBuilder();
		for (Attribute attr : attributes) {
			DebugUtil.indentDebugDump(sb, 1);
			sb.append(attr.getClass().getSimpleName()).append("(");
			sb.append("name=").append(attr.getName());
			List<Object> value = attr.getValue();
			sb.append(", value=").append(value);
			if (value != null && !value.isEmpty()) {
				sb.append(", type=").append(value.iterator().next().getClass().getSimpleName());
			}
			sb.append(")\n");
		}
		return sb.toString();
	}
	
	public static Object dump(Filter filter) {
		StringBuilder sb = new StringBuilder();
		dump(filter, sb, 0);
		return sb.toString();
	}
	
	private static void dump(Filter filter, StringBuilder sb, int indent) {
		DebugUtil.indentDebugDump(sb, indent);
		if (filter == null) {
			sb.append("null");
			return;
		}
		sb.append(filter.toString());
		if (filter instanceof AttributeFilter) {
			sb.append("(");
			Attribute attribute = ((AttributeFilter)filter).getAttribute();
			sb.append(attribute.getName());
			sb.append(": ");
			List<Object> value = attribute.getValue();
			sb.append(value);
//			if (value != null && !value.isEmpty()) {
//				sb.append(" :").append(attribute.getValue().iterator().next().getClass().getSimpleName());
//			}
			sb.append(")");
		}
		if (filter instanceof CompositeFilter) {
			for(Filter subfilter: ((CompositeFilter)filter).getFilters()) {
				sb.append("\n");
				dump(subfilter,sb,indent+1);
			}
		}
	}

	private static String createMessageFromAllExceptions(String prefix, Throwable ex) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
			sb.append(": ");
		}
		addAllExceptionsToMessage(sb,ex);
		return sb.toString();
	}

	private static void addAllExceptionsToMessage(StringBuilder sb, Throwable ex) {
		sb.append(ex.getClass().getName());
		sb.append("(");
		// Make sure that no non-printable chars shall pass
		// e.g. AD LDAP produces non-printable chars in the messages
		if (ex.getMessage() == null) {
			sb.append("null");
		} else {
			sb.append(ex.getMessage().replaceAll("\\p{C}", "?"));
		}
		sb.append(")");
		if (ex.getCause() != null) {
			sb.append("->");
			addAllExceptionsToMessage(sb, ex.getCause());
		}
	}
	
	private static String createMessageFromInnermostException(String prefix, Throwable ex) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
			sb.append(": ");
		}
		addInnermostExceptionsToMessage(sb,ex);
		return sb.toString();
	}

	private static void addInnermostExceptionsToMessage(StringBuilder sb, Throwable ex) {
		if (ex.getCause() != null) {
			addInnermostExceptionsToMessage(sb, ex.getCause());
		} else {
			sb.append(ex.getMessage());
		}
	}
	
	public static ResourceAttributeDefinition<String> getUidDefinition(ObjectClassComplexTypeDefinition def, ResourceSchema schema) {
		ObjectClassComplexTypeDefinition concreteObjectClassDefinition = getConcreteObjectClassDefinition(def, schema);
		if (concreteObjectClassDefinition == null) {
			return null;
		} else {
			return getUidDefinition(concreteObjectClassDefinition);
		}
	}
	
	public static ObjectClassComplexTypeDefinition getConcreteObjectClassDefinition(ObjectClassComplexTypeDefinition def, ResourceSchema schema) {
		if (def == null) {
			// Return definition from any structural object class. If there is no specific object class definition then
			// the UID definition must be the same in all structural object classes and that means that we can use
			// definition from any structural object class.
			for (ObjectClassComplexTypeDefinition objectClassDefinition: schema.getObjectClassDefinitions()) {
				if (!objectClassDefinition.isAuxiliary()) {
					return objectClassDefinition;
				}
			}
			return null;
		} else {
			return def;
		}
	}
	
	public static ResourceAttributeDefinition<String> getUidDefinition(ObjectClassComplexTypeDefinition def) {
		Collection<? extends ResourceAttributeDefinition> primaryIdentifiers = def.getPrimaryIdentifiers();
		if (primaryIdentifiers.size() > 1) {
			throw new UnsupportedOperationException("Multiple primary identifiers are not supported");
		}
		if (primaryIdentifiers.size() == 1) {
			return primaryIdentifiers.iterator().next();
		} else {
			// fallback, compatibility
			return def.findAttributeDefinition(ConnectorFactoryIcfImpl.ICFS_UID);
		}
	}
	
	public static ResourceAttributeDefinition<String> getNameDefinition(ObjectClassComplexTypeDefinition def) {
		Collection<? extends ResourceAttributeDefinition> secondaryIdentifiers = def.getSecondaryIdentifiers();
		if (secondaryIdentifiers.size() > 1) {
			throw new UnsupportedOperationException("Multiple secondary identifiers are not supported");
		}
		if (secondaryIdentifiers.size() == 1) {
			return secondaryIdentifiers.iterator().next();
		} else {
			// fallback, compatibility
			return def.findAttributeDefinition(ConnectorFactoryIcfImpl.ICFS_NAME);
		}
	}
	
	public static Collection<ResourceAttribute<?>> convertToIdentifiers(Uid uid, 
			ObjectClassComplexTypeDefinition ocDef, ResourceSchema resourceSchema) throws SchemaException {
		ObjectClassComplexTypeDefinition concreteObjectClassDefinition = getConcreteObjectClassDefinition(ocDef, resourceSchema);
		if (concreteObjectClassDefinition == null) {
			throw new SchemaException("Concrete object class of "+ocDef+" cannot be found");
		}
		ResourceAttributeDefinition<String> uidDefinition = getUidDefinition(concreteObjectClassDefinition);
		if (uidDefinition == null) {
			throw new SchemaException("No definition for ConnId UID attribute found in definition "
					+ ocDef);
		}
		Collection<ResourceAttribute<?>> identifiers = new ArrayList<ResourceAttribute<?>>(2);
		ResourceAttribute<String> uidRoa = uidDefinition.instantiate();
		uidRoa.setValue(new PrismPropertyValue<String>(uid.getUidValue()));
		identifiers.add(uidRoa);
		if (uid.getNameHint() != null) {
			ResourceAttributeDefinition<String> nameDefinition = getNameDefinition(concreteObjectClassDefinition);
			if (nameDefinition == null) {
				throw new SchemaException("No definition for ConnId NAME attribute found in definition "
						+ ocDef);
			}
			ResourceAttribute<String> nameRoa = nameDefinition.instantiate();
			nameRoa.setValue(new PrismPropertyValue<String>(uid.getNameHintValue()));
			identifiers.add(nameRoa);
		}
		return identifiers;
	}

	public static GuardedString toGuardedString(ProtectedStringType ps, String propertyName, Protector protector) {
		if (ps == null) {
			return null;
		}
		if (!protector.isEncrypted(ps)) {
			if (ps.getClearValue() == null) {
				return null;
			}
			LOGGER.warn("Using cleartext value for {}", propertyName);
			return new GuardedString(ps.getClearValue().toCharArray());
		}
		try {
			return new GuardedString(protector.decryptString(ps).toCharArray());
		} catch (EncryptionException e) {
			LOGGER.error("Unable to decrypt value of element {}: {}",
					new Object[] { propertyName, e.getMessage(), e });
			throw new SystemException("Unable to decrypt value of element " + propertyName + ": "
					+ e.getMessage(), e);
		} catch (RuntimeException e) {
			// The ConnId will mask encryption exceptions into RuntimeException
			throw new SystemException("Unable to encrypt value of element " + propertyName + ": "
					+ e.getMessage(), e);
		}
	}
}
