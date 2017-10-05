package ro.isdc.wro.extensions.processor.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoTypeScriptProcessor} but will prefer using {@link NodeTypeScriptProcessor} if it is supported
 * and will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @since 1.6.3
 * @created 21 Jan 2013
 */
@SupportedResourceType(ResourceType.JS)
public class TypeScriptProcessor
    extends AbstractNodeWithFallbackProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(TypeScriptProcessor.class);
  public static final String ALIAS = "typeScript";

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createNodeProcessor() {
    LOG.debug("creating NodeTypeScriptProcessor");
    return new NodeTypeScriptProcessor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createFallbackProcessor() {
    LOG.debug("Node TypeScript is not supported. Using fallback Rhino processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoTypeScriptProcessor();
      }
    });
  }
}
