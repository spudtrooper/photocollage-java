package com.jeffpalm.photocollage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

final class Util {
  
  private Util() {}

  /**
   * Resizes the smaller side to <code>size</code>.
   * 
   * @param img
   *          image to resize
   * @param size
   *          new size of the smaller side
   * @return resized image
   * @throws IOException
   */
  public static BufferedImage resize(BufferedImage img, int size) throws IOException {
    int width = img.getWidth();
    int height = img.getHeight();
    int newWidth, newHeight;
    if (width < height) {
      newWidth = size;
      newHeight = (int) (height * (1.0 * size / width));
    } else {
      newWidth = (int) (width * (1.0 * size / height));
      newHeight = size;
    }
    BufferedImage dimg = new BufferedImage(newWidth, newHeight, img.getType());
    Graphics2D g = dimg.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, width, height, null);
    g.dispose();
    g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    return dimg;
  }

  public static BufferedImage rotate(BufferedImage src) {
    if (src == null) {

      System.out.println("getRotatedImage: input image is null");
      return null;

    }

    BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
    Graphics2D g2d = dest.createGraphics();

    AffineTransform origAT = g2d.getTransform();

    AffineTransform rot = new AffineTransform();
    rot.rotate(Math.toRadians(90), src.getWidth() / 2, src.getHeight() / 2);
    g2d.transform(rot);

    g2d.drawImage(src, 0, 0, null);

    g2d.setTransform(origAT);
    g2d.dispose();

    return dest;
  }

  public static double distance(Color a, Color b) {
    return Math.sqrt(Math.pow(b.getRed() - a.getRed(), 2)
        + Math.pow(b.getGreen() - a.getGreen(), 2) + Math.pow(b.getBlue() - a.getBlue(), 2));
  }
}
