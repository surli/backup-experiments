/*
 * Copyright (c) 2010-2017 Evolveum
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

package com.evolveum.midpoint.model.impl.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public abstract class MidpointAbstractProvider<T> extends AbstractConfigurableProvider implements MessageBodyReader<T>, MessageBodyWriter<T>{

	private static transient Trace LOGGER = TraceManager.getTrace(MidpointAbstractProvider.class);
	
	@Autowired(required=true)
	protected PrismContext prismContext;
	
//	@Override
    public void init(List<ClassResourceInfo> cris) {
        setEnableStreaming(true);
    }
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		if (type.getPackage().getName().startsWith("com.evolveum.midpoint") || type.getPackage().getName().startsWith("com.evolveum.prism")){
			return true;
		}
		return false;
	}

	@Override
	public long getSize(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void writeTo(T object, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {

		// TODO implement in the standard serializer; also change root name
		QName fakeQName = new QName(PrismConstants.NS_PREFIX + "debug", "debugPrintObject");
		String xml;
		
		PrismSerializer<String> serializer = getSerializer();
		
		try {
			if (object instanceof PrismObject) {
				xml = serializer.serialize((PrismObject<?>) object);
			} else if (object instanceof OperationResult) {
				OperationResultType operationResultType = ((OperationResult) object).createOperationResultType();
				xml = serializer.serializeAnyData(operationResultType, fakeQName);
			} else {
				xml = serializer.serializeAnyData(object, fakeQName);
			}
			entityStream.write(xml.getBytes("utf-8"));
		} catch (SchemaException | RuntimeException e) {
			LoggingUtils.logException(LOGGER, "Couldn't marshal element to string: {}", e, object);
		}
	}
	
	protected abstract PrismSerializer<String> getSerializer();
	protected abstract PrismParser getParser(InputStream entityStream);

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		if (type.getPackage().getName().startsWith("com.evolveum.midpoint") || type.getPackage().getName().startsWith("com.evolveum.prism")){
			return true;
		}
		return false;
	}

	@Override
	public T readFrom(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		
		if (entityStream == null){
			return null;
		}
		
		PrismParser parser = getParser(entityStream);
				
		T object = null;
		try {
			LOGGER.info("type of respose: {}", type);
			if (PrismObject.class.isAssignableFrom(type) || type.isAssignableFrom(PrismObject.class) || ObjectType.class.isAssignableFrom(type)){
				object = (T) parser.parse();
			} else {
                object = parser.parseRealValue();
			}
			
			return object;
		} catch (SchemaException ex){
			throw new WebApplicationException(ex);
		} catch (IOException ex){
			throw new IOException(ex);
		}
		
	}
	

}
