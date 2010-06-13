package org.homelinux.nx01.proxyma.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.homelinux.nx01.proxyma.beans.ProxyConfigurationBean;
import org.homelinux.nx01.proxyma.beans.ResponseResourceBean;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * <p/>
 * User: makko
 * Date: 18-lug-2007
 * Time: 15.51.48
 * </p><p>
 * This class is a custom wrapper for EHCACHE subsystem.
 * It implements a Proxy Cache used by the Request Forworder to speed-up retriving of static resources.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ResourceCache {
    public ResourceCache(ProxyConfigurationBean configuration) {
        //Check if a valid configuration file have been passed
        if (configuration.getCacheConfigurationFile() == null) {
            //Configuration file not valid.. I will use default values for the caches.
            ehCacheManager = new CacheManager(new ByteArrayInputStream(CacheDefaultValues.DEFAULT_CONFIGURATION));
        } else {
            //configuration file exists.. try to use it.
            ehCacheManager = new CacheManager(configuration.getCacheConfigurationFile());
        }

        //Allocate the ResourceCache
        cacheContext = configuration.getProxyContext().replaceAll("/", ProxymaConstants.DOT);
        ehCacheManager.addCache(cacheContext);
        theCache = ehCacheManager.getCache(cacheContext);
    }

    /**
     * Stores an object into the cache
     *
     * @param obj the Response Resource to store into the cache
     */
    public void storeResource(ResponseResourceBean obj) {
        try {
            obj.prepareForCache();
            Element elem = new Element((Serializable)obj.getUrl(), (Serializable)obj);
            theCache.putQuiet(elem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrives an object from the binary chache
     *
     * @param url the url of the object to retrive
     * @return the wanted object or null if it can't be found.
     */
    public ResponseResourceBean getResource(String url) {
        Element elem = theCache.get(url);
        Serializable retVal = null;
        if (elem != null)
           retVal = elem.getValue();

        return (ResponseResourceBean)retVal;
    }

    /**
     * Correctly finalize the caches.
     */
    public void shutdown() {
        if (theCache != null)
            theCache.removeAll();
        if (ehCacheManager != null) {
            ehCacheManager.removeCache(cacheContext);
            ehCacheManager.shutdown();
        }
    }

    /**
     * Retrives some useful informations about the cache..
     *
     * @return chache informations
     */
    public String toString() {
        StringBuffer stats = new StringBuffer();
        stats.append("---- Proxyma ResourceCache Subsystem Statisctics ----");
        stats.append("\nCache instance Name: ");
        stats.append(theCache.getName());
        stats.append("\nCache status: ");
        stats.append(theCache.getStatus());
        stats.append("\nNumber of elements currently on the cache: ");
        stats.append(theCache.getSize());
        stats.append("\nNumber of elements into the Memory Store: ");
        stats.append(theCache.getMemoryStoreSize());
        stats.append("\nNumber of elements into the Disk Store: ");
        stats.append(theCache.getDiskStoreSize());
        stats.append("\n---- End of ResourceCache Subsystem Statisctics ----\n");
        return stats.toString();
    }

    /**
     * Obtains an iterator to the keys of the cache.
     *
     * @return the list iterator of cached urls
     */
    public Iterator getCachedURLs() {
        return theCache.getKeys().iterator();
    }

    //The cache manager for this instance of Proxyma
    private CacheManager ehCacheManager = null;

    //The cache to handle binary and static data
    private Cache theCache = null;

    //The cache name
    private String cacheContext = null;
}
