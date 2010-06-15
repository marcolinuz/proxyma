package m.c.m.proxyma.util;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * This class handles the setup and the changes od log level of the
 * proxyma default logger.
 * The logger name is composed with the context name, so any instance of proxyma
 * will have its onw logger and its own logFile.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class ProxymaLogger {

    /**
     * Initialize the logger to write on the passed file with the given log level.
     *
     * @param logger the logger to initialize
     * @param logLevel the log level to set
     * @param fileName the file where to write logs.
     * @param maxLogSize the max number of lines in a single lo file
     * @param logRetention the max number of old log files to keep on filesystem.
     */
    public static void initializeContextLogger(Logger logger, String fileName, String logLevel, int maxLogSize, int logRetention) {
        logger.info("Setting up a new logger \"" + logger.getName() + "\" with output file " + fileName + " and level: " + logLevel );

        try {
            Handler[] handlers = logger.getHandlers();
            boolean foundFileHandler = false;
            for (int index = 0; index < handlers.length; index++) {
                // set console handler
                if (handlers[index] instanceof FileHandler) {
                    handlers[index].setLevel(Level.parse(logLevel));
                    foundFileHandler = true;
                }
            }
            if (!foundFileHandler) {
                // no console handler found
                logger.info("No fileHandler found for this logger, adding one.");
                FileHandler fileHandler = new FileHandler(fileName, maxLogSize, logRetention);
                fileHandler.setLevel(Level.parse(logLevel));
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.setLevel(Level.parse(logLevel));
            }
        } catch (Throwable t) {
            logger.severe("Unexpected Error setting up new log level for logger: " + logger.getName() + " \n" + t);
        }
    }

    /**
     * Changes the log level of the context logger.
     *
     * @param logLevel the new logLevel
     */
    public static void updateLogLevel(Logger logger, String newLevel) {
        logger.info("Setting the log level of logger \"" + logger.getName() + "\" to: " + newLevel);

        try {
            Handler[] handlers = logger.getHandlers();
            boolean foundFileHandler = false;
            for (int index = 0; index < handlers.length; index++) {
                // set console handler
                if (handlers[index] instanceof FileHandler) {
                    handlers[index].setLevel(Level.parse(newLevel));
                    foundFileHandler = true;
                }
            }
            if (!foundFileHandler) {
                // no console handler found
                logger.info("No file handler found for this logger.. nothing done.");
            }
        } catch (Throwable t) {
            logger.severe("Unexpected Error setting up new log level for logger: " + logger.getName() + " \n" + t);
        }
    }
}