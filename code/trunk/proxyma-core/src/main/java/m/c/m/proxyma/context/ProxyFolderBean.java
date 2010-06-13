package m.c.m.proxyma.context;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import org.apache.commons.lang.NullArgumentException;

/**
 *
 * <p>
 * This class is the bean that represents a remote destination for the
 * reverse proxy engine.
 * It is a data-object that counteins all the configuration needed by the
 * ProxymaCore to achieve its work.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 *
 */
public class ProxyFolderBean implements Serializable {

    /**
     * Default constructor for this class it builds a destination.
     * NOTE: The folder is not ready to work as it is created.
     * Actually, it needs to be configured with at least a valid
     * resource retriver and a valid serializer.
     *
     * @param FolderName the path (and name) of the proxy folder.
     * @param destination the destination URI to masquerade
     * @param context the proxyma context where to get logger settings.
     * @throws NullArgumentException if some parameter is null
     * @throws IllegalArgumentException if the folder name or the destination parameter are invalid or malformed
     * @throws UnsupportedEncodingException if the default encoding charset specified on the configuration is not supported.
     */
    public ProxyFolderBean (String folderName, String destination, ProxymaContext context) throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        log = context.getLogger();
        this.defaultEncoding = context.getDefaultEncoding();

        setFolderName(folderName);
        setDestination(destination);
        this.preprocessors = new ConcurrentHashMap<String,String>();
        this.transformers = new ConcurrentHashMap<String,String>();
    
