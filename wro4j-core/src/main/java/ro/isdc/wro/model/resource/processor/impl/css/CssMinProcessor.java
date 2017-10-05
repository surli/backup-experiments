/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.CSSMin;


/**
 * A processor implementation using {@link CSSMin} algorithm. This processor can be used as both: PreProcessor &
 * postProcessor.<br/>
 * This processor is annotated with {@link Minimize} because it performs minimization.
 *
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.CSS)
public class CssMinProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssMinProcessor.class);
  public static final String ALIAS = "cssMin";

  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      new CSSMin().formatFile(content, writer);
      writer.flush();
    } catch (final Exception e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      String message = "Exception while applying " + getClass().getSimpleName() + " processor on the "
          + resourceUri + " resource";
      LOG.error(message, e);
      throw new IOException(message);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
