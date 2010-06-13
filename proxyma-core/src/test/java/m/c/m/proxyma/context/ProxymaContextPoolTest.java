package m.c.m.proxyma.context;

import java.io.UnsupportedEncodingException;
import junit.framework.TestCase;
import org.apache.commons.lang.NullArgumentException;

/**
 *
 * @author mcm
 */
public class ProxymaContextPoolTest extends TestCase {
    
    public ProxymaContextPoolTest(String testName) {
        super(testName);
    }

    /**
     * Test of getInstance method, of class ProxymaContextPool.
     */
    public void testGetInstance() {
        System.out.println("getInstance");
        ProxymaContextPool result = ProxymaContextPool.getInstance();

        //Testing instance creation
        assertNotNull("ContextPool not created.",result);

        //Testing singleton behavior
        ProxymaContextPool again = ProxymaContextPool.getInstance();
        assertSame("ContextPool is not a singleton.", result, again);
    }

    /**
     * Test of testRegisterNewContext method, of class ProxymaContextPool.
     */
    public void testRegisterNewContext() {
        System.out.println("registerNewContext");
        String contextName = "default";
        String contextURI = "/";
        String configurationFile = "src/test/resources/testFile.xml";
        ProxymaContextPool instance = ProxymaContextPool.getInstance();
        ProxymaContext result = instance.registerNewContext(contextName, contextURI, configurationFile);

        //Testing context creation
        assertNotNull("Context not created.",result);

        //Testing double context registration
        try {
            ProxymaContext retry = instance.registerNewContext(contextName, contextURI, configurationFile);
            fail("Exception not thrown");
        } catch (IllegalArgumentException ex){
            assertTrue(true);
        }

        //cleanup pool
        try {
            instance.unregisterContext(result);
        } catch (Exception x) {
            fail(x.getMessage());
        }
    }

    /**
     * Test of testUnregisterNewContext method, of class ProxymaContextPool.
     */
    public void testUnregisterContext() {
        System.out.println("unregisterContext");
        String contextName = "default";
        String contextPath = "/";
        String configurationFile = "src/test/resources/testFile.xml";
        ProxymaContextPool instance = ProxymaContextPool.getInstance();
        ProxymaContext result = instance.registerNewContext(contextName, contextPath, configurationFile);
        ProxyFolderBean bean = null;

        //Testing context creation
        assertNotNull("Context not created.",result);

        //Testing remove null context
        try {
            instance.unregisterContext(null);
            fail("Exception not thrown");
        } catch (NullArgumentException ex){
            assertTrue(true);
        }

        //Testing remove unregistered context
        try {
            ProxymaContext unregistered = new ProxymaContext ("unregistered", contextPath, configurationFile);
            instance.unregisterContext(unregistered);
            fail("Exception not thrown");
        } catch (IllegalArgumentException ex){
            assertTrue(true);
        }

        //Testing delete not null context
        try {
            bean = new ProxyFolderBean("default", "http://www.google.com/", result);
            result.addProxyFolder(bean);
            instance.unregisterContext(result);
            fail("Exception not thrown");
        } catch (IllegalStateException ex){
            result.removeProxyFolder(bean);
        } catch (UnsupportedEncodingException e) {
            fail("troubles while creating the ProxyFolderBean");
        }

        //Testing normal delete of a context
         try {
            instance.unregisterContext(result);
            assertNull(instance.getContextByName(result.getName()));
        } catch (Exception x) {
            fail(x.getMessage());
        }
    }


    /**
     * Test of getContext method, of class ProxymaContextPool.
     */
    public void testGetContext() {
        System.out.println("getContext");
        String contextName = "default";
        String contextPath = "/";
        String configurationFile = "src/test/resources/testFile.xml";
        ProxymaContextPool instance = ProxymaContextPool.getInstance();
        ProxymaContext newContext = instance.registerNewContext(contextName, contextPath, configurationFile);


        //Testing loaded context
        ProxymaContext result = instance.getContextByName(contextName);
        assertNotNull("Context not created.",result);
        assertSame("Context retrived is not the same that was created.", result, newContext);

        //cleanup pool
        try {
            instance.unregisterContext(result);
        } catch (Exception x) {
            fail(x.getMessage());
        }
    }
}
