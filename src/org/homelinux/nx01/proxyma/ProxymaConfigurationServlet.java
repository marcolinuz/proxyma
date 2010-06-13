package org.homelinux.nx01.proxyma;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.ProxymaRuleFactory;
import org.homelinux.nx01.proxyma.core.ProxymaRuleSet;
import org.homelinux.nx01.proxyma.core.ProxymaRuleSetsPool;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 * </p><p>
 * User: makko
 * Date: 28-Apr-2007
 * Time: 12.02.44
 * </p><p>
 * This class is the configuration servlet for Proxyma rules
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 */
public class ProxymaConfigurationServlet extends HttpServlet {

    /**
     * This method retrives configuration parameters from the web.xml and
     * initialize the private members of the servlet.
     *
     * @throws javax.servlet.ServletException
     */
    public void init() throws ServletException {
        httpReverseProxyServletSubPath = getInitParameter(ConfigurationConstants.httpReverseProxyServletSubPath).trim();
        if ((httpReverseProxyServletSubPath == null) || (ProxymaConstants.EMPTY_STRING.equals(httpReverseProxyServletSubPath)))
            System.out.println("WARNING! Configuration parameter \"" + ConfigurationConstants.httpReverseProxyServletSubPath + "\" was not found, So the Configuration pages Will not work properly!!");
    }

