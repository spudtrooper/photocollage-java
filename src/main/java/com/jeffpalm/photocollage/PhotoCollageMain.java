package com.jeffpalm.photocollage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class PhotoCollageMain {

  public static void main(String[] args) {
    try {
      System.exit(new PhotoCollageMain().realMain(args));
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      printHelp();
      System.exit(1);
    }
  }

  public int realMain(String[] args) {
    PhotoCollageCreator.Builder builder = PhotoCollageCreator.newBuilder();
    FileFinder finder = new FileFinder();
    File inputImageFile = null;
    boolean getColorEagerly = false;
    for (int i = 0; i < args.length;) {
      String arg = args[i++];
      if (isOption(arg, "outdir")) {
        builder.setOutDir(new File(args[i++]));
      } else if (isOption(arg, "rows")) {
        builder.setNumRows(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "cols")) {
        builder.setNumCols(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "width")) {
        builder.setResizedWidth(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "smallwidth")) {
        builder.setSmallImageWidth(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "smallheight")) {
        builder.setSmallImageHeight(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "nearestimagethreshhold")) {
        builder.setNearestImageThreshhold(Integer.parseInt(args[i++]));
      } else if (isOption(arg, "getcoloreagerly")) {
        getColorEagerly = true;
      } else if (isOption(arg, "help")) {
        printHelp();
        return 0;
      } else {
        File f = new File(arg);
        if (f.exists()) {
          if (inputImageFile == null) {
            inputImageFile = f;
          } else {
            finder.addFile(f);
          }
        }
      }
    }
    if (inputImageFile == null) {
      throw new IllegalArgumentException("Source image required");
    }
    Collection<File> imageFiles = finder.findFiles();
    if (imageFiles.isEmpty()) {
      throw new IllegalArgumentException("Input files required");
    }
    try {
      builder.build().createCollage(inputImageFile, imageFiles, getColorEagerly);
    } catch (IOException e) {
      return 1;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return 1;
    }
    return 0;
  }

  private static void printHelp() {
    System.err.println("java " + PhotoCollageMain.class.getName() + " <options> <sourceImage> <inputImage>+");
    System.err.println("where options include:");
    System.err.println("  --help              Print this message");
    System.err.println("  --outdir <dir>      Output images to dir");
    System.err.println("  --rows num          Split up the output into num rows (Defaults to 1)");
    System.err.println("  --cols num          Split up the output into num columns (Defaults to 1)");
    System.err.println("  --width num         Resize the input image to num (Defaults to 200px)");
    System.err.println("  --smallwidth num    Use images of width num for the pixels (Defaults to 50px)");
    System.err.println("  --smallheight num   Use images of height num for the pixels (Defaults to 50px)");
  }

  private boolean isOption(String arg, String option) {
    return ("-" + option).equals(arg) || ("--" + option).equals(arg);
  }
}
