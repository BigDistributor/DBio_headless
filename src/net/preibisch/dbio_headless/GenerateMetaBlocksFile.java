package net.preibisch.dbio_headless;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfo;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfoGenerator;
import net.preibisch.distribution.algorithm.controllers.items.Job;
import net.preibisch.distribution.algorithm.controllers.items.Metadata;
import net.preibisch.distribution.io.img.XMLFile;
import net.preibisch.distribution.tools.helpers.ArrayHelpers;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class GenerateMetaBlocksFile implements Callable<Void> {

	@Option(names = { "-d", "--data" }, required = true, description = "The path of the Data")
	private String dataPath;

	@Option(names = { "-s", "--blockSize" }, required = true, description = "The size of the expected blocks en pixels")
	private Integer blockSize;
	
	@Option(names = { "-o", "--overlap" }, required = true, description = "The size of overlap between blocks")
	private int overlap;

	@Option(names = { "-path" }, required = true, description = "The path of the output file")
	private String outpath;

	public static void main(String[] args) {
		CommandLine.call(new GenerateMetaBlocksFile(), args);
		System.exit(0);
	}

	@Override
	public Void call() throws IncompatibleTypeException, SpimDataException, IOException {

		XMLFile<FloatType> inputData = XMLFile.XMLFile(dataPath);
		long[] blocksizes = ArrayHelpers.fill(BasicBlockInfoGenerator.BLOCK_SIZE, inputData.getDims().length);
		Map<Integer, BasicBlockInfo> blocks = BasicBlockInfoGenerator.divideIntoBlockInfo(inputData.bb());

		Metadata md = new Metadata(Job.get().getId(),blocks, blocksizes, dataPath ,blocks.size());
		md.toJson(new File(outpath));
		return null;
	}


}
