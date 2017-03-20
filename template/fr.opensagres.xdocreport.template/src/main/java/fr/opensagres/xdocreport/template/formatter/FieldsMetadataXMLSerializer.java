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
package fr.opensagres.xdocreport.template.formatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.opensagres.xdocreport.core.utils.StringUtils;
import fr.opensagres.xdocreport.template.formatter.sax.FieldsMetadataContentHandler;

/**
 * Fields metadata serializer used to load {@link FieldsMetadata} from XML and serialize {@link FieldsMetadata} to XML.
 */
public class FieldsMetadataXMLSerializer
{

    private static final String LF = System.getProperty( "line.separator" );

    private static final String TAB = "\t";

    private static final FieldsMetadataXMLSerializer INSTANCE = new FieldsMetadataXMLSerializer();

    public static FieldsMetadataXMLSerializer getInstance()
    {
        return INSTANCE;
    }

    protected FieldsMetadataXMLSerializer()
    {

    }

    /**
     * Load fields metadata in the given {@link FieldsMetadata} from the given XML reader. Here a sample of XML reader :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" listType="false" />
     * 	<field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param inputStream the reader of the XML fields.
     * @throws SAXException
     * @throws IOException
     */
    public FieldsMetadata load( Reader input )
        throws SAXException, IOException
    {
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        FieldsMetadataContentHandler myContentHandler = new FieldsMetadataContentHandler();
        saxReader.setContentHandler( myContentHandler );
        saxReader.parse( new InputSource( input ) );
        return myContentHandler.getFieldsMetadata();
    }

    /**
     * Load fields metadata in the given {@link FieldsMetadata} from the given XML reader. Here a sample of XML reader :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" listType="false" />
     * 	<field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param inputStream the input stream of the XML fields.
     * @throws SAXException
     * @throws IOException
     */
    public FieldsMetadata load( InputStream inputStream )
        throws SAXException, IOException
    {

        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        FieldsMetadataContentHandler myContentHandler = new FieldsMetadataContentHandler();
        saxReader.setContentHandler( myContentHandler );
        saxReader.parse( new InputSource( inputStream ) );
        return myContentHandler.getFieldsMetadata();
    }

    /**
     * Serialize as XML the given {@link FieldsMetadata} to the given XML writer. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     *  <field name="project.Name" imageName="" listType="false" />
     *  <field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param fieldsMetadata the metadata to serialize to XML.
     * @param writer the writer.
     * @throws IOException
     */
    public void save( FieldsMetadata fieldsMetadata, Writer writer )
        throws IOException
    {
        save( fieldsMetadata, writer, null, false, false );
    }

    /**
     * Serialize as XML the given {@link FieldsMetadata} to the given XML writer. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     *  <field name="project.Name" imageName="" listType="false" />
     *  <field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param fieldsMetadata the metadata to serialize to XML.
     * @param writer the writer.
     * @param indent true if indent must be managed and false otherwise.
     * @throws IOException
     */
    public void save( FieldsMetadata fieldsMetadata, Writer writer, boolean indent )
        throws IOException
    {
        save( fieldsMetadata, writer, null, indent, false );
    }

    /**
     * Serialize as XML the given {@link FieldsMetadata} to the given XML writer. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" listType="false" />
     * 	<field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param fieldsMetadata the metadata to serialize to XML.
     * @param writer the writer.
     * @param indent true if indent must be managed and false otherwise.
     * @param formatAsJavaString true if format as Java String to be done and false otherwise.
     * @throws IOException
     */
    public void save( FieldsMetadata fieldsMetadata, Writer writer, boolean indent, boolean formatAsJavaString )
        throws IOException
    {
        save( fieldsMetadata, writer, null, indent, formatAsJavaString );
    }

    /**
     * Serialize as XML the given {@link FieldsMetadata} to the given XML output stream. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" listType="false" />
     * 	<field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param fieldsMetadata the metadata to serialize to XML.
     * @param outputstream the output steam.
     * @param indent true if indent must be managed and false otherwise.
     * @param formatAsJavaString true if format as Java String to be done and false otherwise. * @throws IOException
     */
    public void save( FieldsMetadata fieldsMetadata, OutputStream out, boolean indent, boolean formatAsJavaString )
        throws IOException
    {
        save( fieldsMetadata, null, out, indent, formatAsJavaString );
    }

