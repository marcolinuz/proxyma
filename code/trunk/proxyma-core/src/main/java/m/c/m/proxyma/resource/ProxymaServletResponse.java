package m.c.m.proxyma.resource;

import java.io.Serializable;
import java.util.Collection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.buffers.ByteBuffer;

/**
 * This class implements a response wrapper.
 * It adapts servlet container response to be managed by Proxyma Serializers.
 * Furthermore, they are done to be "rewritable" in any part allowing plugins
 * to operate on the same headers and parameters many times.
 * Finally, it is the only class that "knows" how to sen the data back to the
 * client.
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
     * @param aResponse
     */
    public ProxymaServletResponse (HttpServletResponse aResponse) {
        this.theOriginalResponse = aResponse;
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHeader(String headerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHeader(String headerName, String headerValue) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsHeader(String headerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unsetHeader(String headerName) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Cookie> getCookies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCookie(String cookieName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addCookie(Cookie aCookie) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsCookie(String cookieName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeCookie(String cookieName) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteBuffer getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteBuffer setData() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setStatus(int value) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCacheable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCacheable(boolean value) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getContentLenght() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void serializeToClient() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private HttpServletResponse theOriginalResponse = null;
}
