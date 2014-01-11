package com.jeffpalm.photocollage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageCache {
  private final static String IMAGE_CACHE_DIR = "imageCache";

  public File getResizedImage(ClassifiedImage classifiedImage, int width)
      throws IOException {
    File imageCache = new File(IMAGE_CACHE_DIR);
    if (!imageCache.exists()) {
      imageCache.mkdirs();
    }
    File subDir = new File(imageCache, String.valueOf(width));
    if (!subDir.exists()) {
      subDir.mkdirs();
    }
    File cachedImageFile = new File(subDir, classifiedImage.getFile().getName());
    if (!cachedImageFile.exists()) {
      BufferedImage resizedImage = classifiedImage.resize(width);
      ImageIO.write(resizedImage, "jpg", cachedImageFile);
    }
    return cachedImageFile;
  }
}
