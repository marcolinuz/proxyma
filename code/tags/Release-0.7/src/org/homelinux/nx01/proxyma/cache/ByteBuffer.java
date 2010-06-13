package org.homelinux.nx01.proxyma.cache;

import java.io.IOException;

/**
 * </p>
 * User: makko
 * Date: 10-ago-2007
 * Time: 10.07.38
 * <p></p>
 * This is a common interface for my Buffer classes that permits me to change the implementation
 * without touch the code.. :O)
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public interface ByteBuffer {

    /**
     * Append the passed byte array to the buffer making an internal copy of it.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException if something goes wrong
     */
    public long append(byte[] data, int size) throws IOException;

    /**
     * Append the passed byte to the buffer.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException if something goes wrong
     */
    public long append(int data) throws IOException;

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     */
    public void setNoMoreData();

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize();

    /**
     * check if the buffer is still writable
     *
     * @return the current status.
     */
    public boolean isAppendable();
}
