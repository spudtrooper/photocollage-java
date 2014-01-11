package com.jeffpalm.photocollage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

final class HtmlOutput implements Output {

  private File outFile;
  private PrintStream out;

  public Output open(File imageOutFile, int newWidth, int newHeight) throws IOException {
    outFile = new File(imageOutFile.getParentFile(), imageOutFile.getName() + ".html");
    Log.getLog().info("Creating new image of size " + newWidth + "x" + newHeight + "...");
    out = new PrintStream(new FileOutputStream(outFile));
    out.println("<html>");
    out.println("<body>");
    out.println("<div style='width:" + newWidth + "'>");
    return this;
  }

  public void write(File bufImageFile, int row, int col) throws IOException {
    if (col == 0) {
      out.println("<br/>");
    }
    out.println("<img src='" + bufImageFile.getAbsolutePath() + "'/>");
  }

  public void close() throws IOException {
    out.println("</div>");
    out.println("</body>");
    out.println("</html>");
    out.flush();
    out.close();
    Log.getLog().info("Writing to " + outFile + "...");
  }
}
