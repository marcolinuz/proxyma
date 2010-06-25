package m.c.m.proxyma.rewrite;

import java.net.URL;
import java.util.logging.Logger;
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
public class CookieRewriteEngine {
    public CookieRewriteEngine (ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
    }

    /**
     * Masquerade to the client a cookie that comes froma a remote host by
     * setting its domain to the domain of proxyma and the path to the path
     * of the  current proxy-folder.
     *
     * @param cookie the cookie to masquerade
     * @param aResource the resource that owns the Cookie
     */
    public void masqueradeCookie(Cookie cookie, ProxyFolderBean folder, ProxymaResource aResource) {
        //calculate the new Path of the cookie
        URL proxymaRootURL = aResource.getProxymaRootURL();
        StringBuffer newPath = new StringBuffer(proxymaRootURL.getPath());
        newPath.append("/").append(aResource.getProxyFolder().getURLEncodedFolderName());

        //calculate the old domain of the  cookie
        StringBuffer originalDomainAndPath = null;
        if (cookie.getDomain() == null)
            originalDomainAndPath = new StringBuffer(folder.getDestinationAsURL().getHost()).append(COMMENT_FIELDS_SEPARATOR);
        else
            originalDomainAndPath = new StringBuffer(cookie.getDomain()).append(COMMENT_FIELDS_SEPARATOR);
        
        // calculate old path of the cookie
        if (cookie.getPath() == null)
            originalDomainAndPath.append("/");
        else
            originalDomainAndPath.append(cookie.getPath());

        //Set the new cookie values for the client
        cookie.setDomain(proxymaRootURL.getHost());
        cookie.setPath(newPath.toString());
        cookie.setComment(originalDomainAndPath.toString());

        log.finest("Masqueraded Cookie, old Host/domain=" + originalDomainAndPath.toString() +
                   " New Host/Domain=" + proxymaRootURL.getHost() + COMMENT_FIELDS_SEPARATOR + newPath.toString());
    }

    /**
     * Rebuilds the original cookie from a masqueraded one.
     * @param cookie the cookie to unmasquerade
     * @param aResource the resource that owns the Cookie
     */
    public void unmasqueradeCookie (Cookie cookie) {
        String[] originalValues = cookie.getComment().split (COMMENT_FIELDS_SEPARATOR);
        cookie.setDomain(originalValues[0]);
        cookie.setPath(originalValues[1]);

        log.finest("Unmasqueraded Cookie, original Host/domain " + cookie.getComment() + " restored."); 
    }

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * The separator used into the cookie comment to store the original
     * domain and path fields.
     */
    private static final String COMMENT_FIELDS_SEPARATOR = "@";
}
