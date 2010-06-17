package m.c.m.proxyma.core;

import java.util.Collection;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the interface to implement to provide a CacheProvider.
 * The default implementation of it is made using ECHACHE an Open Source Cache Manager.
 * IMPORTANT: The cache plugn is used 2 times into the engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run. In this way the engine can evaulate if there is a cached version of the response already available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public interface CacheProvider {
    /**
     * Method provided to initialize the chache subsystem.
     * You can use the proxyma configuration file to add specific parameters
     * for each plugin you create under the <plugins-specific> element.
     * Then, you can use the context methods to retrive the values.
     *
     * @param context the proxyma context
     */
    public void initialize (ProxymaContext context);

    /**
     * Method provided to finalize the cache subsystem
     */
    public void shutdown ();

    /**
     * Method provided to store the response of the passed resource into the cache subsystem.
     *
     * @param aResource the resource countaining the response to store.
     */
    public void storeResponse(ProxymaResource aResource);

    /**
     * Method provided to search for a response that fits the passed resource into the cache.
     *
     * @param aResource the resource to complete with a response.
     * If a fittin gresponse is found it will be attached to the resource.
     *
     * @return false if no fitting response were found into the cache.
     */
    public boolean getResponseData(ProxymaResource aResource);

    /**
     * Method provided to get a collection on URIs (keys for the cache matching) that are stored
     * into the cache subsystem.
     *
     * @return a collection of URIs
     */
    public Collection<String> getCachedURIs ();

    /**
     * Some cache subsystems keeps track of the opeations and maintains an
     * internal statistic chat can be queried.
     * This method is provided to let you use this feature if available.
     *
     * @return some statistics data about the cache status and usage.
     */
    public String getStatistics();


    /**
     * Returns true if the chache subsystem need initialization before start
     * to work.<br/>
     * Note: This method is invoked any time a new request comes to the server.
     * @return true if is needed to run the initialize() method
     */
    public boolean needInitialization();

    /**
     * Returns true if the cache subsystem need to cleanup its environment on
     * server shutdown.<br/>
     * Note: This metho is invoked more any time a reverse proxy instance
     * is finalized.
     * @return true if is needed to run the shutdown() method.
     */
    public boolean needFinalization();
}
