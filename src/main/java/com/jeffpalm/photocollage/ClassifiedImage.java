package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

final class ClassifiedImage implements Comparable<ClassifiedImage> {

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

  private final Map<ImageSegment, Color> segmentsTocolors = new HashMap<>();
  public Color getColor(ImageSegment s) throws IOException {
    Color color = segmentsTocolors.get(s);
    if (color == null) {
      color = imageClassifier.classify(file, s);
      segmentsTocolors.put(s, color);
    }
    return color;
  }

  public Color getColor() throws IOException {
    if (color == null) {
      color = imageClassifier.classify(file, ImageSegment.ALL);
    }
    return color;
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  @Override
  public int compareTo(ClassifiedImage that) {
    return this.file.compareTo(that.file);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ClassifiedImage)) {
      return false;
    }
    ClassifiedImage that = (ClassifiedImage) o;
    return this.file.equals(that.file);
  }
}
