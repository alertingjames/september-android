package com.septmb.septmb.septmb.main;

/**
 * Created by sonback123456 on 9/18/2017.
 */

import java.io.File;

import android.os.Environment;

import com.septmb.septmb.septmb.main.AlbumStorageDirFactory;

public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

    @Override
    public File getAlbumStorageDir(String albumName) {
        // TODO Auto-generated method stub
        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}
