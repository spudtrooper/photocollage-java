package com.jeffpalm.photocollage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

final class ImageOutput implements Output {

  private File outFile;
  private Graphics2D g2d;
  private BufferedImage outImage;

  public Output open(File outFile, int newWidth, int newHeight) {
    this.outFile = outFile;
    Log.getLog().info("Creating new image of size " + newWidth + "x" + newHeight + "...");
    this.outImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    this.g2d = outImage.createGraphics();
    return this;
  }

  public void write(File bufImageFile, int row, int col) throws IOException {
    BufferedImage bufImage = ImageIO.read(bufImageFile);
    g2d.drawImage(bufImage, row, col, null);
  }

  public void close() throws IOException {
    Log.getLog().info("Writing to " + outFile + "...");
    ImageIO.write(outImage, "png", outFile);
    Log.getLog().info("Wrote to " + outFile);
  }
}
