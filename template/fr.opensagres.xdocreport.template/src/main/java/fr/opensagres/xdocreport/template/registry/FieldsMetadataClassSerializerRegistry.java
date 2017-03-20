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
package fr.opensagres.xdocreport.template.registry;

import java.util.HashMap;
import java.util.Map;

import fr.opensagres.xdocreport.core.registry.AbstractRegistry;
import fr.opensagres.xdocreport.template.formatter.IFieldsMetadataClassSerializer;

public class FieldsMetadataClassSerializerRegistry
    extends AbstractRegistry<IFieldsMetadataClassSerializer>
{

    private static final FieldsMetadataClassSerializerRegistry INSTANCE = new FieldsMetadataClassSerializerRegistry();

    private Map<String, IFieldsMetadataClassSerializer> serializers =
        new HashMap<String, IFieldsMetadataClassSerializer>();

    public static FieldsMetadataClassSerializerRegistry getRegistry()
    {
        return INSTANCE;
    }

    public FieldsMetadataClassSerializerRegistry()
    {
        super( IFieldsMetadataClassSerializer.class );
    }

    @Override
    protected boolean registerInstance( IFieldsMetadataClassSerializer factory )
    {
        serializers.put( factory.getId(), factory );
        return true;
    }

    @Override
    protected void doDispose()
    {
        serializers.clear();
    }

    public IFieldsMetadataClassSerializer getSerializer( String id )
    {
        if ( id == null )
        {
            return null;
        }
        initializeIfNeeded();
        return serializers.get( id );
    }

}
