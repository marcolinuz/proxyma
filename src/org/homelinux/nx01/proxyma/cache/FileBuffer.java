package org.homelinux.nx01.proxyma.cache;

import java.io.*;

/**
 * </p>
 * User: makko
 * Date: 10-ago-2007
 * Time: 10.07.38
 * <p></p>
 * This class implements a ByteBuffer that can be used to store large and small binary data.
 * It uses a temporary file to mantain the data.
 * You can't write simultaneouusly from separate threads (write operations are not thread-safe).
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class FileBuffer implements Serializable, Cloneable, ByteBuffer {
    /**
     * Create a file buffer over a temporary file.
     */
    public FileBuffer() throws IOException {
        buffer = File.createTempFile("FileBuffer", "raw");
        if (!buffer.canWrite())
            throw new IOException("Can't write into temporary file \"" + buffer.getName() + "\"");

        fileFullPath = buffer.getAbsolutePath();
        bos = new BufferedOutputStream(new FileOutputStream(buffer));
    }

    /**
     * Append the passed byte array to the buffer storing data into the temporary file.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException
     */
    public long append(byte[] data, int size) throws IOException {
        if (appendable) {
            bos.write(data, 0, size);
            this.size += size;
        }
        return this.size;
    }

    /**
     * Append the passed byte to the file.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     */
    public long append(int data) throws IOException {
        if (appendable) {
            bos.write(data);
            size++;
        }
        return size;
    }
   
    /**
     * Sets the buffer as ReadOnly an close the output stream on it..
     * no more data can be written into this buffer.
     */
    public void setNoMoreData() {
        appendable = false;

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bos = null;
        }

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
     * Obtain the full path of the file with the data
     *
     * @return the full path of the file
     */
    public String getFileFullPath() {
        return fileFullPath;
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
     * close and delete the temporary file..
     */
    protected void finalize() throws Throwable {
        try {
            if (bos != null)
                bos.close();
            buffer.delete();
        } finally {
            super.finalize();
        }
    }

    //The file that rappresents the buffer
    private File buffer = null;

    //The output stream to write data into the buffer
    private BufferedOutputStream bos = null;

    //Flag to set if the object is cached (useless in this implementation)
    private boolean appendable = true;

    //File name and path
    private String fileFullPath = null;

    //The size of the stored data.
    private long size = 0;
}
