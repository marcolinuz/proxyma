package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.RuleBean;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

/**
 * <p/>
 * User: makko
 * Date: 30-Apr-2007
 * Time: 08.54.16
 * </p><p>
 * This class is a Proxyma rule factory, and it should be used to create (and eventually
 * check) the rules for the proxy folders.
 * Please consider to use the ProxymaFacade to do the same work.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class RuleFactory {

    /**
     * Validate a passed rule.. this could be useful when loading rules from file, db, etc..
     *
     * @param rule the rule to validate..
     * @return true if the rule is valid, otherwise false.
     * @throws IllegalArgumentException This is throwed if something goes wrong.
     */
    public boolean isValidRule(RuleBean rule) {
        //To validate a rule I try to create a new rule with the same arguments of the passed rule,
        //if no exception is thrown the rule should be OK.
        boolean retValue = true;

        try {
            getNewRuleInstance(rule.getProxyFolder(),
                    rule.getProxyPassHost(),
                    rule.getProxyUser(),
                    rule.getProxyPassword(),
                    rule.getLoggingPolicy(),
                    rule.getJavascriptHandlingMode(),
                    rule.getNewContext(),
                    rule.getMaxPostSize(),
                    rule.getMaxCachedResourceSize(),
                    rule.isCacheEnabled(),
                    rule.isRewriteEngineEnabled(),
                    rule.isRuleEnabled());
        } catch (Exception e) {
            retValue = false;
            e.printStackTrace();
        }

        return retValue;
    }

    /**
     * Create a new RuleInstance with only the required options.  For all other options, acceptable
     * defaults are selected (see ProxymaConstants to know them).
     *
     * @param proxyFolder   folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost remote host that we are masquerading (required.)
     * @see ProxymaConstants
     */
    public RuleBean getNewRuleInstance(String proxyFolder, String proxyPassHost)
            throws IllegalArgumentException, NullPointerException {

        return getNewRuleInstance(proxyFolder,
                proxyPassHost,
                null,
                null,
                ProxymaConstants.DEFAULT_LOGGING_POLICY,
                ProxymaConstants.DEFAULT_JAVASCRIPT_HANDLING_MODE,
                null,
                ProxymaConstants.DEFAULT_MAX_POST_SIZE,
                ProxymaConstants.DEFAULT_MAX_CACHED_RESOURCE_SIZE,
                ProxymaConstants.DEFAULT_CACHE_STATUS,
                ProxymaConstants.DEFAULT_REWRITER_ENGINE_ENABLED,
                ProxymaConstants.DEFAULT_RULE_ENABLED);
    }

    /**
     * Create a customized proxy rule instance, passing in all the various options as Strings.
     * This constructor will throw an IllegalArgumentException or a NullPointerException
     * if required parameters are not set or optional parameters are set incorrectly.
     *
     * @param proxyFolder        folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost      remote host that we are masquerading (required.)
     * @param proxyUser          user, if using a local http proxy (optional.)
     * @param proxyPass          password, if using a local http proxy (optional.)
     * @param loggingPolicy      Logging policy for this rule the default value is for Production (optional)
     * @param javascriptHandling If you enabled the Url rewrite engine
     *                           with this parameter you can control javascript
     *                           handling. There are 2 possible values:
     *                           RewriterConstants.rewrite -> try to rewrite urls into javascript code
     *                           RewriterConstants.remove -> remove all the javascript code. (optional.)
     * @param forcedSourceURL    (optional) forced source url for use when this proxy instance is behind another reverse proxy.
     * @param maxPostDataSize    (optional) max size in bytes allowed for a POST operation through this proxy.  default size is 64K.
     * @param maxCachedResourceSize (optional) max size of a cached resource. Greater objects will never been cached.
     * @param cacheEnabled          true if the cache subsystem is enabled for this rule.
     * @param rewriteEngine      true if rewrite engine should be enabled (default it true)
     * @param ruleEnabled        true if the rule is enabled (so it works).
     */
    public RuleBean getNewRuleInstance(String proxyFolder,
                                              String proxyPassHost,
                                              String proxyUser,
                                              String proxyPass,
                                              String loggingPolicy,
                                              String javascriptHandling,
                                              String forcedSourceURL,
                                              String maxPostDataSize,
                                              String maxCachedResourceSize,
                                              String cacheEnabled,
                                              String rewriteEngine,
                                              String ruleEnabled)
            throws IllegalArgumentException, NullPointerException {

        // check if the logging policy was passed (default is for production)
        int intLoggingPolicy = ProxymaConstants.DEFAULT_LOGGING_POLICY;
        if ((loggingPolicy != null) && (!(ProxymaConstants.EMPTY_STRING.equals(loggingPolicy.trim()))))
            intLoggingPolicy = Integer.parseInt(loggingPolicy);

        //Javascript mode Handling
        int intJavascriptHandlingMode = RewriterConstants.PROCESS;
        if ((javascriptHandling != null) && (!(ProxymaConstants.EMPTY_STRING.equals(javascriptHandling.trim())))) {
            if (ProxymaConstants.PROCESS.equalsIgnoreCase(javascriptHandling))
                intJavascriptHandlingMode = RewriterConstants.PROCESS;
            else if (ProxymaConstants.REMOVE.equalsIgnoreCase(javascriptHandling))
                intJavascriptHandlingMode = RewriterConstants.REMOVE;
        }

        //Handling maxpostsize parameter
        int intMaxPostSize = ProxymaConstants.DEFAULT_MAX_POST_SIZE;
        if ((maxPostDataSize != null) && (!(ProxymaConstants.EMPTY_STRING.equals(maxPostDataSize.trim()))))
            intMaxPostSize = Integer.parseInt(maxPostDataSize);

        //Handling maxcachedresourcesize parameter
        int intMaxCachedResourceSize = ProxymaConstants.DEFAULT_MAX_CACHED_RESOURCE_SIZE;
        if ((maxCachedResourceSize != null) && (!(ProxymaConstants.EMPTY_STRING.equals(maxCachedResourceSize.trim()))))
            intMaxCachedResourceSize = Integer.parseInt(maxCachedResourceSize);

        // check if cache subsystem be enabled
        boolean booleanCacheEnabled;
        if (rewriteEngine == null)
            booleanCacheEnabled = ProxymaConstants.DEFAULT_CACHE_STATUS;
        else if (ProxymaConstants.TRUE.equalsIgnoreCase(cacheEnabled.trim()))
            booleanCacheEnabled = true;
        else
            booleanCacheEnabled = false;

        // check if rewrite engine should be enabled
        boolean booleanRewtriteEngineEnabled;
        if (rewriteEngine == null)
            booleanRewtriteEngineEnabled = ProxymaConstants.DEFAULT_REWRITER_ENGINE_ENABLED;
        else if (ProxymaConstants.TRUE.equalsIgnoreCase(rewriteEngine.trim()))
            booleanRewtriteEngineEnabled = true;
        else
            booleanRewtriteEngineEnabled = false;

        // Check if the rule must be enabled by default
        boolean booleanRuleEnabled;
        if (ruleEnabled == null)
            booleanRuleEnabled = ProxymaConstants.DEFAULT_RULE_ENABLED;
        else if (ProxymaConstants.TRUE.equalsIgnoreCase(ruleEnabled.trim()))
            booleanRuleEnabled = true;
        else
            booleanRuleEnabled = false;

        // Call specific method to obtain the new rule instance.
        return getNewRuleInstance(proxyFolder, proxyPassHost, proxyUser, proxyPass, intLoggingPolicy,
                intJavascriptHandlingMode, forcedSourceURL, intMaxPostSize, intMaxCachedResourceSize,
                booleanCacheEnabled, booleanRewtriteEngineEnabled, booleanRuleEnabled);
    }

    /**
     * Create a customized proxy rule instance, passing in all the options.
     * This constructor will throw an IllegalArgumentException or a NullPointerException.
     * if required parameters are not set or optional parameters were incorrectly setted.
     *
     * @param proxyFolder           folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost         remote host that we are masquerading (required.)
     * @param proxyUser             user, if using a local http proxy (optional.)
     * @param proxyPass             password, if using a local http proxy (optional.)
     * @param loggingPolicy         Logging policy for this rule the default value is for Production (optional)
     * @param javascriptHandling    If you enabled the Url rewrite engine
     *                              with this parameter you can control javascript
     *                              handling. There are 2 possible values:
     *                              RewriterConstants.rewrite -> try to rewrite urls into javascript code
     *                              RewriterConstants.remove -> remove all the javascript code. (optional.)
     * @param forcedSourceURL       (optional) forced source url for use when this proxy instance is behind another reverse proxy.
     * @param maxPostDataSize       (optional) max size in bytes allowed for a POST operation through this proxy.  default size is 64K.
     * @param maxCachedResourceSize (optional) max size of a cached resource. Greater objects will never been cached.
     * @param cacheEnabled          true if the cache subsystem is enabled for this rule.
     * @param rewriteEngine         true if rewrite engine should be enabled (default it true)
     * @param ruleEnabled           true if the rule is enabled (so it works).
     */
    public RuleBean getNewRuleInstance(String proxyFolder,
                                              String proxyPassHost,
                                              String proxyUser,
                                              String proxyPass,
                                              int loggingPolicy,
                                              int javascriptHandling,
                                              String forcedSourceURL,
                                              int maxPostDataSize,
                                              int maxCachedResourceSize,
                                              boolean cacheEnabled,
                                              boolean rewriteEngine,
                                              boolean ruleEnabled)
            throws IllegalArgumentException, NullPointerException {

        RuleBean newRule = new RuleBean();


        // check and set folder Mapping for this rule
        if (proxyFolder.indexOf(ProxymaConstants.PATH_SEPARATOR) > -1) {
            ProxymaLog.instance.errors("WARNING: You can't specify a complex path (with inner slashes \"/\" into proxyFolder attribute)");
            throw new IllegalArgumentException("You can't specify a complex path (with inner slashes \"/\" into proxyFolder attribute)");
        } else {
            newRule.setProxyFolder(proxyFolder);
        }

        // set remote host and folder to masquerade.
        newRule.setProxyPassHost(proxyPassHost);

        // try to understand if the outgoing proxy needs authentication
        if ((proxyUser != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyUser.trim()))) &&
                (proxyPass != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyPass.trim())))) {
            newRule.setProxyAuthentication(new String(Base64.encodeBase64((proxyUser + ":" + proxyPass).getBytes())));
        }

        // set the logging policy for this rule
        newRule.setLoggingPolicy(loggingPolicy);

        // set the Javascript Handling mode
        newRule.setJavascriptHandlingMode(javascriptHandling);

        // try to understand if proxyma is deployed behind another reverse proxy (for example Apache with mod-proxy enabled)
        if ((forcedSourceURL != null) && (!(ProxymaConstants.EMPTY_STRING.equals(forcedSourceURL.trim())))) {
            if (forcedSourceURL.startsWith(ProxymaConstants.PATH_SEPARATOR)) {
                //This could be an absolute path and so must begin and end with a "/"
                if (forcedSourceURL.endsWith(ProxymaConstants.PATH_SEPARATOR)) {
                    newRule.setNewContext(forcedSourceURL);
                } else {
                    ProxymaLog.instance.errors("WARNING: New Context needs an absolute path with starting and ending \"/\" (slash).");
                    throw new IllegalArgumentException("New Context needs an absolute path with starting and ending \"/\" (slash).");
                }
            } else {
                //This could be an URL (and in that case it must be validated
                try {
                    // if the url is correct force the passed value as the "new url" for the rewrite engine
                    URL parseUrl = new URL(forcedSourceURL);
                    newRule.setNewContext(parseUrl.getPath());
                } catch (MalformedURLException e) {
                    ProxymaLog.instance.errors("WARNING: New Context needs a VALID URL (or an Absolute Path).");
                    throw new IllegalArgumentException("New Context needs a VALID URL (or an Absolute Path).");
                }
            }
        }

        //Handling maxpostsize parameter
        if (maxPostDataSize > 0)
            newRule.setMaxPostSize(maxPostDataSize);
        else
            newRule.setMaxPostSize(ProxymaConstants.DEFAULT_MAX_POST_SIZE);

        //Handling maxpostsize parameter
        newRule.setMaxPostSize(maxPostDataSize);

        //handling maxCachedResourceSize parameter
        newRule.setMaxCachedResourceSize(maxCachedResourceSize);

        // Set RewriterEngine for this rule
        newRule.setRewriteEngineEnabled(rewriteEngine);

        //set the cache status for this rule
        newRule.setCacheEnabled(cacheEnabled);

        // Set if rule is enabled
        newRule.setRuleEnabled(ruleEnabled);

        //Return the new Rule to the caller.
        return newRule;
    }
}
