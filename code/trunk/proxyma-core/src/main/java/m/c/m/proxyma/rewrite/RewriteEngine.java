package m.c.m.proxyma.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This Class implements the logic od the URL rewriter engine.<br/>
 * It is used by the plugins that performs URL and Cookie rewriting.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id$
 */
public class RewriteEngine {
    public RewriteEngine (ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
    }

    /**
     * This method rewrites the passed url if it siteMatches the path of the
     * specified proxy-folder.
     * If not, it tryes to match any other configured fproxy-folder to keep the
     * client into the reverse proxy if possible.
     *
     * @param theURL the URL that have to be rewritten
     * @param aResource the resource that is currently processed by the ProxyEngine.
     * @return the rewritten URL.
     */
    public String rewriteURL (String theURL, ProxyFolderBean folder, ProxymaResource aResource) {
        String retVal = null;
        //guess if it'an absolute or relative URL
        Matcher matcher = netAbsouleURLPattern.matcher(theURL);
        if (matcher.matches()) {
            //This is an absolute URL with schema://host[:potr]/and/path
            retVal = rewriteNetAbsoluteURL(theURL, folder, aResource);
        } else {
            matcher = siteAbsoulteURLPattern.matcher(theURL);
            if(matcher.matches()) {
                //This is a site absolute URL
                retVal = rewriteSiteAbsoluteURL(theURL, folder, aResource);
            } else {
                //This is a relative URL no rewriting is needed..
                retVal = theURL;
            }
        }

        return retVal;
    }

    /**
     * This method rewrites the full URLs<br/>
     * In other words it parses and rewrites complete URLS like
     * "http://site.host[:port]/path/to/resource.ext"
     * 
     * @param theURL the URL that have to be rewritten
     * @param aResource the resource that is currently processed by the ProxyEngine.
     * @return the rewritten URL.
     */
    private String  rewriteNetAbsoluteURL(String theURL, ProxyFolderBean folder, ProxymaResource aResource) {
        String retVal = theURL;
        try {
            //Transform the URLs into jav.net.URL
            URL proxymaRootURL = aResource.getProxymaRootURL();
            URL urlToRewrite = new URL(theURL);

            //Check if the schema siteMatches
            boolean siteMatches = true;
            if (!proxymaRootURL.getProtocol().equals(urlToRewrite.getProtocol()))
                siteMatches = false;

            //Check if the host name siteMatches
            if (siteMatches && !(proxymaRootURL.getHost().equals(urlToRewrite.getHost())))
                siteMatches = false;

            //guess ports for both urls
            int proxymaRootPort = URLUtils.getPort(proxymaRootURL);
            int urlToRewritePort = URLUtils.getPort(urlToRewrite);
            if (siteMatches && !(proxymaRootPort == urlToRewritePort))
                siteMatches = false;

            if (siteMatches) {
                //Rewrites the url using the site relative method
                retVal = rewriteSiteAbsoluteURL(urlToRewrite.getPath(), folder, aResource);
            } else {
                //Searches into the context for a matching destination
                ProxyFolderBean matchingFolder = searchMatchingProxyFolderIntoContext(urlToRewrite, aResource.getContext());
                
                //Rewrite the URL based upon the matched folder.
                if (matchingFolder != null)
                    retVal = rewriteSiteAbsoluteURL(urlToRewrite.getPath(), matchingFolder, aResource);
            }

        } catch (MalformedURLException ex) {
            log.fine("Malformed URL \"" + theURL + "\"not Rewritten");
        }
        return retVal;
    }

    /**
     * Masquerade to the client a cookie that comes froma a remote host by
     * setting its domain to the domain of proxyma and the path to the path
     * of the  current proxy-folder.
     *
     * @param cookie the cookie to masquerade
     * @param aResource the resource that owns the Cookie
     */
    public void masqueradeCookie(Cookie cookie, ProxymaResource aResource) {
        URL proxymaRootURL = aResource.getProxymaRootURL();
        StringBuffer newPath = new StringBuffer(proxymaRootURL.getPath());
        newPath.append("/").append(aResource.getProxyFolder().getURLEncodedFolderName());
        cookie.setDomain(proxymaRootURL.getHost());
        cookie.setPath(newPath.toString());
    }

    /**
     * Rebuilds the original cookie from a masqueraded one.
     * @param cookie the cookie to unmasquerade
     * @param aResource the resource that owns the Cookie
     */
    public void unmasqueradeCookie (Cookie cookie, ProxymaResource aResource) {
        cookie.setPath("/");
        cookie.setDomain(aResource.getProxyFolder().getDestinationAsURL().getHost());
    }

    /**
     * This method rewrites the urls that belongs to the site with an absoute path<br/>
     * In other words it parses and rewrites complete URLS like
     * "/path/to/resource.ext"
     *
     * @param theURL the URL that have to be rewritten
     * @param folder the proxy folder that "captured" the URL
     * @param context the current proxyma context.
     * @return the rewritten URL.
     */
    private String rewriteSiteAbsoluteURL(String theURL, ProxyFolderBean folder, ProxymaResource aResource) {
        //Get the path of the masqueraded destination
        String masqueradedPath = folder.getDestinationAsURL().getPath();

        //Get the Proxyma root path
        StringBuffer newPrefixURL = new StringBuffer(aResource.getProxymaRootURL().getPath());

        //Add the proxyFolder to the proxyma path and obtaining the new prefix
        newPrefixURL.append("/").append(folder.getURLEncodedFolderName());
        
        //return the new url to the invoker.
        return theURL.replaceFirst(masqueradedPath, newPrefixURL.toString());
    }

    /**
     * Search into the context for a proxy-folder that matches the passed URL.
     * @param theURL the url to search into the destinations
     * @param context the context to inspect
     * @return the matching proxy-folder if found.. else it returns null.
     */
    private ProxyFolderBean searchMatchingProxyFolderIntoContext(URL theURL, ProxymaContext context) {
        ProxyFolderBean retVal = null;
        String searchHost = URLUtils.getDestinationHost(theURL);
        Collection foundHosts = context.getProxyFolderByDestinationHost(searchHost);
        if (foundHosts != null) {
            Iterator<ProxyFolderBean> iterator = foundHosts.iterator();
            ProxyFolderBean folder = null;
            String URLPath = theURL.getPath();
            URL foundedDestination = null;
            boolean found = false;
            while (!found && iterator.hasNext()) {
                folder = iterator.next();
                foundedDestination = folder.getDestinationAsURL();
                if (URLPath.startsWith(foundedDestination.getPath())) {
                    retVal = folder;
                    found = true;
                }
            }
        }
        return retVal;
    }

    /**
     * The logger for this class
     */
    private Logger log = null;
    /**
     * This pattern siteMatches absolute URLS that can link anything on the net
     */
    private Pattern netAbsouleURLPattern = Pattern.compile("^.*://.*$");

    /**
     * This patterm siteMatches site absoute URLS that can link only resources
     * that belongs only to the masqueraded site.
     */
    private Pattern siteAbsoulteURLPattern = Pattern.compile("^/.*$");
}
