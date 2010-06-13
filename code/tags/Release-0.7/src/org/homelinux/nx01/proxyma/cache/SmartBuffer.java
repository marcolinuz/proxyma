package org.homelinux.nx01.proxyma.cache;

import java.io.Serializable;
import java.io.IOException;

/**
 * </p><p>
 * User: makko
 * Date: 10-ago-2007
 * Time: 10.13.30
 * </p><p>
 * This class implements a generslyzed Buffer that can be used to store large and small binary data.
 * It uses both a RamBuffer and a FileBuffer to mantain the data.
 * If the data that comes into the buffer are more than its soft-limit, the buffer
 * will store the exceding data into a temporary file to prevent high memory consumption.
 * You can't write simultaneouusly from separate threads (write operations are not thread-safe).
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class SmartBuffer implements Serializable, Cloneable, ByteBuffer {
    /**
     * Create a smart buffer that can countain into RAM a maximum of 256Kb of data.
     * If the data that comes into the buffer are more than this limit, the buffer
     * will store the exceding data into a temporary file.
     */
    public SmartBuffer() {
        this.maxSize = 262144; //256Kb
    }

    /**
     * Create a smart buffer that can countain into RAM a specific amount of data.
     * If the data that comes into the buffer are more than this limit, the buffer
     * will store the exceding data into a temporary file.
     *
     * @param maxRamSize
     */
    public SmartBuffer(int maxRamSize) {
        this.maxSize = maxRamSize;
    }


    /**
     * Append the passed byte array to the buffer.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException
     */
    public long append(byte[] data, int size) throws IOException {
        if (appendable) {
            this.size += size;
            if (!writeLimitExcedded && this.size > maxSize) {
                fileBuf = new FileBuffer();
                writeLimitExcedded = true;
            }

            if (writeLimitExcedded)
                fileBuf.append(data, size);
            else
                ramBuf.append(data, size);
        }
        return this.size;
    }


    /**
     * Append the passed byte to the buffer.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException if something goes wrong
     */
    public long append(int data) throws IOException {
        if (appendable) {
            this.size++;
            if (!writeLimitExcedded && this.size > maxSize) {
                fileBuf = new FileBuffer();
                writeLimitExcedded = true;
            }

            if (writeLimitExcedded)
                fileBuf.append(data);
            else
                ramBuf.append(data);
        }
        return this.size;
    }

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     */
    public void setNoMoreData() {
        ramBuf.setNoMoreData();
        if (fileBuf != null)
            fileBuf.setNoMoreData();
        appendable = false;
    }

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * return true if the buffer is still appendable
     *
     * @return the current status.
     */
    public boolean isAppendable() {
        return appendable;
    }

    /**
     * Obtain the internal FileBuffer to read into it.
     * @return the filebuffer.
     */
    protected FileBuffer getFileBuffer () {
        return this.fileBuf;
    }


    /**
     * Obtain the internal RamBuffer to read into it.
     * @return the filebuffer.
     */
      protected RamBuffer getRamBuffer () {
        return this.ramBuf; 
    }

    //Maximum size for the RamBuffer
    private int maxSize;

    //Size of the buffer
    private long size = 0;

    //The internal RamBuffer to store a small amount of data
    private RamBuffer ramBuf = new RamBuffer(RamBuffer.SMALL_BINARY_DATA);

    //The internal FileBuffer to store large amount of data
    private FileBuffer fileBuf = null;

    //Flag to set if the limit was break when writing on the buffer.
    private boolean writeLimitExcedded = false;

    //Flag to set if the object is cached
    private boolean appendable = true;
}
