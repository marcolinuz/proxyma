package org.homelinux.nx01.proxyma.cache;

import java.io.IOException;

/**
 * </p><p>
 * User: makko
 * Date: 10-ago-2007
 * Time: 10.13.30
 * </p><p>
 * This class implements a Reader Class for the SmartBuffer.
 * To read more than once the same data you have to execute reset() method
 * between every read operation.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * </p>
 */
public class SmartBufferReader implements ByteBufferReader {
    /**
     * This constructor builds a new Reader based upon the passed SmartBuffer.
     *
     * @param buffer the SmartBuffer where read data
     */
    public SmartBufferReader(SmartBuffer buffer) throws IOException {
        theBuffer = buffer;
        ramBuf = new RamBufferReader(theBuffer.getRamBuffer());
        if (theBuffer.getFileBuffer() != null) {
            fileBuf = new FileBufferReader(theBuffer.getFileBuffer());
            writeLimitExcedded = true;
        } else {
            writeLimitExcedded = false;
        }
    }

    /**
     * Reads data from the buffer and stores them into the provided byte array.
     *
     * @param data the byte array where data will be written
     * @param size the max number of data that can be written.
     * @return the number of transfered bytes or -1 if there are no more data to read (the end of the buffer was reached).
     * @throws java.io.IOException if something goes wrong
     */
    public int read(byte[] data, int size) throws IOException {
        if (writeLimitExcedded) {
            if (readFromFile) {
                return fileBuf.read(data, size);
            } else {
                int i = ramBuf.read(data, size);
                if (i == -1) {
                    readFromFile = true;
                    i = fileBuf.read(data, size);
                }
                return i;
            }
        } else {
            return ramBuf.read(data, size);
        }
    }


    /**
     * Read a single byte of data from the buffer
     *
     * @return the int value of the byte or -1 if the end of the data was reached.
     * @throws java.io.IOException if something goes wrong
     */
    public int read() throws IOException {
        if (writeLimitExcedded) {
            if (readFromFile) {
                return fileBuf.read();
            } else {
                int i = ramBuf.read();
                if (i == -1) {
                    readFromFile = true;
                    i = fileBuf.read();
                }
                return i;
            }
        } else {
            return ramBuf.read();
        }
    }

    /**
     * Resets the Reader, next read operation will start fom the begin of the buffer.
     */
    public void reset() throws IOException {
        ramBuf.reset();
        if (writeLimitExcedded)
            fileBuf.reset();
    }

    /**
     * Returns the whole buffer into a byte array.
     * WARNING! This method could be memory hungry if used with large size buffers.
     *
     * @return the buffer content.
     */
    public byte[] getBytes() throws IOException {
        long size = theBuffer.getSize();
        long ramSize = ramBuf.getSize();
        long fileSize = 0;

        //Load the data.
        byte[] retVal = new byte[(int) (size)];
        read(retVal, (int) ramSize);

        if (writeLimitExcedded) {
            fileSize = fileBuf.getSize();
            read(retVal, (int) fileSize);
        }
        return retVal;
    }

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize() {
        return theBuffer.getSize();
    }

    //The SmartBuffer
    SmartBuffer theBuffer = null;

    //The internal RamBuffer to store a small amount of data
    private RamBufferReader ramBuf = null;

    //The internal FileBuffer to store large amount of data
    private FileBufferReader fileBuf = null;

    //Flag to set if the data to read are into the fileBuffer.
    private boolean readFromFile = false;

    //Flag to set if the limit was break when writing on the buffer.
    private boolean writeLimitExcedded = false;
}
