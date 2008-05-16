package translator.xcsp2ep;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import translator.TranslationSettings;
import translator.xcsp2ep.mapper.Mapper;
import translator.xcsp2ep.mapper.MapperException;
import translator.xcsp2ep.parser.InstanceParser;
import translator.xcsp2ep.parser.XCSPInstance;
import translator.xcsp2ep.mapper.EssencePModel;
import translator.normaliser.NormalisedModel;


/**
 * XCSP2EP is a converter that translates XCSP instances into Essence'
 * instances. It uses the XCSP parser provided by Christophe Lecoutre 
 * which is under the X11 license. 
 * 
 * @author andrea
 *
 */

public class Xcsp2Ep{

	// the file we will write the Essence' instance into
	private static String OUTPUT_FILE_NAME;
	private static boolean WRITE_TIME_STATS;
	private static boolean PRINT_INFO;
	TranslationSettings settings;
	
	public Xcsp2Ep(TranslationSettings settings) {
		WRITE_TIME_STATS = settings.giveTranslationTimeInfo();
		PRINT_INFO = settings.giveTranslationInfo();
		OUTPUT_FILE_NAME = settings.getEssencePrimeOutputFileName();
		this.settings = settings;
	}
	
	public Xcsp2Ep() {
		this.settings = new TranslationSettings();
		WRITE_TIME_STATS = true;
		PRINT_INFO = true;
		OUTPUT_FILE_NAME = this.settings.getEssencePrimeOutputFileName();
	}
	
	/**
	 * Translate the XCSP instance in 'inputFile' into Essence'
	 * and write it into file 'outputFile'
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws MapperException
	 * @throws Exception
	 */
	public static void translateXCSP(String inputFile, String outputFile) 
		throws MapperException, Exception {
		
		writeStartMessage(inputFile);
		long startTime = System.currentTimeMillis();
		
		// parse XCSP instance
		boolean displayParsedInstance = false;
		InstanceParser parser = new InstanceParser();

		parser.loadInstance(inputFile);	
		XCSPInstance xcspInstance = parser.parse(displayParsedInstance);
	
		// map the XCSP syntax tree to Essence
		Mapper mapper = new Mapper();
		EssencePModel essencePmodel = mapper.mapToEssencePrime(xcspInstance);
		writeStringIntoFile(outputFile, essencePmodel.toString());
		long stopTime = System.currentTimeMillis();
		
		double translationTime = (stopTime - startTime) / 1000.0;
		
		writeEndMessage(outputFile, translationTime);
	}
	
	
	public static String translateXCSP(String fileName) 
		throws MapperException, Exception {
		
		writeStartMessage(fileName);
		long startTime = System.currentTimeMillis();
		
		// parse the input file
		boolean displayParsedInstance = false;
		InstanceParser parser = new InstanceParser();
		parser.loadInstance(fileName);

		XCSPInstance xcspInstance = parser.parse(displayParsedInstance);
	
		
		// map the XCSP syntax tree to Essence
		Mapper mapper = new Mapper();
		EssencePModel essencePmodel = mapper.mapToEssencePrime(xcspInstance);
		writeStringIntoFile(OUTPUT_FILE_NAME, essencePmodel.toString());
		long stopTime = System.currentTimeMillis();
		
		double translationTime = (stopTime - startTime) / 1000.0;
		
		writeEndMessage(OUTPUT_FILE_NAME, translationTime);
		return essencePmodel.toString();
	}
	
	
	public NormalisedModel translateToNormalisedModel(String fileName) 
		throws MapperException, Exception {
		
		boolean displayParsedInstance = false;
		long startTime = System.currentTimeMillis();
		
		InstanceParser parser = new InstanceParser();
		parser.loadInstance(fileName);
		XCSPInstance xcspInstance = parser.parse(displayParsedInstance);	
		long stopTime = System.currentTimeMillis();
		double time = (stopTime - startTime) / 1000.0;
		writeTimeInfo("XCSP parsing time: "+time+"sec");
		
		Mapper mapper = new Mapper();
		EssencePModel essencePmodel = mapper.mapToEssencePrime(xcspInstance);
		startTime = stopTime;
		stopTime = System.currentTimeMillis();
		time = (stopTime - startTime) / 1000.0;
		
		writeTimeInfo("Time for Mapping XCSP to Essence': "+time+"sec");
		if(this.settings.getWriteEssencePrimeModelIntoFile()) {
			writeStringIntoFileNonStatic(fileName+".eprime", essencePmodel.toString());
			startTime = stopTime;
			stopTime = System.currentTimeMillis();
			time = (stopTime - startTime) / 1000.0;
			writeInfo("Written Essence' model into file: "+fileName+".eprime");
			writeTimeInfo("Writing time:"+time+"sec");
		}
		
		return essencePmodel.mapToNormalisedModel();
	}
	
	
	private static void writeStringIntoFile(String fileName, String stringToWrite) 
	throws IOException {
	
		File file = new File(fileName);
		file.createNewFile();
	
		FileWriter writer = new FileWriter(file);
		writer.write(stringToWrite);
    	
		writer.flush();
		writer.close();
	
	}

	
	private void writeStringIntoFileNonStatic(String fileName, String stringToWrite) 
	throws IOException {
	
		File file = new File(fileName);
		file.createNewFile();
	
		FileWriter writer = new FileWriter(file);
		writer.write(stringToWrite);
    	
		writer.flush();
		writer.close();
	
	}

	
	public static void writeStartMessage(String inputFileName) {
		System.out.println("# Welcome to xcsp2ep v0.1"+
				"\n# A converter from Format XCSP 2.1 to Essence' 1.b.a"+
				"\n# Bug reports: andrea@cs.st-and.ac.uk"
				+"\n\nConverting XCSP file '"+inputFileName+"' to Essence' ...");
	}
	
	public static void writeEndMessage(String outputFileName, double translationTime) {
		System.out.println("Conversion successfull.\nWritten Essence' instance into file '"+outputFileName+"'.\n" +
				"Translation time: "+translationTime+" sec\n");
	}
	
	public static void writeHelpMessage() {
		
		System.out.println("Usage: xcsp2ep inputFile [outputFile] "+
				"\n\ninputfile:\tfile that contains XCSP instance"+
				"\noutputfile:\tfile where Essence' instance should be written into (optional)"+
				"\n\t\tdefault output file:"+OUTPUT_FILE_NAME+
				"\nbug reports: andrea@cs.st-and.ac.uk\n");
	}
	
	public static void main(String[] args) {
		
		try {
			
			if(args.length == 1) 
				translateXCSP(args[0]);
			
			else if(args.length == 2)
				translateXCSP(args[0], args[1]);
			
			else writeHelpMessage();
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot convert XCSP instance to Essence' instance.\n");
			writeHelpMessage();
			System.exit(1);
		}
		
	}
	
	public static void writeTimeInfo(String info) {
		if(WRITE_TIME_STATS)
			System.out.println(info);
		
	}
	
	private static void writeInfo(String info) {
		if(PRINT_INFO)
			System.out.println(info);
		
	}
	
}
