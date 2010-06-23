package m.c.m.proxyma.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This Class its a factory of standard responses.<br/>
 * It's used by the ProxyEngine to build internal responses as error pages,
 * redirects and the page that shows the registered proxy folders.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id$
 */
public class ProxyInternalResponsesFactory {
    /**
     * Generates a listPage response to the specified destination.
     * @param destination the complete URL of the destination
     * @return a new response data bean ready to be sent to the client
     */
    public static ProxymaResponseDataBean createRedirectResponse(String destination, ProxymaContext context) throws MalformedURLException {
        //Check if the passed destination is a valid url
        URL test = new URL(destination);

        //The return value
        ProxymaResponseDataBean redirect = new ProxymaResponseDataBean();

        //Set response status
        redirect.setStatus(STATUS_FOUND);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        redirect.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        redirect.addHeader(SERVER_HEADER, context.getProxymaVersion());

        //Add headers for the cache providers to avoid to cache this resource
        redirect.addHeader(CACHE_CONTROL_HEADER, NO_CACHE);
        redirect.addHeader(PRAGMA_HEADER, NO_CACHE);

        //Add proxyma URI location header
        redirect.addHeader(LOCATION_HEADER, destination);

        return redirect;
    }

    /**
     * Generates an error response.
     * @param the error status to report
     * @return a new response data bean ready to be sent to the client
     */
    public static ProxymaResponseDataBean createErrorResponse(int errorCode, ProxymaContext context) {
        ProxymaResponseDataBean error = new ProxymaResponseDataBean();

        //Set response status
        error.setStatus(errorCode);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        error.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        error.addHeader(SERVER_HEADER, context.getProxymaVersion());

        //Add headers for the cache providers to avoid to cache this resource
        error.addHeader(CACHE_CONTROL_HEADER, NO_CACHE);
        error.addHeader(PRAGMA_HEADER, NO_CACHE);

        return error;
    }

    /**
     * Generates a new response page with the list of the configured folders.<br/>
     * The fields of the list are: folderName, Destination, folderStatus.<br/>
     * Note: this methos is made to be invoked only by the proxy engine.
     * @param contezt the context that contains the registered folders to show.
     * @return a new response data bean ready to be sent to the client
     */
    protected static ProxymaResponseDataBean createFoldersListResponse(ProxymaContext context) {
        ProxymaResponseDataBean listPage = new ProxymaResponseDataBean();
        Logger log = context.getLogger();
        String charsetEncoding = context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);

        log.fine("Generating folders list page..");

        //Set response status
        listPage.setStatus(STATUS_OK);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        listPage.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        listPage.addHeader(SERVER_HEADER, context.getProxymaVersion());

        //Add Content-Type header
        listPage.addHeader(CONTENT_TYPE_HEADER, "text/html;charset="+charsetEncoding);

        //Add headers for the cache providers to avoid to cache this resource
        listPage.addHeader(CACHE_CONTROL_HEADER, NO_CACHE);
        listPage.addHeader(PRAGMA_HEADER, NO_CACHE);

        //Prepare the byte buffer with the page content.
        try {
            ByteBuffer out = ByteBufferFactory.createNewByteBuffer(context);

            //write the header of the page
            byte[] data = html_head_template.getBytes(charsetEncoding);
            out.appendBytes(data,data.length);

            //iterate the configured folders
            Iterator<ProxyFolderBean> iter = context.getProxyFoldersAsCollection().iterator();
            boolean even = true;
            while (iter.hasNext()) {
                ProxyFolderBean folder = iter.next();
                log.finest("   adding folder \"" + folder.getFolderName() + "\" to the page..");
                String ruleRow = html_resource_row_template.replaceAll(proxyFolderName, folder.getFolderName());
                ruleRow = ruleRow.replaceAll(proxyFolderURI, folder.getURLEncodedFolderName());
                ruleRow = ruleRow.replaceAll(proxyDestination, folder.getDestinationAsString());
                ruleRow = ruleRow.replaceFirst(status, folder.isEnabled() ? "Active" : "Locked");
                ruleRow = ruleRow.replaceFirst(statusColor, folder.isEnabled() ? "black" : "red");
                ruleRow = ruleRow.replaceFirst(bgcolor, even?evenBgcolor:oddBgcolor);
                even = !even;

                //append data to the buffer
                data = ruleRow.getBytes(charsetEncoding);
                out.appendBytes(data,data.length);
            }

            //write the footer of the page
            data = html_tail_template.getBytes(charsetEncoding);
            out.appendBytes(data,data.length);

            //add the buffer to the response
            listPage.setData(out);
        } catch (Exception e) {
            log.severe("Unable to generate the folder list page.");
            e.printStackTrace();
            listPage = createErrorResponse(STATUS_SERVER_ERROR, context);
        }
        log.finest("page creation done.");

