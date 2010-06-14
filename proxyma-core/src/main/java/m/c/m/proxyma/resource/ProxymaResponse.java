package m.c.m.proxyma.resource;

/**
 * <p>
 * This the "Adapter" interface that will be used by Proxyma to manage client
 * responses. Any concrete class that implements this interface can be used as
 * Client response by Proxyma.
 * Through this interface Proxyma can transparently handle  "Servlet" or
 * "Portlet" responses in the same way.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public interface ProxymaResponse {

    /**
     * Sets a reference to the object that countains the response data.
     * @param responseData the object with the data of the response.
     * @see ProxymaResponseData
     */
    public void setResponseData (ProxymaResponseDataBean responseData);

    /**
     * Returns a reference to the object that countains the response data.
     * @return the object with the data of the response.
     * @see ProxymaResponseData
     */
    public ProxymaResponseDataBean getResponseData ();

    /**
     * This flag is setted by the Cache Provider if the inspection of the
     * cache related headers tags says that this resource as cacheable.
     *
     * @return true if the resource is cacheable
     */
    public boolean isCacheable ();

    /**
     * Sets the vale of the flag that reflect the cacheable status of this resource.
     *
     * @param value the new flag value
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void setCacheable(boolean value);

    /**
     * Serializes and send-out the data of the response to the client.
     *
     */
    public void serializeDataToClient();
}
