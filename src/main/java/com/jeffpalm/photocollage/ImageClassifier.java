package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

  private static <T> Cache<T> getCacheFromFile(String fileName) {
    Map<String, T> tmpFileNamesToTs = null;
    File f = new File(fileName);
    try {
      FileInputStream fis = new FileInputStream(f);
      ObjectInputStream in = new ObjectInputStream(fis);
      tmpFileNamesToTs = (Map<String, T>) in.readObject();
      fis.close();
    } catch (Throwable t) {
      tmpFileNamesToTs = new HashMap<String, T>();
    }
    return new Cache<T>(f, tmpFileNamesToTs);
  }

  private static void serialize(Object o, File file) {
    try {
      OutputStream fos = new FileOutputStream(file);
      ObjectOutputStream out = new ObjectOutputStream(fos);
      out.writeObject(o);
      out.flush();
      out.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private static void serialize(Cache<?> cache) {
    serialize(cache.getCache(), cache.getFile());
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

  public final Color classify(File imageFile) throws InterruptedException, IOException {
    long start = System.currentTimeMillis();
    long endClassify = 0;
    Color result = colorCache.get(imageFile.getAbsolutePath());
    if (result == null) {
      result = doClassify(imageFile);
      endClassify = System.currentTimeMillis();
      colorCache.put(imageFile.getAbsolutePath(), result);
    } else {
      LOG.info("Found cached value");
    }
    LOG.info("Classified " + imageFile + " in " + (endClassify- start) + "ms; total = "+ (System.currentTimeMillis() - start) + "ms");
    return result;
  }

  private final Color doClassify(File imageFile) throws InterruptedException, IOException {
    BufferedImage image = ImageIO.read(imageFile);
    if (image == null) {
      return null;
    }
    final int width = image.getWidth();
    final int height = image.getHeight();

    final int firstRow = (int) ((1 - config.heightPercentage) / 2 * height);
    final int lastRow = height - firstRow;
    final int firstCol = (int) ((1 - config.widthPercentage) / 2 * width);
    final int lastCol = width - firstCol;

    return getColor(image, firstRow, lastRow, firstCol, lastCol);
  }

  private final Color getColor(BufferedImage image, int firstRow, int lastRow, int firstCol,
      int lastCol) throws InterruptedException {
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
