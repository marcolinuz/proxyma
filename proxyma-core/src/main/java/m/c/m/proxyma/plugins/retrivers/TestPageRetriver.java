package m.c.m.proxyma.plugins.retrivers;

import m.c.m.proxyma.plugins.preprocessors.*;
import java.util.logging.Logger;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This preprocessor realize a component that registers into a resource attribute
 * the time when the request has come to the server. Working in conjunction with
 * the PerformanceTestSerializer it is useful to test the performances of the server.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class TestPageRetriver extends m.c.m.proxyma.plugins.retrivers.AbstractRetriver {


    /**
     * This method register the time when it runs into a resource attribute.
     * @param aResource any ProxymaResource
     * @see PerformanceTestSerializer for more info.
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
    private static final String name = "Test Page Retriver";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This retriver simply generates a test page.<br/>" +
            "It's useful only for testing puropses.";
}
