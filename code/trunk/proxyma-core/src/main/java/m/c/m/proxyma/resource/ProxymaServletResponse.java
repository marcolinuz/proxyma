package m.c.m.proxyma.resource;

import java.io.Serializable;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class implements a response wrapper.
 * It adapts servlet container response to be managed by Proxyma Serializers.
 * it is the only class that "knows" how to send the data back to the client.
 * Through this class Proxyma can transparently act as a standard Servlet.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class ProxymaServletResponse implements Serializable, ProxymaResponse {

    /**
     * Default constructor for this class.
     * It thakes the original servlet response to wrap as parameter.
     * @param aResponse the response to wrap
     */
    public ProxymaServletResponse (HttpServletResponse aResponse) throws NullArgumentException {
        if (aResponse == null)
            throw new NullArgumentException("I can't build a ProxymaResponse without an HttpServletResponse.");
        this.theApplicationServerResponse = aResponse;
    }

    /**
     * Set the new ResponseDataBean overwriting any previous value.
     * This means that this method changes the whole data of the response.
     * <br/>
     * Note: if this attribute is null no data will be sent back to the client.
     * @param responseData the new data for the response.
     * @see ProxymaResponseDataBean
     */
    @Override
    public void setResponseData(ProxymaResponseDataBean responseData) {
        this.responseData = responseData;
    }

    /**
     * Returns the ResponseDataBEan that countains all the data of the response.
     * This method is used by plugins, cache providers and serializers to perform
     * their job over the data that will be sent back to the client.
     * @return the bean countaining the response data.
     * @see ProxymaResponseDataBean
     */
    @Override
    public ProxymaResponseDataBean getResponseData() {
        return this.responseData;
    }

    /**
     * Returns true if the resource can be stored into the cache subsystem.
     * @return true or false
     */
    @Override
    public boolean isCacheable() {
        return this.isCacheable;
    }

    /**
     * Sets the new value for the flag that mark the resource as cacheable
     * @param value the new flag value
     */
    @Override
    public void setCacheable(boolean value) {
        this.isCacheable = value;
    }

    /**
     * This method uses the wrapped HttpServletResponse to send the whole
     * response data (headers, status, cookies and binary data) to the
     * Client.
     */
    @Override
    public void serializeDataToClient() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The Application server's http response wrapped by this class.
     */
    private HttpServletResponse theApplicationServerResponse = null;

    /**
     * The data-object that countains the response for the client.
     */
    private ProxymaResponseDataBean responseData = null;

    /**
     * The flag that marks the resource as cacheable
     */
    boolean isCacheable = false;
}
