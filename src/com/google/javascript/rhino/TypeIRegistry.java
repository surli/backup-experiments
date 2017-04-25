/*
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Ben Lickly
 *   Dimitris Vardoulakis
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.google.javascript.rhino;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.jstype.JSTypeNative;
import java.util.List;
import java.util.Map;

/**
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
public interface TypeIRegistry {
  // TODO(dimvar): Some methods in this interface are polymorphic because they are used
  // in compiler passes mixed with the old type system.
  // Polymorphism avoids the need for casting in many cases (fewer casts in java 8 than in java 7).
  // After all non-type-checking passes use TypeI, we should make these methods not polymorphic.
  //
  // We considered defining the interface as TypeIRegistry<T extends TypeI>, but it would only
  // help with getNativeType and getType, not with the other methods, and it would increase
  // verbosity in all places that use the interface.

  TypeI createTypeFromCommentNode(Node n);

  <T extends FunctionTypeI> T getNativeFunctionType(JSTypeNative typeId);

  <T extends ObjectTypeI> T getNativeObjectType(JSTypeNative typeId);

  <T extends TypeI> T getNativeType(JSTypeNative typeId);

  String getReadableTypeName(Node n);

  <T extends TypeI> T getType(String typeName);

  TypeI createUnionType(List<? extends TypeI> variants);

  /**
   * Creates an anonymous structural type with the given properties.
   */
  TypeI createRecordType(Map<String, ? extends TypeI> props);

  TypeI instantiateGenericType(ObjectTypeI genericType, ImmutableList<? extends TypeI> typeArgs);

  TypeI evaluateTypeExpressionInGlobalScope(JSTypeExpression expr);

  TypeI evaluateTypeExpression(JSTypeExpression expr, TypeIEnv<TypeI> typeEnv);

  TypeI buildRecordTypeFromObject(ObjectTypeI obj);
}
