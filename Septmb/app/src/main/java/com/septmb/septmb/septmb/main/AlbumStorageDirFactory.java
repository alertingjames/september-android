package com.septmb.septmb.septmb.main;

/**
 * Created by sonback123456 on 9/18/2017.
 */

import java.io.File;

abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
