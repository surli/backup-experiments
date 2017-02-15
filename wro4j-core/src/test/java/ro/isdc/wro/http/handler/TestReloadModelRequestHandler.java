package ro.isdc.wro.http.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Ivar Conradi Østhus
 */
public class TestReloadModelRequestHandler {
  private RequestHandler victim;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ReloadModelRequestHandler();
    
    Context.set(Context.webContext(request, response, mock(FilterConfig.class)));
    WroTestUtils.createInjector().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test
  public void shouldHandleRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/reloadModel");
    assertTrue(victim.accept(request));
  }
  
  @Test
  public void shouldNotHandleRequest() {
    when(request.getRequestURI()).thenReturn("wroApi/somethingElse");
    assertFalse(victim.accept(request));
  }
  
  @Test
  public void shouldReloadCache()
      throws IOException, ServletException {
    victim.handle(request, response);
    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
  }
}
