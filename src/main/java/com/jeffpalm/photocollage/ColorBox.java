package com.jeffpalm.photocollage;

import java.awt.image.BufferedImage;

import com.jeffpalm.photocollage.ImageClassifier.Config;

final class ColorBox {

  final int firstRow;
  final int lastRow;
  final int firstCol;
  final int lastCol;

  private ColorBox(int firstRow, int lastRow, int firstCol, int lastCol) {
    this.firstRow = firstRow;
    this.lastRow = lastRow;
    this.firstCol = firstCol;
    this.lastCol = lastCol;
  }

  /* package */ static ColorBox newForTesting(int firstRow, int lastRow, int firstCol, int lastCol) {
    return new ColorBox(firstRow, lastRow, firstCol, lastCol);
  }

  public static ColorBox getColorBox(BufferedImage image, Config config, ImageSegment s) {
    return forDebugging(image, config, s).colorBox;
  }

  public static ForDebugging forDebugging(BufferedImage image, Config config, ImageSegment s) {
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

    ColorBox colorBox = new ColorBox(firstRow, lastRow, firstCol, lastCol);

    return new ForDebugging(colorBox, width, height, realFirstRow, realLastRow, realFirstCol, realLastCol);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColorBox)) {
      return false;
    }
    ColorBox that = (ColorBox) o;
    return this.firstRow == that.firstRow && this.lastRow == that.lastRow && this.firstCol == that.firstCol
        && this.lastCol == that.lastCol;
  }

  @Override
  public int hashCode() {
    return (firstRow << 24) + (lastRow << 16) + (firstCol << 8) + lastCol;
  }

  /* package */static class ForDebugging {
    final ColorBox colorBox;
    final int imageWidth;
    final int imageHeight;
    final int realFirstRow;
    final int realLastRow;
    final int realFirstCol;
    final int realLastCol;

    private ForDebugging(ColorBox colorBox, int imageWidth, int imageHeight, int realFirstRow, int realLastRow,
        int realFirstCol, int realLastCol) {
      this.colorBox = colorBox;
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
      this.realFirstRow = realFirstRow;
      this.realLastRow = realLastRow;
      this.realFirstCol = realFirstCol;
      this.realLastCol = realLastCol;
    }

    @Override
    public String toString() {
      return String.format(
          "ForDebugging(/*imageWidth=*/%d, /*imageHeight=*/%d, /*realFirstRow=*/%d, /*realLastRow=*/%d, /*realFirstCol=*/%d, /*realLastCol=*/%d)",
          imageWidth, imageHeight, realFirstRow, realLastRow, realFirstCol, realLastCol);
      // return new MoreObjects.toStringHelper(this).toString();
    }
  }

  @Override
  public String toString() {
    return String.format("ColorBox(/*firstRow=*/%d, /*lastRow=*/%d, /*firstCol=*/%d, /*lastCol=*/%d)",
        firstRow, lastRow, firstCol, lastCol);
    // return new MoreObjects.toStringHelper(this).toString();
  }
}
