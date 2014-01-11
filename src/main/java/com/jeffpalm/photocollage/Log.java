package com.jeffpalm.photocollage;

final class Log {
  
  private final static Logger LOG = Logger.getLogger(PhotoCollageCreator.class.getSimpleName());
  private int height, width;
  private int row = 0, col = 0;
  private boolean done = false;
  
  private Log() {}

  public void start(int height, int width) {
    this.height = height;
    this.width = width;
    this.row = 0;
    this.col = 0;
    this.done = false;
  }

  public void done() {
    done = true;
  }

  public void info(String str) {
    String msg;
    if (done) {
      msg = "DONE " + str;
    } else if (height == 0 || width == 0) {
      msg = str;
    } else {
      int total = height * width;
      int current = row * width + col;
      String currentString = String.format("[(" + row + "/" + height + " " + current + "/" + total
          + " %.3f%%", 100.0 * current / total);
      msg = currentString + " " + str;
    }
    LOG.info(msg);
  }

  public void nextRow() {
    row++;
    col = 0;
  }

  public void nextCol() {
    col++;
  }

  public static Log getLog() {
    return Holder.INSTANCE;
  }

  private final static class Holder {
    private static Log INSTANCE = new Log();
  }
}