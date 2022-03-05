
package com.jeffpalm.photocollage;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ColorBoxTest {

  @Test
  public void getColorBox() throws InterruptedException, IOException {
    File f = TestUtils.getTestImageFiles()[2];
    ImageClassifier.Config config = new ImageClassifier.Config();
    BufferedImage image = ImageIO.read(f);

    {
      ColorBox.ForDebugging got = ColorBox.forDebugging(image, config, ImageSegment.LEFT);
      ColorBox want = ColorBox.newForTesting(/* firstRow= */0, /* lastRow= */495, /* firstCol= */0, /* lastCol= */165);
      assertEquals(want, got);
    }
  }

  static void assertEquals(ColorBox want, ColorBox.ForDebugging gotDebug) {
    ColorBox got = gotDebug.colorBox;
    String msg = String.format("want{%s} != got{%s}", want, gotDebug);
    org.junit.Assert.assertEquals(msg, want, got);
  }
}
