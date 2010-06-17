/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.core;

import java.util.logging.Level;
import junit.framework.TestCase;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 *
 * @author shad0w
 */
public class ProxyEngineTest extends TestCase {

    /**
     * Test of doProxy method, of class ProxyEngine.
     */
    public void testDoProxy() {
        System.out.println("doProxy");
        ProxymaResource aResource = null;
        ProxyEngine instance = null;
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        try {
            instance = proxyma.createNewProxyEngine(context);
        } catch (IllegalAccessException ex) {
            context.getLogger().log(Level.SEVERE, null, ex);
        }

        // TODO review the generated test code and remove the default call to fail.

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}
