package m.c.m.proxyma.plugins.transformers;

import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaTags.HandlerType;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the "null" implementation of a transformer plugin.
 * It does absolutely nothing. :O)
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public abstract class AbstractTransformer implements m.c.m.proxyma.core.ResourceHandler {

    /**
     * This method is required to declare the type of plugin that this class
     * implements.
     * @return the type of this plugin: TRANSFORMER
     */
    @Override
    public final HandlerType getType() {
        return ProxymaTags.HandlerType.TRANSFORMER;
    }

    /**
     * This method only initialize its internal logger.
     * It does absolutely nothing to the passed resource leaving it untouched.
     * @param aResource any ProxymaResource
     */
    @Override
    public abstract void process(ProxymaResource aResource);

    /**
     * Implement this method to return the name of the plugin.
     * @return the name of the plugin
     */
    @Override
    public abstract String getName();

    /**
     * Implement this method to provide a short description of what the plugin
     * does.. you can use html tags into it.
     * @return a short description of the plugin
     */
    @Override
    public abstract String getHtmlDescription();
}
