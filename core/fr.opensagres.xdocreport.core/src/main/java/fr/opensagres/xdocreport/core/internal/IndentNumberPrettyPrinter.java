/**
 * Copyright (C) 2011-2015 The XDocReport Team <xdocreport@googlegroups.com>
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fr.opensagres.xdocreport.core.internal;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * XML Pretty Printer implemented with "indent-number". This XML Pretty Printer implementation can crash with this error
 * :
 * 
 * <pre>
 *  java.lang.IllegalArgumentException: Not supported: indent-number
 * </pre>
 * 
 * (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6519088)
 */
public class IndentNumberPrettyPrinter
    implements IXMLPrettyPrinter
{

    private static final String YES = "yes";

    private static final String INDENT_NUMBER = "indent-number";

    public static final IXMLPrettyPrinter INSTANCE = new IndentNumberPrettyPrinter();

    public String prettyPrint( String xml, int indent )
        throws Exception
    {
        TransformerFactory factory = TransformerFactory.newInstance();

        factory.setAttribute( INDENT_NUMBER, indent );
        Transformer transformer = factory.newTransformer();

        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, YES );
        transformer.setOutputProperty( OutputKeys.INDENT, YES );

        final StringWriter out = new StringWriter();
        transformer.transform( new StreamSource( new StringReader( xml ) ), new StreamResult( out ) );
        return out.toString();
    }
}
