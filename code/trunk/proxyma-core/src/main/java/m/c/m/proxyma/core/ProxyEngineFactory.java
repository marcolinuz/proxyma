package m.c.m.proxyma.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * This class is the factory method of the Proxy Engine
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class ProxyEngineFactory {
    /**
     * The constructor for this class.
     *
     * @param context the context to use to retrive configuration data
     */
    public ProxyEngine createNewProxyEngine(ProxymaContext context) throws IllegalAccessException {
        //set the logger if is not already set.
        if (log == null)
            this.log = context.getLogger();

        //initialize the logger for this class.
        ProxyEngine newEngine = new ProxyEngine(context);

        //Create the Maps for the available plugins
        HashMap<String, CacheProvider> availableCacheProviders = new HashMap();
        HashMap<String, ResourceHandler> availablePreprocessors = new HashMap();
        HashMap<String, ResourceHandler> availableRetrivers = new HashMap();
        HashMap<String, ResourceHandler> availableTransformers = new HashMap();
        HashMap<String, ResourceHandler> availableSerializers = new HashMap();

        // *** Load the available Cache Provider Plugins ***
        Iterator<String> availableCaches = context.getMultiValueParameter(ProxymaTags.AVAILABLE_CACHE_PROVIDERS).iterator();
        loadCacheProviders(availableCaches, availableCacheProviders);
        
        // *** Load the available Resource Handler Plugins ***
        Iterator<String> availablePlugins = null;

        //Load the preprocessors
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_PREPROCESSORS).iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.PREPROCESSOR, availablePreprocessors);

        //Load the retrivers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_RETRIVERS).iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.RETRIVER, availableRetrivers);

        //Load the transformers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_TRANSFORMERS).iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.TRANSFORMER, availableTransformers);

        //Load the serializers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_SERIALIZERS).iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.SERIALIZER, availableSerializers);

        //set the values into the proxy engine
        newEngine.setAvailableCacheProviders(availableCacheProviders);
        newEngine.setAvailablePreprocessors(availablePreprocessors);
        newEngine.setAvailableRetrivers(availableRetrivers);
        newEngine.setAvailableSerializers(availableSerializers);
        newEngine.setAvailableTransformers(availableTransformers);

        //Set the value for the "show folders on root uri" flag
        String configValue = context.getSingleValueParameter(ProxymaTags.GLOBAL_SHOW_FOLDERS_LIST);
        if (configValue != null)
            newEngine.setEnableShowFoldersListOnRootURI(configValue.equalsIgnoreCase("true")?true:false);

        //Initialize all the available cache providers
        Iterator<CacheProvider> caches = newEngine.getRegisteredCachePlugins().iterator();
        while (caches.hasNext()) {
            CacheProvider cache = caches.next();
            if (cache.needInitialization())
                cache.initialize(context);
        }

        //return the builded object
        return newEngine;
    }

    /**
     * Load into the cache provider container the passed list of plugins.
     * NOTE: Before load the plugin it checks if its type is correct.
     *       if not, the plugin will not be loaded.
     *
     * @param availablePlugins an iterator of plugin class names to load
     * @param container the cache provider container to fill.
     */
    private void loadCacheProviders(Iterator<String> availableCaches, HashMap<String, CacheProvider> availableCacheProviders) throws IllegalAccessException {
        String cachePluginName = "";
        while (availableCaches.hasNext()) {
            try {
                cachePluginName = availableCaches.next();
                Object cachePlugin = Class.forName(cachePluginName).newInstance();
                if (cachePlugin instanceof CacheProvider) {
                    registerCacheProvider((CacheProvider)cachePlugin, availableCacheProviders);
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
    private void loadPlugins(Iterator<String> availablePlugins, ProxymaTags.HandlerType requiredType, HashMap<String, ResourceHandler> pluginContainer) throws IllegalAccessException {
        String pluginName = "";
        while (availablePlugins.hasNext()) {
            try {
                pluginName = availablePlugins.next();
                Object thePlugin = Class.forName(pluginName).newInstance();
                if ((thePlugin instanceof ResourceHandler) && ((ResourceHandler)thePlugin).getType() == requiredType) {
                    registerNewPlugin((ResourceHandler)thePlugin, pluginContainer);
                } else {
                    log.warning("The Class \"" + pluginName  + "\" is not a " + requiredType + ".. plugin not loaded.");
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
    private void registerCacheProvider(CacheProvider providerImpl, HashMap<String, CacheProvider> availableCacheProviders) {
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
    private void registerNewPlugin(ResourceHandler pluginImpl, HashMap<String, ResourceHandler> pluginContainer) {
        if (pluginImpl == null) {
            log.warning("Null handler implementation parameter.. Ignoring operation");
        } else {
            boolean exists;
            switch (pluginImpl.getType()) {
                case PREPROCESSOR:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Preprocessor \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding preprocessor " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case RETRIVER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Retriver \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding retriver " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case TRANSFORMER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Transformer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding transformer " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case SERIALIZER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Serializer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finest("Adding serializer " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                default:
                    log.warning("Unknown ResourceHandler type \"" + pluginImpl.getType() + "\" .. nothing done.");
                    break;
            }
        }
    }

    /**
     * The logger of the context
     */
    private Logger log = null;
}
