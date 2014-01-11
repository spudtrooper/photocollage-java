package com.jeffpalm.photocollage;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class FileFinderTest {

  @Test
  public void testEmpty() {
    FileFinder finder = new FileFinder();
    assertFalse(finder.findFiles().iterator().hasNext());
  }

  @Test
  public void testFindFiles() {
    File[] paths = TestUtils.getTestImageFiles();
    
    FileFinder finder = new FileFinder();
    finder.addFile(paths[0]);
    finder.addFile(paths[1]);
    Iterator<File> files = finder.findFiles().iterator();
    
    List<File> expectedFileList = Arrays.asList(paths);
    assertTrue(expectedFileList.contains(files.next()));
    assertTrue(expectedFileList.contains(files.next()));
    assertFalse(files.hasNext());
  }
}
