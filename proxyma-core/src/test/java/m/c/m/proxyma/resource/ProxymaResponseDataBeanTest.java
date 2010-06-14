/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.resource;

import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import junit.framework.TestCase;
import org.apache.commons.lang.NullArgumentException;

/**
 *
 * @author shad0w
 */
public class ProxymaResponseDataBeanTest extends TestCase {
    
    public ProxymaResponseDataBeanTest(String testName) {
        super(testName);
    }

    /**
     * Test of getHeaderNames method, of class ProxymaResponseDataBean.
     */
    public void testGetHeaderNames() {
        System.out.println("getHeaderNames");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("name1", "value1");
        instance.addHeader("name1 ", "value1.1");
        instance.addHeader(" name2", "value2");
        Collection<String> result = instance.getHeaderNames();

        //test collection size
        assertEquals(2, result.size());

        //Test values
        Iterator<String> iter = result.iterator();
        if ("name1".equals(iter.next())) {
            assertEquals("name2", iter.next());
        } else {
            assertEquals("name1", iter.next());
        }

    }

    /**
     * Test of getMultipleHeaderValues method, of class ProxymaResponseDataBean.
     */
    public void testGetMultvalueHeader() {
        System.out.println("getMultivalueHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("name1 ", "value1");
        instance.addHeader(" name1", "value2");
        instance.addHeader("name3", "value3");
        Collection<ProxymaHttpHeader> result = instance.getMultivalueHeader("nAmE1");

        //Test size
        assertEquals(2, result.size());

        //Test multi values header
        Iterator<ProxymaHttpHeader> iter = result.iterator();
        if ("name1: value1".equals(iter.next().toString())) {
            assertEquals("name1: value2", iter.next().toString());
        } else {
            assertEquals("name1: value1", iter.next().toString());
        }

        //Test single value header
        result = instance.getMultivalueHeader("NAME3");
        assertEquals(1, result.size());
        iter = result.iterator();
        assertEquals("name3: value3", iter.next().toString());

        //Test unexisting header
        result = instance.getMultivalueHeader("Unexisting");
        assertNull(result);
    }

    /**
     * Test of getHeader method, of class ProxymaResponseDataBean.
     */
    public void testGetHeader() {
        System.out.println("getHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("name1", "value1 ");
        instance.addHeader("name1 ", "value2");
        instance.addHeader("Name3", " Value3");
        ProxymaHttpHeader result = instance.getHeader("nAmE1");
        assertEquals("name1: value1", result.toString());

        result = instance.getHeader("name3");
        assertEquals("Name3: Value3", result.toString());

        result = instance.getHeader("Unexisting");
        assertNull(result);
    }

    /**
     * Test of addHeader method, of class ProxymaResponseDataBean.
     */
    public void testAddHeader() {
        System.out.println("addHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        
        try {
            instance.addHeader(null, null);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
            assertTrue(true);
        }

        try {
            instance.addHeader("  ", "");
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
            assertTrue(true);
        }

        instance.addHeader("Name ", " Value");
        assertEquals("Name: Value", instance.getHeader("nAmE").toString());

        //testLocking
        instance.lock();
        try {
            instance.addHeader("name4", "value4");
            fail("exception not thrown");
        } catch (IllegalStateException x) {
            assertTrue(true);
        }
    }

    /**
     * Test of containsHeader method, of class ProxymaResponseDataBean.
     */
    public void testContainsHeader() {
        System.out.println("containsHeader");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader(" name1 ", "value1");

        boolean result = instance.containsHeader("NaMe1");
        assertTrue(result);

        result = instance.containsHeader("Unexisting");
        assertFalse(result);
    }

    /**
     * Test of deleteHeader method, of class ProxymaResponseDataBean.
     */
    public void testDeleteHeader() {
        System.out.println("deleteHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("name1 ", "value1");
        instance.addHeader("name1", "value2");
        instance.addHeader(" Name3 ", "Value3");

        Collection result = instance.getHeaderNames();
        assertEquals(2, result.size());

        instance.deleteHeader("nAmE3");
        result = instance.getHeaderNames();
        assertEquals(1, result.size());

        instance.deleteHeader("nAmE2");
        result = instance.getHeaderNames();
        assertEquals(1, result.size());

        instance.deleteHeader("NAme1");
        result = instance.getHeaderNames();
        assertEquals(0, result.size());

        //testLocking
        instance.lock();
        try {
            instance.deleteHeader("name2");
            fail("exception not thrown");
        } catch (IllegalStateException x) {
            assertTrue(true);
        }
    }

    /**
     * Test of getCookies method, of class ProxymaResponseDataBean.
     */
    public void testGetCookies() {
        System.out.println("getCookies");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Collection<Cookie> result = instance.getCookies();
        assertEquals(2, result.size());

        //Test multi values header
        Iterator<Cookie> iter = result.iterator();
        Cookie cookie = iter.next();
        if ("name1".equals(cookie.getName())) {
            assertEquals("value3", cookie.getValue());
            assertEquals("value2", iter.next().getValue());
        } else {
            assertEquals("name2", cookie.getName());
            assertEquals("value2", cookie.getValue());
            assertEquals("value3", iter.next().getValue());
        }

        instance = new ProxymaResponseDataBean();
        result = instance.getCookies();
        assertEquals(0, result.size());
    }

    /**
     * Test of getCookie method, of class ProxymaResponseDataBean.
     */
    public void testGetCookie() {
        System.out.println("getCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Cookie result = instance.getCookie("name1");
        assertEquals("value3", result.getValue());

        result = instance.getCookie("name2");
        assertEquals("value2", result.getValue());

        //Test unexisting value
        result = instance.getCookie("unexistent");
        assertNull(result);
    }

    /**
     * Test of addCookie method, of class ProxymaResponseDataBean.
     */
    public void testAddCookie() {
        System.out.println("addCookie");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();

        //Add null cookie
        try {
            instance.addCookie(null);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //add empty cookie
        try {
            instance.addCookie(new Cookie(null, null));
            fail("exception not thrown");
        } catch (Exception x) {
            assertTrue(true);
        }

        instance.addCookie(new Cookie("name1", "value1"));
        assertEquals(1, instance.getCookies().size());

        //testLocking
        instance.lock();
        try {
            instance.addCookie(new Cookie("name4", "value4"));
            fail("exception not thrown");
        } catch (IllegalStateException x) {
            assertTrue(true);
        }
    }

    /**
     * Test of containsCookie method, of class ProxymaResponseDataBean.
     */
    public void testContainsCookie() {
        System.out.println("containsCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        boolean result = instance.containsCookie("name1");
        assertTrue(result);

        result = instance.containsCookie("name2");
        assertTrue(result);

        //Test unexisting value
        result = instance.containsCookie("unexistent");
        assertFalse(result);

        //test null search
        result = instance.containsCookie(null);
        assertFalse(result);
    }

    /**
     * Test of deleteCookie method, of class ProxymaResponseDataBean.
     */
    public void testDeleteCookie() {
        System.out.println("deleteCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Collection<Cookie> result = instance.getCookies();
        assertEquals(2, result.size());

        try {
            instance.deleteCookie(null);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        instance.deleteCookie("unexistent");
        result = instance.getCookies();
        assertEquals(2, result.size());

        instance.deleteCookie("name1");
        result = instance.getCookies();
        assertEquals(1, result.size());

        //testLocking
        instance.lock();
        try {
            instance.deleteCookie("name1");
            fail("exception not thrown");
        } catch (IllegalStateException x) {
            assertTrue(true);
        }
    }

    /**
     * Test of getBufferSize method, of class ProxymaResponseDataBean.
     */
    public void testLock_isLocked() {
        System.out.println("lock/isLocked");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();

        assertFalse(instance.islocked());
        instance.lock();
        assertTrue(instance.islocked());
    }

}