    /**
     * This method handles the POST requests.
     *
     * @param httpservletrequest  the request..
     * @param httpservletresponse the response..
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doPost(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        doGet(httpservletrequest, httpservletresponse);
    }

    /**
     * This method handles the GET requests.
     *
     * @param httpservletrequest  the requrest..
     * @param httpservletresponse the response..
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doGet(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        //regexp that matches the basepath of proxyma
        Pattern pattern = getServletPathMatchPattern(httpservletrequest.getServletPath());
        Matcher matcher = pattern.matcher(httpservletrequest.getRequestURI());

        //searching for proxyma base url.
        matcher.find();

        //This is the proxyma base path url (without trailing "/")
        //NOTE: Here an exception is trown if the path is the same as the basepath and did not end with a "/"
        String configBasePath = null;
        try {
            configBasePath = matcher.group();
        } catch (IllegalStateException e) {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Otain the global Ruleset for thit instance of proxyma.
        String removeFromRegexp = httpservletrequest.getServletPath();
        ProxymaRuleSetsPool ruleSetsPool = ProxymaRuleSetsPool.getInstance();
        String proxymaInstanceContext = configBasePath.replaceFirst(removeFromRegexp, ProxymaConstants.EMPTY_STRING);
        ProxymaRuleSet ruleSet = ruleSetsPool.getRuleSet(proxymaInstanceContext);

        //Getting parameters
        String method = httpservletrequest.getParameter(ConfigurationConstants.method).trim();
        String target = httpservletrequest.getParameter(ConfigurationConstants.target).trim();
        String action = httpservletrequest.getParameter(ConfigurationConstants.action).trim();

        if (!(validateParameters(method, target, action))) {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Choose what to do..
        //manages updateRule method
        if (ConfigurationConstants.updateRule.equals(method)) {
            //Update or modify an existent rule
            if (ConfigurationConstants.enable.equals(action)) {
                //enble a disabled rule
                enableRule(target, ruleSet);
                httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                httpservletrequest.setAttribute(ConfigurationConstants.message, "Rule \"" + target + "\" successfully enabled.");
                forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            } else if (ConfigurationConstants.disable.equals(action)) {
                //disable an enabled rule
                disableRule(target, ruleSet);
                httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                httpservletrequest.setAttribute(ConfigurationConstants.message, "Rule \"" + target + "\" successfully disabled.");
                forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            } else if (ConfigurationConstants.edit.equals(action)) {
                //Edit an existent rule
                ProxymaRuleBean rule = ruleSet.getRule(target);
                if (rule != null) {
                    httpservletrequest.setAttribute(ConfigurationConstants.ruleBean, rule);
                    forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
                } else {
                    httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }else if (ConfigurationConstants.delete.equals(action)) {
                //delete an existent rule
                try {
                    ruleSet.removeRule(target);
                    httpservletrequest.setAttribute(ConfigurationConstants.message, "Rule \"" + target + "\" successfully deleted.");
                } catch (NullPointerException e) {
                    httpservletrequest.setAttribute(ConfigurationConstants.message, e.getMessage());
                }

                httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            } else {
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        //Manages refreshPage method
        } else if (ConfigurationConstants.refreshPage.equals(method)) {
            //Refresh page data
            if (ConfigurationConstants.ruleList.equals(action)) {
                httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            } else {
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        //Manages addRule method by forwarding to the edit rule page
        } else if (ConfigurationConstants.addRule.equals(method)) {
            //Add a new Rule
            forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
        //todo: NOT YET IMPLEMENTED, will manage massive rules import from DB, XML, file.. etc...
        } else if (ConfigurationConstants.importRules.equals(method)) {
            //Import rules from external sources
            httpservletrequest.setAttribute(ConfigurationConstants.message, "Function not yet Implemented :O(");
            httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
            forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            return;
        //todo: NOT YET IMPLEMENTED, will manage massive rules export to DB, XML, file.. etc...
        } else if (ConfigurationConstants.exportRules.equals(method)) {
            //Import rules from external sources
            httpservletrequest.setAttribute(ConfigurationConstants.message, "Function not yet Implemented :O(");
             httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
            forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
            return;
        //Manages edit page requests.
        } else if (ConfigurationConstants.editRule.equals(method)) {
            //Modify or Create a new Rule
            if (ConfigurationConstants.addNewRule.equals(action)) {
                //Add a new Rule
                ProxymaRuleBean newRule = null;
                try {
                    newRule = buildRuleFromRequest(httpservletrequest);
                } catch (Exception e) {
                    httpservletrequest.setAttribute(ConfigurationConstants.message, e.getMessage());
                    forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
                }

                if (newRule != null) {
                    try {
                        ruleSet.addNewRule(newRule);
                    } catch (InstantiationException e) {
                        httpservletrequest.setAttribute(ConfigurationConstants.message, e.getMessage());
                        httpservletrequest.setAttribute(ConfigurationConstants.ruleBean, newRule);
                        forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
                    }

                    httpservletrequest.setAttribute(ConfigurationConstants.message, "Rule \"" + newRule.getProxyFolder() + "\" successfully added.");
                    httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                    forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
                } else {
                    httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

            } else if (ConfigurationConstants.modifyRule.equals(action)) {
                //Update an existent Rule
                ProxymaRuleBean newRule = null;
                try {
                    newRule = buildRuleFromRequest(httpservletrequest);
                } catch (Exception e) {
                    httpservletrequest.setAttribute(ConfigurationConstants.message, e.getMessage());
                    httpservletrequest.setAttribute(ConfigurationConstants.ruleBean, ruleSet.getRule(target));
                    forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
                }

                if (newRule != null) {
                    try {
                        ProxymaRuleBean oldRule = ruleSet.getRule(target);
                        ruleSet.updateRule(oldRule, newRule);
                    } catch (InstantiationException e) {
                        httpservletrequest.setAttribute(ConfigurationConstants.message, e.getMessage());
                        httpservletrequest.setAttribute(ConfigurationConstants.ruleBean, newRule);
                        forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.editRulePage, proxymaInstanceContext);
                    }

                    httpservletrequest.setAttribute(ConfigurationConstants.message, "Rule \"" + newRule.getProxyFolder() + "\" successfully updated.");
                    httpservletrequest.setAttribute(ConfigurationConstants.ruleCollection, ruleSet.getRulesAsCollection());
                    forwardToJsp(httpservletrequest, httpservletresponse, ConfigurationConstants.listRulesPage, proxymaInstanceContext);
                } else {
                    httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        //Send an error if method is not recognized.
        } else {
            //Send an error
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    /**
     * This method try to create a new Rule from the request paqssed parameters
     *
     * @param request the servlet request passed from edtit_rule.jsp
     * @return the ruleBean if all parameters are valid.
     */
    private ProxymaRuleBean buildRuleFromRequest(HttpServletRequest request)
            throws IllegalArgumentException, NullPointerException {
        String proxyFolder = request.getParameter(ConfigurationConstants.proxyFolder);
        String proxyPassHost = request.getParameter(ConfigurationConstants.proxyPassHost);
        String proxyUser = request.getParameter(ConfigurationConstants.proxyUser);
        String proxyPassword = request.getParameter(ConfigurationConstants.proxyPassword);
        String newContext = request.getParameter(ConfigurationConstants.newContext);
        String maxPostSize = request.getParameter(ConfigurationConstants.maxPostSize);
        String javascriptHandlingMode = request.getParameter(ConfigurationConstants.javascriptHandlingMode);
        String isRewriteEngineEnabled = request.getParameter(ConfigurationConstants.isRewriteEngineEnabled);
        String isRuleEnabled = request.getParameter(ConfigurationConstants.isRuleEnabled);
        String isDebugModeEnabled = request.getParameter(ConfigurationConstants.isDebugModeEnabled);

        ProxymaRuleFactory factory = new ProxymaRuleFactory();
        ProxymaRuleBean retValue = factory.getNewRuleInstance(proxyFolder,
                proxyPassHost,
                proxyUser,
                proxyPassword,
                (isDebugModeEnabled!= null)?ProxymaConstants.TRUE:ProxymaConstants.FALSE,
                javascriptHandlingMode,
                newContext,
                maxPostSize,
                (isRewriteEngineEnabled!= null)?ProxymaConstants.TRUE:ProxymaConstants.FALSE,
                (isRuleEnabled!= null)?ProxymaConstants.TRUE:ProxymaConstants.FALSE);

        return retValue;
    }

