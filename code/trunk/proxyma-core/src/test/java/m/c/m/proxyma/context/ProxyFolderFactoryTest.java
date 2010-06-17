/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.context;

import java.util.Iterator;
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
        
        assertEquals(expResult.getFolderName(), proxyFolderName);
        assertEquals(expResult.getDestination(), proxyFolderDestination);
        assertEquals(expResult.getMaxPostSize(), Integer.parseInt(context.getSingleValueParameter(ProxymaTags.FOLDER_MAX_POST_SIZE)));
        assertEquals(expResult.isEnabled(), context.getSingleValueParameter(ProxymaTags.FOLDER_ENABLED).equalsIgnoreCase("true")?true:false);
        assertEquals(expResult.getCacheProvider(), context.getSingleValueParameter(ProxymaTags.FOLDER_CACHEPROVIDER));
        assertEquals(expResult.getRetriver(), context.getSingleValueParameter(ProxymaTags.FOLDER_RETRIVER));
        assertEquals(expResult.getSerializer(), context.getSingleValueParameter(ProxymaTags.FOLDER_SERIALIZER));

        Iterator <String> preprocessors = expResult.getPreprocessors();
        int counter = 0;
        while (preprocessors.hasNext()) {
            preprocessors.next();
            counter++;
        }
        assertEquals(0, counter);


        Iterator <String> transformers = expResult.getTransformers();
        counter = 0;
        String expResults[] = new String[] {"m.c.m.proxyma.plugins.transformers.HtmlUrlRewriter",
                              "m.c.m.proxyma.plugins.transformers.CssUrlRewriter",
                              "m.c.m.proxyma.plugins.transformers.HttpCookiesRevriter",
                              "m.c.m.proxyma.plugins.transformers.HttpRedirectRewriter"};
        while (transformers.hasNext()) {
            assertEquals(transformers.next(), expResults[counter]);
            counter++;
        }
        assertEquals(4, counter);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}
