# Overview

A Java library for creating photo collages from individual images. A example is here:

  http://jeffpalm.com/sarah
  
# Building

To build:

To build a runnable jar in `target/photocollage-VERSION-runnable.jar`:

<pre>
mvn assembly:assembly
</pre>

Then you can run the main class with:
<pre>
java -jar target/photocollage-VERSION-runnable.jar
</pre>

# Running

The main class is *com.jeffpalm.photocollage.PhotoCollageMain*, and its usage is

<pre>
java com.jeffpalm.photocollage.PhotoCollageMain inputImage sourceImage+ [option+]
</pre>

where options include:

<pre>
--help              Print the help message
--outdir dir        Output images to dir
--rows num          Split up the output into num rows (Defaults to 1)
--cols num          Split up the output into num columns (Defaults to 1)
--width num         Resize the input image to num (Defaults to 200px)
--smallwidth num    Use images of width num for the pixels (Defaults to 50px)
--smallheight num   Use images of height num for the pixels (Defaults to 50px)
</pre>

This will read *inputImage* and create an output image (whose 
name depends on the options) form the input images *sourceImage*s.

By default, a single output image is created. If you want to split the output into multiple 
images use the *--rows* or *--cols* option.