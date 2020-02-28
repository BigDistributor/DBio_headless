package net.preibisch.dbio_headless.readimg;

import java.io.File;

import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.io.img.load.LoadTIFF;

public class ReadTiff {
	private static final String input2d = "/Users/Marwan/Desktop/Task/example_dataset/DrosophilaWing.tif";
	private static final String input3d = "/Users/Marwan/Desktop/Task/example_dataset/affine.tif";

	public static void main(String[] args) throws Exception {

		new ImageJ();
		File file = new File(input3d);
		if (!file.isFile())
			throw new Exception("Invalid Input !");

		RandomAccessibleInterval<FloatType> image = LoadTIFF.load(file);
		final ImagePlus imp = ImageJFunctions.show(image);
		imp.setDisplayRange( 0, 255 );
	}
}





