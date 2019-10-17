package com.quod.downloader;

import com.quod.Constants;

/**
 * Download service provider factory closs
 */
public class DownloadServiceProvider {
    private DownloadServiceProvider(){}

    public static DownloadService getDownloadService(Integer type){
        if (Constants.MULTITHREAD_DOWNLOAD_OPTIONS.equals(type))
        {
            System.out.println("--------------Download data by multithread-------------------");
            return new MultiThreadDownloadService();
        }
        else if (Constants.SINGLETHREAD_DOWNLOAD_OPTIONS.equals(type))
        {
            System.out.println("--------------Download data by singlethread------------------");
            return new SingleThreadDownloadService();
        };

        return null;
    }
}
