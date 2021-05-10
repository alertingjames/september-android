package com.septmb.septmb.septmb.main;

/**
 * Created by sonback123456 on 9/18/2017.
 */

import java.io.File;

import android.os.Environment;

final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

    // Standard storage location for digital camera files
    private static final String CAMERA_DIR = "/dcim/";

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()
                        + CAMERA_DIR
                        + albumName
        );
    }
}
