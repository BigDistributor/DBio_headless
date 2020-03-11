package net.preibisch.dbio_headless;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfo;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfoGenerator;
import net.preibisch.distribution.algorithm.blockmanagement.io.BlockFileManager;
import net.preibisch.distribution.algorithm.controllers.items.Job;
import net.preibisch.distribution.algorithm.controllers.items.Metadata;
import net.preibisch.distribution.algorithm.errorhandler.logmanager.MyLogger;
import net.preibisch.distribution.algorithm.multithreading.Threads;
import net.preibisch.distribution.io.DataExtension;
import net.preibisch.distribution.io.img.load.LoadTIFF;
import net.preibisch.distribution.io.img.n5.N5File;
import net.preibisch.distribution.io.img.xml.XMLFile;
import net.preibisch.distribution.tools.helpers.ArrayHelpers;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;

public class TotalPipeline {
	static String inputFilePath = "/Users/Marwan/Desktop/Task/example_dataset/3d/dataset.xml";
	static String outputN5Path = "/Users/Marwan/Desktop/Task/example_dataset/3d/output.n5";
	static String metadataPath = "/Users/Marwan/Desktop/Task/example_dataset/3d/metadata.json";
	static String tiffFolderPath = "/Users/Marwan/Desktop/Task/example_dataset/3d/output/";

	public static void main(String[] args) throws SpimDataException, IOException {
		generateMetadata(inputFilePath, outputN5Path, metadataPath);
		splittoTiffs(inputFilePath, tiffFolderPath, metadataPath);
		mergeToN5(metadataPath, tiffFolderPath, outputN5Path);
		
	}

	public static void generateMetadata(String inputPath, String outputPath, String metadataPath)
			throws SpimDataException, IOException {
		XMLFile<FloatType> inputData = XMLFile.XMLFile(inputPath);
		long[] blocksizes = ArrayHelpers.fill(BasicBlockInfoGenerator.BLOCK_SIZE, inputData.getDims().length);
		Map<Integer, BasicBlockInfo> blocks = BasicBlockInfoGenerator.divideIntoBlockInfo(inputData.bb());
		MyLogger.log().info("Total blocks: " + blocks.size());

		Metadata md = new Metadata(Job.get().getId(), inputPath, outputPath, new BoundingBox(inputData.bb()),
				blocksizes, blocks);
		MyLogger.log().info(md.toString());
		md.toJson(metadataPath);
	}

	public static void splittoTiffs(String inputPath, String outputTiffPath, String metadataPath)
			throws SpimDataException, IOException {
		XMLFile<FloatType> inputData = XMLFile.XMLFile(inputPath);
		Metadata md = Metadata.fromJson(metadataPath);
		MyLogger.log().info(md.toString());
		MyLogger.log().info("Start process.. ");
		MyLogger.log().info("Block Generated.. ");
		ExecutorService service = Threads.createExService(Threads.numThreads());
		File file = new File(outputTiffPath);
		file.mkdir();
		BlockFileManager.saveAllBlocks(service, inputData.getImg(), md.getBlocksInfo(), outputTiffPath, DataExtension.TIF);
		MyLogger.log().info("Done !");
	}

	public static void mergeToN5(String metadataPath, String tiffFolderPath, String outputn5Path) throws IOException {

		Metadata md = Metadata.fromJson(metadataPath);
		long[] dims = md.getBb().getDimensions(1);

		N5File n5 = createOuptut(dims, outputn5Path);

		Map<Integer, BasicBlockInfo> blocks = md.getBlocksInfo();
		File folder = new File(tiffFolderPath);
		for (File f : folder.listFiles()) {
			if (DataExtension.fromURI(f.getName()) == DataExtension.TIF) {
				Integer x = Integer.valueOf(DataExtension.removeExtension(f.getName())) ;
				// File file = new File(outputPath, DataExtension.TIF.file(String.valueOf(x)));
				MyLogger.log().info("Image : " + f.getAbsolutePath());

				RandomAccessibleInterval<FloatType> blockImage = LoadTIFF.load(f);
				long[] gridOffset = blocks.get(x).getGridOffset();
				n5.saveBlock(blockImage, gridOffset);

				MyLogger.log().info("Block " + x + " saved in: " + Util.printCoordinates(gridOffset));
			}
		}
		n5.show("Result");

	}

	private static N5File createOuptut(long[] dims, String outputPath) throws IOException {
		N5File outputFile = new N5File(outputPath, dims);
		outputFile.create();
		MyLogger.log().info("N5 Generated");
		return outputFile;
	}

}