    /**
     * Enables the passed rule
     *
     * @param target
     * @param ruleSet
     */
    private void enableRule(String target, ProxymaRuleSet ruleSet) {
        ProxymaRuleBean rule = ruleSet.getRule(target);
        if (rule != null)
            rule.setRuleEnabled(true);
    }

    /**
     * Disables the passed rule
     *
     * @param target
     * @param ruleSet
     */
    private void disableRule(String target, ProxymaRuleSet ruleSet) {
        ProxymaRuleBean rule = (ProxymaRuleBean) ruleSet.getRule(target);
        if (rule != null)
            rule.setRuleEnabled(false);
    }

    /**
     * Validate input from client
     *
     * @param method method request parameter
     * @param target target request parameter
     * @param action action request parameter
     * @return true if all required parameters were passed.
     */
    private boolean validateParameters(String method, String target, String action) {
        if ((method == null) || ProxymaConstants.EMPTY_STRING.equals(method))
            return false;

        if ((target == null) || ProxymaConstants.EMPTY_STRING.equals(target))
            return false;

        if ((action == null) || ProxymaConstants.EMPTY_STRING.equals(action))
            return false;

        return true;
    }

    /**
     * Initializes the Pattern for the matching of the contextPath of proxyma.
     * It is used to rebuild the original url of the requested resource.
     *
     * @param servletPath the current path of the HttpRewriterProxy servlet.
     * @return a regexp pattern.
     */
    private Pattern getServletPathMatchPattern(String servletPath) {
        if (matchPattern == null)
            matchPattern = Pattern.compile(new StringBuffer().append("^.*").append(servletPath).toString());
        return matchPattern;
    }

    /**
     * Utility method to forward the flow to a jsp..
     *
     * @param httpservletrequest        the request
     * @param httpservletresponse       the response
     * @param page           the page to forward
     * @param proxymaInstanceContext the proxymaContextPath
     */
    private void forwardToJsp(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse,
                              String page, String proxymaInstanceContext) throws IOException, ServletException {

        httpservletrequest.setAttribute(ConfigurationConstants.proxymaContext, proxymaInstanceContext);
        httpservletrequest.setAttribute(ConfigurationConstants.reverseProxyServletSubPath, httpReverseProxyServletSubPath);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(page);
        dispatcher.forward(httpservletrequest, httpservletresponse);
    }

    //Servlet Private attributes
    private Pattern matchPattern = null;
    private String httpReverseProxyServletSubPath = null;
}
