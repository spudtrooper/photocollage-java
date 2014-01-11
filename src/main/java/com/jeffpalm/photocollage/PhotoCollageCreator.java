package com.jeffpalm.photocollage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.jeffpalm.builder.GenericBuilder;

final class PhotoCollageCreator {

  private int resizedWidth = 200;
  private int numRows = 1;
  private int numCols = 1;
  private int smallImageWidth = 50;
  private int smallImageHeight = 50;
  private File outDir;

  public interface Builder extends com.jeffpalm.builder.Builder<PhotoCollageCreator> {
    Builder setOutDir(File outDir);
    Builder setResizedWidth(int resizedWidth);
    Builder setNumRows(int numRows);
    Builder setNumCols(int numCols);
    Builder setSmallImageWidth(int smallImageWidth);
    Builder setSmallImageHeight(int smallImageHeight);
  }

  public static Builder newBuilder() {
    return new GenericBuilder<Builder>(new PhotoCollageCreator(), Builder.class).asBuilder();
  }

  private final Log log = Log.getLog();

  public void createCollage(File inputImageFile, Iterable<File> imageFiles) throws IOException,
      InterruptedException {

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
    BufferedImage inputImage = ImageIO.read(inputImageFile);
    BufferedImage resizedInputImage = Util.resize(inputImage, resizedWidth);

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        createCollage(inputImageFile, images, resizedInputImage, row, col);
      }
    }
  }

  private void createCollage(File inputImageFile, List<ClassifiedImage> images,
      BufferedImage image, int row, int col) throws IOException, InterruptedException {

    final int nearestImageThreshhold = 100;

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
      File outDir = this.outDir != null ? this.outDir : new File(baseName + "-" + numRows + "x"
          + numCols + "-out");
      outDir.mkdirs();
      outFile = new File(outDir, baseName + "-" + row + "-" + col + "." + ext);
    }

    final Output output = new ImageOutput().open(outFile, newWidth, newHeight);

    log.start(width, height);
    final int rowStart = height * row;
    final int colStart = width * col;
    for (int i = rowStart; i < rowStart + height; i++, log.nextRow()) {
      log.info("Starting row " + i);
      for (int j = colStart; j < colStart + width; j++, log.nextCol()) {
        int pixel = image.getRGB(j, i);
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        // Reduce the red a little. This makes the colors come out more nicely.
        if (red > 100) {
          red -= 50;
        }

        Color color = new Color(red, green, blue);
        Comparable<Color> comp = new ComparableColor(color);
        ClassifiedImage classifiedImage = nearestImage(comp, images, nearestImageThreshhold);
        File bufImageFile = classifiedImage.getResizedImage(smallImageWidth);
        int x = smallImageWidth * (j - colStart);
        int y = smallImageHeight * (i - rowStart);
        output.write(bufImageFile, x, y);
      }
    }

    log.done();
    output.close();
    log.info("Done");
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

  private ClassifiedImage nearestImage(Comparable<Color> color, List<ClassifiedImage> images,
      int threshhold) throws InterruptedException, IOException {
    final List<ClassifiedImage> chosenImages = new ArrayList<ClassifiedImage>();
    int nearestDistance = Integer.MAX_VALUE;
    ClassifiedImage nearestImage = null;
    for (ClassifiedImage image : images) {
      int dist = color.compareTo(image.getColor());
      if (nearestDistance > dist) {
        nearestDistance = dist;
        nearestImage = image;
      }
      if (dist < threshhold) {
        chosenImages.add(image);
      }
    }
    log.info("threshhold=" + threshhold + " nearestDist=" + nearestDistance + " #images="
        + chosenImages.size() + " color=" + color);
    if (chosenImages.size() == 0) {
      return nearestImage(color, images, threshhold + threshhold / 2);
    }
    // Make the
    int newThreshhold = threshhold;
    while (chosenImages.size() > 10) {
      newThreshhold /= 2;
      List<ClassifiedImage> newChosenImages = new ArrayList<ClassifiedImage>();
      for (ClassifiedImage image : chosenImages) {
        if (color.compareTo(image.getColor()) <= newThreshhold) {
          newChosenImages.add(image);
        }
      }
      chosenImages.clear();
      chosenImages.addAll(newChosenImages);
    }
    if (chosenImages.isEmpty()) {
      return nearestImage;
    }
    int index = (int) (chosenImages.size() * Math.random());
    return chosenImages.get(index);
  }
}
