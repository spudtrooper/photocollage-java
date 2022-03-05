package com.jeffpalm.photocollage;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ImageClassifierTest {

  private ImageClassifier c;

  @Before
  public void setUp() {
    c = new ImageClassifier();
  }

  @Test
  public void classify() throws InterruptedException, IOException {
    File f = TestUtils.getTestImageFiles()[0];

    assertEquals(new Color(154, 128, 122), c.classify(f, ImageSegment.ALL));
  }
}
