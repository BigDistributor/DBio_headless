package net.preibisch.dbio_headless.readimg;

import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.Illumination;
import mpicbg.spim.data.sequence.ImgLoader;
import mpicbg.spim.data.sequence.Tile;
import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.io.img.load.LoadXML;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;

public class ReadXML {
	private static final String xmlData = "/Users/Marwan/Desktop/Task/example_dataset/dataset.xml";

	public static void main(String[] args) throws Exception {
		new ImageJ();
		IOFunctions.printIJLog = true;

		final LoadXML load = new LoadXML(xmlData);

		// select all views to process
		final List< ViewId > viewIds = new ArrayList< ViewId >();
		viewIds.addAll( load.getSpimData().getSequenceDescription().getViewDescriptions().values() );

		for ( final ViewId viewId : viewIds )
		{
			final ViewDescription vd = load.getSpimData().getSequenceDescription().getViewDescription( viewId );
			
			final int tpId = vd.getTimePointId();
			final int setupId = vd.getViewSetupId();
			
			final Channel channel = vd.getViewSetup().getChannel();
			final Illumination illum = vd.getViewSetup().getIllumination();
			final Tile tile = vd.getViewSetup().getTile();
			final Angle angle = vd.getViewSetup().getAngle();

			ImgLoader loader = load.getSpimData().getSequenceDescription().getImgLoader();

			// this is a 3d image stack - for 1 specific illum, tile, angle & channel
			RandomAccessibleInterval img = loader.getSetupImgLoader( viewId.getViewSetupId() ).getImage( viewId.getTimePointId() );
		}

		// all defined a bounding boxes
		for ( final BoundingBox bb : load.getSpimData().getBoundingBoxes().getBoundingBoxes() )
			IOFunctions.println( bb );
		

		// now the bounding box, I put quite a lot of options here of how to get one, you can play around with it
		Interval bb = load.getSpimData().getBoundingBoxes().getBoundingBoxes().get( 0 );

		// select a bounding box
		//bb = load.spimData.getBoundingBoxes().getBoundingBoxes().get( 0 );

		// bounding box around everything
		//bb = new BoundingBoxMaximal( viewIds, load.spimData ).estimate( "Full Bounding Box" );

		// bounding box with BDV
		//bb = new BoundingBoxBigDataViewer( load.spimData, viewIds ).estimate( "BDV bounding box" );

		// or just define manually (261, 332, 5) >>> (1057, 773, 387)
//		bb = new FinalInterval( new long[]{ 261, 332, 5 }, new long[]{ 1057, 773, 387 });

		// get and display
		final RandomAccessibleInterval< FloatType > fused = load.fuse(load.getSpimData(), viewIds, bb );

		final ImagePlus imp = ImageJFunctions.show( fused );
//		imp.setDimensions( 1, (int)fused.dimension( 2 ), 1 );
//		imp.setSlice( (int)fused.dimension( 2 )/2 );
		imp.setDisplayRange( 0, 255 );
	}
}
