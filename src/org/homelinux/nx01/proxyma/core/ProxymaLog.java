package org.homelinux.nx01.proxyma.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The central place for logging of proxyma.
 * This is so that the rest of Proxyma itself won't be dependent on any given logging
 * implementation.  This class can be changed alone if the details of the logging
 * system used changes.
 *
 * @author Arthur Blake
 */
public class ProxymaLog {

    /**
     * The SLF4J logger used for proxyma audit logging.
     */
    private static final Logger auditLog = LoggerFactory.getLogger("proxyma.audit");

    /**
     * The  SLF4J logger used for proxyma error logging.
     */
    private static final Logger errorsLog = LoggerFactory.getLogger("proxyma.errors");

    /**
     * The  SLF4J logger used for proxyma access logging.
     */
    private static final Logger accessLog = LoggerFactory.getLogger("proxyma.access");

    /**
     * A Stream that forwards anything written to it to the auditLog logger.
     */
    private final PrintStream auditStream = new PrintStream(new LineLoggingStreamInterceptor(auditLog));

    /**
     * A Stream that forwards anything written to it to the error logger.
     */
    private final PrintStream errorsStream = new PrintStream(new LineLoggingStreamInterceptor(errorsLog));

    /**
     * A Stream that forwards anything written to it to the access logger.
     */
    private final PrintStream accessStream = new PrintStream(new LineLoggingStreamInterceptor(accessLog));

    /**
     * a global formatter for the dates in Common Logging Format.
     */
    private static final Format formatter = new SimpleDateFormat(" [dd/MMM/yyyy:HH:mm:ss Z] ");


    public static final ProxymaLog instance = new ProxymaLog();

    /**
     * Prevent external construction.  External classes must use the instance singleton.
     */
    private ProxymaLog() {
    }

    /**
     * Get the stream used for auditing.
     * Anything written to this stream will get sent to the logging system
     * in a line-oriented mode.
     *
     * @return OutputStream for writing audit messages.
     */
    public PrintStream getAuditOutputStream() {
        return auditStream;
    }

    /**
     * Get the stream used for errors.
     * Anything written to this stream will get sent to the logging system
     * in a line-oriented mode.
     *
     * @return OutputStream for writing error messages.
     */
    public PrintStream getErrorOutputStream() {
        return errorsStream;
    }

    /**
     * Get the stream used for access log.
     * Anything written to this stream will get sent to the logging system
     * in a line-oriented mode.
     *
     * @return OutputStream for writing access messages.
     */
    public PrintStream getAccessOutputStream() {
        return accessStream;
    }

    /**
     * Log a proxyma error message.
     *
     * @param msg the error message.
     */
    public void errors(String msg) {
        errorsLog.warn(msg);
    }

    /**
     * Log a proxyma audit message.
     *
     * @param msg the audit message.
     */
    public void audit(String msg) {
        auditLog.info(msg);
    }

    /**
     * Format and write an access record into the access log.
     *
     * @param remoteIp            is the client remote IP  [REQUIRED]
     * @param remoteUser          is the remote user if known [OPTIONAL]
     * @param requestMethod       is the method called (GET or POST) [REQUIRED]
     * @param requestPath         is the path of the request relative to the proxyma fetch basePath [REQUIRED]
     * @param requestProtocol     is the protocol of the request (for example HTTP/1.1) [REQUIRED]
     * @param masqueradedResource is the resource masqueraded by proxyma. This value will be written into the <b>referrer</b> field. [REQUIRED]
     * @param userAgent           if provided from the client browser we will keep track of it [OPTIONAL]
     * @param responseReturnCode  is the return code of the masquerading operation. [REQUIRED]
     */
    public void accessLog(String remoteIp,
                          String remoteUser,
                          String requestMethod,
                          String requestPath,
                          String requestProtocol,
                          String masqueradedResource,
                          String userAgent,
                          int responseReturnCode) {

        //Check for required values.
        if ((remoteIp == null) || (requestMethod == null) || (requestPath == null) ||
                (requestProtocol == null) || (masqueradedResource == null) || (responseReturnCode < 100)) {
            errorsLog.warn("WARNING: can't write the record into access log, a required field is missing.");
        }

        //Create a line in common-log-format into the access.log
        StringBuffer theLine = new StringBuffer();

        //write the remote ip address
        theLine.append(remoteIp);
        theLine.append(" - ");

        //write the remote user
        theLine.append(remoteUser == null ? "-" : remoteUser);

        //formatting the date
        theLine.append(formatter.format(new Date()));

        //get the remote requested resource
        theLine.append("\"");
        theLine.append(requestMethod);
        theLine.append(" ");
        theLine.append(requestPath);
        theLine.append(" ");
        theLine.append(requestProtocol);
        theLine.append("\" ");

        //get the return code
        theLine.append(responseReturnCode);
        theLine.append(" - ");

        //write the remote masqueraded resource into the referrer field
        theLine.append("\"");
        theLine.append(masqueradedResource);
        theLine.append("\" \"");

        //Add informations about the client browser
        theLine.append(userAgent == null ? "User-Agent not provided." : userAgent);
        theLine.append("\"");

        accessLog.info(theLine.toString());
    }

