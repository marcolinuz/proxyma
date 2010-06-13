/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.context;

import java.util.Collection;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaFacade;

/**
 *
 * @author shad0w
 */
public class ProxyFolderFactoryTest extends TestCase {

    /**
     * Test of createNewProxyFolder method, of class ProxyFolderFactory.
     */
    public void testCreateNewProxyFolder() {
        System.out.println("createNewProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        
        assertEquals(expResult.getFolderName(), proxyFolderName+"/");
        assertEquals(expResult.getDestination(), proxyFolderDestination+"/");
        assertEquals(expResult.getMaxPostSize(), Integer.parseInt(context.getSingleValueParameter(ProxymaTags.FOLDER_MAX_POST_SIZE)));
        assertEquals(expResult.isEnabled(), context.getSingleValueParameter(ProxymaTags.FOLDER_ENABLED).equalsIgnoreCase("true")?true:false);
        assertEquals(expResult.getCacheProvider(), context.getSingleValueParameter(ProxymaTags.FOLDER_CACHEPROVIDER));
        assertEquals(expResult.getRetriver(), context.getSingleValueParameter(ProxymaTags.FOLDER_RETRIVER));
        assertEquals(expResult.getSerializer(), context.getSingleValueParameter(ProxymaTags.FOLDER_SERIALIZER));

        Collection <String> preprocessors = expResult.getPreprocessors();
        assertEquals(preprocessors.size(), 0);

        Collection <String> transformers = expResult.getTransformers();
        assertEquals(transformers.size(), 4);

        assertTrue(transformers.contains("m.c.m.proxyma.plugins.transformers.HtmlUriRewriter"));
        assertTrue(transformers.contains("m.c.m.proxyma.plugins.transformers.CssUriRewriter"));
        assertTrue(transformers.contains("m.c.m.proxyma.plugins.transformers.HttpCookiesRevriter"));
        assertTrue(transformers.contains("m.c.m.proxyma.plugins.transformers.HttpRedirectRewriter"));

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}
