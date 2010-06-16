package m.c.m.proxyma.context;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaFacade;
import org.apache.commons.lang.NullArgumentException;

/**
 *
 * @author mcm
 */
public class ProxymaContextTest extends TestCase {
    
    public ProxymaContextTest(String testName) {
        super(testName);
    }

    /**
     * Test of getProxyFolder method, of class ProxymaContext.
     */
    public void testGetProxyFolderByName() {
        System.out.println("getProxyFolderByName");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        proxyma.registerProxyFolderIntoContext(expResult, instance);

        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        result = instance.getProxyFolderByURLEncodedName("notExists");
        assertNull(result);

        //clean up the context for further tests
        proxyma.unregisterProxyFolderFromContext(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getProxyFolder method, of class ProxymaContext.
     */
    public void testGetProxyFolderByDestination() {
        System.out.println("getProxyFolderByDestination");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        proxyma.registerProxyFolderIntoContext(expResult, instance);

        ProxyFolderBean result = instance.getProxyFolderByDestination(expResult.getDestination());
        assertSame(expResult, result);

        result = instance.getProxyFolderByDestination("notExists");
        assertNull(result);

        //clean up the context for further tests
        proxyma.unregisterProxyFolderFromContext(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of addProxyFolder method, of class ProxymaContext.
     */
    public void testAddProxyFolder() {
        System.out.println("addProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        try {
            instance.addProxyFolder(null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(null);
            assertNull(result);
        }

        instance.addProxyFolder(expResult);
        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        try {
            instance.addProxyFolder(expResult);
            fail("Exception not thrown");
        } catch (IllegalArgumentException x) {
            int expResultCount = 1;
            int resultCount = instance.getProxyFoldersAsCollection().size();
            assertEquals(expResultCount, resultCount);   
        }

        //clean up the context for further tests
        proxyma.unregisterProxyFolderFromContext(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of removeProxyFolder method, of class ProxymaContext.
     */
    public void testRemoveProxyFolder() {
        System.out.println("removeProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        instance.addProxyFolder(expResult);

        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        int expResultCount = 1;
        int resultCount = instance.getProxyFoldersAsCollection().size();
        assertEquals(expResultCount, resultCount);

        try {
            instance.removeProxyFolder(null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            instance.removeProxyFolder(result);
            result = instance.getProxyFolderByURLEncodedName(proxyFolderName);
            assertNull(result);
        }

        try {
            instance.removeProxyFolder(result);
            fail("Exception not thrown");
        } catch (IllegalArgumentException x) {
            expResultCount = 0;
            resultCount = instance.getProxyFoldersAsCollection().size();
            assertEquals(expResultCount, resultCount);
        }

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setLogLevel method, of class ProxyFolderBean.
     */
    public void testSetLogLevel() {
        System.out.println("setLogLevel");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");

        context.setLogLevel("UNEXISTENT");
        assertEquals(context.getLogLevel(), ProxymaTags.UNSPECIFIED_LOGLEVEL);

        context.setLogLevel("FINER");
        assertEquals(context.getLogLevel(), "FINER");

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getProxyFoldersAsCollection method, of class ProxymaContext.
     */
    public void testGetProxyFoldersAsCollection() {
        System.out.println("getProxyFoldersAsCollection");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        instance.addProxyFolder(expResult);

        int expResultCount = 1;
        int resultCount = instance.getProxyFoldersAsCollection().size();
        assertEquals(expResultCount, resultCount);

        Collection result = instance.getProxyFoldersAsCollection();
        assertTrue (result instanceof Collection);
        
        //clean up the context for further tests
        proxyma.unregisterProxyFolderFromContext(expResult, instance);
        
        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test the get getContextFoldersCount method
     */
    public void getProxyFoldersCount() {
        System.out.println("getProxyFoldersCount");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        instance.addProxyFolder(expResult);

        int expResultCount = 1;
        assertEquals(expResultCount, instance.getProxyFoldersCount());

        //clean up the context for further tests
        proxyma.unregisterProxyFolderFromContext(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of loadConfiguration file and its methods.
     */
    public void testLoadAndGetConfigurationParameters() throws Exception {
        System.out.println("loadAndGetConfigurationParameters");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/testFile.xml");

        // Test Single value loading
        assertEquals("Single attribute load failed.", "single", instance.getSingleValueParameter("singleParameter"));

        // Test Attrivute value loading
        assertEquals("Attribute load failed", "attribute", instance.getSingleValueParameter("loadAttribute/@value"));

        // Test Multiple values loading
        Collection multiValueNames = instance.getMultiValueParameter("aggregation/multivalue/@name");
        Collection multiValueValues = instance.getMultiValueParameter("aggregation/multivalue");
        assertEquals("Number of multiple parameters wrong.", 3, multiValueNames.size());
        assertEquals("Number of multiple parameters wrong.", 3, multiValueValues.size());

        Iterator iterNames = multiValueNames.iterator();
        Iterator iterValues = multiValueValues.iterator();
        int counter = 0;
        while (iterNames.hasNext() && iterValues.hasNext()) {
            assertEquals((String)iterNames.next(), "name"+counter);
            assertEquals((String)iterValues.next(), "value"+counter);
            counter++;
        }

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}
