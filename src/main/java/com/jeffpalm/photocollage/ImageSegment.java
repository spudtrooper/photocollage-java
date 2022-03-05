package com.jeffpalm.photocollage;

enum ImageSegment {
  ALL, LEFT, RIGHT, TOP, BOTTOM;

  public ImageSegment getOpposite() {
    switch (this) {
      case LEFT:
        return RIGHT;
      case RIGHT:
        return LEFT;
      case TOP:
        return BOTTOM;
      case BOTTOM:
        return TOP;
      case ALL:
        return ALL;
    }
    throw new RuntimeException("can't happen");
  }
}
