package m.c.m.proxyma.context;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.log.ProxymaLoggersUtil;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class is the main context for an instance of Proxyma.
 * Multiple instance of proxyma are allowed to run into a single VM.
 * It countains the logic to access and get parameters form a required
 * configuration file.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class ProxymaContext {
    public ProxymaContext (String contextName, String contextBaseURI, String configurationFile) {
        // Initialize private attributes
        try {
            this.contextName = contextName;
            this.contextBasePath = contextBaseURI;
            proxyFoldersByName = new ConcurrentHashMap<String, ProxyFolderBean>();
            proxyFoldersByDestination = new ConcurrentHashMap<String, ProxyFolderBean>();
            config = new XMLConfiguration(configurationFile);
            config.setExpressionEngine(new XPathExpressionEngine());
            if (this.log == null) {
                //create a unique logger for the whole context
                String name = ProxymaTags.DEFAULT_LOGGER_PREFIX + "." + contextName;
                this.log = Logger.getLogger(name);

                String file = getSingleValueParameter(ProxymaTags.GLOBAL_LOGFILE_PREFIX) + "-" + contextName + ".log";
                String level = getSingleValueParameter(ProxymaTags.GLOBAL_LOGLEVEL);
                int maxSize = Integer.parseInt(getSingleValueParameter(ProxymaTags.GLOBAL_LOGFILE_MAXSIZE));
                int retention = Integer.parseInt(getSingleValueParameter(ProxymaTags.GLOBAL_LOGFILES_RETENTION));
                ProxymaLoggersUtil.initializeContextLogger(this.log, file, level, maxSize, retention);
            }
            this.defaultEncoding = getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);
            this.proxymaVersion = "Proxyma-Core Engine Rel. " + getSingleValueParameter(ProxymaTags.CONFIG_FILE_VERSION) + " (by MCM).";
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get a proxy folder by folder name (if exists)
     *
     * @param proxyFolderURLEncodedName the wanted ProxyFolderBean
     * @return null if the proxyFolder doesn't exists.
     */
    public ProxyFolderBean getProxyFolderByURLEncodedName(String proxyFolderURLEncodedName) {
       ProxyFolderBean retVal = null;
       log.finest("Searching for Proxy folder " + proxyFolderURLEncodedName);
       if (proxyFolderURLEncodedName == null) {
           log.warning("Null proxyFolderName parameter.. Ignoring operation");
       } else if (proxyFoldersByName.containsKey(proxyFolderURLEncodedName)) {
           retVal = proxyFoldersByName.get(proxyFolderURLEncodedName);
       } else {
           log.finest("Proxy folder " + proxyFolderURLEncodedName + " not found.");
       }
       return retVal;
    }

    /**
     * Get a proxy folder by folder name (if exists)
     *
     * @param proxyFolderURLEncodedName the wanted ProxyFolderBean
     * @return null if the proxyFolder doesn't exists.
     */
    public ProxyFolderBean getProxyFolderByDestination(String proxyFolderDestination) {
       ProxyFolderBean retVal = null;
       log.finest("Searching for Proxy folder destination " + proxyFolderDestination);
       if (proxyFolderDestination == null) {
           log.warning("Null proxyFolderDestination parameter.. Ignoring operation");
       } else if (proxyFoldersByDestination.containsKey(proxyFolderDestination)) {
           retVal = proxyFoldersByDestination.get(proxyFolderDestination);
       } else {
           log.finest("Proxy folder destination for " + proxyFolderDestination + " not found.");
       }
       return retVal;
    }

    /**
     * Add a new ProxyFolder to the context.
     *
     * @param proxyFolder the ProxyFolderBean to add
     * @throws IllegalArgumentException if the context is already registered
     * @throws NullArgumentException if the argument is null
     */
    public void addProxyFolder(ProxyFolderBean proxyFolder) throws IllegalArgumentException, NullArgumentException {
        if (proxyFolder == null) {
            log.warning("Null ProxyFolderBean parameter.. Ignoring operation");
            throw new NullArgumentException("Null ProxyFolderBean parameter.. Ignoring operation");
        } else {
            boolean exists = proxyFoldersByName.containsKey(proxyFolder.getFolderName());
            if (exists) {
                log.warning("The Proxy foder already exists.. nothing done.");
                throw new IllegalArgumentException("The Proxy foder already exists.. nothing done.");
            } else {
                log.finest("Adding Proxy folder " + proxyFolder.getFolderName());
                proxyFoldersByName.put(proxyFolder.getFolderName(), proxyFolder);
                proxyFoldersByDestination.put(proxyFolder.getDestination(), proxyFolder);
            }
        }
    }

    /**
     * Remove a ProxyFolder from the context
     * @param proxyFolder the proxyFolder to remove.
     * @throws IllegalArgumentException if the context doesn't exist
     * @throws NullArgumentException if the argument is null
     */
    public void removeProxyFolder (ProxyFolderBean proxyFolder) throws IllegalArgumentException, NullArgumentException {
       if (proxyFolder == null) {
            log.warning("Null ProxyFolderBean parameter.. Ignoring operation");
            throw new NullArgumentException("Null ProxyFolderBean parameter.. Ignoring operation");
        } else {
            boolean exists = proxyFoldersByName.containsKey(proxyFolder.getFolderName());
            if (!exists) {
                log.warning("The Proxy foder doesn't exists.. nothing done.");
                throw new IllegalArgumentException("The Proxy foder doesn't exists.. nothing done.");
            } else {
                log.finest("Deleting existing Proxy folder " + proxyFolder.getFolderName());
                proxyFoldersByName.remove(proxyFolder.getFolderName());
                proxyFoldersByDestination.remove(proxyFolder.getDestination());
            }
        }
    }

    /**
     * Get a collection of all the proxy folders into the context
     * @return a Collection of ProxyFolders
     */
    public Collection<ProxyFolderBean> getProxyFoldersAsCollection () {
        return proxyFoldersByName.values();
    }

    /**
     * Returns the number of proxy folders handled by the context.
     * @return the number of proxy folders into the context.
     */
    public int getProxyFoldersCount () {
        return proxyFoldersByName.size();
    }

    /**
     * Get a single value paramenter from the configuration
     *
     * @param parameterName the parameter name
     * @see ParameterTags
     * @return the parameter value
     *
     */
    public String getSingleValueParameter(String parameterXPath) {
        String retVal = config.getString(parameterXPath);
        log.finest("Getting single value of parameter on " + parameterXPath + ": " + retVal);
        return retVal;
    }

    /**
     * Get a list of values for a parameter from the configuration
     *
     * @param parameterName the parameter name
     * @see ParameterTags
     * @return the parameter value
     * @throws IllegalArgumentException if the parameter is not a single value parameter.
     */
    public Collection<String> getMultiValueParameter(String parameterXPath) {
        Collection<String> retValue = config.getList(parameterXPath);
        if (!(retValue instanceof Collection)) {
            log.warning("Parameter on " + parameterXPath + " is not multivalue");
        } else if (retValue.isEmpty()) {
            log.warning("Parameter on " + parameterXPath + " do not have any value");
        } else {
            log.finest("Multiple value parameter on " + parameterXPath + " loaded [" + retValue.size() + " elements]");
        }
        return retValue;
    }

    /**
     * Standard getter method to obtain the current log level.
     *
     * @return the log level as defined into the java standard Logger.
     * Possible values are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Standard setter method to set the new log level to use at run time.
     *
     * @param the new log level as defined into the java standard Logger.
     * Possible values are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
     */
    public void setLogLevel(String logLevel) {
        if (logLevel == null) {
           log.warning("Null loglevel.. setting it to " + ProxymaTags.UNSPECIFIED_LOGLEVEL);
           this.logLevel = ProxymaTags.UNSPECIFIED_LOGLEVEL;
        } else if (ProxymaTags.LogLevels.ALL.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINEST.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINER.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINE.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.CONFIG.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.INFO.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.WARNING.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.SEVERE.toString().equals(logLevel)) {
            this.logLevel = logLevel;
        } else {
           log.warning("Unknown log level \"" + logLevel + "\" setting it to " + ProxymaTags.UNSPECIFIED_LOGLEVEL);
           this.logLevel = ProxymaTags.UNSPECIFIED_LOGLEVEL;
        }
        ProxymaLoggersUtil.updateLogLevel(log, logLevel);
    }

    /**
     * Get the logger for this context instance.
     * This function provides a simple way to allow any plugin to attach its
     * own logs to the proxyma main log file.
     *
     * @return the context logger
     */
    public Logger getLogger() {
        return this.log;
    }


    /**
     * Get the name of this ccontext
     * @return the context name as String
     */
    public String getName() {
        return contextName;
    }

    /**
     * Get the default encoding charset used to encode/decode URLs and to parse html files.
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Get the curren relese nubler of Proxyma-core library
     * @return the default encoding
     */
    public String getProxymaVersion() {
        return proxymaVersion;
    }

    /**
     * Get the contextPath for this context
     * @return the contextURI as String
     */
    public String getBasePath() {
        return contextBasePath;
    }

    /**
     * The context name.
     */
    private String contextName = null;

    /**
     * The context base path.
     */
    private String contextBasePath = null;

    /**
     * The registered ProxyFolders for this context indexed by name
     * @see ProxyFolder
     */
    private ConcurrentHashMap<String, ProxyFolderBean> proxyFoldersByName = null;

    /**
     * The registered ProxyFolders for this context indexed by destination
     * @see ProxyFolder
     */
    private ConcurrentHashMap<String, ProxyFolderBean> proxyFoldersByDestination = null;

    /**
     * The Global Configuration managed by "Commons Configuration" component.
     */
    private XMLConfiguration config = null;

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * The default encodig to use to encode/decode URLs and for html parsing
     */
    private String defaultEncoding = null;

    /**
     * The current release of proxyma
     */
    private String proxymaVersion = null;

    /**
     * The log level to use on this context
     */
    private String logLevel = "INFO";
}
