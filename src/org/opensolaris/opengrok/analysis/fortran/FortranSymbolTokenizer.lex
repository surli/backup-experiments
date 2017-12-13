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
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2017, Chris Fraire <cfraire@me.com>.
 */

package org.opensolaris.opengrok.analysis.fortran;

import org.opensolaris.opengrok.analysis.JFlexTokenizer;
%%
%public
%class FortranSymbolTokenizer
%extends JFlexTokenizer
%unicode
%ignorecase
%init{
super(in);
%init}
%int
%include CommonTokenizer.lexh
%char

// (OK to exclude LCOMMENT state used in FortranXref.)
%state STRING SCOMMENT QSTRING

%include Common.lexh
%include Fortran.lexh
%%

<YYINITIAL> {
 ^{Label} { }
 ^[^ \t\f\r\n]+ { yybegin(SCOMMENT); }
{Identifier} {String id = yytext();
                if(!Consts.kwd.contains(id.toLowerCase())) {
                        setAttribs(id, yychar, yychar + yylength());
                        return yystate(); }
              }

 {Number}        {}

 \"     { yybegin(STRING); }
 \'     { yybegin(QSTRING); }
 \!     { yybegin(SCOMMENT); }
}

<STRING> {
 \"\"    {}
 \"     { yybegin(YYINITIAL); }
}

<QSTRING> {
 \'\'    {}
 \'     { yybegin(YYINITIAL); }
}

<SCOMMENT> {
{WhiteSpace}    {}
{EOL}    { yybegin(YYINITIAL);}
}

<YYINITIAL, STRING, SCOMMENT, QSTRING> {
[^]    {}
}
