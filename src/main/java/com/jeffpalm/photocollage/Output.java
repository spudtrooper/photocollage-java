package com.jeffpalm.photocollage;

import java.io.File;
import java.io.IOException;

interface Output {
  Output open(File outFile, int newWidth, int newHeight) throws IOException;
  void write(File bufImage, int x, int y) throws IOException;
  void close() throws IOException;
}