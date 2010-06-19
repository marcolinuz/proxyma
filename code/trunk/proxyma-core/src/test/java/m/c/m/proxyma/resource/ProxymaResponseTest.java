/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.resource;

import junit.framework.TestCase;

/**
 *
 * @author shad0w
 */
public class ProxymaResponseTest extends TestCase {
    
    public ProxymaResponseTest(String testName) {
        super(testName);
    }

    /**
     * Test of setResponseData method, of class ProxymaResponse.
     */
    public void testSetGetResponseData() {
        System.out.println("set/get ResponseData");
        ProxymaResponseDataBean responseData = new ProxymaResponseDataBean();
        ProxymaResponse instance = new ProxymaResponseImpl();
        
        //perform tests
        instance.setResponseData(responseData);
        assertSame(responseData, instance.getResponseData());

        instance.setResponseData(null);
        assertNull(instance.getResponseData());
    }
   
    /**
     * Test of hasBeenSent method, of class ProxymaResponse.
     */
    public void testSendingHasBeenSent() {
        System.out.println("sendingData/hasBeenSent");
        ProxymaResponse instance = new ProxymaResponseImpl();

        assertFalse(instance.hasBeenSent());

        instance.sendingData();
        assertTrue(instance.hasBeenSent());
    }

    public class ProxymaResponseImpl extends ProxymaResponse {

        public int sendDataToClient() throws IllegalStateException {
            return 0;
        }
    }

}
