/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.core;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.resource.ProxymaResource;
import org.xml.sax.SAXException;

/**
 *
 * @author shad0w
 */
public class ProxyEngineTest extends TestCase {

    public ProxyEngineTest(String testName) {
        super(testName);
    }

    /**
     * Test of doProxy method, of class ProxyEngine.
     */
    public void testDoProxy() throws IOException, SAXException, IllegalAccessException {
        System.out.println("doProxy");

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "myServlet", TestServlet.class.getName() );
        ServletUnitClient sc = sr.newClient();

        System.out.println(" --->test contextroot (expected redirect)");
        WebRequest wreq   = new GetMethodWebRequest( "http://localhost:0/myServlet");
        WebResponse wres = sc.getResponse( wreq );
        InvocationContext ic = sc.newInvocation( wreq );
        HttpServletRequest request = ic.getRequest();
        HttpServletResponse response = ic.getResponse();
        ProxymaResource aResource = proxyma.createNewResourceInstance(request, response, context);
        ProxyEngine instance = proxyma.createNewProxyEngine(context);
        ProxyFolderBean folder = proxyma.createNewProxyFolder("google", "http://www.google.com", context);
        proxyma.registerProxyFolderIntoContext(folder, context);
        folder = proxyma.createNewProxyFolder("apple", "http://www.apple.com", context);
        folder.setEnabled(false);
        proxyma.registerProxyFolderIntoContext(folder, context);

        int retval = instance.doProxy(aResource);
        assertEquals(404, retval);
        assertEquals("Proxyma", aResource.getResponse().getResponseData().getHeader("Server").getValue());

       

        //Cleanup the pool
        try {
            proxyma.unregisterProxyFolderFromContext(proxyma.getProxyFolderByURLEncodedName("google", context), context);
            proxyma.unregisterProxyFolderFromContext(proxyma.getProxyFolderByURLEncodedName("apple", context), context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}
