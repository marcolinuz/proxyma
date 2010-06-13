package org.homelinux.nx01.proxyma;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>
 * User: makko
 * Date: 27-mar-2007
 * Time: 18.06.06
 * </p><p>
 * This class is the main portlet of the proxyma-portlet adapter.
 * It implements a lightweight reverse-proxy with basic url-rewriting capabilities.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * todo: make this work..
 */
public class HttpReverseProxyPortlet extends GenericPortlet {

    /**
     * Initialization lifecycle method.  This convenience method
     * is invoked by the GenericPortlet implementation of
     * {@link #init(javax.portlet.PortletConfig)}.
     */
    public void init() throws PortletException {

    }

    /**
     * Destruction lifecycle method.  This method is invoked by
     * the container in order to notify the portlet that it is
     * being taken out of service.
     */
    public void destroy() {
        
    }

    public void processAction(ActionRequest req, ActionResponse res)
    throws PortletException {
        String greeting = getGreeting(req);
        res.setRenderParameter("greeting", greeting);

        /* generate links into a portlet:
          
        PortletURL url = renderResponse.createActionURL;
        url.setParameters(renderRequest.getParameterMap());
        */
    }

    /**
     * Render processing method invoked by
     * {@link #doDispatch(RenderRequest,RenderResponse)}
     * whenever the current portlet mode is
     * {@link javax.portlet.PortletMode.VIEW}.
     *
     * @param req the portlet RenderRequest
     * @param res the portlet RenderResponse
     */
    public void doView(RenderRequest req, RenderResponse res)
    throws IOException {
	res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println(getGreeting(req) + " World!");

        /* To generate a link to images, css etc into a portlet:
           res.encodeURL(req.getContextPath()+ "The/url/relative/to/the/portlet");
        */
    }

    /**
     * Content generating method used by
     * {@link #doDispatch(RenderRequest,RenderResponse)}
     * whenever the current Portlet Mode is
     * {@link javax.portlet.PortletMode.EDIT}.
     *
     * This implementation utilizes a request dispatcher
     * to forward the request to a jsp.
     *
     * @param req the portlet RenderRequest
     * @param res the portlet RenderResponse
     *
     * @throws IOException
     */
    public void doEdit(RenderRequest req, RenderResponse res)
    throws PortletException, IOException {
        PortletRequestDispatcher dispatcher =
            getPortletContext().getRequestDispatcher("/edit.jsp");
        dispatcher.include(req, res);
    }

    /**
     * Render processing method invoked by
     * {@link #doDispatch(RenderRequest,RenderResponse)}
     * whenever the current portlet mode is
     * {@link javax.portlet.PortletMode.HELP}.
     *
     * @param req the portlet RenderRequest
     * @param res the portlet RenderResponse
     */
    public void doHelp(RenderRequest req, RenderResponse res)
    throws PortletException, IOException {
	res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("Help Text Here!");
    }

    private String getGreeting(PortletRequest req) {
        String greeting = req.getParameter("greeting");
        if(greeting == null)
            greeting = this.defaultGreeting;
        return greeting;
    }

    //Portlet Private attributes
    private String defaultGreeting = null;
}
