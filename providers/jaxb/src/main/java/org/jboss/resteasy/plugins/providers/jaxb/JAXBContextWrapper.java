package org.jboss.resteasy.plugins.providers.jaxb;

import org.jboss.resteasy.annotations.providers.jaxb.JAXBConfig;
import org.jboss.resteasy.plugins.providers.jaxb.i18n.LogMessages;
import org.jboss.resteasy.plugins.providers.jaxb.i18n.Messages;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;

/**
 * A wrapper class around a JAXBContext that enables additional features
 * to the RESTEasy JAXB-based providers.
 *
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision:$
 */
@SuppressWarnings("deprecation")
public class JAXBContextWrapper extends JAXBContext
{

   private static final String NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
   private static Constructor mapperConstructor = null;

   static
   {
      try
      {
         // check to see if NamespacePrefixMapper is in classpath
         final Class[] namespace = new Class[1];
         final Class[] mapper = new Class[1];

         if (System.getSecurityManager() == null)
         {
            namespace[0] =  JAXBContextWrapper.class.getClassLoader().loadClass("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
            mapper[0] =  JAXBContextWrapper.class.getClassLoader().loadClass("org.jboss.resteasy.plugins.providers.jaxb.XmlNamespacePrefixMapper");
         }
         else
         {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
               @Override public Void run() throws Exception {
                  namespace[0] =  JAXBContextWrapper.class.getClassLoader().loadClass("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
                  mapper[0] =  JAXBContextWrapper.class.getClassLoader().loadClass("org.jboss.resteasy.plugins.providers.jaxb.XmlNamespacePrefixMapper");

                  return null;
               }
            });
         }

         mapperConstructor = mapper[0].getConstructors()[0];
      }
      catch (ClassNotFoundException e)
      {

      }
      catch (PrivilegedActionException pae) {

      }

   }

   private JAXBContext wrappedContext;

   /**
    * An optional namespace mapper that is used to apply prefixes to elements with a given namespace.
    */
   private Object mapper;

   /**
    * The optional Schema that is bound to this context
    */
   private Schema schema;

   public JAXBContextWrapper(JAXBContext wrappedContext, JAXBConfig config) throws JAXBException
   {
      processConfig(config);
      this.wrappedContext = wrappedContext;
   }

   /**
    * Create a new JAXBContextWrapper.
    *
    * @param classes
    * @param properties
    * @param config
    * @throws JAXBException
    */
   public JAXBContextWrapper(final Class<?>[] classes, final Map<String, Object> properties, JAXBConfig config)
           throws JAXBException
   {
      processConfig(config);
      try
      {
         if (System.getSecurityManager() == null)
         {
            wrappedContext = JAXBContext.newInstance(classes, properties);
         }
         else
         {
            wrappedContext = AccessController.doPrivileged(new PrivilegedExceptionAction<JAXBContext>()
            {
               @Override
               public JAXBContext run() throws JAXBException
               {
                  return JAXBContext.newInstance(classes, properties);
               }
            });
         }
      }
      catch (PrivilegedActionException paex)
      {
         throw new JAXBException(paex.getMessage());
      }
   }

   /**
    * Create a new JAXBContextWrapper.
    *
    * @param contextPath
    * @param config
    * @throws JAXBException
    */
   public JAXBContextWrapper(String contextPath, JAXBConfig config) throws JAXBException
   {
      processConfig(config);
      wrappedContext = JAXBContext.newInstance(contextPath);
   }

   /**
    * Create a new JAXBContextWrapper.
    *
    * @param classes
    * @param config
    * @throws JAXBException
    */
   public JAXBContextWrapper(JAXBConfig config, Class<?>... classes) throws JAXBException
   {
      this(classes, Collections.<String, Object>emptyMap(), config);
   }

   /**
    * FIXME Comment this
    *
    * @param config
    */
   private void processConfig(JAXBConfig config) throws JAXBException
   {
      if (config != null)
      {
         if (config.useNameSpacePrefix())
         {
            if (mapperConstructor == null)
            {
               throw new JAXBException(Messages.MESSAGES.namespacePrefixMapperNotInClassPath());
            }
            try
            {
               mapper = mapperConstructor.newInstance((Object[])config.namespaces());
            }
            catch (Exception e)
            {
               throw new JAXBException(e);
            }
         }
         if (!"".equals(config.schema()))
         {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(config.schemaType());
            try
            {
               InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                       config.schema());
               schema = schemaFactory.newSchema(new StreamSource(in));
            }
            catch (SAXException e)
            {
               throw new JAXBException(Messages.MESSAGES.errorTryingToLoadSchema(config.schema()), e);
            }
         }

      }
   }

   /**
    * Get the schema.
    *
    * @return the schema.
    */
   public Schema getSchema()
   {
      return schema;
   }

   /**
    * Set the schema.
    *
    * @param schema The schema to set.
    */
   public void setSchema(Schema schema)
   {
      this.schema = schema;
   }

   /**
    * @return
    * @see javax.xml.bind.JAXBContext#createBinder()
    */
   public Binder<Node> createBinder()
   {
      return wrappedContext.createBinder();
   }

   /**
    * @param <T>
    * @param domType
    * @return
    * @see javax.xml.bind.JAXBContext#createBinder(java.lang.Class)
    */
   public <T> Binder<T> createBinder(Class<T> domType)
   {
      return wrappedContext.createBinder(domType);
   }

   /**
    * @return
    * @see javax.xml.bind.JAXBContext#createJAXBIntrospector()
    */
   public JAXBIntrospector createJAXBIntrospector()
   {
      return wrappedContext.createJAXBIntrospector();
   }

   /**
    * @return
    * @throws JAXBException
    * @see javax.xml.bind.JAXBContext#createMarshaller()
    */
   public Marshaller createMarshaller() throws JAXBException
   {
      Marshaller marshaller = wrappedContext.createMarshaller();
      if (mapper != null)
      {
         try
         {
            marshaller.setProperty(NAMESPACE_PREFIX_MAPPER, mapper);
         }
         catch (PropertyException e)
         {
            LogMessages.LOGGER.warn(e.getMessage());
         }
      }
      return marshaller;
   }

   /**
    * @return
    * @throws JAXBException
    * @see javax.xml.bind.JAXBContext#createUnmarshaller()
    */
   public Unmarshaller createUnmarshaller() throws JAXBException
   {
      return wrappedContext.createUnmarshaller();
   }

   /**
    * @return
    * @throws JAXBException
    * @see javax.xml.bind.JAXBContext#createValidator()
    * @deprecated See javax.xml.bind.JAXBContext#createValidator().
    */
   public Validator createValidator() throws JAXBException
   {
      return wrappedContext.createValidator();
   }

   /**
    * @param outputResolver
    * @throws IOException
    * @see javax.xml.bind.JAXBContext#generateSchema(javax.xml.bind.SchemaOutputResolver)
    */
   public void generateSchema(SchemaOutputResolver outputResolver) throws IOException
   {
      wrappedContext.generateSchema(outputResolver);
   }

}