    /**
     * Serialize as XML the given {@link FieldsMetadata} to the given XML output stream. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     *  <field name="project.Name" imageName="" listType="false" />
     *  <field name="developers.Name" imageName="" listType="true" />
     * <field name="project.Logo" imageName="Logo" listType="false" />
     * </fields>
     * </pre>
     * 
     * @param fieldsMetadata the metadata to serialize to XML.
     * @param writer the writer (null if outputstream is not null).
     * @param outputstream the output steam (null if writer is not null).
     * @param indent true if indent must be managed and false otherwise.
     * @param formatAsJavaString true if format as Java String to be done and false otherwise. * @throws IOException
     */
    private void save( FieldsMetadata fieldsMetadata, Writer writer, OutputStream out, boolean indent,
                       boolean formatAsJavaString )
        throws IOException
    {
        Collection<FieldMetadata> fields = fieldsMetadata.getFields();
        // <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        if ( formatAsJavaString )
        {
            write( "\"", writer, out );
            write( XMLFieldsConstants.XML_DECLARATION_AS_JAVA_STRING, writer, out );
        }
        else
        {
            write( XMLFieldsConstants.XML_DECLARATION, writer, out );
        }
        if ( indent )
        {
            write( LF, writer, out );
        }
        // <fields>
        write( XMLFieldsConstants.FIELDS_TAG_START_ELT, writer, out );
        // <fields/@templateEngineKind
        writeAttr( XMLFieldsConstants.TEMPLATE_ENGINE_KIND_ATTR, fieldsMetadata.getTemplateEngineKind(),
                   formatAsJavaString, writer, out );
        write( " >", writer, out );
        if ( indent )
        {
            write( LF, writer, out );
            write( TAB, writer, out );
        }
        // <description>
        write( XMLFieldsConstants.DESCRIPTION_START_ELT, writer, out );
        write( XMLFieldsConstants.START_CDATA, writer, out );
        if ( fieldsMetadata.getDescription() != null )
        {
            write( fieldsMetadata.getDescription(), writer, out );
        }
        write( XMLFieldsConstants.END_CDATA, writer, out );
        // </description>
        write( XMLFieldsConstants.DESCRIPTION_END_ELT, writer, out );
        // list of <field>
        for ( FieldMetadata field : fields )
        {
            save( field, writer, out, indent, formatAsJavaString );
        }
        if ( indent )
        {
            write( LF, writer, out );
        }
        // </fields>
        write( XMLFieldsConstants.FIELDS_END_ELT, writer, out );
        if ( formatAsJavaString )
        {
            write( "\"", writer, out );
        }
    }

    private void save( FieldMetadata field, Writer writer, OutputStream out, boolean indent, boolean formatAsJavaString )
        throws IOException
    {
        if ( indent )
        {
            write( LF, writer, out );
            write( TAB, writer, out );
        }
        write( XMLFieldsConstants.FIELD_TAG_START_ELT, writer, out );
        writeAttr( XMLFieldsConstants.NAME_ATTR, field.getFieldName(), formatAsJavaString, writer, out );
        writeAttr( XMLFieldsConstants.LIST_ATTR, field.isListType(), formatAsJavaString, writer, out );
        writeAttr( XMLFieldsConstants.IMAGE_NAME_ATTR, field.getImageName(), formatAsJavaString, writer, out );
        writeAttr( XMLFieldsConstants.SYNTAX_KIND_ATTR, field.getSyntaxKind(), formatAsJavaString, writer, out );
        write( ">", writer, out );
        if ( indent )
        {
            write( LF, writer, out );
            write( TAB, writer, out );
            write( TAB, writer, out );
        }
        // Description
        write( XMLFieldsConstants.DESCRIPTION_START_ELT, writer, out );
        write( XMLFieldsConstants.START_CDATA, writer, out );
        if ( field.getDescription() != null )
        {
            write( field.getDescription(), writer, out );
        }
        write( XMLFieldsConstants.END_CDATA, writer, out );
        write( XMLFieldsConstants.DESCRIPTION_END_ELT, writer, out );
        if ( indent )
        {
            write( LF, writer, out );
            write( TAB, writer, out );
        }
        write( XMLFieldsConstants.FIELD_END_ELT, writer, out );
    }

    private void write( String s, Writer writer, OutputStream out )
        throws IOException
    {
        if ( writer == null )
        {
            out.write( s.getBytes() );
        }
        else
        {
            writer.write( s );
        }
    }

    private void writeAttr( String attrName, String attrValue, boolean formatAsJavaString, Writer writer,
                            OutputStream out )
        throws IOException
    {
        write( " ", writer, out );
        write( attrName, writer, out );
        if ( formatAsJavaString )
        {
            write( "=\\\"", writer, out );
        }
        else
        {
            write( "=\"", writer, out );
        }
        write( attrValue != null ? attrValue : "", writer, out );
        if ( formatAsJavaString )
        {
            write( "\\\"", writer, out );
        }
        else
        {
            write( "\"", writer, out );
        }
    }

    private void writeAttr( String attrName, boolean attrValue, boolean formatAsJavaString, Writer writer,
                            OutputStream out )
        throws IOException
    {
        writeAttr( attrName, attrValue ? StringUtils.TRUE : StringUtils.FALSE, formatAsJavaString, writer, out );
    }
}
