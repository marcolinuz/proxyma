package m.c.m.proxyma.rewrite;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;import javax.servlet.http.Cookie;
;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the RewriterEngine
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id$
 */
public class RewriteEngineTest extends TestCase {
    
    public RewriteEngineTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml");
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
     * Test of masqueradeURL method, of class RewriteEngine.
     */
    public void testMasqueradeURL() throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        System.out.println("masqueradeURL");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResourceInstance(request, response, context);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");
        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        proxyma.registerProxyFolderIntoContext(folder1, context);
        proxyma.registerProxyFolderIntoContext(folder2, context);
        RewriteEngine instance = new RewriteEngine(context);

        String theUrl = "http://www.yahoo.it/profile/it.html";
        String expected = "http://www.yahoo.it/profile/it.html";
        String result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);

        theUrl = "http://www.google.com:80/it/profile/io.html";
        expected = "/proxyma/host1/profile/io.html";
        result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);

        theUrl = "/it/profile/io.html";
        expected = "/proxyma/host1/profile/io.html";
        result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);

        theUrl = "profile/io.html";
        expected = "profile/io.html";
        result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);


        theUrl = "https://www.apple.com:443/en/macbook/new.html";
        expected = "/proxyma/host2/macbook/new.html";
        result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);

        proxyma.unregisterProxyFolderFromContext(folder2, context);

        theUrl = "https://www.apple.com/en/macbook/new.html";
        expected = "https://www.apple.com/en/macbook/new.html";
        result = instance.masqueradeURL(theUrl, folder1, aResource);
        assertEquals(expected, result);


        proxyma.unregisterProxyFolderFromContext(folder1, context);
    }

    public void testMasquerade_Unmasquerade_Cookie() throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        System.out.println("masquerade/unmasqueradeCookie");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        proxyma.registerProxyFolderIntoContext(folder1, context);
        proxyma.registerProxyFolderIntoContext(folder2, context);
        ProxymaResource aResource = proxyma.createNewResourceInstance(request, response, context);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");
        aResource.setProxyFolder(folder1);
        RewriteEngine instance = new RewriteEngine(context);

        Cookie theCookie = new Cookie("cookie1", "Value1");
        theCookie.setDomain("google.com");
        theCookie.setPath("/it");
        instance.masqueradeCookie(theCookie, folder1, aResource);

        String expected = "localhost";
        assertEquals(expected, theCookie.getDomain());

        expected = "/proxyma/host1";
        assertEquals(expected, theCookie.getPath());


        instance.unmasqueradeCookie(theCookie);

        expected = "google.com";
        assertEquals(expected, theCookie.getDomain());

        expected = "/it";
        assertEquals(expected, theCookie.getPath());

        theCookie = new Cookie("cookie2", "Value2");
        instance.masqueradeCookie(theCookie, folder1, aResource);

        expected = "localhost";
        assertEquals(expected, theCookie.getDomain());

        expected = "/proxyma/host1";
        assertEquals(expected, theCookie.getPath());

        instance.unmasqueradeCookie(theCookie);

        expected = "www.google.com";
        assertEquals(expected, theCookie.getDomain());

        expected = "/";
        assertEquals(expected, theCookie.getPath());

        proxyma.unregisterProxyFolderFromContext(folder2, context);
        proxyma.unregisterProxyFolderFromContext(folder1, context);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}