        log.finest("ProxyFolder " + folderName + " for " + destination + "created.");
    }

    /**
     * Standard getter method for the folderName
     * @return the folderName
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * Standard getter method for the URL encoded version of the folderName
     * @return the URL encoded folderName
     */
    public String getURLEncodedFolderName() {
        return URLEncodedName;
    }

    /**
     * Standard setter method for folderName
     * @param folderName the folder name to set
     * @throws NullArgumentException if some parameter is null
     * @throws IllegalArgumentException if the folder name is not valid
     * @throws UnsupportedEncodingException if the default encoding charset specified on the configuration is not supported.
     */
    public synchronized void setFolderName(String newFolderName) throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        if (newFolderName == null) {
            log.warning("Null folderName passed.");
            throw new NullArgumentException("Null folderName passed.");
        } else {
            this.folderName = newFolderName.trim();
            if (this.folderName.length() == 0) {
                log.warning("The passed folderName is an empty (or blank)");
                throw new IllegalArgumentException("The passed folderName is an empty (or blank)");
            } else {
                //encoding-decoding the folder name
                if (this.folderName.endsWith("/")) {
                    this.URLEncodedName = URLEncoder.encode(this.folderName.substring(0,this.folderName.length()-1), defaultEncoding) + "/";
                } else {
                    //check for the last character presence
                    this.URLEncodedName = URLEncoder.encode(this.folderName, defaultEncoding) + "/";
                    this.folderName = this.folderName + "/";
                }
                if (this.folderName.indexOf("/") != this.folderName.length()-1) {
                    log.warning("The foldername must contain a \"/\" only as last character");
                    throw new IllegalArgumentException("The foldername can contain a \"/\" only as last character");
                }
            }
        }
    }
 
    /**
     * Standard getter method for the destination
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Standard setter method for destination
     * @param destination the remote destination for this folder
     * @throws NullArgumentException if some parameter is null
     * @throws IllegalArgumentException if the destination parameter is a malformed URL
     */
    public synchronized void setDestination(String destination) {
        if (destination == null) {
            log.warning("Null destination passed.");
            throw new NullArgumentException("Null destination passed.");
        } else {
            destination = destination.trim();
            if (destination.length() == 0) {
                log.warning("The passed destination is an empty (or blank) string");
                throw new IllegalArgumentException("The passed folderName is an empty (or blank) string");
            } else {
                if (!destination.endsWith("/"))
                    destination = destination + "/";

                //Check if it's a valid URL
                try {
                    URL url = new URL(destination);
                    this.destination = destination;
                } catch (MalformedURLException ex) {
                    log.warning("Destination \"" + destination + "\" is an Invalid URL.");
                    throw new IllegalArgumentException("Destination \"" + destination + "\" is an Invalid URL.");
                }
            }
        }
    }

    /**
     * Standard getter method for the max POST size attribute
     * @return the current maximum accepted size for POST operations
     */
    public int getMaxPostSize() {
        return maxPostSize;
    }

    /**
     * Standard setter method for the max POST size attribute
     * @param the new maximum accepted size for POST operations
     */
    public synchronized void setMaxPostSize(int maxPostSize) {
        if (maxPostSize < 0) {
            log.warning("Max post size can't be a negative number.. setting it to " + ProxymaTags.UNSPECIFIED_POST_SIZE);
            this.maxPostSize = ProxymaTags.UNSPECIFIED_POST_SIZE;
        } else {
            this.maxPostSize = maxPostSize;
        }
    }

    /**
     * Standard getter method to know if the proxy folder is enabled
     * @return true if the folder is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Standard setter method to set the status of the proxy folder
     * @param true enabled the folder, false disables it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Standard getter method to obtain the active cache provider class name
     * @return the class name of the current cache provider.
     */
    public String getCacheProvider() {
        return cacheProvider;
    }

    /**
     * Standard setter method to set the class name of the current active cache provider.
     * @param the class name of the new cache provider.
     */
    public synchronized void setCacheProvider(String cacheProviderClassName) {
        if (cacheProviderClassName == null) {
            log.warning("Null Cache Provoder.. Setting it to \"" + ProxymaTags.UNSPECIFIED_CACHEPROVIDER + "\"");
            this.cacheProvider = ProxymaTags.UNSPECIFIED_CACHEPROVIDER;
        } else {
            cacheProviderClassName = cacheProviderClassName.trim();
            if (cacheProviderClassName.length() == 0) {
                log.warning("The Chache Provider is an empty (or blank) string.. Setting it to \"" + ProxymaTags.UNSPECIFIED_CACHEPROVIDER + "\"");
                this.cacheProvider = ProxymaTags.UNSPECIFIED_CACHEPROVIDER;
            } else {
                this.cacheProvider = cacheProviderClassName;
            }
        }
    }

     /**
     * Standard getter method to obtain the active resource retriver class name
     * @return the class name of the current resource retriver.
     */
    public String getRetriver() {
        return retriver;
    }

    /**
     * Standard setter method to set the class name of the current resurce retriver.
     * @param the class name of the new resource retriver.
     */
    public synchronized void setRetriver(String retriverClassName) {
        if (retriverClassName == null) {
            log.warning("Null Retriver.. Setting it to \"" + ProxymaTags.UNSPECIFIED_RETRIVER + "\"");
            this.retriver = ProxymaTags.UNSPECIFIED_RETRIVER;
        } else {
            retriverClassName = retriverClassName.trim();
            if (retriverClassName.length() == 0) {
                log.warning("The Retriver is an empty (or blank) string.. Setting it to \"" + ProxymaTags.UNSPECIFIED_RETRIVER + "\"");
                this.retriver = ProxymaTags.UNSPECIFIED_RETRIVER;
            } else {
                this.retriver = retriverClassName;
            }
        }
    }

     /**
     * Standard getter method to obtain the active serializer class name
     * @return the class name of the current serializer.
     */
    public String getSerializer() {
        return serializer;
    }

    /**
     * Standard setter method to set the class name of the current serializer.
     * @param the class name of the new serializer.
     */
    public synchronized void setSerializer(String serializerClassName) {
        if (serializerClassName == null) {
            log.warning("Null Serializer.. Setting it to \"" + ProxymaTags.UNSPECIFIED_SERIALIZER + "\"");
            this.serializer = ProxymaTags.UNSPECIFIED_SERIALIZER;
        } else {
            serializerClassName = serializerClassName.trim();
            if (serializerClassName.length() == 0) {
                log.warning("The Serializer is an empty (or blank) string.. Setting it to \"" + ProxymaTags.UNSPECIFIED_SERIALIZER + "\"");
                this.serializer = ProxymaTags.UNSPECIFIED_SERIALIZER;
            } else {
                this.serializer = serializerClassName;
            }
        }
    }

    /**
     * Register a new preprocessor class name for this folder.
     * @param preprocessorClassName the name of the class that implements the preprocessor to register
     */
    public void registerPreprocessor (String preprocessorClassName) {
        if (preprocessorClassName == null) {
            log.warning("Null class name parameter.. nothing done");
        } else {
            preprocessorClassName = preprocessorClassName.trim();
            if (preprocessorClassName.length() == 0) {
                log.warning("The preprocessor class name is an empty (or blank) string.. nothing done");
            } else if (preprocessors.containsKey(preprocessorClassName)) {
                log.warning("The preprocessor \"" + preprocessorClassName + "\" is already registered in proxy folder \"" + getFolderName() + "\".. nothing done.");
            } else {
                log.finest("Registering new preprocessor \"" + preprocessorClassName + "\" for proxy folder \"" + getFolderName() + "\"");
                preprocessors.put(preprocessorClassName,preprocessorClassName);
            }
        }
    }

    /**
     * Unregister a preprocessor class name for this folder.
     * @param preprocessorClassName the name of the class that implements the preprocessor to remove
     */
    public void unregisterPreprocessor (String preprocessorClassName) {
        if (preprocessorClassName == null) {
            log.warning("Null class name parameter.. Ignoring operation");
        } else {
            preprocessorClassName = preprocessorClassName.trim();
            if (preprocessors.containsKey(preprocessorClassName)) {
                log.finest("Unregistering preprocessor \"" + preprocessorClassName + "\" for proxy folder \"" + getFolderName() + "\"");
                preprocessors.remove(preprocessorClassName);
            } else {
                log.warning("Preprocessor \"" + preprocessorClassName + "\" not present in proxy folder \""+ getFolderName() + "\".. nothing done.");
            }
        }
    }

    /**
     * Obtain a collection of preprocessor class names registered for the proxy folder
     * @return a Collection of class names.
     */
    public Collection<String> getPreprocessors () {
        return preprocessors.values();
    }

    /**
     * Register a new transformer class name for this folder.
     * @param transformerClassName the name of the class that implements the transformer to register
     */
    public void registerTransformer (String transformerClassName) {
        if (transformerClassName == null) {
            log.warning("Null class name parameter.. Ignoring operation");
        } else {
            transformerClassName = transformerClassName.trim();
            if (transformerClassName.length() == 0) {
                log.warning("The transformer class name is an empty (or blank) string.. nothing done");
            } else if (transformers.containsKey(transformerClassName)) {
                log.warning("The transformer \"" + transformerClassName + "\" is already registered in proxy folder \"" + getFolderName() + "\".. nothing done.");
            } else {
                log.finest("Registering new transformer \"" + transformerClassName + "\" for proxy folder \"" + getFolderName() + "\"");
                transformers.put(transformerClassName,transformerClassName);
            }
        }
    }

    /**
     * Unregister a transformer class name for this folder.
     * @param transformerClassName the name of the class that implements the transformer to remove
     */
    public void unregisterTransformer (String transformerClassName) {
        if (transformerClassName == null) {
            log.warning("Null class name parameter.. Ignoring operation");
        } else {
            transformerClassName = transformerClassName.trim();
            if (transformers.containsKey(transformerClassName)) {
                log.finest("Unregistering transformer \"" + transformerClassName + "\" for proxy folder \"" + getFolderName() + "\"");
                transformers.remove(transformerClassName);
            } else {
                log.warning("Transformer \"" + transformerClassName + "\" not present in proxy folder \""+ getFolderName() + "\".. nothing done.");
            }
        }
    }

    /**
     * Obtain a collection of transformers class names registered for the proxy folder
     * @return a Collection of class names.
     */
    public Collection<String> getTransformers () {
        return transformers.values();
    }

    /**
     * The proxy folder name
     */
    private String folderName = null;

    /**
     * the url encoded versione of the proxy folder name.
     */
    private String URLEncodedName = null;

    /**
     * The proxy folder destination
     */
    private String destination = null;

    /**
     * Max content length accepted for a single POST operation.
     */
    private int maxPostSize = ProxymaTags.UNSPECIFIED_POST_SIZE;

    /**
     * Specifies if the proxy folder is enabled to operate
     */
    private boolean enabled = true;

    /**
     * The name of the Class that will be used as CacheProvider
     */
    private String cacheProvider = ProxymaTags.UNSPECIFIED_CACHEPROVIDER;

    /**
     * The name of the Class that will be used as resource Retriver
     */
    private String retriver = ProxymaTags.UNSPECIFIED_RETRIVER;

    /**
     * The name of the Class that will be used as serializer
     */
    private String serializer = ProxymaTags.UNSPECIFIED_SERIALIZER;

    /**
     * The list of the preprocessor Classes to apply to the resource
     */
    private ConcurrentHashMap<String, String> preprocessors = null;

    /**
     * The list of the transformer Classes to apply to the resource
     */
    private ConcurrentHashMap<String, String> transformers = null;

    /**
     * The default encodig to use to encode/decode URLs
     */
    private String defaultEncoding = null;

    /**
     * The logger for this class
     */
    private Logger log = null;
}