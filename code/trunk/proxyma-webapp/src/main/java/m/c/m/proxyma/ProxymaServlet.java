package m.c.m.proxyma;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ProxyEngine;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is a simple servlet that uses the proxyma-core library to provide
 * a multiple reverse proxy with URL Rewrite capabilities.
 *
 * NOTE: This is only the hook to the reverse-proxy engine. If you are looking
 *       for proxyma intercafe take a look to the proxyma-console webapp.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id$
 */
public class ProxymaServlet extends HttpServlet {

    /**
     * Initialize the servlet and the proxyma environment.
     */
    public void init() {
        try {
            //Obtain configuration parameters..
            ServletConfig config = this.getServletConfig();
            String proxymaConfigFile = config.getServletContext().getRealPath("/WEB-INF/proxyma-config.xml");
            String proxymaContextName = this.getInitParameter("ProxymaContextName");
            String proxymaLogsDirectory = this.getInitParameter("ProxymaLogsDir");
            
            //Check if the logs directory init-parameter ends with "/"
            if (!proxymaLogsDirectory.endsWith("/")) {
                proxymaLogsDirectory = proxymaLogsDirectory + "/";
            }

            //Create a new proxyma facade
            this.proxyma = new ProxymaFacade();

            //Create a new proxyma context
            this.proxymaContext = proxyma.createNewContext(proxymaContextName, config.getServletContext().getContextPath(), proxymaConfigFile, proxymaLogsDirectory);
            
            //Create a reverse proxy engine for this servlet thread
            this.proxymaEngine = proxyma.createNewProxyEngine(proxymaContext);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * This method uses the proxyma-core reverse proxy engine to serve the clients
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            //Obtain a new Proxyma Resource form the sequest and response
            ProxymaResource clientRequest = proxyma.createNewResource(request, response, proxymaContext);
            //process the resource with the proxy engine
            proxymaEngine.doProxy(clientRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
    } 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * The instance of the proxyma-core facade class that will be used to
     * setup the reverse proxy environment
     */
    private ProxymaFacade proxyma = null;

    /**
     * The reverse proxy engine used to handle the client requests
     */
    private ProxyEngine proxymaEngine = null;

    /**
     * The proxyma-context where this webapp will live.
     */
    private ProxymaContext proxymaContext = null;
}
