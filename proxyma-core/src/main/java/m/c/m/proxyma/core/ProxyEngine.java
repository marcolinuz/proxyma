package m.c.m.proxyma.core;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponse;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

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
    public int doProxy(ProxymaResource aResource) {
        //A new resource request has come..
        int retValue = STATUS_OK;
        ProxymaContext context = aResource.getContext();
        ProxymaRequest request = aResource.getRequest();
        ProxymaResponse response = aResource.getResponse();
        ResourceHandler defaultSerializer = availableRetrivers.get(proxyDefaultSerializer);

        //set proxyma root URI
        aResource.setProxymaRootURI(getProxymaRootURI(request));

        // *** Try to understand what kind of request was come and if it belongs to any proxyFolder ***
        String subPath = request.getRequestURI().replaceFirst(request.getBasePath(), EMPTY_STRING);
        ProxymaResponseDataBean responseData = null;
        if (EMPTY_STRING.equals(subPath)) {
            //The path is not complete, redirect the client to proxyma root path
            try {
                //prepare a redirect response to the "Proxyma root uri"
                log.fine("Requested the proxyma path without trailing \"/\".. Redirecting to root uri: " + aResource.getProxymaRootURI());
                responseData = ProxyStandardResponsesFactory.createRedirectResponse(aResource.getProxymaRootURI());
            } catch (MalformedURLException ex) {
                //if the URL is malformed send back an error page.
                log.severe("Malformed URL found (" + aResource.getProxymaRootURI() + ") for the proxyma root URI!");
                responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_BAD_REQUEST);
            }

            //Serialize the response with the default serializer
            aResource.getResponse().setResponseData(responseData);
            retValue = responseData.getStatus();
            defaultSerializer.process(aResource);
        } else if (PATH_SEPARATOR.equals(subPath)) {
            //The Proxyma root path was requested.
            if (isEnableShowFoldersListOnRootURI()) {
                //The folders list page is enabled, prepre the response with the folders list
                log.fine("Requested the \"registered folders page\", generating it..");
                responseData = ProxyStandardResponsesFactory.createFoldersListResponse(context);
            } else {
                //The folders list page is disabled by configuration, send a 404 error response
                log.fine("Requested the proxyma root uri but the \"registered folders page\" is denyed by configuration.");
                responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_NOT_FOUND);
            }
            //Serialize the response with the default serializer
            aResource.getResponse().setResponseData(responseData);
            retValue = responseData.getStatus();
            defaultSerializer.process(aResource);
        } else {
            //Searching into the context for a matching proxyFolder using "URLEncoded value"
            String URLEncodedProxyFolder = subPath.split("/")[1];
            ProxyFolderBean folder = context.getProxyFolderByURLEncodedName(URLEncodedProxyFolder);

            if (folder == null) {
                //The proxyFolder doesn't exists, (send a 404 error response)
                log.fine("Requested an unexistent or proxy folder (" + folder.getFolderName() + "), sending error response..");
                responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_NOT_FOUND);
                aResource.getResponse().setResponseData(responseData);
                retValue = responseData.getStatus();
                defaultSerializer.process(aResource);
            } else if (!folder.isEnabled()) {
                //The requested proxy folder exixts but is disabled (send a 403 error response)
                log.fine("Requested a disabled or proxy folder (" + folder.getFolderName() + "), sending error response..");
                responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_FORBIDDEN);
                aResource.getResponse().setResponseData(responseData);
                retValue = responseData.getStatus();
                defaultSerializer.process(aResource);
            } else {
                //The requested proxy folder exists and is enabled.
                log.fine("Requested an available and enabled proxy folder content, go to process it");
                
                //Set the matched proxyFolder into the resource
                aResource.setProxyFolder(folder);

                //Set the destination subpath into the resource
                aResource.setDestinationSubPath(subPath.replaceFirst(PATH_SEPARATOR+URLEncodedProxyFolder, EMPTY_STRING));

                // *** NOW I know what the user has Just asked for ***
                Iterator<String> configuredPlugins = null;
                ResourceHandler plugin = null;
                CacheProvider cache = null;
                try {
                    //Applying all the folder-specific preprocessors to the resource in registration order
                    configuredPlugins = folder.getPreprocessors();
                    while (configuredPlugins.hasNext()) {
                        plugin = availablePreprocessors.get(configuredPlugins.next());
                        plugin.process(aResource);
                    }

                    //Use the folder-specific cache provider to search for the wanted resource into the cache subsystem
                    cache = availableCacheProviders.get(folder.getCacheProvider());
                    if (!cache.getResponseData(aResource)) {
                        // *** The resource is not present into the cache **
                        //Go to retrive it using the folder-specific retriver
                        plugin = availableRetrivers.get(folder.getRetriver());
                        plugin.process(aResource);

                        //Inspect the response headers and set the cacheable flag.
                        setCacheableFlag(aResource);

                        //Apply the folder-specific transformer in registration order
                        configuredPlugins = folder.getTransformers();
                        while (configuredPlugins.hasNext()) {
                            plugin = availableTransformers.get(configuredPlugins.next());
                            plugin.process(aResource);
                        }

                        //Try to understand if the resource is cacheable
                        if (aResource.getResponse().isCacheable())
                            cache.storeResponseData(aResource);
                    }

                    //Finally pass the resource to the folder-specific serializer
                    plugin = availableSerializers.get(folder.getSerializer());
                    retValue = aResource.getResponse().getResponseData().getStatus();
                    plugin.process(aResource);

                } catch (Exception e) {
                    //If any unexpected exception is thrown, send the back the error resource to the client.
                    log.warning("Trhere was an error Processing the request \"" + aResource.getRequest().getRequestURI() +  "\" by the Proxy folder \"" + folder.getFolderName() + "\"");
                    e.printStackTrace();
                    responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_INTERNAL_SERVER_ERROR);
                    aResource.getResponse().setResponseData(responseData);
                    retValue = responseData.getStatus();
                    defaultSerializer.process(aResource);
                }
            }
        }

        //Return the status to the caller
        return retValue;
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
     * Get the new value for the flag<br/>
     * If true, proxyma will show the list of the registered folders
     * if the client access to the root uri of the proxy.
     * @return true or false
     */
    protected boolean isEnableShowFoldersListOnRootURI() {
        return enableShowFoldersListOnRootURI;
    }

    /**
     * Sets  the new value for the flag.
     * @param enableShowFoldersListOnRootURI
     * @see isShowFoldersListOnRootURI
     */
    protected void setEnableShowFoldersListOnRootURI(boolean showFoldersListOnRootURI) {
        this.enableShowFoldersListOnRootURI = showFoldersListOnRootURI;
    }


    /**
     * Calculates the root URI of the reverse proxy context.
     *
     * @param request the request to use for the calculus.
     * @return http://proxyma.host[:proxymaPort]/
     */
    private String getProxymaRootURI (ProxymaRequest request) {
        StringBuffer retVal = new StringBuffer();

        retVal.append(request.getScheme()).append("://");
        retVal.append(request.getServerName());

        if ((request.getServerPort() == 80) && (HTTP_SCHEME.equals(request.getScheme()))) {
        } else if ((request.getServerPort() == 443) && (HTTPS_SCHEME.equals(request.getScheme()))) {
        } else {
            retVal.append(":").append(request.getServerPort());
        }
        retVal.append("/");
        
        return retVal.toString();
    }

    /**
     * Inspect the response Headers to understand if the resource can be
     * stored into the cache.
     *
     * @param aResource the resource that countains the response data
     * @return true if the response is cacheable.
     */
    private void setCacheableFlag(ProxymaResource aResource) {
        ProxymaResponseDataBean responseData = aResource.getResponse().getResponseData();
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

        //Set the value of the ispection into the resource data
        aResource.getResponse().setCacheable(cacheableFlag);
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

    /**
     * flag that tells to proxyma to show the list of the registered folders
     * if the client access to the root uri of the proxy.
     */
    private boolean enableShowFoldersListOnRootURI = false;

    /* SOME USEFUL CONSTANTS */
    /**
     * an empty string..
     */
    private static final String EMPTY_STRING = "";

    /**
     * The separator character for the URI paths
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * http
     */
    private static final String HTTP_SCHEME = "http";

    /**
     * https
     */
    private static final String HTTPS_SCHEME = "https";
    
    /**
     * Http status code for "not found" resources
     */
    private static final int STATUS_NOT_FOUND = 404;

    /**
     * Http status code for "forbidden" resources
     */
    private static final int STATUS_FORBIDDEN = 403;

    /**
     * Http status code for "Malformed requests"
     */
    private static final int STATUS_BAD_REQUEST = 400;

    /**
     * Http status code for "Internal Server Error"
     */
    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    /**
     * Http status code for "Ok"
     */
    private static final int STATUS_OK = 200;

    /**
     * Http status code for redirect "Found"
     */
    private static final int STATUS_FOUND = 302;

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
     * Cache control value that means: this is not to cache..
     */
    public final static String CACHE_CONTROL_MAX_AGE_ZERO = "max-age=0";

    /**
     * The default serializer to use for internal generated resources
     */
    private static final String proxyDefaultSerializer = "m.c.m.proxyma.plugins.serializers.SimpleSerializer";
}
