package de.fiz.escidoc.factory.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.om.item.Item;
import de.fiz.escidoc.factory.EscidocObjects;

public final class CommandlineInterface extends Questionary {
	public static final String PROPERTY_RANDOM_NUM_FILES = "generator.random.num";
	public static final String PROPERTY_RANDOM_DATA = "generator.random.data";
	public static final String PROPERTY_RANDOM_SIZE_FILES = "generator.random.size";
	public static final String PROPERTY_TARGET_DIRECTORY = "generator.target.directory";
	public static final String PROPERTY_INPUT_DIRECTORY = "generator.input.directory";
	public static final String PROPERTY_CONTEXT_ID = "generator.context.id";
	public static final String PROPERTY_CONTENTMODEL_ID = "generator.contentmodel.id";
	public static final String PROPERTY_RESULT_PATH = "generator.result.path";

	private final Properties properties;
	private final Marshaller<Item> itemMarshaller = Marshaller.getMarshaller(Item.class);

	private CommandlineInterface(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}

	private void interactive() {
		System.out.println();
		try {
			this.questionTargetDirectory();
			this.questionResultFile();
			this.questionRandomData();
			this.questionContextId();
			this.questionContentModelId();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void questionTargetDirectory() throws Exception {
		File targetDirectory;
		do {
			targetDirectory = poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the xml files be written to [default="
					+ System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
			if (!targetDirectory.exists()){
				if (poseQuestion(Boolean.class, true, "Create directory " + targetDirectory.getAbsolutePath() + " [default=yes] ?")){
					targetDirectory.mkdir();
				}
			}
		} while (!targetDirectory.exists() && !targetDirectory.canWrite());
		properties.setProperty(PROPERTY_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
	}

	private void questionContentModelId() throws Exception {
		String contentModelId;
		do {
			contentModelId = poseQuestion(String.class, "", "What's the Content Model ID to use for the items?");
		} while (contentModelId.length() == 0);
		properties.setProperty(PROPERTY_CONTENTMODEL_ID, contentModelId);
	}

	private void questionResultFile() throws Exception {
		String resultFile;
		do {
			resultFile = poseQuestion(String.class, properties.getProperty(PROPERTY_TARGET_DIRECTORY) + "/testdaten.csv", "What's the path to the result file [default="
					+ properties.getProperty(PROPERTY_TARGET_DIRECTORY) + "/testdaten.csv] ?");
		} while (resultFile.length() == 0);
		properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
	}

	private void questionContextId() throws Exception {
		String contextId;
		do {
			contextId = poseQuestion(String.class, "", "What's the Context ID to use for the items?");
		} while (contextId.length() == 0);
		properties.setProperty(PROPERTY_CONTEXT_ID, contextId);
	}

	private void questionRandomData() throws Exception {
		final boolean randomData = this.poseQuestion(Boolean.class, true, "Do you want to use random data [default=yes] ?");
		this.properties.setProperty(PROPERTY_RANDOM_DATA, String.valueOf(randomData));
		if (randomData) {
			final int numObjects = this.poseQuestion(Integer.class, 10, "How many objects should be created [default=10] ?");
			this.properties.setProperty(PROPERTY_RANDOM_NUM_FILES, String.valueOf(numObjects));
			final long size = this.poseQuestion(Long.class, 1000L, "What size in kilobytes should the random data have [default=10] ?");
			this.properties.setProperty(PROPERTY_RANDOM_SIZE_FILES, String.valueOf(size*1024));
		} else {
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir")), "What's the location of the test files [default="
						+ System.getProperty("java.io.tmpdir") + "] ?");
			} while (!dir.exists() && !dir.canRead());
			this.properties.setProperty(PROPERTY_INPUT_DIRECTORY, dir.getAbsolutePath());

		}
	}

	public static void main(String[] args) {
		final Properties props = new Properties();
		final CommandlineInterface cli = new CommandlineInterface(props);
		if (args.length == 2 && args[0].equals("-p")) {
			try {
				props.load(new FileInputStream(args[1]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			cli.interactive();
		}
		try {
			final String msg;
			if (props.getProperty(PROPERTY_RANDOM_DATA).equals("true")){
				final long size=(Long.parseLong(props.getProperty(PROPERTY_RANDOM_NUM_FILES)) * Long.parseLong(props.getProperty(PROPERTY_RANDOM_SIZE_FILES)) * 1024L);
				msg="generating " + props.getProperty(PROPERTY_RANDOM_NUM_FILES) + " random files with " + props.getProperty(PROPERTY_RANDOM_SIZE_FILES) + " kb each totaling in " + formatSize(size/1024L) + " of data";
			}else{
				msg="generating escidoc objects from " + props.getProperty(PROPERTY_INPUT_DIRECTORY);
			}
			System.out.println(msg);
			List<File> items = cli.createObjects();
			System.out.println("\n");
			props.store(System.out, "printing the setting for convienience");
			props.store(new FileOutputStream("generator.properties"), "created by escidoc-object-generator");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<File> createObjects() throws Exception {
		final List<File> files = new ArrayList<File>();
		final boolean randomData = Boolean.parseBoolean(properties.getProperty(PROPERTY_RANDOM_DATA));
		final File targetDirectory = new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		final long size = Long.parseLong(properties.getProperty(PROPERTY_RANDOM_SIZE_FILES));
		final String contextId = properties.getProperty(PROPERTY_CONTEXT_ID);
		final String contentModelId = properties.getProperty(PROPERTY_CONTENTMODEL_ID);
		System.out.println();
		if (randomData) {
			final int numFiles = Integer.parseInt(properties.getProperty(PROPERTY_RANDOM_NUM_FILES));
			int currentPercent=0;
			int oldPercent=0;
			for (int i = 0; i < numFiles; i++) {
				Item item = EscidocObjects.createItem(contextId, contentModelId, Arrays.asList(EscidocObjects.createComponentFromRandomData(targetDirectory, size)));
				String xml = itemMarshaller.marshalDocument(item);
				FileOutputStream out = null;
				try {
					File outFile = File.createTempFile("escidoc-", ".xml", targetDirectory);
					out = new FileOutputStream(outFile);
					files.add(outFile);
					IOUtils.write(xml, out);
					oldPercent=currentPercent;
					currentPercent=(int) ((double)i/(double)numFiles * 100d);
					if (currentPercent > oldPercent){
						printProgressBar(currentPercent);
					}
				} finally {
					out.close();
				}
				printProgressBar(100);
			}
			File result = new File(properties.getProperty(PROPERTY_RESULT_PATH));
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(result, false);
				for (File f : files) {
					out.write(new String(f.getAbsolutePath() + "," + f.getName() + ",text/xml\n").getBytes("UTF-8"));
					out.flush();
				}
			} finally {
				IOUtils.closeQuietly(out);
			}
		} else {

		}
		return files;
	}
	
	public static void printProgressBar(int percent){
	    StringBuilder bar = new StringBuilder("[");

	    for(int i = 0; i < 50; i++){
	        if( i < (percent/2)){
	            bar.append("=");
	        }else if( i == (percent/2)){
	            bar.append(">");
	        }else{
	            bar.append(" ");
	        }
	    }

	    bar.append("]   " + percent + "%     ");
	    System.out.print("\r" + bar.toString());
	}
	
	private static final String formatSize(long size){
		    if(size <= 0) return "0";
		    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
