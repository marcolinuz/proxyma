package org.homelinux.nx01.proxyma;

import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.ReverseProxyForServlets;
import org.homelinux.nx01.proxyma.core.ProxymaLog;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 12.06.36
 * </p><p>
 * This class uses the main servlet of the proxyma project to
 * provide a lightweight reverse-proxyForServlets with basic url-rewriting capabilities.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class HttpReverseProxyServlet extends HttpServlet {

    /**
     * This method retrieves configuration parameters from the web.xml and
     * initializes the private members of the servlet.
     */
    public void init() {
        //Set the System properties for the path of the log files of the Proxyma library
        String loggingBasepath = this.getServletContext().getRealPath(ConfigurationConstants.loggingDirectory);
        System.setProperty(ProxymaConstants.proxymaLoggingBasepathProperty, loggingBasepath);

        //Set the System property for the path of the log4j configuration file of the Proxyma Library
        String loggingConfigurationFile = this.getServletContext().getRealPath(ConfigurationConstants.loggingConfigurationFilePath+"/"+ProxymaConstants.log4jConfigurationFileName);
        System.setProperty(ProxymaConstants.log4jConfigurationFileProperty, loggingConfigurationFile);

        //And initialize it..
        ProxymaLog.checkAndLoadDefaultLog4jConfigurationFileProperty();

        // try to understand if a proxy server is required for outgoing connections
        String proxyHost = getInitParameter(ProxymaConstants.proxyHost);
        String proxyPort = getInitParameter(ProxymaConstants.proxyPort);

        if ((proxyHost != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyHost.trim()))) &&
                (proxyPort != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyPort.trim())))) {
            System.getProperties().put("proxySet", ProxymaConstants.TRUE);
            System.getProperties().put("proxyHost", proxyHost.trim());
            System.getProperties().put("proxyPort", proxyPort.trim());
        }

        //Allocates the proxyForServlets instance for this servlet.
        proxyForServlets = new ReverseProxyForServlets();

        // check if should show the masqueraded resources on incomplete requests
        String showMasqueradedResources = getInitParameter(ProxymaConstants.showMasqueradedResources);
        if ((showMasqueradedResources == null) || (ProxymaConstants.EMPTY_STRING.equals(showMasqueradedResources.trim()))) {
            proxyForServlets.setShowMasqueradedResources(ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES);
        } else {
            if (ProxymaConstants.TRUE.equalsIgnoreCase(showMasqueradedResources.trim()))
                proxyForServlets.setShowMasqueradedResources(true);
            else
                proxyForServlets.setShowMasqueradedResources(false);
        }
    }

    /**
     * This method handles the GET requests.
     *
     * @param request  the request..
     * @param response the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        proxyForServlets.doProxy(request, response, false);
    }

    /**
     * This method handles the POST requests.
     *
     * @param request  the request..
     * @param response the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        proxyForServlets.doProxy(request, response, true);
    }


    //The ReverseProxyForServlets Private attribute
    private ReverseProxyForServlets proxyForServlets = null;
}
