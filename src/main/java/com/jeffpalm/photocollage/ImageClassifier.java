package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

@SuppressWarnings("unchecked")
final class ImageClassifier {

  private static class Cache<T> {
    private final Map<String, T> cache;
    private final File file;

    Cache(File file, Map<String, T> cache) {
      this.file = file;
      this.cache = cache;
    }

    public File getFile() {
      return file;
    }

    public Map<String, T> getCache() {
      return cache;
    }

    public void put(String s, T value) {
      cache.put(s, value);
    }

    public T get(String s) {
      return cache.get(s);
    }
  }

  private final static Logger LOG = Logger.getLogger(ImageClassifier.class.getName());
  private final static String COLOR_CACHE_NAME = "ImageClassifierCache";
  private final static Cache<Color> colorCache = getCacheFromFile(COLOR_CACHE_NAME);
  {
    System.err.println("colorCache: " + colorCache.getCache().size());
  }

  private static Cache<Color> getCacheFromFile(String fileName) {
    File f = new File(fileName);
    Map<String, Color> map = getCacheMapFromFile(f);
    return new Cache<Color>(f, map);
  }

  private static Map<String, Color> getCacheMapFromFile(File f) {
    Map<String, Color> res = new HashMap<>();
    if (!f.exists()) {
      return res;
    }
    try {
      BufferedReader in = new BufferedReader(new FileReader(f));
      String line;
      while ((line = in.readLine()) != null) {
        if (line.equals("")) {
          continue;
        }
        String[] parts = line.split(":");
        String key = parts[0];
        int c = Integer.parseInt(parts[1]);
        Color color = new Color(c);
        res.put(key, color);
      }
      in.close();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
    return res;
  }

  private static void serialize(Cache<Color> cache) {
    File file = cache.getFile();
    Map<String, Color> map = cache.getCache();
    LOG.infof("serializing %d objects to %s", map.size(), file);
    serialize(map, file);
  }

  // Java serialization isn't working...so write to a text file.
  private static void serialize(Map<String, Color> o, File file) {
    try {
      PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
      for (Map.Entry<String, Color> e : o.entrySet()) {
        String line = String.format("%s:%d", e.getKey(), e.getValue().getRGB());
        out.println(line);
      }
      out.flush();
      out.close();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private final Config config;

  final static class Config {
    float widthPercentage = 1f;
    float heightPercentage = 1f;
    int redThreshhold = 0;
    int greenThreshhold = 0;
    int blueThreshhold = 0;
  }

  ImageClassifier(Config config) {
    this.config = config;
  }

  ImageClassifier() {
    this(new Config());
  }

  public void flush() {
    serialize(colorCache);
  }

  public final Color classify(File imageFile, ImageSegment s) throws IOException {
    long start = System.currentTimeMillis();
    long endClassify = 0;
    Color result = colorCache.get(imageFile.getAbsolutePath());
    if (result == null) {
      result = doClassify(imageFile, s);
      endClassify = System.currentTimeMillis();
      colorCache.put(imageFile.getAbsolutePath(), result);
    } else {
      LOG.info("Found cached value");
    }
    LOG.info("Classified " + imageFile + " in " + (endClassify - start) + "ms; total = "
        + (System.currentTimeMillis() - start) + "ms");
    return result;
  }

  private final Color doClassify(File imageFile, ImageSegment s) throws IOException {
    BufferedImage image = ImageIO.read(imageFile);
    if (image == null) {
      return null;
    }
    int width = image.getWidth();
    int height = image.getHeight();

    int firstRow = 0, lastRow = 0, firstCol = 0, lastCol = 0;

    int realFirstRow = (int) ((1 - config.heightPercentage) / 2 * height);
    int realLastRow = height - realFirstRow;
    int realFirstCol = (int) ((1 - config.widthPercentage) / 2 * width);
    int realLastCol = width - realFirstCol;

    switch (s) {
      case ALL:
        firstRow = realFirstRow;
        lastRow = realLastRow;
        firstCol = realFirstCol;
        lastCol = realLastCol;
        break;
      case LEFT:
        firstRow = realFirstRow;
        lastRow = realLastRow;
        firstCol = realFirstCol;
        lastCol = realLastCol / 3;
        break;
      case RIGHT:
        firstRow = realFirstRow;
        lastRow = realLastRow;
        firstCol = 2 * realFirstCol / 3;
        lastCol = realLastCol;
        break;
      case TOP:
        firstRow = realFirstRow;
        lastRow = realLastRow / 3;
        firstCol = realFirstCol;
        lastCol = realLastCol;
        break;
      case BOTTOM:
        firstRow = 2 * realFirstRow / 3;
        lastRow = realLastRow;
        firstCol = realFirstCol;
        lastCol = realLastCol;
        break;
    }

    return getColor(image, firstRow, lastRow, firstCol, lastCol);
  }

  private final Color getColor(BufferedImage image, int firstRow, int lastRow, int firstCol, int lastCol) {
    long redBucket = 0;
    long greenBucket = 0;
    long blueBucket = 0;
    long pixelCount = 0;

    for (int i = firstRow; i < lastRow; i++) {
      for (int j = firstCol; j < lastCol; j++) {
        int c = image.getRGB(j, i);
        int red = (c >> 16) & 0xff;
        int green = (c >> 8) & 0xff;
        int blue = c & 0xff;
        pixelCount++;
        redBucket += red;
        greenBucket += green;
        blueBucket += blue;
      }
    }

    int red = (int) (redBucket / pixelCount);
    int green = (int) (greenBucket / pixelCount);
    int blue = (int) (blueBucket / pixelCount);
    return new Color(red, green, blue);
  }

}
