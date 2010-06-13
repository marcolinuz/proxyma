package org.homelinux.nx01.proxyma.cache;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.IOException;

/**
 * </p>
 * User: makko
 * Date: 9-ago-2007
 * Time: 14.48.08
 * <p></p>
 * This class implements a ByeBuffer that can be used to store large (and small) binary data into RAM.
 * It uses a list of "pages" (byte array of a specified size) to mantain the data, and it's capable
 * of self-expansion when needed.
 * To avoid memory loss you should choose a proper value for the size of the pages.
 * A right value will be useful to optimize the use of the ram and to avoid internal fragmentation.
 * I have added some "esay constructors" that sets page size based upon the type of data that
 * you may want to store or based upon the size of the data to store.
 *
 * Note: you can read the data whenever you want with a RamBufferReader.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */

/**
 * This class is a custom buffer to read and write byte data into RAM
 */
public class RamBuffer implements Serializable, Cloneable, ByteBuffer {

    /**
     * This constructor builds a new RamBuffer based upon the type of data
     * passed as a string and initializes all internal attributes.
     *
     * @param dataType the constant value that specifies the type of data to store.
     */
    public RamBuffer(String dataType) {
        buffer = new ArrayList();

        //try to select the buffersize for the different data types.
        if (TEXT_DATA.equalsIgnoreCase(dataType))
            this.pageSize = 1024; //1kb
        else if (SMALL_BINARY_DATA.equalsIgnoreCase(dataType))
            this.pageSize = 2048; //2kb
        else if (LARGE_BINARY_DATA.equalsIgnoreCase(dataType))
            this.pageSize = 4096; //4kb

        byte[] page = new byte[pageSize];
        buffer.add(page);
        currInputpage = page;
    }

    /**
     * This constructor builds a new RamBuffer setting the pagesize to the
     * passed valut and initializes all internal attributes.
     *
     * @param pageSize the size of single a page (it can have the same size of the data to store).
     */
    public RamBuffer(int pageSize) {
        buffer = new ArrayList();
        this.pageSize = pageSize;
        byte[] page = new byte[pageSize];
        buffer.add(page);
        currInputpage = page;
    }

    /**
     * Append the passed byte array to the buffer making an internal copy of it.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException
     */
    public long append(byte[] data, int size) throws IOException {
        int count = 0;
        if (appendable) {
            if (size > data.length) {
                throw new IOException("Size of data can't be greater than the array's size.");
            } else {
                while (count < size) {
                    for (; (inputPtr < pageSize) && (count < size); inputPtr++, count++) {
                        currInputpage[inputPtr] = data[count];
                    }

                    if ((inputPtr == pageSize) && (count < size)) {
                        //I have to allocate a new page
                        currInputpage = allocatepage();
                    }
                }
            }
            this.size += count;
        }
        return this.size;
    }

    /**
     * Append the passed byte to the buffer.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException
     */
    public long append(int data) throws IOException {
        if (appendable) {
            if ((inputPtr == pageSize)) {
                //I have to allocate a new page
                currInputpage = allocatepage();
            }
            currInputpage[inputPtr] = (byte) data;
            size++;
            inputPtr++;
        }
        return size;
    }

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     */
    public void setNoMoreData() {
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
     * Obtain the current page size for this buffer
     *
     * @return the page size
     */
    public int getpageSize() {
        return pageSize;
    }

    /**
     *  Get the specified page of data
     *
     * @param pageNumber the number of the wanted page
     * @return the wanted page
     */
    protected byte [] getPage(int pageNumber) {
        return (byte [])buffer.get(pageNumber);
    }

    /**
     * Expand the buffer by allocating a new empty page and returns it.
     *
     * @return the new page.
     */
    private byte[] allocatepage() {
        byte[] newpage = new byte[pageSize];
        buffer.add(newpage);
        inputPtr = 0;
        return newpage;
    }

    //The main container of the data
    private List buffer;

    //The size of the data into the buffer
    private long size = 0;

    //The size of a single page
    private int pageSize;

    //The pointers to the current pages
    private byte[] currInputpage;

    //the pointers to the current page number and posiztion
    private int inputPtr = 0;

    //Flag to set if the object is cached
    private boolean appendable = true;

    //public constants
    public static final String TEXT_DATA = "TEXT";
    public static final String SMALL_BINARY_DATA = "SMALLBIN";
    public static final String LARGE_BINARY_DATA = "LARGEBIN";
}
