/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.plugins.caches;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ResourceHandler;
import m.c.m.proxyma.plugins.retrivers.TestPageRetriver;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 *
 * @author shad0w
 */
public class EhcacheCacheProviderTest extends TestCase {
    
    public EhcacheCacheProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "myServlet", TestServlet.class.getName() );
        ServletUnitClient sc = sr.newClient();
        WebRequest wreq   = new GetMethodWebRequest( "http://test.meterware.com/myServlet?a=1&b=2" );
        wreq.setParameter( "color", "red" );
        WebResponse wres = sc.getResponse( wreq );
        InvocationContext ic = sc.newInvocation( wreq );
        request = ic.getRequest();
        response = ic.getResponse();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        //Cleanup the pool
        try {
            ProxymaFacade proxyma = new ProxymaFacade();
            ProxymaContext context = proxyma.getContextByName("default");
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of storeResponseDataIfCacheable method, of class EhcacheCacheProvider.
     */
    public void testAllMethods() throws Exception {
        System.out.println("testing all methods");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResourceInstance(request, response, context);
        EhcacheCacheProvider instance = new EhcacheCacheProvider(context);

        //Create a testpage for the tests..
        ResourceHandler page = new TestPageRetriver(context);
        page.process(aResource);

        //for default, the test pages are not cacheable.
        instance.storeResponseDataIfCacheable(aResource);
        assertFalse(instance.getResponseData(aResource));

        Collection values = instance.getCachedURIs();
        assertEquals(0, values.size());

        //make page cacheable
        ProxymaResponseDataBean data = aResource.getResponse().getResponseData();
        data.deleteHeader("Pragma");
        data.deleteHeader("Cache-Control");
        data.addHeader("Cache-Control","public");

        //for default, the test pages are not cacheable.
        instance.storeResponseDataIfCacheable(aResource);
        assertTrue(instance.getResponseData(aResource));
        assertNotNull(aResource.getResponse().getResponseData());

        values = instance.getCachedURIs();
        assertEquals(1, values.size());

        String statistics = instance.getStatistics();
        assertNotNull(statistics);
        assertTrue(statistics.contains("Number of elements currently on the cache: 1"));

        System.out.println(statistics);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}
