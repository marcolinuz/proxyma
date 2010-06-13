package org.homelinux.nx01.proxyma.cache;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import org.homelinux.nx01.proxyma.beans.ProxyConfigurationBean;

/**
 * <p>
 * User: makko
 * Date: 21-lug-2007
 * Time: 16.36.16
 * </p><p>
 * This class is a Singleton that stores an hashtable of ResourceCaches.
 * Every record in the hashtable is referred to a single instance of Proxyma and
 * countains the manager for the caches of all instances.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class CachesPool {
    /**
     * Constructor for this class
     */
    protected CachesPool() {
        this.proxymaCaches = new ConcurrentHashMap();
    }

    /**
     * Static method  to obtain the singleton class that manages the caches
     * for all the proxyma instances in this virtual machine.
     *
     * @return the only instance of the global RuleSetsPool
     */
    public static synchronized CachesPool getInstance() {
        if (instance == null) {
            instance = new CachesPool();
        }
        return instance;
    }

    /**
     * Obtain the cache for the passed applicationContext
     *
     * @param config a configuration bean that belongs to the current application context. The context is used as unique identifier for the instance of proxyma (NOTE: I alwais use the web-application context path)
     * @return a ResourceCache that is a wrapper class for the ehcache system.
     */
    public ResourceCache getCache(ProxyConfigurationBean config) {
        ResourceCache retValue = null;
        String applicationContext = config.getProxyContext();
        if (proxymaCaches.containsKey(applicationContext)) {
            retValue = (ResourceCache) proxymaCaches.get(applicationContext);
        } else {
            retValue = new ResourceCache(config);
            proxymaCaches.put(applicationContext, retValue);
        }

        return retValue;
    }

    private static CachesPool instance = null;
    private ConcurrentHashMap proxymaCaches = null;
}
