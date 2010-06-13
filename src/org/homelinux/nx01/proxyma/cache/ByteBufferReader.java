package org.homelinux.nx01.proxyma.cache;

import java.io.IOException;

/**
 * </p>
 * User: makko
 * Date: 10-ago-2007
 * Time: 10.07.38
 * <p></p>
 * This is a common interface to read into my Buffer classes.
 * The implementation as a Reader that let me read data on them in thread-safe mode.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public interface ByteBufferReader {

    /**
     * Reads data from the buffer and stores them into the provided byte array.
     *
     * @param data the byte array where data will be written
     * @param size the max number of data that can be written.
     * @return the number of transfered bytes or -1 if there are no more data to read (the end of the buffer was reached).
     * @throws java.io.IOException if something goes wrong
     */
    public int read(byte[] data, int size) throws IOException;

    /**
     * Read a single byte of data from the buffer
     *
     * @return the int value of the byte or -1 if the end of the data was reached.
     * @throws java.io.IOException if something goes wrong
     */
    public int read() throws IOException;

    /**
     * Reset the Reader and rewinds the read-head to the begin of the buffer.
     */
    public void reset() throws IOException;

    /**
     * Returns the whole buffer into a byte array.
     * WARNING! This method could be memory hungry if used with large size buffers.
     *
     * @return the buffer content.
     */
    public byte[] getBytes() throws IOException;

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize();
}
