/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 *
 * @author shad0w
 */
public class ProxyStandardResponsesFactoryTest extends TestCase {
    
    public ProxyStandardResponsesFactoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of createRedirectResponse method, of class ProxyStandardResponsesFactory.
     */
    public void testCreateRedirectResponse() {
        System.out.println("createRedirectResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxymaResponseDataBean instance = null;

        String destination = "htpo:/inv,alid.url/";
        try {
            instance = ProxyStandardResponsesFactory.createRedirectResponse(destination);
            fail("expected exception not thrown");
        } catch (MalformedURLException ex) {
            assertTrue(true);
        }

        destination = "http://www.google.com/";
        try {
            instance = ProxyStandardResponsesFactory.createRedirectResponse(destination);
        } catch (MalformedURLException ex) {
            fail("unexpected malformed url exception thrown");
        }

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), "Proxyma");
        assertEquals(instance.getHeader("Location").getValue(), destination);
        assertEquals(instance.getStatus(), 302);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of createErrorResponse method, of class ProxyStandardResponsesFactory.
     */
    public void testCreateErrorResponse() {
        System.out.println("createErrorResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxymaResponseDataBean instance = null;

        int code = 500;
        instance = ProxyStandardResponsesFactory.createErrorResponse(code);

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), "Proxyma");
        assertEquals(instance.getStatus(), code);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }


    /**
     * Test of createFoldersListResponse method, of class ProxyStandardResponsesFactory.
     */
    public void testcreateFoldersListResponse() throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        System.out.println("createFoldersListResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean folder =  proxyma.createNewProxyFolder("GoogleFolder", "http://www.google.com", context);
        proxyma.registerProxyFolderIntoContext(folder, context);
        folder = proxyma.createNewProxyFolder("AppleFolder", "http://www.apple.com", context);
        proxyma.registerProxyFolderIntoContext(folder, context);
        ProxymaResponseDataBean instance = null;

        instance = ProxyStandardResponsesFactory.createFoldersListResponse(context);

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), "Proxyma");
        assertEquals(instance.getStatus(), 200);
        
        assertEquals(instance.getHeader("Content-type").getValue(), "text/html;charset="+context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING));
        assertTrue(instance.getContentLenght() > 0);

        ByteBufferReader data = ByteBufferFactory.createNewByteBufferReader(instance.getData());

        byte[] result = data.getWholeBufferAsByteArray();
        String resultString = new String(result,context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING));

        assertTrue(resultString.startsWith("<!DOCTYPE HTML PUBLIC"));
        assertTrue(resultString.contains("<td align=\"left\"><a href=\"./GoogleFolder/\">GoogleFolder</a></td>"));
        assertTrue(resultString.contains("<td align=\"left\"><a href=\"./AppleFolder/\">AppleFolder</a></td>"));
        assertTrue(resultString.endsWith("</html>\n"));

        //Cleanup pool
        try {
            proxyma.unregisterProxyFolderFromContext(context.getProxyFolderByURLEncodedName("GoogleFolder"), context);
            proxyma.unregisterProxyFolderFromContext(context.getProxyFolderByURLEncodedName("AppleFolder"), context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}
