package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.jeffpalm.builder.GenericBuilder;

final class PhotoCollageCreator {

  private final static Logger LOG = Logger.getLogger(PhotoCollageCreator.class.getName());

  private int resizedWidth = 200;
  private int numRows = 1;
  private int numCols = 1;
  private int smallImageWidth = 25;
  private int smallImageHeight = 25;
  private int nearestImageThreshhold = 20;
  private File outDir;

  public interface Builder extends com.jeffpalm.builder.Builder<PhotoCollageCreator> {
    Builder setOutDir(File outDir);

    Builder setResizedWidth(int resizedWidth);

    Builder setNumRows(int numRows);

    Builder setNumCols(int numCols);

    Builder setSmallImageWidth(int smallImageWidth);

    Builder setSmallImageHeight(int smallImageHeight);

    Builder setNearestImageThreshhold(int nearestImageThreshhold);
  }

  public static Builder newBuilder() {
    return new GenericBuilder<Builder>(new PhotoCollageCreator(), Builder.class).asBuilder();
  }

  private final Log log = Log.getLog();

  public void createCollage(File inputImageFile, Iterable<File> imageFiles, boolean getColorEagerly)
      throws IOException, InterruptedException {

    if (!inputImageFile.exists()) {
      throw new IllegalArgumentException(inputImageFile + " must exist");
    }

    ImageClassifier.Config config = new ImageClassifier.Config();
    ImageClassifier imageClassifier = new ImageClassifier(config);
    List<ClassifiedImage> images = new ArrayList<ClassifiedImage>();
    for (File imageFile : imageFiles) {
      ClassifiedImage image = new ClassifiedImage(imageFile, imageClassifier);
      images.add(image);
    }
    if (getColorEagerly) {
      final Vector<ClassifiedImage> q = new Vector<ClassifiedImage>(images);
      final int[] cnt = { 0 };
      final int n = images.size();
      List<Thread> threads = new ArrayList<Thread>();
      for (int i = 0; i < 300; i++) {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {

            while (!q.isEmpty()) {
              ClassifiedImage image = q.remove(0);
              try {
                LOG.infof("[%d/%d (%.2f%%)] getting color for %s", cnt[0], n, 100 * ((float) cnt[0] / (float) n),
                    image);
                cnt[0]++;
                image.getColor();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        });
        threads.add(t);
        t.start();
      }
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      imageClassifier.flush();
    }

    BufferedImage inputImage = ImageIO.read(inputImageFile);
    BufferedImage resizedInputImage = Util.resize(inputImage, resizedWidth);

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        createCollage(inputImageFile, images, resizedInputImage, row, col);
      }
    }
  }

  private Map<ImageSegment, Comparable<Color>> getComparableSegments(BufferedImage image, int width, int height, int x,
      int y) {
    Comparable<Color> top = null, right = null, bottom = null, left = null;
    if (y > 0) {
      top = getComparableColor(image, x, y - 1);
    }
    if (x < width - 1) {
      right = getComparableColor(image, x + 1, y);
    }
    if (y < height - 1) {
      bottom = getComparableColor(image, x, y + 1);
    }
    if (x > 0) {
      left = getComparableColor(image, x - 1, y);
    }

    Map<ImageSegment, Comparable<Color>> res = new HashMap<>();
    res.put(ImageSegment.TOP, top);
    res.put(ImageSegment.RIGHT, right);
    res.put(ImageSegment.BOTTOM, bottom);
    res.put(ImageSegment.LEFT, left);
    return res;
  }

  private void createCollage(File inputImageFile, final List<ClassifiedImage> images, final BufferedImage image,
      int row, int col) throws IOException, InterruptedException {

    final int width = image.getWidth() / numCols;
    final int height = image.getHeight() / numRows;

    final int newWidth = width * smallImageWidth;
    final int newHeight = height * smallImageHeight;

    File outFile;
    String name = inputImageFile.getName();
    int ilastDot = name.lastIndexOf(".");
    String baseName = name.substring(0, ilastDot);
    String ext = name.substring(ilastDot + 1);
    // Create a file in this directory if we're only creating one image.
    baseName += "-" + resizedWidth + "w" + smallImageWidth + "xh" + smallImageHeight;
    if (numRows == 1 && numCols == 1) {
      outFile = new File(baseName + "-out." + ext);
    } else {
      File outDir = this.outDir != null ? this.outDir : new File(baseName + "-" + numRows + "x" + numCols + "-out");
      outDir.mkdirs();
      outFile = new File(outDir, baseName + "-" + row + "-" + col + "." + ext);
    }

    final Output output = new ImageOutput().open(outFile, newWidth, newHeight);
    log.start(width, height);
    final int rowStart = height * row;
    final int colStart = width * col;
    for (int y = rowStart; y < rowStart + height; y++, log.nextRow()) {
      log.info("Starting row " + y);
      for (int x = colStart; x < colStart + width; x++, log.nextCol()) {
        Comparable<Color> comp = getComparableColor(image, x, y);
        Map<ImageSegment, Comparable<Color>> segmentColors = getComparableSegments(image, width, height, x, y);
        ClassifiedImage classifiedImage = nearestImageWithNeighbors(comp, images, nearestImageThreshhold,
            segmentColors.get(ImageSegment.TOP), segmentColors.get(ImageSegment.RIGHT),
            segmentColors.get(ImageSegment.BOTTOM), segmentColors.get(ImageSegment.LEFT));
        File bufImageFile = classifiedImage.getResizedImage(smallImageWidth);
        BufferedImage bufImage = ImageIO.read(bufImageFile);
        if (bufImage == null) {
          throw new RuntimeException("bad image");
        }
        int nx = smallImageWidth * (x - colStart);
        int ny = smallImageHeight * (y - rowStart);
        output.write(bufImageFile, nx, ny);
      }
    }

    log.done();
    output.close();
    log.info("Done");
  }

  private Comparable<Color> getComparableColor(BufferedImage image, int x, int y) {
    int pixel = 0;
    try {
      pixel = image.getRGB(x, y);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new RuntimeException(
          String.format("width=%d height=%d x=%d y=%d", image.getWidth(), image.getHeight(), x, y), e);
    }
    int red = (pixel >> 16) & 0xff;
    int green = (pixel >> 8) & 0xff;
    int blue = (pixel) & 0xff;

    // Reduce the red a little. This makes the colors come out more nicely.
    if (red > 100) {
      red -= 50;
    }

    Color color = new Color(red, green, blue);
    Comparable<Color> comp = new ComparableColor(color);
    return comp;
  }

  private final static class ComparableColor implements Comparable<Color> {
    private final Color color;

    ComparableColor(Color color) {
      this.color = color;
    }

    public int compareTo(Color c) {
      return (int) Util.distance(color, c);
    }
  }

  private static <T> Set<T> intersect(Iterable<T> a, Collection<T> b) {
    Set<T> res = new HashSet<T>();
    for (T t : a) {
      if (b.contains(t)) {
        res.add(t);
      }
    }
    return res;
  }

  private final Map<ClassifiedImage, Integer> lastUsed = new HashMap<ClassifiedImage, Integer>();
  private int lastUsedCount = 0;

  private ClassifiedImage nearestImageWithNeighbors(Comparable<Color> color, List<ClassifiedImage> images,
      int threshhold, Comparable<Color> top, Comparable<Color> left, Comparable<Color> bottom, Comparable<Color> right)
      throws InterruptedException, IOException {
    List<ClassifiedImage> topImages = nearestImages(top, ImageSegment.TOP, images, threshhold, false);
    List<ClassifiedImage> rightImages = nearestImages(right, ImageSegment.RIGHT, images, threshhold, false);
    List<ClassifiedImage> bottomImages = nearestImages(bottom, ImageSegment.BOTTOM, images, threshhold, false);
    List<ClassifiedImage> leftImages = nearestImages(left, ImageSegment.LEFT, images, threshhold, false);

    Set<ClassifiedImage> intersection = new HashSet<>(topImages);

    intersection = intersect(intersection, rightImages);
    intersection = intersect(intersection, bottomImages);
    intersection = intersect(intersection, leftImages);

    if (!intersection.isEmpty()) {
      return chooseLeastUsed(intersection);
    }

    List<ClassifiedImage> chosenImages = nearestImages(color, ImageSegment.ALL, images, threshhold, true);
    return chooseLeastUsed(chosenImages);
  }

  private ClassifiedImage nearestImage(Comparable<Color> color, Iterable<ClassifiedImage> images, int threshhold)
      throws IOException {
    List<ClassifiedImage> chosenImages = nearestImages(color, ImageSegment.ALL, images, threshhold, true);
    return chooseLeastUsed(chosenImages);
  }

  private List<ClassifiedImage> nearestImages(Comparable<Color> color, ImageSegment s, Iterable<ClassifiedImage> images,
      int threshhold, boolean recur) throws IOException {
    if (color == null) {
      return Collections.emptyList();
    }
    final List<ClassifiedImage> chosenImages = new ArrayList<ClassifiedImage>();
    int nearestDistance = Integer.MAX_VALUE;
    ClassifiedImage nearestImage = null;
    for (ClassifiedImage image : images) {
      int dist = color.compareTo(image.getColor(s.getOpposite()));
      if (nearestDistance > dist) {
        nearestDistance = dist;
        nearestImage = image;
      }
      if (dist < threshhold) {
        chosenImages.add(image);
      }
    }
    if (chosenImages.isEmpty()) {
      return nearestImages(color, s, images, threshhold + threshhold / 2, recur);
    }
    if (recur) {
      int newThreshhold = threshhold;
      while (chosenImages.size() > 50) {
        newThreshhold /= 2;
        List<ClassifiedImage> newChosenImages = new ArrayList<ClassifiedImage>();
        for (ClassifiedImage image : chosenImages) {
          if (color.compareTo(image.getColor(s.getOpposite())) <= newThreshhold) {
            newChosenImages.add(image);
          }
        }
        chosenImages.clear();
        chosenImages.addAll(newChosenImages);
        log.info("reducing threshold to " + newThreshhold + " #images=" + chosenImages.size());
      }
    }

    if (chosenImages.isEmpty()) {
      chosenImages.add(nearestImage);
    }

    return chosenImages;
  }

  private ClassifiedImage chooseLeastUsed(Iterable<ClassifiedImage> chosenImages) {
    int minLastUsed = Integer.MAX_VALUE;
    ClassifiedImage minLastUsedimg = null;
    for (ClassifiedImage img : chosenImages) {
      Integer imgLastUsed = lastUsed.get(img);
      if (imgLastUsed == null) {
        lastUsed.put(img, lastUsedCount++);
        return img;
      }
      if (imgLastUsed < minLastUsed) {
        minLastUsed = imgLastUsed;
        minLastUsedimg = img;
      }
    }
    lastUsed.put(minLastUsedimg, lastUsedCount++);
    return minLastUsedimg;
  }
}
