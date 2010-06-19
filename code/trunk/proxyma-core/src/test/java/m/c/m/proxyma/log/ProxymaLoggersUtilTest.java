/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.log;

import m.c.m.proxyma.log.ProxymaLoggersUtil;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author shad0w
 */
public class ProxymaLoggersUtilTest extends TestCase {
    
    public ProxymaLoggersUtilTest(String testName) {
        super(testName);
    }

        /**
     * Test of initializeContextLogger method, of class ProxymaLoggersUtil.
     */
    public void testInitializeCustomLogger() {
        System.out.println("initializeCustomLogger");
        Logger logger = Logger.getLogger("proxyma.custom.test");
        String fileName = "/tmp/proxyma-custom-testlog.log";
        int maxLogSize = 1000;
        int logRetention = 1;
        ProxymaLoggersUtil.initializeCustomLogger(logger, fileName, maxLogSize, logRetention);
        logger.info("logging test..");

        File theLogFile = new File(fileName);
        assertTrue(theLogFile.exists());
        assertTrue(theLogFile.length() > 0);
        theLogFile.delete();
    }

    /**
     * Test of initializeContextLogger method, of class ProxymaLoggersUtil.
     */
    public void testInitializeContextLogger() {
        System.out.println("initializeContextLogger");
        Logger logger = Logger.getLogger("proxyma.test");
        String fileName = "/tmp/proxyma-testlog.log";
        String logLevel = "ALL";
        int maxLogSize = 1000;
        int logRetention = 1;
        ProxymaLoggersUtil.initializeContextLogger(logger, fileName, logLevel, maxLogSize, logRetention);
        logger.info("logging test..");

        File theLogFile = new File(fileName);
        assertTrue(theLogFile.exists());
        assertTrue(theLogFile.length() > 0);
        theLogFile.delete();
    }

    /**
     * Test of updateLogLevel method, of class ProxymaLoggersUtil.
     */
    public void testUpdateLogLevel() {
        System.out.println("updateLogLevel");
        Logger logger = Logger.getLogger("proxyma.test");
        String newLevel = "INFO";
        Level curLevel = null;

        try {
            Handler[] handlers = logger.getHandlers();
            boolean foundFileHandler = false;
            for (int index = 0; index < handlers.length; index++) {
                // set console handler
                if (handlers[index] instanceof FileHandler) {
                    curLevel = handlers[index].getLevel();
                    foundFileHandler = true;
                }
            }
            assertTrue(foundFileHandler);
            assertEquals(curLevel.toString(), "ALL");
        } catch (Throwable t) {
            fail("unexpected Exception raised.");
        }

        ProxymaLoggersUtil.updateLogLevel(logger, newLevel);

        try {
            Handler[] handlers = logger.getHandlers();
            boolean foundFileHandler = false;
            for (int index = 0; index < handlers.length; index++) {
                // set console handler
                if (handlers[index] instanceof FileHandler) {
                    curLevel = handlers[index].getLevel();
                    foundFileHandler = true;
                }
            }
            assertTrue(foundFileHandler);
            assertEquals(curLevel.toString(), newLevel);
        } catch (Throwable t) {
            fail("unexpected Exception raised.");
        }
    }

}
