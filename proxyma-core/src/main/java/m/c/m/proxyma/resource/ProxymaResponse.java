package m.c.m.proxyma.resource;

import java.util.Collection;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.buffers.ByteBuffer;

/**
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
     * Returns an enumeration of all the header names this response contains.
     * If the response has no headers, this method returns an empty collection.
     * @return a collection of header names.
     */
    public Collection<String> getHeaderNames();

    /**
     * Returns the value of the specified response header as a String.
     * If the named header wasn't found, this method returns null.
     * The header name is case insensitive. You can use this method with any request header.
     *
     * @return the header value
     */
    public String getHeader(String headerName);

    /**
     * Adds a field to the response header with the given name and value.
     * If the field had already been set, the new value overwrites the previous one.
     * The containsHeader method can be used to test for the presence of a
     * header before setting its value.
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void setHeader(String headerName, String headerValue) throws IllegalStateException;

    /**
     * Checks whether the response message header has a field with the specified name.
     *
     * @param headerName The name of the header to check
     * @return true if the header was found
     */
    public boolean containsHeader(String headerName);

    /**
     * Removes a field from the response headers with the given name.
     * If the field is not found nothing is done but anyway, the containsHeader
     * method can be used to test for the presence of a header before remove it.
     *
     * @param headerName The name of the header to remove
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void unsetHeader(String headerName) throws IllegalStateException;

    /**
     * Returns an collection of all of the Cookie objects set into the response.
     * This method returns null if there are no cookies.
     * @return an array of cookies
     *
     */
    public Collection<Cookie> getCookies();

    /**
     * Returns the specified cookie.
     * If the cookie wasn't found, this method returns null.
     *
     * @return the header value
     */
    public String getCookie(String cookieName);

    /**
     * Adds a Cookie to the response.
     * If the Cookie had already been set, the new value overwrites the previous one.
     * The containsCookie method can be used to test for the presence of a
     * Cookie before setting its value.
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void addCookie(Cookie aCookie) throws IllegalStateException;

    /**
     * Checks whether the response has a Cookie with the specified name.
     *
     * @param cookieName The name of the Cookie to check
     * @return true if the cookie was found
     */
    public boolean containsCookie(String cookieName);

    /**
     * Removes a Cookie the response headers with the given name.
     * If the Cookie is not found, nothing is done but anyway, the containsCookie
     * method can be used to test for the presence of a Cookie before remove it.
     *
     * @param cookieName The name of the cookie to remove
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void removeCookie(String cookieName) throws IllegalStateException;

    /**
     * Get the raw binary data of the response
     * @return a ByteBuffer containing the raw data of the resource to send
     */
    public ByteBuffer getData();

    /**
     * Set the raw binary data of the response
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public ByteBuffer setData() throws IllegalStateException;

    /**
     * Get the current value of the status code of the response
     * @return the current status
     */
    public int getStatus ();

    /**
     * Sets the status code for this response.
     * This method is used to set the return status code when there is no error
     * (for example, for the status codes SC_OK or SC_MOVED_TEMPORARILY).
     *
     * @param value the new status value
     * @throws IllegalStateException is called after the method serializeToClient
     * @see serializeToClient
     */
    public void setStatus (int value)  throws IllegalStateException;

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
    public void setCacheable(boolean value) throws IllegalStateException;

    /**
     * Returns the size in bytes of the raw data into this response
     * @return the size of the response data.
     */
    public int getContentLenght();

    /**
     * Serializes and send-out the response to the client.
     * After the calling of this method, the response is locked and it can't
     * be subjected to firther modifications so all the "writing" methods
     * will rise an IllegalStateException.
     *
     */
    public void serializeToClient();
}
