package m.c.m.proxyma.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This class is the main skeleton of this whole project and is a mixture of many design patterns.
 * </p><p>
 * <ul>
 * <li>It's a "Builder" because it separates the building of a cmplex object (the proxy response) from its implementation.</li>
 * <li>It's a "Mediator" because it encapsulate the collaboration strategies of a group of objects that doesn't know anything about each other</li>
 * <li>It's a "Strategy" because the algorithms to build of the proxy response are choosen at run time form the ProxyFolder configuration</li>
 * <li>It's a "Template Method" because it defines the skeleton of the algorithm and delegates to the plugins the implementation of the manipulation algorithms</li>
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
    public ProxyEngine(ProxymaContext context) throws IllegalAccessException {
        //initialize the logger for this class.
        log = context.getLogger();

        // *** Load the available Cache Provider Plugins ***
        Iterator<String> availableCaches = context.getMultiValueParameter(ProxymaTags.AVAILABLE_CACHE_PROVIDERS).iterator();
        loadCacheProviders(availableCaches);
        
        // *** Load the available Resource Handler Plugins ***
        Iterator<String> availablePlugins = null;

        //Load the preprocessors
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_PREPROCESSORS).iterator();
        loadPlugins(availablePlugins);

        //Load the retrivers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_RETRIVERS).iterator();
        loadPlugins(availablePlugins);

        //Load the transformers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_TRANSFORMERS).iterator();
        loadPlugins(availablePlugins);

        //Load the serializers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_SERIALIZERS).iterator();
        loadPlugins(availablePlugins);
    }

    /**
     * This is the core method of this project.
     * It applies to the passed resource all the configured plugins defined
     * into the ProxyFolder that is responsible of its creation.
     *
     * @param aResource a resource to masquerade by the proxy.
     */
    public void doProxy(ProxymaResource aResource) {
        //TODO: Implement the main logic for the proxy.
        throw new UnsupportedOperationException("Not yet implemented");
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
     * Load into the cache provider container the passed list of plugins.
     * NOTE: Before load the plugin it checks if its type is correct.
     *       if not, the plugin will not be loaded.
     *
     * @param availablePlugins an iterator of plugin class names to load
     * @param container the cache provider container to fill.
     */
    private void loadCacheProviders(Iterator<String> availableCaches) throws IllegalAccessException {
        String cachePluginName = "";
        while (availableCaches.hasNext()) {
            try {
                cachePluginName = availableCaches.next();
                Object cachePlugin = Class.forName(cachePluginName).newInstance();
                if (cachePlugin instanceof CacheProvider) {
                    registerCacheProvider((CacheProvider)cachePlugin);
                } else {
                    log.warning("The Class \"" + cachePluginName  + "\" is not a CacheProvider.. plugin not loaded.");
                }
            } catch (ClassNotFoundException ex) {
                log.log(Level.WARNING, "Cache Provider \"" + cachePluginName  + "\" not found.. plugin not loaded.", ex);
            } catch (InstantiationException ex) {
                log.log(Level.WARNING, "Cache Provider \"" + cachePluginName  + "\" cannot be instantiated.. plugin not loaded.", ex);
            }
        }
    }

    /**
     * Load into the proper plugin containers the passed list of plugins.
     *
     * @param availablePlugins an iterator of plugin class names to load
     */
    private void loadPlugins(Iterator<String> availablePlugins) throws IllegalAccessException {
        String pluginName = "";
        while (availablePlugins.hasNext()) {
            try {
                pluginName = availablePlugins.next();
                Object thePlugin = Class.forName(pluginName).newInstance();
                if (!(thePlugin instanceof ResourceHandler)) {
                    log.warning("The Class \"" + pluginName  + "\" is not a ResourceHandler.. plugin not loaded.");
                } else { 
                    registerNewPlugin((ResourceHandler)thePlugin);
                } 
            } catch (ClassNotFoundException ex) {
                log.log(Level.WARNING, "Plugin \"" + pluginName  + "\" not found, plugin not loaded.", ex);
            } catch (InstantiationException ex) {
                log.log(Level.WARNING, "Plugin \"" + pluginName  + "\" cannot be instantiated, plugin not loaded.", ex);
            }
        }
    }

    /**
     * Register a new cache provoder implementation into the cache collection
     * @param providerImpl class that implements a CacheProvider
     */
    private void registerCacheProvider(CacheProvider providerImpl) {
        if (providerImpl == null) {
            log.warning("Null provider implementation parameter.. Ignoring operation");
        } else {
            boolean exists = availableCacheProviders.containsKey(providerImpl.getClass().getName());
            if (exists) {
                log.warning("Cache provider \"" + providerImpl.getClass().getName() + "\" already registered.. nothing done.");
            } else {
                log.finest("Adding cache provider " + providerImpl.getClass().getName());
                availableCacheProviders.put(providerImpl.getClass().getName(), providerImpl);
            }
        }
    }

    /**
     * Register a new resource handler implementation into the proper collection
     *
     * @param handlerImpl class that implements the handler
     */
    private void registerNewPlugin(ResourceHandler pluginImpl) {
        if (pluginImpl == null) {
            log.warning("Null handler implementation parameter.. Ignoring operation");
        } else {
            boolean exists;
            switch (pluginImpl.getType()) {
                case PREPROCESSOR:
                    exists = availablePreprocessors.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Preprocessor \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding preprocessor " + pluginImpl.getClass().getName());
                        availablePreprocessors.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case RETRIVER:
                    exists = availableRetrivers.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Retriver \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding retriver " + pluginImpl.getClass().getName());
                        availableRetrivers.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case TRANSFORMER:
                    exists = availableTransformers.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Transformer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding transformer " + pluginImpl.getClass().getName());
                        availableTransformers.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case SERIALIZER:
                    exists = availableSerializers.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Serializer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding serializer " + pluginImpl.getClass().getName());
                        availableSerializers.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                default:
                    log.warning("Unknown ResourceHandler type \"" + pluginImpl.getType() + "\" .. nothing done.");
                    break;
            }
        }
    }

    /**
     * The collection of all available cache providers plugins
     */
    private HashMap<String, CacheProvider> availableCacheProviders = new HashMap();

    /**
     * The collection of all available preprocessor plugins
     */
    private HashMap<String, ResourceHandler> availablePreprocessors = new HashMap();

    /**
     * The collection of all available retriver plugins
     */
    private HashMap<String, ResourceHandler> availableRetrivers = new HashMap();

    /**
     * The collection of all available transformer plugins
     */
    private HashMap<String, ResourceHandler> availableTransformers = new HashMap();

    /**
     * The collection of all available serializer plugins
     */
    private HashMap<String, ResourceHandler> availableSerializers = new HashMap();

    /**
     * The logger for this class
     */
    private Logger log = null;
}
