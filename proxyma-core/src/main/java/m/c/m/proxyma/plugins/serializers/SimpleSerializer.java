package m.c.m.proxyma.plugins.serializers;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.log.ProxymaLoggersUtil;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This plugin implements a simple serializer.<br/>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class SimpleSerializer extends m.c.m.proxyma.plugins.serializers.AbstractSerializer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public SimpleSerializer (ProxymaContext context) {
        //initialize the context logget
        log = context.getLogger();

        //Find the specific-plugin section for this plugin xpath to retrive the directives from the configuration file.
        String configXpath = ProxymaTags.PLUGINS_SPECIFIC_BASE_XPATH + "plugin[@name='" + this.getClass().getName() + "']";
             
        //Get configuration files direxctives
        String logFilePrefix = context.getSingleValueParameter(configXpath+"/filePrefix");
        int maxLogSize = Integer.parseInt(context.getSingleValueParameter(configXpath+"/maxLinesPerFile"));
        int logRetention = Integer.parseInt(context.getSingleValueParameter(configXpath+"/retention"));

        //Set up the access-log logger
        accessLog = Logger.getLogger(ProxymaTags.DEFAULT_LOGGER_PREFIX + "." + context.getName() + ".access");
        String logFilePath = logFilePrefix + "-" + context.getName() + "-access.log";
        ProxymaLoggersUtil.initializeCustomLogger(accessLog, logFilePath, maxLogSize, logRetention);
    }

    /**
     * This method sends back to the client the response-data of the resource
     * adding to it only some useful headers.<br/>
     * It writes also the access log records in common logging format.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws IOException{
        //Get the response data from the resource
        ProxymaResponseDataBean response = aResource.getResponse().getResponseData();

        //Sets the content-lenght header to the real value of the content size.
        if (response.getData() != null)
            response.addHeader(CONTENT_LENGTH_HEADER, response.getData().getSize());

        //Set the special header that is a de facto standard for identifying the
        //originating IP address of a client connecting to a web server through an HTTP proxy
        response.addHeader(X_FORWARDED_FOR_HEADER, aResource.getRequest().getRemoteAddress());

        //Send the resource back to the client
        int statusCode = aResource.getResponse().sendDataToClient();
        
        //Writes the record into the access-log file
        accessLog.info(generateExtendedAccessLog(aResource, statusCode));
    }

    /**
     * Returns the name of the plugin.
     * @return the name of the plugin.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of what the plugin does..<br/>
     * You can use html tags into it.<br/>
     * The result of this method call can be used by any interface
     * to explain for what is the puropse of the plugin.
     *
     * @return a short description of the plugin
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * Creates the log line in extended common logging format using the passed
     * resource to obtain the data.
     *
     * @param aResource the resource to inspect for logging data
     * @param statusCode the status code returned by the operation
     * @return the message to write into the access-log
     */
    private String generateExtendedAccessLog(ProxymaResource aResource, int statusCode) {
        //Create a line in common-log-format into the access.log
        StringBuffer theLRecord = new StringBuffer(1024);

        //write the remote ip address
        theLRecord.append(aResource.getRequest().getRemoteAddress());
        theLRecord.append(" - ");

        //write the remote user
        String remoteUser = aResource.getRequest().getRemoteUser();
        theLRecord.append(remoteUser == null ? "-" : remoteUser);

        //formatting the date
        theLRecord.append(dateFormatter.format(new Date()));

        //get the remote requested resource
        theLRecord.append("\"");
        theLRecord.append(aResource.getRequest().getMethod());
        theLRecord.append(" ");
        theLRecord.append(aResource.getRequest().getRequestURI());
        theLRecord.append(" ");
        theLRecord.append(aResource.getRequest().getProtocol());
        theLRecord.append("\" ");

        //get the return code
        theLRecord.append(statusCode);

        //get the content size
        ByteBuffer data = aResource.getResponse().getResponseData().getData();
        theLRecord.append(" ");
        theLRecord.append(data==null?0:data.getSize());
        theLRecord.append(" ");

        //write the remote masqueraded resource into the referrer field
        ProxyFolderBean folder = aResource.getProxyFolder();
        theLRecord.append("\"");
        theLRecord.append(folder==null?aResource.getProxymaRootURI():folder.getDestination());
        theLRecord.append(aResource.getDestinationSubPath()==null?EMPTY_STRING:aResource.getDestinationSubPath());
        theLRecord.append("\" \"");

        //Add informations about the client browser
        String userAgent = aResource.getRequest().getHeader("User-Agent");
        theLRecord.append(userAgent == null ? "User-Agent not provided." : userAgent);
        theLRecord.append("\"");

        return theLRecord.toString();
    }

    /**
     * The logger of the context..
     */
    private Logger log = null;

    /**
     * The logger to write the access log..
     */
    private Logger accessLog = null;

    /**
     * The date formatter for the common logging format.
     */
    private final Format dateFormatter = new SimpleDateFormat(" [dd/MMM/yyyy:HH:mm:ss Z] ");

    /**
     * The name of this plugin.
     */
    private static final String name = "Simple Serializer";

    /**
     * The content length header name
     */
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    /**
     * The special header that tells to the real servers for who the proxy is working
     */
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /**
     * An empty string..
     */
    private static final String EMPTY_STRING = "";
    
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a simple HTTP serializer.<br/>"
            + "Its work is to send back to the client the response adding to it "
            + "only a few only useful headers.<br/>"
            + "It writes also the \"access log\" in common logging format.";
}