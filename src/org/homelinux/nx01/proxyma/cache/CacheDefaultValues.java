package org.homelinux.nx01.proxyma.cache;

/**
 * <p>
 * Created by IntelliJ IDEA.
 * User: makko
 * Date: 22-lug-2007
 * Time: 15.39.49*
 * </p><p>
 * This class is a class of constans that provides default values for the initialization of the caches.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class CacheDefaultValues {

    //Defalult Configuration for the ehcache subsystem
    public static final String DEFAULT_CACHE_MAX_ELEMENTS_ON_MEMORY = "10000";
    public static final String DEFAULT_CACHE_IS_ETHERNAL = "false";
    public static final String DEFAULT_CACHE_TIME_TO_IDLE_SECONDS = "300";
    public static final String DEFAULT_CACHE_TIME_TO_LIVE_SECONDS = "1800";
    public static final String DEFAULT_CACHE_CAN_OVERFLOW_TO_DISK = "true";
    public static final String DEFAULT_CACHE_DISK_SPOOL_BUFFER_SIZE = "30"; //these are MegaBytes
    public static final String DEFAULT_CACHE_MAX_ELEMENTS_ON_DISK = "10000000";
    public static final String DEFAULT_CACHE_IS_DISK_PERSISTENT = "false";
    public static final String DEFAULT_CACHE_DISK_EXPPIRY_THREAD_INTERVAL_SECONDS = "300";
    public static final String DEFAULT_CACHE_EVICTION_POLICY = "LFU"; //Least Frequently Used

    //Internal configuration file based upon previous constants
    public static final byte[] DEFAULT_CONFIGURATION = ("<ehcache> \n" +
                 "<diskStore path=\"java.io.tmpdir\"/> \n" +
                 "<defaultCache \n" +
                 "            maxElementsInMemory=\"" + DEFAULT_CACHE_MAX_ELEMENTS_ON_MEMORY + "\" \n" +
                 "            eternal=\""+ DEFAULT_CACHE_IS_ETHERNAL +"\" \n" +
                 "            timeToIdleSeconds=\""+ DEFAULT_CACHE_TIME_TO_IDLE_SECONDS +"\" \n" +
                 "            timeToLiveSeconds=\""+ DEFAULT_CACHE_TIME_TO_LIVE_SECONDS +"\" \n" +
                 "            overflowToDisk=\""+ DEFAULT_CACHE_CAN_OVERFLOW_TO_DISK +"\" \n" +
                 "            diskSpoolBufferSizeMB=\""+ DEFAULT_CACHE_DISK_SPOOL_BUFFER_SIZE +"\" \n" +
                 "            maxElementsOnDisk=\""+ DEFAULT_CACHE_MAX_ELEMENTS_ON_DISK +"\" \n" +
                 "            diskPersistent=\""+ DEFAULT_CACHE_IS_DISK_PERSISTENT +"\" \n" +
                 "            diskExpiryThreadIntervalSeconds=\""+ DEFAULT_CACHE_DISK_EXPPIRY_THREAD_INTERVAL_SECONDS +"\" \n" +
                 "            memoryStoreEvictionPolicy=\""+ DEFAULT_CACHE_EVICTION_POLICY +"\" \n" +
                 "            /> " +
                 "\n" +
                 "</ehcache> ").getBytes();

}
