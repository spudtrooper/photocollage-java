package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

final class ClassifiedImage {

  private final static ImageCache imageCache = new ImageCache();

  private final File file;
  private final ImageClassifier imageClassifier;
  private Color color;

  ClassifiedImage(File file, ImageClassifier imageClassifier) {
    this.file = file;
    this.imageClassifier = imageClassifier;
  }

  public File getFile() {
    return file;
  }

  public BufferedImage getBufferedImage() throws IOException {
    return ImageIO.read(file);
  }

  public File getResizedImage(int width) throws IOException {
    return imageCache.getResizedImage(this, width);
  }

  public BufferedImage resize(int width) throws IOException {
    return Util.resize(getBufferedImage(), width);
  }

  public Color getColor() throws InterruptedException, IOException {
    if (color == null) {
      color = imageClassifier.classify(file);
    }
    return color;
  }
}
