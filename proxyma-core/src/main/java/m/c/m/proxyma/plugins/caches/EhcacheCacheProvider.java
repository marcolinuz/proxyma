package m.c.m.proxyma.plugins.caches;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * </p><p>
 * This is a wrapper for the famous ecache cache subsystem.<br/>
 * It implements the CacheProvider interface to make Proxyma able to work with it.
 * IMPORTANT: Any cache plugn is used 2 times into the engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run ot evaluate if there is a cached version of the response available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class EhcacheCacheProvider implements m.c.m.proxyma.plugins.caches.CacheProvider {

    /**
     * The default constructor for all the plugins.
     * @param context the context where the plugin is instantiated
     */
    public EhcacheCacheProvider (ProxymaContext context) {
        log = context.getLogger();
        log.info("Ehcahe subsystem initialized..");
    }

    /**
     * This method does nothing.
     * @param aResource
     */
    @Override
    public void storeResponseDataIfCacheable(ProxymaResource aResource) {
        if (isCacheable(aResource.getResponse().getResponseData())) {
            log.finest("The resource is cacheable.. storing it into the cache");
        } else {
            log.finest("The resource is not cacheable.. nothing done.");
        }
    }

    /**
     * This method does nothing.
     * @param aResource a ProxymaResource
     * @return always false
     */
    @Override
    public boolean getResponseData(ProxymaResource aResource) {
        log.finest("Null cache cant't get any respones..");
        return false;
    }

    /**
     * This method does nothing.
     * @return always an empty Collection.
     */
    @Override
    public Collection<String> getCachedURIs() {
        log.finest("Null cache always returns an empty collection..");
        return new LinkedList();
    }

    /**
     * This method does nothing.
     * @return always the same message
     */
    @Override
    public String getStatistics() {
        log.finest("Null cache doesn't provide statistics..");
        return "Null cache doesn't provide statistics..";
    }

    /*
     * Returns the name of the cache provider.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of the cache provider formatted for html output.
     * @return the description of the cache provider
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * Inspect the response Headers to understand if the resource can be
     * stored into the cache.
     *
     * @param responseData the response data to inspect
     * @return true if the response is cacheable.
     */
    private boolean isCacheable(ProxymaResponseDataBean responseData) {
        boolean cacheableFlag = false;

        //Check for intresting HTTP headers
        String pragmaHeader = responseData.getHeader(PRAGMA_HEADER).getValue();
        String cacheControlHeader = responseData.getHeader(CACHE_CONTROL_HEADER).getValue();

        //If there are no directives we can cache it
        if ((pragmaHeader == null) && (cacheControlHeader == null)) {
            //No directives found: we can cache it!!!
            cacheableFlag = true;
        } else {
            // check for pragma directives..
            if (pragmaHeader != null) {
                //Check the Pragma header..
                if (pragmaHeader.indexOf(PRAGMA_NO_CACHE) > 0)
                //we can't cache this...
                    cacheableFlag = false;
                else
                    cacheableFlag = true;
            }

            // check for cache-control directives
            if (cacheControlHeader != null) {
                //Check the value
                if ((cacheControlHeader.indexOf(CACHE_CONTROL_NO_CACHE) > 0) ||
                        (cacheControlHeader.indexOf(CACHE_CONTROL_NO_STORE) > 0) ||
                        (cacheControlHeader.indexOf(CACHE_CONTROL_PRIVATE) > 0)) {
                    //we can't cache this
                    cacheableFlag = false;
                } else if (cacheControlHeader.indexOf(CACHE_CONTROL_PUBLIC) > 0) {
                    cacheableFlag = true;
                } else {
                    //Ok.. we have to parse the whole string.. :O(
                    //we split the values by "," and search for max-age values.
                    String[] entries = cacheControlHeader.split(",");
                    for (int i=0; i<entries.length; i++) {
                        if (entries[i].indexOf(CACHE_CONTROL_MAX_AGE) > 0) {
                            //find maxage value
                            int value = Integer.parseInt(entries[i].replaceFirst(CACHE_CONTROL_MAX_AGE, EMPTY_STRING));
                            if (value > 0)
                                cacheableFlag = true;
                            else
                                cacheableFlag = false;
                            break;
                        } else if (entries[i].indexOf(CACHE_CONTROL_S_MAXAGE) > 0) {
                            //find maxage value
                            int value = Integer.parseInt(entries[i].replaceFirst(CACHE_CONTROL_S_MAXAGE, EMPTY_STRING));
                            if (value > 0)
                                cacheableFlag = true;
                            else
                                cacheableFlag = false;
                            break;
                        }
                    }
                }
            } else {
                //we can cache this
                cacheableFlag = true;
            }
        }

        //return the value of the ispection
        return cacheableFlag;
    }

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * an empty string..
     */
    private static final String EMPTY_STRING = "";
    
    /**
     * An header for the cache control
     */
    private static final String PRAGMA_HEADER = "Pragma";

    /**
     * No cache value for the Pragma header
     */
    public final static String PRAGMA_NO_CACHE = "no-cache";

    /**
     * Another Header for the cache control
     */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    /**
     * Cache control value that means: ok, cache it!
     */
    public final static String CACHE_CONTROL_PUBLIC = "public";

    /**
     * Cache control value that means: You can cache it only for this user.
     */
    public final static String CACHE_CONTROL_PRIVATE = "private";

    /**
     * No cache value for the Pragma header
     */
    public final static String CACHE_CONTROL_NO_CACHE = "no-cache";

    /**
     * Cache control value that means: no, don't cache this
     */
    public final static String CACHE_CONTROL_NO_STORE = "no-store";

    /**
     * Cache control value that means: this can be cached for X seconds
     */
    public final static String CACHE_CONTROL_MAX_AGE = "max-age=";

    /**
     * Similar to the previous but for shared caches
     */
    public final static String CACHE_CONTROL_S_MAXAGE = "s-maxage=";

    /**
     * The name of this plugin.
     */
    private static final String name = "Ehcache Cache Provider";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This is a wrapper for the famouns Ehcache subsystem.<br/>" +
            "Use this plugin uses the Ecache engine to implement a robust and fast " +
            "cache on the proxy-folder retrived data.<br/>" +
            "It costs in terms of RAM utilization, but it can give a great speed up " +
            "to your proxy operations.";
}
