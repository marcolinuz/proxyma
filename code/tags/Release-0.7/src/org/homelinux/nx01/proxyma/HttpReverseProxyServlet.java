package org.homelinux.nx01.proxyma;

import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.ReverseProxy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p/>
 * User: makko
 * Date: 5-dic-2006
 * Time: 12.06.36
 * </p><p>
 * This class uses the main interface (ProxymaFacade) of the proxyma library to
 * provide a lightweight reverse-proxy with basic url-rewriting capabilities.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * todo: support virtual hosts by using host header as proxyma context.
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class HttpReverseProxyServlet extends HttpServlet {

    /**
     * This method retrieves configuration parameters from the web.xml and
     * initializes Proxyma library using the ProxymaFacade class.
     */
    public void init() {
        // try to understand if a proxy server is required for outgoing connections
        String proxyHost = getInitParameter(ProxymaConstants.proxyHost);
        String proxyPort = getInitParameter(ProxymaConstants.proxyPort);
        if ((proxyHost != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyHost.trim()))) &&
                (proxyPort != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyPort.trim())))) {
            System.setProperty("proxySet", ProxymaConstants.TRUE);
            System.setProperty("proxyHost", proxyHost.trim());
            System.setProperty("proxyPort", proxyPort.trim());
        }

        //Initialize the proxyma library
        //Set the the path of the logging files and configuration files of the Proxyma library
        String loggingBasepath = this.getServletContext().getRealPath(ConfigurationConstants.loggingDirectory);
        String loggingConfigurationFile = this.getServletContext().getRealPath(ConfigurationConstants.loggingConfigurationFilePath + "/" + ProxymaConstants.log4jConfigurationFileName);
        ProxymaFacade.initialize(loggingBasepath, loggingConfigurationFile);

        // obtains the value of the "show the masqueraded resources on incomplete requests" configuration parameter.
        String showMasqueradedResources = getInitParameter(ProxymaConstants.showMasqueradedResources);

        // obtain the cache subsystem configuration file path.
        String ehcacheSubsystemConfigurationFile = getInitParameter(ProxymaConstants.ehcacheSubsystemConfigurationFile);

        //Allocates the reverse proxy instance for this servlet.
        proxy = ProxymaFacade.getNewReverseProxy(ehcacheSubsystemConfigurationFile, showMasqueradedResources);
    }

    /**
     * Finalize the Proxyma Libary.
     */
    public void finalize() {
        ProxymaFacade.shutdown(proxy);
    }

    /**
     * This method handles the GET requests using the reverse proxy to process them.
     *
     * @param request  the request..
     * @param response the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        proxy.doProxy(request, response);
    }

    /**
     * This method handles the POST requests  using the reverse proxy to process them.
     *
     * @param request  the request..
     * @param response the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        proxy.doProxy(request, response);
    }

    //The ReverseProxy Private attribute for this servlet.
    private ReverseProxy proxy = null;
}
