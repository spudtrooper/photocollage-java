package com.jeffpalm.photocollage;

import java.io.File;

final class TestUtils {
  
  private final static String DATA_DIR = "src/test/data";

  static File[] getTestImageFiles() {
    File[] paths = { 
        new File(getDataDir(), "IMG_5880.JPG"),
        new File(getDataDir() ,"IMG_5931.jpg") 
    };
    return paths;
  }

  static String getDataDirName() {
    return DATA_DIR;
  }
  
  static File getDataDir() {
    return new File(getDataDirName());
  }
}
