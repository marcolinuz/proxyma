/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.plugins.caches.NullCacheProvider;
import m.c.m.proxyma.plugins.preprocessors.NullPreprocessor;
import m.c.m.proxyma.plugins.retrivers.NullRetriver;
import m.c.m.proxyma.plugins.serializers.NullSerializer;
import m.c.m.proxyma.plugins.transformers.NullTransformer;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 *
 * @author shad0w
 */
public class ProxyEngineTest extends TestCase {

    /**
     * Test of ProxyEngine Constructor.
     */
    public void testProxyEngineConstructor() {
        ProxyEngine instance = null;
        Collection plugins = null;
        Iterator iter = null;
        System.out.println("ProxyEngineConstructor");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        try {
            instance = new ProxyEngine(context);
        } catch (IllegalAccessException ex) {
            context.getLogger().log(Level.SEVERE, null, ex);
        }

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredCachePlugins();
        assertEquals(1, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  NullCacheProvider);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.PREPROCESSOR);
        assertEquals(1, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  NullPreprocessor);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.RETRIVER);
        assertEquals(1, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  NullRetriver);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.SERIALIZER);
        assertEquals(1, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  NullSerializer);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.TRANSFORMER);
        assertEquals(1, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  NullTransformer);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

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
            instance = new ProxyEngine(context);
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

        //Remove after implementation.
        fail("The test case is a prototype.");
    }

}
