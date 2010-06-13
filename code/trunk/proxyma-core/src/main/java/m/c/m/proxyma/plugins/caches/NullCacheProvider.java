package m.c.m.proxyma.plugins.caches;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.util.ProxymaLogger;

/**
 * </p><p>
 * This is a null implementation of the interface CacheProvider.
 * It doesn't stores anything and never returns anything.
 * IMPORTANT: The cache plugn is used 2 times into the engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run. In this way the engine can evaulate if there is a cached version of the response already available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class NullCacheProvider implements m.c.m.proxyma.core.CacheProvider {

    /**
     * This method does nothing.
     *
     * @param context the context for the logging initialization.
     */
    @Override
    public void initialize(ProxymaContext context) {
        //initialize the logger for this class.
        if (log == null)
           context.getLogger();
        
        log.info("Null cache initialized..");
    }

    /**
     * This method does nothing.
     */
    @Override
    public void shutdown() {
        log.info("Null cache shutdown..");
    }

    /**
     * This method does nothing.
     * @param aResource
     */
    @Override
    public void storeResponse(ProxymaResource aResource) {
        log.info("Null cache cant't store responses..");
    }

    /**
     * This method does nothing.
     * @param aResource a ProxymaResource
     * @return always false
     */
    @Override
    public boolean getResponse(ProxymaResource aResource) {
        log.info("Null cache cant't get respones..");
        return false;
    }

    /**
     * This method does nothing.
     * @return always an empty Collection.
     */
    @Override
    public Collection<String> getCachedURIs() {
        log.info("Null cache always returns an empty collection..");
        return new LinkedList();
    }

    /**
     * This method does nothing.
     * @return always the same message
     */
    @Override
    public String getStatistics() {
        log.info("Null cache doesn't provide statistics..");
        return "Null cache doesn't provide statistics..";
    }

    /**
     * The logger for this class
     */
    private Logger log = null;
}
