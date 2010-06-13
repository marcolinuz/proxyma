package m.c.m.proxyma.core;

import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the interface to implement Preprocessors, Transformers, Serializers
 * and Retrivers.
 * However, it's not recommanded to build plugins starting from this interface.
 * To do so, you should extend and override the "process" method of the "NullHandlers".
 *
 * @see NullPreprocessord, NillSerializer, NullTransformer and NullRetriver
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public interface ResourceHandler {

    /**
     * Return the the type of the Resource handler.
     * There are 4 types of resource handlers:
     * PREPROCESSOR, RETRIVER, TRANSFORMER and SERIALIZER.
     *
     * @return the value that rappresents the type of the handler.
     */
    public ProxymaTags.HandlerType getType();

    /**
     * Implements the business logic of the plugin.
     *
     * @param aResource the resource to process.
     */
    public void process(ProxymaResource aResource);
}
