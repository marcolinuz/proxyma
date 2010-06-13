package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 * </p><p>
 * User: makko
 * Date: 30-Apr-2007
 * Time: 08.54.16
 * </p><p>
 * This class is a Proxyma rule factory, and it should be used to create (and eventually
 * check) the rules for the proxy folders.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 */
public class ProxymaRuleFactory {

    /**
     * Validate a passed rule.. this could be useful when loading rules from file, db, etc..
     *
     * @param rule the rule to validate..
     * @throws IllegalArgumentException This is throwed if something goes wrong.
     */
    public boolean isValidRule(ProxymaRuleBean rule) {
        //To validate a rule I try to create a new rule with the same arguments of the passed rule,
        //if no exception is thrown the rule should be OK.
        boolean retValue = true;

        try {
            getNewRuleInstance(rule.getProxyFolder(),
                    rule.getProxyPassHost(),
                    rule.getProxyUser(),
                    rule.getProxyPassword(),
                    rule.isDebugModeEnabled(),
                    rule.getJavascriptHandlingMode(),
                    rule.getNewContext(),
                    rule.getMaxPostSize(),
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
    public ProxymaRuleBean getNewRuleInstance(String proxyFolder, String proxyPassHost)
            throws IllegalArgumentException, NullPointerException {

        return getNewRuleInstance(proxyFolder,
                proxyPassHost,
                null,
                null,
                ProxymaConstants.DEFAULT_DEBUG_MODE_ENABLED,
                ProxymaConstants.DEFAULT_JAVASCRIPT_HANDLING_MODE,
                null,
                ProxymaConstants.DEFAULT_MAX_POST_SIZE,
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
     * @param debugMode          "true" if debug mode is on for this ProxyInstance (optional.)
     * @param javascriptHandling If you enabled the Url rewrite engine
     *                           with this parameter you can control javascript
     *                           handling. There are 2 possible values:
     *                           RewriterConstants.rewrite -> try to rewrite urls into javascript code
     *                           RewriterConstants.remove -> remove all the javascript code. (optional.)
     * @param forcedSourceURL    (optional) forced source url for use when this proxy instance is behind another reverse proxy.
     * @param maxPostDataSize    (optional) max size in bytes allowed for a POST operation through this proxy.  default size is 64K.
     * @param rewriteEngine      true if rewrite engine should be enabled (default it true)
     * @param ruleEnabled        true if the rule is enabled (so it works).
     */
    public ProxymaRuleBean getNewRuleInstance(String proxyFolder,
                                              String proxyPassHost,
                                              String proxyUser,
                                              String proxyPass,
                                              String debugMode,
                                              String javascriptHandling,
                                              String forcedSourceURL,
                                              String maxPostDataSize,
                                              String rewriteEngine,
                                              String ruleEnabled)
            throws IllegalArgumentException, NullPointerException {

        // check if the debug mode is enabled (default is False)
        boolean booleanDebugMode = false;
        if ((debugMode != null) && (ProxymaConstants.TRUE.equalsIgnoreCase(debugMode.trim())))
            booleanDebugMode = true;

        //Javascript mode Handling
        int intJavascriptHandlingMode = RewriterConstants.REWRITE;
        if ((javascriptHandling != null) && (!(ProxymaConstants.EMPTY_STRING.equals(javascriptHandling.trim())))) {
            if (ProxymaConstants.REWRITE.equalsIgnoreCase(javascriptHandling))
                intJavascriptHandlingMode = RewriterConstants.REWRITE;
            else if (ProxymaConstants.REMOVE.equalsIgnoreCase(javascriptHandling))
                intJavascriptHandlingMode = RewriterConstants.REMOVE;
        }

        //Handling maxpostsize parameter
        int intMaxPostSize = ProxymaConstants.DEFAULT_MAX_POST_SIZE;
        if ((maxPostDataSize != null) && (!(ProxymaConstants.EMPTY_STRING.equals(maxPostDataSize.trim()))))
            intMaxPostSize = Integer.parseInt(maxPostDataSize);


        // check if rewrite engine should be enabled (default is True)
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
        return getNewRuleInstance(proxyFolder, proxyPassHost, proxyUser, proxyPass, booleanDebugMode,
                intJavascriptHandlingMode, forcedSourceURL, intMaxPostSize, booleanRewtriteEngineEnabled, booleanRuleEnabled);
    }

    /**
     * Create a customized proxy rule instance, passing in all the options.
     * This constructor will throw an IllegalArgumentException or a NullPointerException.
     * if required parameters are not set or optional parameters were incorrectly setted.
     *
     * @param proxyFolder        folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost      remote host that we are masquerading (required.)
     * @param proxyUser          user, if using a local http proxy (optional.)
     * @param proxyPass          password, if using a local http proxy (optional.)
     * @param debugMode          "true" if debug mode is on for this ProxyInstance (optional.)
     * @param javascriptHandling If you enabled the Url rewrite engine
     *                           with this parameter you can control javascript
     *                           handling. There are 2 possible values:
     *                           RewriterConstants.rewrite -> try to rewrite urls into javascript code
     *                           RewriterConstants.remove -> remove all the javascript code. (optional.)
     * @param forcedSourceURL    (optional) forced source url for use when this proxy instance is behind another reverse proxy.
     * @param maxPostDataSize    (optional) max size in bytes allowed for a POST operation through this proxy.  default size is 64K.
     * @param rewriteEngine      true if rewrite engine should be enabled (default it true)
     * @param ruleEnabled        true if the rule is enabled (so it works).
     */
    public ProxymaRuleBean getNewRuleInstance(String proxyFolder,
                                              String proxyPassHost,
                                              String proxyUser,
                                              String proxyPass,
                                              boolean debugMode,
                                              int javascriptHandling,
                                              String forcedSourceURL,
                                              int maxPostDataSize,
                                              boolean rewriteEngine,
                                              boolean ruleEnabled)
            throws IllegalArgumentException, NullPointerException {

        ProxymaRuleBean newRule = new ProxymaRuleBean();


        //Check and set folder Mapping for this rule
        if (proxyFolder.indexOf(ProxymaConstants.PATH_SEPARATOR) > -1) {
            System.out.println("You can't specify a complex path (with inner slashes \"/\" into proxyFolder attribute)");
            throw new IllegalArgumentException("You can't specify a complex path (with inner slashes \"/\" into proxyFolder attribute)");
        } else {
            newRule.setProxyFolder(proxyFolder);
        }

        //Set remote host and folder to masquerade.
        newRule.setProxyPassHost(proxyPassHost);

        //try to understand if the outgoing proxy needs authentication
        if ((proxyUser != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyUser.trim()))) &&
                (proxyPass != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyPass.trim())))) {
            newRule.setProxyAuthentication(new String(Base64.encodeBase64((proxyUser + ":" + proxyPass).getBytes())));
        }

        //set the debug mode for this rule
        newRule.setDebugModeEnabled(debugMode);

        //Set the Javascript Handling mode
        newRule.setJavascriptHandlingMode(javascriptHandling);

        // try to understand if proxyma is deployed behind another reverse proxy (for example Apache with mod-proxy enabled)
        if ((forcedSourceURL != null) && (!(ProxymaConstants.EMPTY_STRING.equals(forcedSourceURL.trim())))) {
            if (forcedSourceURL.startsWith(ProxymaConstants.PATH_SEPARATOR)) {
                //This could be an absolute path and so must begin and end with a "/"
                if (forcedSourceURL.endsWith(ProxymaConstants.PATH_SEPARATOR)) {
                    newRule.setNewContext(forcedSourceURL);
                } else {
                    System.out.println("New Context needs an absolute path with starting and ending \"/\" (slash).");
                    throw new IllegalArgumentException("New Context needs an absolute path with starting and ending \"/\" (slash).");
                }
            } else {
                //This could be an URL (and in that case it must be validated
                try {
                    // if the url is correct force the passed value as the "new url" for the rewrite engine
                    URL parseUrl = new URL(forcedSourceURL);
                    newRule.setNewContext(parseUrl.getPath());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    System.out.println("New Context needs a VALID URL (or an Absolute Path).");
                    throw new IllegalArgumentException("New Context needs a VALID URL (or an Absolute Path).");
                }
            }
        }

        //Handling maxpostsize parameter
        newRule.setMaxPostSize(maxPostDataSize);

        // Set RewriterEngine for this rule
        newRule.setRewriteEngineEnabled(rewriteEngine);

        // Set if rule is enabled
        newRule.setRuleEnabled(ruleEnabled);

        //Return the new Rule to the caller.
        return newRule;
    }
}