        return listPage;
    }

    /************************************************/
    /* Misc constants useful to build the responses */
    /************************************************/
    
    /**
     * Standard status code for the redirect
     */
    private static final int STATUS_FOUND = 302;

    /**
     * Standard status code for a successfull response
     */
    private static final int STATUS_OK = 200;

    /**
     * Standard status code for an internal server error
     */
    private static final int STATUS_SERVER_ERROR = 500;

    /**
     * The date header name
     */
    private static final String DATE_HEADER = "Date";

    /**
     * The originating server header name
     */
    private static final String SERVER_HEADER = "Server";

    /**
     * The redirect locarion header name
     */
    private static final String LOCATION_HEADER = "Location";

    /**
     * The content typp / encoding  header name
     */
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * One of the headers that controls the behavior of the caches
     */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    /**
     * One of the headers that controls the behavior of the caches
     */
    private static final String PRAGMA_HEADER = "Pragma";

    /**
     * Directive to avoid chaching
     */
    private static final String NO_CACHE = "no-cache";

    /*********************************************************/
    /* String constants to build a row of the list page data */
    /*********************************************************/

    //Placeholters for the substitutions into the row template
    private final static String proxyFolderName = "%PROXYFOLDER%";
    private final static String proxyFolderURI = "%PROXYFOLDERURI%";
    private final static String proxyDestination = "%PROXYPASSHOST%";
    private final static String status = "%STATUS%";
    private final static String statusColor = "%COLOR%";
    private final static String bgcolor = "%BGCOLOR%";
    private final static String evenBgcolor = "#f0f0f0";
    private final static String oddBgcolor = "#daeaff";


    /*******************************************************************/
    /* Static content template data to shows the list of proxy folders */
    /*******************************************************************/

    /**
     * Header of the html page to show the list of the registered proxy-folders
     */
    private final static String html_head_template = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html>\n" +
            "<head><title>Proxyma Available Resources:</title><style type=\"text/css\">td{font-family: Verdana, Arial, Helvetica, sans-serif; font-size: small;}</style></head>\n" +
            "<body><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"medium\"><b>Available Rules:</b><hr/>\n" +
            "<table align=\"center\" width=\"95%\">\n" +
            "<tr bgcolor=\"#bacaff\">\n" +
            "<td width=\"20%\" align=\"left\"><b>Resource</b></td>" +
            "<td width=\"65%\" align=\"left\"><b>Masqueraded Resource</b></td>" +
            "<td width=\"15%\" align=\"center\"><b>status</b></td>\n" +
            "</tr>\n";

    /**
     * Template for a single row of the list of the registered proxy-folders
     */
    private final static String html_resource_row_template = "" +
            "<tr bgcolor=\"" + bgcolor + "\">\n" +
            "<td align=\"left\"><a href=\"./" + proxyFolderURI + "/\">" + proxyFolderName + "</a></td>" +
            "<td align=\"left\"><a href=\"" + proxyDestination + "\">" + proxyDestination + "</a></td>" +
            "<td align=\"center\"><font color=\"" + statusColor + "\">" + status + "</font></td>\n" +
            "</tr>\n";

    /**
     * Footrer of the html page to show the list of the registered proxy-folders
     */
    private final static String html_tail_template = "" +
            "</table>\n" +
            "<hr/>Generated by Proxyma.\n" +
            "</font></body>\n" +
            "</html>\n";
}
