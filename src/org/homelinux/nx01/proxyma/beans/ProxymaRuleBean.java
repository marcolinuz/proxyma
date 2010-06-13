package org.homelinux.nx01.proxyma.beans;

import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.RewriterPatterns;
import org.homelinux.nx01.proxyma.core.RewriterConstants;
import org.homelinux.nx01.proxyma.core.ProxymaLog;
import org.apache.commons.codec.binary.Base64;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>
 * User: makko
 * Date: 26-Apr-2007
 * Time: 21.33.15
 * </p><p>
 * This class is the configuration bean for an instance of DynamicHttpProxy.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxymaRuleBean {

    /**
     * Obtain the username from the "proxyAuthentication" attribute
     *
     * @return the username
     */
    public String getProxyUser() {
        String retValue = null;
        if (isProxyAuthenticationEnabled()) {
            String authentication = new String(Base64.decodeBase64(proxyAuthentication.getBytes()));
            String[] splittedAuthentication = authentication.split(":");
            retValue = splittedAuthentication[0];
        }

        return retValue;
    }

    /**
     * Obtain the password from the "proxyAuthentication" attribute
     *
     * @return the password
     */
    public String getProxyPassword() {
        String retValue = null;
        if (isProxyAuthenticationEnabled()) {
            String authentication = new String(Base64.decodeBase64(proxyAuthentication.getBytes()));
            String[] splittedAuthentication = authentication.split(":");
            retValue = splittedAuthentication[1];
        }

        return retValue;
    }

    /**
     * Obtain the proxyAuthentication attribute for this bean
     *
     * @return proxyAuthentication base64 encoded string
     */
    public String getProxyAuthentication() {
        return proxyAuthentication;
    }

    /**
     * Set the proxyAuthentication attribute for this bean
     *
     * @param proxyAuthentication the base64encoded string
     */
    public void setProxyAuthentication(String proxyAuthentication) {
        if ((proxyAuthentication != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyAuthentication.trim()))))
            this.proxyAuthentication = proxyAuthentication;
        else
            this.proxyAuthentication = null;
    }

    /**
     * Checks if the proxy authentication is enabled
     *
     * @return true of false.
     */
    public boolean isProxyAuthenticationEnabled() {
        return (proxyAuthentication != null);
    }

    /**
     * Checks if the rewrite engine is enabled
     *
     * @return true or false
     */
    public boolean isRewriteEngineEnabled() {
        return rewriteEngineEnabled;
    }

    /**
     * Sets the status of the rewrite engine
     *
     * @param rewriteEngineEnabled this must be true or false
     */
    public void setRewriteEngineEnabled(boolean rewriteEngineEnabled) {
        this.rewriteEngineEnabled = rewriteEngineEnabled;
    }

    /**
     * Get the proxyFolde mapping for the rule
     *
     * @return the proxyFolder value
     */
    public String getProxyFolder() {
        return proxyFolder;
    }

    /**
     * Set the proxyFolder mapping for the rule
     *
     * @param proxyFolder this must be a string without any "/" character inside.
     */
    public void setProxyFolder(String proxyFolder) {
        //No start or end slash "/" characters are allowed here.
        this.proxyFolder = proxyFolder.replaceAll(ProxymaConstants.PATH_SEPARATOR, ProxymaConstants.EMPTY_STRING);
    }

    /**
     * Obtain the resource to masquerade by the rule
     *
     * @return a full qualified URL
     */
    public String getProxyPassHost() {
        return proxyPassHost;
    }

    /**
     * This method sets the resource that have to be masqueraded by the rule and
     * initialize an instance of RewriterPatterns in according the the passed value.
     *
     * @param proxyPassHost a full qualified URL to masquerade
     * @throws NullPointerException     if no value is passed
     * @throws IllegalArgumentException if the passed value is not a valid URL.
     */
    public void setProxyPassHost(String proxyPassHost) throws NullPointerException, IllegalArgumentException {
        //check that its not null!
        if (proxyPassHost == null) {

            ProxymaLog.instance.errors("ERROR: proxyPassHost needs a VALID URL.");
            throw new NullPointerException("proxyPassHost needs a VALID URL.");
        }

        //check if resource ends with a trailing "/".. if not it will be added.
        if (!(proxyPassHost.trim().endsWith("/"))) {
            this.proxyPassHost = proxyPassHost.trim() + "/";
        } else {
            this.proxyPassHost = proxyPassHost.trim();
        }

        //check if it's a valid URL.
        try {
            URL parseUrl = new URL(this.proxyPassHost);
            parserPatterns = new RewriterPatterns();
            parserPatterns.setProxedHost(this.proxyPassHost);
            parserPatterns.setProxedBasePath(parseUrl.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ProxymaLog.instance.errors("ERROR: proxyPassHost needs a VALID URL.");
            throw new IllegalArgumentException("proxyPassHost needs a VALID URL.");
        }
    }

    /**
     * Get the current logging policy for this rule
     *
     * @return the current logging policy
     */
    public int getLoggingPolicy() {
        return loggingPolicy;
    }

    /**
     * Set the current logging policy for this rule and the flags for a faster flow selection.
     *
     * @param loggingPolicy
     * @see ProxymaConstants to know valid values for this parameter any invalid value will be overridden with the default LoggingPolicy.
     */
    public void setLoggingPolicy(int loggingPolicy) {
        //check if the passed value is valid if not the default value is forced
        if ((loggingPolicy >= 0) && (loggingPolicy < ProxymaConstants.LOGGING_POLICY_STRING.length))
            this.loggingPolicy = loggingPolicy;
        else
            this.loggingPolicy = ProxymaConstants.DEFAULT_LOGGING_POLICY;

        //Set the values of the flags to use for a faster flow selection in running mode.
        if (this.loggingPolicy > ProxymaConstants.LOGGING_POLICY_PRODUCTION) {
            this.debugLoggingEnabled = true;
            this.productionLoggingEnabled = true;
        } else {
            if (this.loggingPolicy == ProxymaConstants.LOGGING_POLICY_NONE) {
                this.debugLoggingEnabled = false;
                this.productionLoggingEnabled = false;
            } else {
                this.debugLoggingEnabled = false;
                this.productionLoggingEnabled = true;
            }
        }
        if (this.loggingPolicy == ProxymaConstants.LOGGING_POLICY_ONLINE_AUDIT)
            this.onlineDebugDisabled = false;
        else
            this.onlineDebugDisabled = true;

    }

    /**
     * Returns true if the proxyma_access.log have to be generated.
     *
     * @return  true if the proxyma_access.log have to be generaqted.
     */
    public boolean isProductionLoggingEnabled() {
        return productionLoggingEnabled;
    }

    /**
     * Returns true if the logging policy needs to generate debug informations.
     *
     * @return true if the logging policy needs to generate debug informations.
     */
    public boolean isDebugLoggingEnabled() {
        return debugLoggingEnabled;
    }


    /**
     * Returns false if the logging policy is setted as Online.
     *
     * @return false if the logging policy is setted as Online.
     */
    public boolean isOnlineDebugDisabled() {
        return onlineDebugDisabled;
    }

    /**
     * This method is used by the RewriterEngine Engine, you should not call it..
     *
     * @return the RewriterPatterns for the rule
     */
    public RewriterPatterns getParserPatterns() {
        return parserPatterns;
    }

    /**
     * Obtain the current value for the maxPostSize Attribute.
     * This is useful to avoid some kind of DOS attacks..
     *
     * @return the current value of the maxPostSize
     */
    public int getMaxPostSize() {
        return maxPostSize;
    }

    /**
     * Sets the current value for the maxPostSize Attribute.
     * This is useful to avoid some kind of DOS attacks.. you could set it
     * to 0 (zero) if you want allow unlimited size for the POST method.
     *
     * @param maxPostSize the new value for this attribute.
     */
    public void setMaxPostSize(int maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    /**
     * Returns an integer value that rappesents the current handling mode for
     * inline and linked javascripts.
     *
     * @return the current value for this attribute
     * @see RewriterConstants for more info
     */
    public int getJavascriptHandlingMode() {
        return javascriptHandlingMode;
    }

    /**
     * Sets the integer value that appresents the new handling mode for
     * inline and linked javascripts.
     *
     * @param javascriptHandlingMode the new value for this attribute.
     * @see RewriterConstants for more info
     */
    public void setJavascriptHandlingMode(int javascriptHandlingMode) {
        //if not valid parameter was passed I will force default value
        switch (javascriptHandlingMode) {
            case RewriterConstants.REWRITE:
            case RewriterConstants.REMOVE:
                this.javascriptHandlingMode = javascriptHandlingMode;
                break;

            default:
                this.javascriptHandlingMode = ProxymaConstants.DEFAULT_JAVASCRIPT_HANDLING_MODE;
                break;
        }

    }

    /**
     * obtain the status of the rule. If this is set to false the rule will not work
     * and a FORBIDDEN return code is sent to the client browser.
     *
     * @return true or false
     */
    public boolean isRuleEnabled() {
        return ruleEnabled;
    }

    /**
     * This flag sets the new status of the rule, if is set to false the rule will not work
     * and a FORBIDDEN return code is sent to the client browser.
     *
     * @param ruleEnabled true or false
     */
    public void setRuleEnabled(boolean ruleEnabled) {
        this.ruleEnabled = ruleEnabled;
    }

    /**
     * Ask to the internal ParserPatterns of the rule to tell you
     * the current value for the context that will be rewritten into the URLS by the
     * RewriterEngine.
     *
     * @return the new context into rewritten URLS
     */
    public String getNewContext() {
        String retValue = null;
        if (parserPatterns != null)
            retValue = parserPatterns.getNewContext();
        return retValue;
    }

    /**
     * This method is used to set the context that  will be rewritten into the URLS by the
     * RewriterEngine. It MUST be called after the setProxedHost() because it try to set
     * an internal attribute for the ParserPatterns that is initialized by the setProxedHost()
     * method.
     * NOTE: due to the implementation of the ParserPatterns, this call will work
     * only the first time that you call it. (for performance reasons).
     *
     * @param newContext the new context to set
     * @throws NullPointerException is raised if the internal ParserPattern is not already initialized.
     */
    public void setNewContext(String newContext) throws NullPointerException {
        if (parserPatterns == null) {
            throw new NullPointerException("ProxedHost is Null! (use setProxedHost before setNewContext)");
        } else {
            parserPatterns.setNewContext(newContext);
        }
    }

    /**
     * This method returns a String description of the Rule.
     *
     * @return a string that describes the rule.
     */
    public String toString() {
        StringBuffer retValue = new StringBuffer();
        retValue.append("Rule Name (Proxy Folder) = " + getProxyFolder());
        retValue.append("\n\tMasqueraded Host = " + getProxyPassHost());
        retValue.append("\n\tMax POST Size = " + getMaxPostSize());
        retValue.append("\n\tProxy Authentication = " + getProxyAuthentication());
        retValue.append("\n\tRewrite engine = " + (isRewriteEngineEnabled() ? "Enabled" : "Disabled"));
        retValue.append("\n\tAudit Mode = " + ProxymaConstants.LOGGING_POLICY_STRING[getLoggingPolicy()]);
        retValue.append("\n\tJavascript Handling Mode = " + ((getJavascriptHandlingMode() == RewriterConstants.REWRITE) ? "Rewrite Javascript Urls" : "Remove Javascript Code"));
        retValue.append("\n\tThis rule is " + (isRuleEnabled() ? "ENABLED" : "DISABLED"));
        retValue.append("\n\n");

        return retValue.toString();
    }

    //Proxy authentication if required is stored here
    private String proxyAuthentication = null;

    //Reweriter Class and rewriter flag for this destination
    private boolean rewriteEngineEnabled = ProxymaConstants.DEFAULT_REWRITER_ENGINE_ENABLED;

    //Handled BasePath for this destination
    private String proxyFolder = null;

    //Destination for this destination
    private String proxyPassHost = null;

    //Logging Policy and debug mode flag appliyed to this rule
    private int loggingPolicy = ProxymaConstants.DEFAULT_LOGGING_POLICY;
    private boolean debugLoggingEnabled = false;
    private boolean productionLoggingEnabled = false;
    private boolean onlineDebugDisabled = true;

    //Javascript handling mode for this destination
    private int javascriptHandlingMode = ProxymaConstants.DEFAULT_JAVASCRIPT_HANDLING_MODE;

    //Substitution patterns for the rewriter engine
    private RewriterPatterns parserPatterns = null;

    //Max post size for this destination
    private int maxPostSize = ProxymaConstants.DEFAULT_MAX_POST_SIZE;

    //Set if the rule is enabled
    private boolean ruleEnabled = ProxymaConstants.DEFAULT_RULE_ENABLED;

}
