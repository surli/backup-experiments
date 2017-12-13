/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
 */
package org.opensolaris.opengrok.util;

/**
 *
 * @author Krystof Tulinger
 */
public class RandomString {
    
    public static String generateLower(int length) {
        return generate(length, "abcdefghijklmnopqrstuvwxyz");
    }

    public static String generateUpper(int length) {
        return generate(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    public static String generateNumeric(int length) {
        return generate(length, "0123456789");
    }
    
    public static String generateLowerUnique(int length, int index) {
        return generate(Math.max(0, length+index), "abcdefghijklmnopqrstuvwxyz");
    }

    public static String generateUpperUnique(int length, int index) {
        return generate(Math.max(0, length+index), "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    public static String generateNumericUnique(int length, int index) {
        return generate(Math.max(0, length+index), "0123456789");
    }
    
    public static String generate(int length) {
        return generate(length, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.");
    }
    
        public static String generateUnique(int length, int index) {
        return generate(length + index, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.");
    }
        
    public static String generate(int length, String charset) {
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i ++) {
            int index = ((int)(Math.random() * Integer.MAX_VALUE)) % charset.length();
            buffer.append(charset.charAt(index));
        }
        return buffer.toString();
    }
}
