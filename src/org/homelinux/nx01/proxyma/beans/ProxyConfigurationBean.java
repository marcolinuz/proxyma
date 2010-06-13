package org.homelinux.nx01.proxyma.beans;

import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.ProxymaLog;

import java.io.File;

/**
 * <p/>
 * User: makko
 * Date: 19-lug-2007
 * Time: 8.55.04
 * </p><p>
 * This class is a bean that countains the configuration for any ReverseProxy.
 * To build it you should use the ProxymaConfigurationFactory.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxyConfigurationBean {

    /**
     * Obtains the current value of the proxy context for this proxy instance.
     * (see setProxyContext() for more info)
     *
     * @return the current value of the unique string that specify the proxy context into the vitrual machine.
     */
    public String getProxyContext() {
        return proxyContext;
    }

    /**
     * Sets the context for the proxy instance.
     * Note that this context is not the context path.. even if it can have the same value.
     * This parameter was made to differentiate many proxy instances (so many proxyma deployed applications)
     * into the same virtual machine.
     *
     * @param proxyContext a unique string that specify the proxy context into the vitrual machine.
     */
    public void setProxyContext(String proxyContext) {
        this.proxyContext = proxyContext;
    }

    /**
     * Gets the status of the showMasqueradedResources flag.
     * see setShowMasqueradedResourcesEnabled() for more informations).
     *
     * @return the status of the flag.
     */
    public boolean isShowMasqueradedResourcesEnabled() {
        return showMasqueradedResourcesEnabled;
    }

    /**
     * Sets the staus of the flag.
     * If its value is true, when the proxy is called without specify any
     * proxyFolder it will provide a page with the current configurated rules.
     * If its value is false, when the proxy is called without specify any
     * proxyFolder an http BAD_REQUEST error is returned to the client
     * Note: the access to this resource will be always logged into the proxyma_access.log
     *
     * @param showMasqueradedResourcesEnabled
     *         the new status for the flag
     */
    public void setShowMasqueradedResourcesEnabled(boolean showMasqueradedResourcesEnabled) {
        this.showMasqueradedResourcesEnabled = showMasqueradedResourcesEnabled;
    }

    /**
     * Sets the staus of the flag.
     * If its value is true, when the proxy is called without specify any
     * proxyFolder it will provide a page with the current configurated rules.
     * If its value is false, when the proxy is called without specify any
     * proxyFolder an http BAD_REQUEST error is returned to the client
     * Note: the access to this resource will be always logged into the proxyma_access.log
     *
     * @param showMasqueradedResourcesStatus
     *         the new status for the flag (ENABLED or DISABLED)
     */
    public void setShowMasqueradedResourcesStatus(String showMasqueradedResourcesStatus) {
        boolean showMasqueradedResourcesEnabled = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;
        if ((showMasqueradedResourcesStatus != null) && (!(ProxymaConstants.EMPTY_STRING.equals(showMasqueradedResourcesStatus)))) {
            if (ProxymaConstants.ENABLED.equalsIgnoreCase(showMasqueradedResourcesStatus))
                showMasqueradedResourcesEnabled = true;
            else
                showMasqueradedResourcesEnabled = false;
        }
        this.showMasqueradedResourcesEnabled = showMasqueradedResourcesEnabled;
    }

    /**
     * Gets the full path of the file xml for the configuration of the caching subsystem
     * based on ehcache.
     *
     * @return the path of the configuration file
     */
    public String getCacheConfigurationFile() {
        return cacheConfigurationFile;
    }

    /**
     * Sets the full path of the file xml for the configuration of the caching subsystem
     * based on ehcache.
     *
     * @param cacheConfigurationFile
     */
    public void setCacheConfigurationFile(String cacheConfigurationFile) {
        if ((cacheConfigurationFile == null) || (ProxymaConstants.EMPTY_STRING.equals(cacheConfigurationFile.trim()))) {
            ProxymaLog.instance.errors("WARNING: The name of the configuration file for the cache subsystem (" + cacheConfigurationFile + ") is not valid (default values will be used).");
            this.cacheConfigurationFile = null;
        } else {
            //Sets the cache system configuration file path if it does exists
            File f = new File(cacheConfigurationFile);
            if (f.exists()) {
                this.cacheConfigurationFile = cacheConfigurationFile;
            } else {
                ProxymaLog.instance.errors("WARNING: The configuration file for the cache subsystem (" + cacheConfigurationFile + ") does not exists (default values will be used).");                
                this.cacheConfigurationFile = null;
            }
        }
    }

    //the context of the proxy (NOTE: This is not its context path.. even if the value can be the same)
    private String proxyContext = null;

    //The flag to specify it the proxy will show to the clients its rules.
    private boolean showMasqueradedResourcesEnabled = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;

    //The path of the configuration file for the caching subsystem
    private String cacheConfigurationFile = null;
}
