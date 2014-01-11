package com.jeffpalm.photocollage;

import java.io.PrintStream;

import com.jeffpalm.builder.GenericBuilder;

public final class GenerateHtmlIndex {

  private String baseName;
  private int numCols, numRows;

  public interface Builder extends com.jeffpalm.builder.Builder<GenerateHtmlIndex> {
    Builder setBaseName(String baseName);
    Builder setNumRows(int numRows);
    Builder setNumCols(int numCols);
  }

  public static Builder newBuilder() {
    return new GenericBuilder<Builder>(new GenerateHtmlIndex(), Builder.class).asBuilder();
  }

  /** Outputs but does not close the stream. */
  public void output(PrintStream out) {
    out.println("<html>");
    out.println("<head>");
    out.println("<style>");
    out.println("img { margin:0; padding:0}");
    out.println("body {white-space:nowrap;}");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        String imageName = baseName + "-" + row + "-" + col + ".jpg";
        out.print("<img src='" + imageName + "' />");
      }
      out.println("<br/>");
    }
    out.println("</body>");
    out.println("</html>");
    out.flush();
  }

  public void output() {
    output(System.out);
  }
}