    /**
     * Obtains the current value for the directory where proxyma will writes its own log files.
     * Note that this is the value of the System property.
     *
     * @return the path of the logging directory.
     */
    public String getLoggingDirectoryPath() {
        return System.getProperty(ProxymaConstants.proxymaLoggingBasepathSystemProperty);
    }

    /**
     * Sets the System Propery with the full path of the directory where proxyma will writes its own log files.
     * These files are: proxyma_errors.log, proxyma_access.log and proxyma_audit.log.
     * If the System property was already setted the funcion doesn't override it.
     * Please Note that if you want to set this directory you have to be sure that this method was called BEFORE the initializeLoggingSubsystem(). If not, the default value for this parameter will be used.
     *
     * @param loggingDirectoryPath the path for the logging directory.
     */
    public static final void setLoggingDirectoryPath(String loggingDirectoryPath) {
        String prop = System.getProperty(ProxymaConstants.proxymaLoggingBasepathSystemProperty);
        if (prop == null) {
            if ((loggingDirectoryPath == null) || (ProxymaConstants.EMPTY_STRING.equals(loggingDirectoryPath.trim())))
                System.setProperty(ProxymaConstants.proxymaLoggingBasepathSystemProperty, ProxymaConstants.DEFAULT_LOGGING_PATH);
            else
                System.setProperty(ProxymaConstants.proxymaLoggingBasepathSystemProperty, loggingDirectoryPath);
        }
    }

    /**
     * If the Proxyma is used as a library and the user forgot to set the System property for the logging basepath,
     * this method will set the default value for it to the current directory and initializes the logging subsystem.
     */
    public final static void initializeLoggingSubsystem(String configurationFile) {
        //make me sure thet the logging directory system property is set.
        ProxymaLog.setLoggingDirectoryPath(null);
        if ((configurationFile == null) || ProxymaConstants.EMPTY_STRING.equals(configurationFile.trim())) {
            PropertyConfigurator.configure(ProxymaConstants.DEFAULT_LOGGING_CONFIGURATION_FILE);
        } else {
            PropertyConfigurator.configure(configurationFile);
        }
    }

    /**
     * A special OutputStream that intercepts line oriented data and forwards it to an SLF4J logger.
     * CR or CR-LF terminates each line, and the line is sent as a single INFO level message to the
     * underlying SLF4J log.
     */
    private class LineLoggingStreamInterceptor extends OutputStream {
        private final Logger targetLogger;

        /**
         * Create a LineLoggingStreamInterceptor with the given target logger.
         *
         * @param target target SLF4J
         */
        public LineLoggingStreamInterceptor(Logger target) {
            targetLogger = target;
        }

        /**
         * Writes the specified byte to this output stream. bytes (characters)
         * are saved until a LF or CR-LF is written, at which point the entire line
         * is written as one log entry to the underlying target logger.
         *
         * @param b the <code>byte</code>.
         * @throws java.io.IOException if an I/O error occurs. In particular,
         *                             an <code>IOException</code> may be thrown if the
         *                             output stream has been closed.
         */
        public void write(int b) throws IOException {
            if (b == LF) {
                if (lastByte > 0 && lastByte != CR) {
                    currentLine.append((char) lastByte);
                }
                lastByte = 0;

                targetLogger.info(currentLine.toString());
                currentLine = new StringBuffer();
            } else {
                if (lastByte > 0) {
                    currentLine.append((char) lastByte);
                }
                lastByte = b;
            }
        }

        // last character written to this stream.
        private int lastByte = 0;

        // current line, as being written
        private StringBuffer currentLine = new StringBuffer();

        // carriage return constant
        private static final int CR = 0x0d;

        // line feed constant
        private static final int LF = 0x0a;
    }

}
