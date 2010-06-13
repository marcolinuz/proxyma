package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ProxyConfigurationBean;

/**
 * <p/>
 * User: makko
 * Date: 19-lug-2007
 * Time: 10.04.35
 * </p><p>
 * This class is a Proxy Configuration factory, and it should be used to create (and eventually
 * check) any ConfigurationBean used to build an instance of ReverseProxy.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxyConfigurationFactory {

    /**
     * Builds a default configuration bean instance.
     *
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewDefaultProxyConfiguration() {
        return getNewCustomProxyConfiguration(null, ProxymaConstants.DEFAULT_EHCACHE_CONFIGURATION_FILE, ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES);
    }

    /**
     * Builds a new custom configuration bean instance by passing in only the proxyContext parameter.
     * Other attributes will have the default value.
     *
     * @param proxyContext the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewCustomProxyConfiguration(String proxyContext) {
        return getNewCustomProxyConfiguration(proxyContext, ProxymaConstants.DEFAULT_EHCACHE_CONFIGURATION_FILE, ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES);
    }

    /**
     * Builds a new custom configuration bean instance by passing in only the flag to show the rules to the clients.
     * Other attributes will have the default value.
     *
     * @param showMasqueradedResourcesEnabled
     *         The flag to specify it the proxy will show to the clients its rules.
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewCustomProxyConfiguration(boolean showMasqueradedResourcesEnabled) {
        return getNewCustomProxyConfiguration(null, ProxymaConstants.DEFAULT_EHCACHE_CONFIGURATION_FILE, showMasqueradedResourcesEnabled);
    }

    /**
     * Builds a new custom configuration bean instance by passing in only the proxyContext and the flag to show the rules to the clients.
     * Other attributes will have the default value.
     *
     * @param proxyContext         the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param showMasqueradedResourcesEnabled
     *                             The flag to specify it the proxy will show to the clients its rules.
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
     public static ProxyConfigurationBean getNewCustomProxyConfiguration(String proxyContext, boolean showMasqueradedResourcesEnabled) {
        return getNewCustomProxyConfiguration(proxyContext, ProxymaConstants.DEFAULT_EHCACHE_CONFIGURATION_FILE, showMasqueradedResourcesEnabled);
    }

    /**
     * Builds a new custom configuration bean instance by passing the proxyContext and the cache configuration file.
     * The showMasqueradedResources attribute will have the default value.
     *
     * @param proxyContext         the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param cacheSubsystemConfigurationFile
     *                             the full path of the configuration file for the cache subsystem
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewCustomProxyConfiguration(String proxyContext,
                                                                        String cacheSubsystemConfigurationFile) {
        return getNewCustomProxyConfiguration(proxyContext, cacheSubsystemConfigurationFile, ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES);
    }

    /**
     * Builds a new custom configuration bean instance by passing in all the options.
     *
     * @param proxyContext         the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param cacheSubsystemConfigurationFile
     *                             the full path of the configuration file for the cache subsystem
     * @param showMasqueradedResourcesStatus
     *                             The flag to specify it the proxy will show to the clients its rules as a Sting (ENABLED or DISABLED).
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewCustomProxyConfiguration(String proxyContext,
                                                                        String cacheSubsystemConfigurationFile,
                                                                        String showMasqueradedResourcesStatus) {

        //Set the showMasqueradedResources flag.
        boolean showMasqueradedResourcesEnabled = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;
        if ((showMasqueradedResourcesStatus != null) && (!(ProxymaConstants.EMPTY_STRING.equals(showMasqueradedResourcesStatus)))) {
            if (ProxymaConstants.ENABLED.equalsIgnoreCase(showMasqueradedResourcesStatus))
                showMasqueradedResourcesEnabled = true;
            else
                showMasqueradedResourcesEnabled = false;
        }
        return getNewCustomProxyConfiguration(proxyContext, cacheSubsystemConfigurationFile, showMasqueradedResourcesEnabled);
    }

    /**
     * Builds a new custom configuration bean instance by passing in all the options.
     *
     * @param proxyContext         the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
     * @param cacheSubsystemConfigurationFile
     *                             the full path of the configuration file for the cache subsystem
     * @param showMasqueradedResourcesEnabled
     *                             The flag to specify it the proxy will show to the clients its rules.
     * @return an instance if the ProxyConfiguration bean that can be used to build a Reverse Proxy
     */
    public static ProxyConfigurationBean getNewCustomProxyConfiguration(String proxyContext,
                                                                        String cacheSubsystemConfigurationFile,
                                                                        boolean showMasqueradedResourcesEnabled) {

        ProxyConfigurationBean retValue = new ProxyConfigurationBean();

        //Set the proxyContext only if have a non empty string value
        if ((proxyContext != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyContext))))
            retValue.setProxyContext(proxyContext);
        else
            retValue.setProxyContext(null);

        //Set the showMasqueradedResources flag.
        retValue.setShowMasqueradedResourcesEnabled(showMasqueradedResourcesEnabled);

        //Sets the cache system configuration file path if it does exists
        retValue.setCacheConfigurationFile(cacheSubsystemConfigurationFile);

        return retValue;
    }
}
