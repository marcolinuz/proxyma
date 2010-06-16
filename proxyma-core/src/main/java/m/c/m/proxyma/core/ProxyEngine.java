package m.c.m.proxyma.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This class is the main skeleton of this whole project and is a mixture of many design patterns.
 * </p><p>
 * <ul>
 * <li>It's like a "Builder" because it separates the building of a cmplex object (the proxy response) from its implementation.</li>
 * <li>It's like a "Mediator" because it encapsulate the collaboration strategies of a group of objects that doesn't know anything about each other</li>
 * <li>It's like a "Strategy" because the algorithms to build of the proxy response are choosen at run time form the ProxyFolder configuration</li>
 * <li>It's like a "Template Method" because it defines the skeleton of the algorithm and delegates to the plugins the implementation of the manipulation algorithms</li>
 * </ul>
 * </p><p>
 * Its purpose is to take a ProximaResource (that has a request inside) and manipulate it in order to fill the response with the requested data.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class ProxyEngine {

    /**
     * The constructor for this class.
     *
     * @param context the context to use to retrive configuration data
     */
    public ProxyEngine(ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
    }

    /**
     * This is the core method of this project.
     * It applies to the passed resource all the configured plugins defined
     * into the ProxyFolder that is responsible of its creation.
     *
     * @param aResource a resource to masquerade by the proxy.
     */
    public void doProxy(ProxymaResource aResource) {
        //A new resource request has come, we have to try to match it with a proxyfolder
        ProxymaContext context = aResource.getContext();

        throw new UnsupportedOperationException("Not yet implemented..");
    }

    /**
     * Get a Collection of CacheProviders registered into the Engine.
     *
     * @return the mentioned collection.
     */
    public Collection<CacheProvider> getRegisteredCachePlugins () {
        return availableCacheProviders.values();
    }

    /**
     * Get a collection of the registered plugins by type.
     *
     * @param type the type of the plugins that have to be returned
     * @return The collection of registered resource handlers
     */
    public Collection<ResourceHandler> getRegisteredPluginsByType(ProxymaTags.HandlerType type) {
        Collection <ResourceHandler> retValue = null;
        switch (type) {
                case PREPROCESSOR:
                    retValue = availablePreprocessors.values();
                    break;

                case RETRIVER:
                    retValue = availableRetrivers.values();
                    break;

                case TRANSFORMER:
                    retValue = availableTransformers.values();
                    break;

                case SERIALIZER:
                    retValue = availableSerializers.values();
                    break;

                default:
                    log.warning("What kind of ResourceHandler type is \"" + type + "\"?!?..");
                    retValue = new LinkedList();
                    break;
            }

        return retValue;
    }

    /**
     * Protected method to set the available cache providers.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble cache providers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableCacheProviders(HashMap<String, CacheProvider> availableCacheProviders) {
        this.availableCacheProviders = availableCacheProviders;
    }

    /**
     * Protected method to set the available preprocessor plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble preprocessors.
     * @see ProxyEngineFactory
     */
    protected void setAvailablePreprocessors(HashMap<String, ResourceHandler> availablePreprocessors) {
        this.availablePreprocessors = availablePreprocessors;
    }

    /**
     * Protected method to set the available retriver plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble retrivers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableRetrivers(HashMap<String, ResourceHandler> availableRetrivers) {
        this.availableRetrivers = availableRetrivers;
    }

    /**
     * Protected method to set the available serializer plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble serializers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableSerializers(HashMap<String, ResourceHandler> availableSerializers) {
        this.availableSerializers = availableSerializers;
    }

    /**
     * Protected method to set the available transformer plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble transformers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableTransformers(HashMap<String, ResourceHandler> availableTransformers) {
        this.availableTransformers = availableTransformers;
    }

    /**
     * The collection of all available cache providers plugins
     */
    private HashMap<String, CacheProvider> availableCacheProviders = null;

    /**
     * The collection of all available preprocessor plugins
     */
    private HashMap<String, ResourceHandler> availablePreprocessors = null;

    /**
     * The collection of all available retriver plugins
     */
    private HashMap<String, ResourceHandler> availableRetrivers = null;

    /**
     * The collection of all available transformer plugins
     */
    private HashMap<String, ResourceHandler> availableTransformers = null;

    /**
     * The collection of all available serializer plugins
     */
    private HashMap<String, ResourceHandler> availableSerializers = null;

    /**
     * The logger for this class
     */
    private Logger log = null;
}
