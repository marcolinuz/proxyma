package m.c.m.proxyma.core;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
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
    public void doProxy(ProxymaResource aResource) {
        //A new resource request has come.. 
        ProxymaContext context = aResource.getContext();
        ProxymaRequest request = aResource.getRequest();
        ProxymaResponse response = aResource.getResponse();
        ResourceHandler defaultSerializer = availableRetrivers.get(proxyDefaultSerializer);

        //set proxyma root URI
        aResource.setProxymaRootURI(getProxymaRootURI(request));

        // *** find if the request belongs to any proxyFolder ***
        String subPath = request.getRequestURI().replaceFirst(request.getBasePath(), EMPTY_STRING);
        if (EMPTY_STRING.equals(subPath)) {
            try {
                //prepare a redirect response to the "Proxyma root uri"
                log.fine("Requested the proxyma path without trailing \"/\".. Redirecting to root uri: " + aResource.getProxymaRootURI());
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createRedirectResponse(aResource.getProxymaRootURI());
                aResource.getResponse().setResponseData(responseData);
            } catch (MalformedURLException ex) {
                //if the URL is malformed send back an error page.
                log.severe("Malformed URL found (" + aResource.getProxymaRootURI() + ") for the proxyma root URI!");
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_BAD_REQUEST);
                aResource.getResponse().setResponseData(responseData);
            }
            defaultSerializer.process(aResource);
        } else if (PATH_SEPARATOR.equals(subPath)) {
            if (isEnableShowFoldersListOnRootURI()) {
                //prepare the response with the list of the rules
                log.fine("Requested the \"registered folders page\", generating it..");
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createFoldersListResponse(context);
                aResource.getResponse().setResponseData(responseData);
                defaultSerializer.process(aResource);
            } else {
                // list view denied by configuration, send a 404 error response
                log.fine("Requested the proxyma root uri but the \"registered folders page\" is denyed by configuration.");
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_NOT_FOUND);
                aResource.getResponse().setResponseData(responseData);
                defaultSerializer.process(aResource);
            }
        } else {
            //Searching for a matching proxyFolderURLEncoded into the context
            String URLEncodedProxyFolder = subPath.split("/")[1];
            ProxyFolderBean folder = context.getProxyFolderByURLEncodedName(URLEncodedProxyFolder);

            if (folder == null) {
                //If the proxyFolder doesn't exists, (send a 404 error response)
                log.fine("Requested an unexistent or proxy folder (" + folder.getFolderName() + "), sending error response..");
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_NOT_FOUND);
                aResource.getResponse().setResponseData(responseData);
                defaultSerializer.process(aResource);
            } else if (!folder.isEnabled()) {
                //The requested proxy folder exixts but is disabled (send a 403 error response)
                log.fine("Requested a disabled or proxy folder (" + folder.getFolderName() + "), sending error response..");
                ProxymaResponseDataBean responseData = ProxyStandardResponsesFactory.createErrorResponse(STATUS_FORBIDDEN);
                aResource.getResponse().setResponseData(responseData);
                defaultSerializer.process(aResource);
            } else {
                log.fine("Requested an available and enabled proxy folder content, go to process it");
                //Set the proxyFolder into the resource
                aResource.setProxyFolder(folder);
                //Set the destination subpath
                aResource.setDestinationSubPath(subPath.replaceFirst(PATH_SEPARATOR+URLEncodedProxyFolder, EMPTY_STRING));

                // *** NOW I know what the user has Just asked for ***
                //Applying configured preprocessors to the resource
            }
        }
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
     * The default serializer to use for internal generated resources
     */
    private static final String proxyDefaultSerializer = "m.c.m.proxyma.plugins.serializers.SimpleSerializer";
}
