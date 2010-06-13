package m.c.m.proxyma.plugins.preprocessors;

import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaTags.HandlerType;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * </p><p>
 * This is the "null" implementation of a preprocessor plugin.
 * It does absolutely nothing. :O)
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class NullPreprocessor implements m.c.m.proxyma.core.ResourceHandler {

    /**
     * This method is required to declare the type of plugin that this class
     * implements.
     * @return the type of this plugin: PREPROCESSOR
     */
    @Override
    public final HandlerType getType() {
        return ProxymaTags.HandlerType.PREPROCESSOR;
    }

    /**
     * This method only initialize its internal logger.
     * It does absolutely nothing to the passed resource leaving it untouched.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) {
        if (log == null)
            log = aResource.getContext().getLogger();

        log.info("Null Preprocessor just did nothing..");
    }

    /**
     * The logger for this class
     */
    private Logger log = null;
}