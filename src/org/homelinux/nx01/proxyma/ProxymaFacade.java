package org.homelinux.nx01.proxyma;

import org.homelinux.nx01.proxyma.core.*;
import org.homelinux.nx01.proxyma.beans.RuleBean;

/**
 * <p/>
 * User: makko
 * Date: 21-lug-2007
 * Time: 14.22.16
 * </p><p>
 * This class is the facade of Proxyma project for the developers.
 * It should be used to initialize the library and to obtain the classes that performs
 * all the work.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxymaFacade {

    /**
     * Initialize the loggin subsystem of the whole library.
     * This method will have effect only the first time it was used.. its does nothing if called more times..
     * This will use as configuration file for the logging subsystem the file "./proxyma_log4j.properties", so it have to be present into the current directory.
     * Note that if the configuration file is the default file of the proxyma distribution the log file directory
     * will be setted as "./" (the currend directory).
     */
    public static final synchronized void initialize() {
        if (libraryUninitialized) {
            //Initialize the logging subsystem of the proxyma library
            ProxymaLog.initializeLoggingSubsystem(null);

            //Set the new vaule for the initialization flag.
            libraryUninitialized = false;
        }
    }

    /**
     * Initialize the loggin subsystem of the whole library.
     * This method will have effect only the first time it was used.. its does nothing if called more times..
     * This will use the passed file as configuration file for the logging Subsystem.
     * Note that if the configuration file is the default file of the proxyma distribution the log file directory
     * will be setted as "./" (the currend directory).
     *
     * @param loggingConfigurationFile the file to use as logging subsystem configuration
     */
    public static final synchronized void initialize(String loggingConfigurationFile) {
        if (libraryUninitialized) {
            //Initialize the logging subsystem of the proxyma library
            ProxymaLog.initializeLoggingSubsystem(loggingConfigurationFile);

            //Set the new vaule for the initialization flag.
            libraryUninitialized = false;
        }
    }

    /**
     * Initialize the loggin subsystem of the whole library.
     * This method will have effect only the first time it was used.. its does nothing if called more times..
     *
     * @param loggingBasePath          the directory to use to write the logs
     * @param loggingConfigurationFile the file to use as logging subsystem configuration
     */
    public static final synchronized void initialize(String loggingBasePath, String loggingConfigurationFile) {
        if (libraryUninitialized) {
            //Set the the path of the logging files of the Proxyma library
            ProxymaLog.setLoggingDirectoryPath(loggingBasePath);
            //Initialize the logging subsystem of the proxyma library
            ProxymaLog.initializeLoggingSubsystem(loggingConfigurationFile);

            //Set the new vaule for the initialization flag.
            libraryUninitialized = false;
        }
    }

    /**
     * Creates a new ReverseProxy object with all default attributes.
     *
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewDefaultReverseProxy() {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewDefaultProxyConfiguration());
        }
    }

    /**
     * Creates a new ReverseProxy object with custom proxyContext and default configuration parameters for other attributes.
     *
     * @param proxyContext the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewReverseProxy(String proxyContext) {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewCustomProxyConfiguration(proxyContext));
        }
    }

    /**
     * Creates a new ReverseProxy object with custom value for the flag to show the rules to the clients and default configuration parameters for other attributes.
     *
     * @param showMasqueradedResourcesEnabled
     *         if is true the proxy will show to the client the list of therules if the proxy is called without a proxyfolder.
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewReverseProxy(boolean showMasqueradedResourcesEnabled) {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewCustomProxyConfiguration(showMasqueradedResourcesEnabled));
        }
    }

    /**
     * Creates a new ReverseProxy object with the specified custom attributes.. default values will be used for the cache.
     *
     * @param proxyContext the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param showMasqueradedResourcesEnabled
     *                     if is true the proxy will show to the client the list of therules if the proxy is called without a proxyfolder.
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewReverseProxy(String proxyContext, boolean showMasqueradedResourcesEnabled) {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewCustomProxyConfiguration(proxyContext, showMasqueradedResourcesEnabled));
        }
    }

    /**
     * Creates a new ReverseProxy object with default proxyContext and custom configurations for the cache and for the flag to show the rules to the clients..
     *
     * @param cacheSubsystemConfigurationFile
     *                                       the full path of the configuration file for the cache subsystem
     * @param showMasqueradedResourcesStatus if is ENABLED the proxy will show to the client the list of the rules (if the proxy is called without a proxyfolder).
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewReverseProxy(String cacheSubsystemConfigurationFile, String showMasqueradedResourcesStatus) {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewCustomProxyConfiguration(null, cacheSubsystemConfigurationFile, showMasqueradedResourcesStatus));
        }
    }

    /**
     * Creates a new totally customized ReverseProxy object.
     *
     * @param proxyContext                   the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param cacheSubsystemConfigurationFile
     *                                       the full path of the configuration file for the cache subsystem
     * @param showMasqueradedResourcesStatus if is ENABLED the proxy will show to the client the list of therules if the proxy is called without a proxyfolder.
     * @return a new instance of a reverse proxy.
     */
    public static final ReverseProxy getNewReverseProxy(String proxyContext, String cacheSubsystemConfigurationFile, String showMasqueradedResourcesStatus) {
        if (libraryUninitialized) {
            throw new RuntimeException(message);
        } else {
            return new ReverseProxy(ProxyConfigurationFactory.getNewCustomProxyConfiguration(proxyContext, cacheSubsystemConfigurationFile, showMasqueradedResourcesStatus));
        }
    }

    /**
     * Obtain a set of configuration rules for the passed applicationContext
     *
     * @param proxymaInstanceContext a unique string that identifies the instance of proxyma (I alwais use the application context path)
     * @return a ConcurrentHashMap that countains the ruleset for the wanted instance.
     */
    public static final RuleSet getRuleSet(String proxymaInstanceContext) {
        RuleSetsPool ruleSetsPool = RuleSetsPool.getInstance();
        return ruleSetsPool.getRuleSet(proxymaInstanceContext);
    }

    /**
     * Create a new Rule Instance with only the required options.  For all other options, acceptable
     * defaults are selected (see RuleFactory for more info).
     *
     * @param proxyFolder   folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost remote host that we are masquerading (required.)
     * @see RuleFactory
     */
    public static final RuleBean getNewDefaultRule(String proxyFolder, String proxyPassHost)
            throws IllegalArgumentException, NullPointerException {
        RuleFactory ruleFactory = new RuleFactory();
        return ruleFactory.getNewRuleInstance(proxyFolder, proxyPassHost);
    }

    /**
     * Create a customized proxy rule instance, passing in all the various options as Strings.
     * This method uses the RuleFactory class to do its work and will throw an IllegalArgumentException or a NullPointerException
     * if required parameters are not set or optional parameters are set incorrectly.
     *
     * @param proxyFolder            folder mapping that will proxy to the remote proxy pass host. (required)
     * @param proxyPassHost          remote host that we are masquerading (required.)
     * @param proxyUser              user, if using a local http proxy (optional.)
     * @param proxyPassword          password, if using a local http proxy (optional.)
     * @param loggingPolicy          Logging policy for this rule the default value is for Production (optional)
     * @param javascriptHandlingMode If you enabled the Url rewrite engine
     *                               with this parameter you can control javascript
     *                               handling. There are 2 possible values:
     *                               RewriterConstants.rewrite -> try to rewrite urls into javascript code
     *                               RewriterConstants.remove -> remove all the javascript code. (optional.)
     * @param forcedSourceURL        (optional) forced source url for use when this proxy instance is behind another reverse proxy.
     * @param maxPostDataSize        (optional) max size in bytes allowed for a POST operation through this proxy.  default size is 64K.
     * @param isRewriteEngineEnabled true if rewrite engine should be enabled (default it true)
     * @param isRuleEnabled          true if the rule is enabled (so it works).
     * @see RuleFactory
     */
    public static final RuleBean getNewCustomRule(String proxyFolder,
                                                         String proxyPassHost,
                                                         String proxyUser,
                                                         String proxyPassword,
                                                         String loggingPolicy,
                                                         String javascriptHandlingMode,
                                                         String forcedSourceURL,
                                                         String maxPostDataSize,
                                                         String maxCacheResourceSize,
                                                         String isCacheEnabled,
                                                         String isRewriteEngineEnabled,
                                                         String isRuleEnabled)
            throws IllegalArgumentException, NullPointerException {
        RuleFactory ruleFactory = new RuleFactory();
        return ruleFactory.getNewRuleInstance(proxyFolder,
                proxyPassHost,
                proxyUser,
                proxyPassword,
                loggingPolicy,
                javascriptHandlingMode,
                forcedSourceURL,
                maxPostDataSize,
                maxCacheResourceSize,
                isCacheEnabled,
                isRewriteEngineEnabled,
                isRuleEnabled);
    }

    /**
     * Finalize the libary and releases the resources of the caching subsystem
     */
    public static final synchronized void shutdown(ReverseProxy proxy) {
        proxy.getCurrentRequestForwarder().getCurrentContextCache().shutdown();
    }

    private static boolean libraryUninitialized = true;
    private static final String message = "ERROR: Proxyma library was not initialized..  Please run ProxymaFacade.initialize() method before start to use the library.";
}
