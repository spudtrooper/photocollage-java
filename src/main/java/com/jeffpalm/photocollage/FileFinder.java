package com.jeffpalm.photocollage;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

final class FileFinder {

  private final static Set<String> IMAGE_FILE_NAME_EXTENSIONS = new HashSet<String>();
  static {
    IMAGE_FILE_NAME_EXTENSIONS.add("jpg");
    IMAGE_FILE_NAME_EXTENSIONS.add("png");
  }

  private final List<File> files = new ArrayList<File>();
  private final List<File> directories = new ArrayList<File>();

  public boolean addFile(File f) {
    if (!f.exists()) {
      return false;
    }
    return (f.isDirectory() ? directories : files).add(f);
  }

  public Collection<File> findFiles() {
    List<File> foundFiles = new ArrayList<File>(files);
    List<File> q = new LinkedList<File>(directories);
    while (!q.isEmpty()) {
      File dir = q.remove(0);
      File[] fs = dir.listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isDirectory() || isImage(pathname);
        }
      });
      for (File f : fs) {
        (f.isDirectory() ? q : foundFiles).add(f);
      }
    }
    return foundFiles;
  }

  private boolean isImage(File f) {
    String name = f.getName();
    int ilastDot = name.lastIndexOf(".");
    if (ilastDot == -1) {
      return false;
    }
    String ext = name.substring(ilastDot + 1).toLowerCase();
    return IMAGE_FILE_NAME_EXTENSIONS.contains(ext);
  }
}
