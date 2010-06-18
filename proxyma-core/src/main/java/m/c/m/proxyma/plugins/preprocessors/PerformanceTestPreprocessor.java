package m.c.m.proxyma.plugins.preprocessors;

import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
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
public class PerformanceTestPreprocessor extends m.c.m.proxyma.plugins.preprocessors.AbstractPreprocessor {
    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public PerformanceTestPreprocessor (ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
    }

    /**
     * This plugin implements a performance test preprocessor.<br/>
     * Its puropse is to write into a resource attribute the time when a
     * new request comes to the server.
     * It works in conjunction with the PerformanceTestSerializer
     * @param aResource any ProxymaResource
     * @see PerformanceTestSerializer
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
    private static final String name = "Performance Test Preprocessor";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This Preprocessor register the time when the request<br/>" +
            "has come to the server. It works in conjunction with the" +
            "Performance Test Serializer.<br/>" +
            "It is a nonsense to enable this plugin without his companion.";
}
