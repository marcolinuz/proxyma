package m.c.m.proxyma.plugins.serializers;

import java.util.logging.Logger;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This plugin implements a simple serializer.<br/>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class SimpleSerializer extends m.c.m.proxyma.plugins.serializers.AbstractSerializer {


    /**
     * This method sends back to the client the response-data of the resource without modify them.<br/>
     * It writes also the access log records for the context in common logging format.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) {
        //initialize the logger
        if (this.log == null) {
            log = aResource.getContext().getLogger();
        }

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
    private static final String name = "Simple Serializer";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This plugin is a simple HTTP serializer.<br/>" +
            "Its work is to send back to the client the response without modify it.<br/>" +
            "It writes also an \"access log\" of in common logging format.";
}
