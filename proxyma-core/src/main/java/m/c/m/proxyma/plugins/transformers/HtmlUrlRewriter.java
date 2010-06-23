package m.c.m.proxyma.plugins.transformers;

import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This plugin implements an URL Rewriter.<br/>
 * It scans the HTML page contained into the response-data searching for any URL.<br/>
 * When it finds an URL relative to the path of the current configured proxy folders,
 * it uses the UrlRewriterEngine to modify the URL.<br/>
 * Its purpose is to make pages and link relative only to proxyma URI in order to fully
 * masquerde the real source of the resources.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id$
 */
public class HtmlUrlRewriter extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {
    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public HtmlUrlRewriter (ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
    }

    /**
     * It scans the HTML page contained into the response searching for any URL.<br/>
     * When it finds an URL relative to the path of the current configured proxy folders,
     * it uses the UrlRewriterEngine to modify the URL.<br/>
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) {
        log.info("Not yet Implemented..");
        throw new UnsupportedOperationException("Not Yet Implemented..");
    }
    
    /**
     * Returns the name of the plugin.
     * @return the name of the plugin.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of what the plugin does..<br/>
     * You can use html tags into it.<br/>
     * The result of this method call can be used by any interface
     * to explain for what is the puropse of the plugin.
     *
     * @return a short description of the plugin
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * The logger of the context..
     */
    private Logger log = null;

    /**
     * The name of this plugin.
     */
    private static final String name = "Html URL Rewriter";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This plugin is an HTML Transformer.<br/>" +
            "Its work is to scan the pages seraching fot links and modify them " +
            "in order to masquerde the real source of the page resources.";
}
