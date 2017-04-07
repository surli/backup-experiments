/*
 * Copyright (c) 2010-2013 Evolveum
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

package com.evolveum.midpoint.schema.util;

import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ApprovalSchemaType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConstructionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoginEventType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceAttributeDefinitionType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import java.util.Date;

/**
 * @author mederly
 */
public class ValueDisplayUtil {
    public static String toStringValue(PrismPropertyValue propertyValue) {
        Object value = propertyValue.getValue();
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof PolyString) {
            return ((PolyString) value).getOrig();
        } else if (value instanceof ProtectedStringType) {
            return "(protected string)";        // todo i18n
        } else if (value instanceof Boolean || value instanceof Integer || value instanceof Long) {
            return value.toString();
        } else if (value instanceof XMLGregorianCalendar) {
            return ((XMLGregorianCalendar) value).toGregorianCalendar().getTime().toLocaleString(); // todo fix
        } else if (value instanceof Date) {
            return ((Date) value).toLocaleString(); // todo fix
        } else if (value instanceof LoginEventType) {
            LoginEventType loginEventType = (LoginEventType) value;
            if (loginEventType.getTimestamp() != null) {
                return loginEventType.getTimestamp().toGregorianCalendar().getTime().toLocaleString(); // todo fix
            } else {
                return "";
            }
        } else if (value instanceof ApprovalSchemaType) {
            ApprovalSchemaType approvalSchemaType = (ApprovalSchemaType) value;
            return approvalSchemaType.getName() + (approvalSchemaType.getDescription() != null ? (": " + approvalSchemaType.getDescription()) : "") + " (...)";
        } else if (value instanceof ConstructionType) {
            ConstructionType ct = (ConstructionType) value;
            Object resource = (ct.getResource() != null ? ct.getResource().getName() : (ct.getResourceRef() != null ? ct.getResourceRef().getOid() : null));
            return "resource object" + (resource != null ? " on " + resource : "") + (ct.getDescription() != null ? ": " + ct.getDescription() : "");
        } else if (value instanceof Enum) {
            return value.toString();
        } else if (value instanceof ResourceAttributeDefinitionType) {
            ResourceAttributeDefinitionType radt = (ResourceAttributeDefinitionType) value;
            ItemPathType ref = radt.getRef();
            String path;
            if (ref != null) {
                path = ref.getItemPath().toString();
            } else {
                path = "(null)";
            }
            StringBuilder sb = new StringBuilder();
            MappingType mappingType = radt.getOutbound();
            if (mappingType != null) {
                if (mappingType.getExpression() == null) {
                    sb.append("Empty mapping for ").append(path);
                } else {
                    sb.append(path).append(" = ");
                    boolean first = true;
                    for (JAXBElement<?> evaluator : mappingType.getExpression().getExpressionEvaluator()) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        if (QNameUtil.match(SchemaConstants.C_VALUE, evaluator.getName()) && evaluator.getValue() instanceof RawType) {
                            RawType raw = (RawType) evaluator.getValue();
                            try {
                                XNode xnode = raw.serializeToXNode();
                                if (xnode instanceof PrimitiveXNode) {
                                    sb.append(((PrimitiveXNode) xnode).getStringValue());
                                } else {
                                    sb.append("(a complex value)");
                                }
                            } catch (SchemaException e) {
                                sb.append("(an invalid value)");
                            }
                        } else {
                            sb.append("(a complex expression)");
                        }
                    }
                }
                if (mappingType.getStrength() != null) {
                    sb.append(" (").append(mappingType.getStrength().value()).append(")");
                }
            } else {
                sb.append("Empty mapping for ").append(path);
            }
            return sb.toString();
        } else if (value instanceof QName) {
            QName qname = (QName) value;
            return qname.getLocalPart();
//            if (StringUtils.isNotEmpty(qname.getNamespaceURI())) {
//                return qname.getLocalPart() + " (in " + qname.getNamespaceURI() + ")";
//            } else {
//                return qname.getLocalPart();
//            }
        } else if (value instanceof Number) {
			return String.valueOf(value);
        } else if (value instanceof byte[]) {
            return "(binary data)";
        } else {
            return "(a value of type " + value.getClass().getName() + ")";  // todo i18n
        }
    }

    public static String toStringValue(PrismReferenceValue ref) {
        String rv = getReferredObjectInformation(ref);
        if (ref.getRelation() != null) {
        	rv += " [" + ref.getRelation().getLocalPart() + "]";
		}
		return rv;
    }

    private static String getReferredObjectInformation(PrismReferenceValue ref) {
        if (ref.getObject() != null) {
            return ref.getObject().toString();
        } else {
            return (ref.getTargetType() != null ? ref.getTargetType().getLocalPart()+":" : "")
					+ (ref.getTargetName() != null ? ref.getTargetName() : ref.getOid());
        }
    }
}
