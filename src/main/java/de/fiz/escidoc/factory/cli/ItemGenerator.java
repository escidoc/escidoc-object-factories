package de.fiz.escidoc.factory.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.StorageType;
import de.fiz.escidoc.factory.EscidocObjects;

public final class ItemGenerator extends Questionary implements Generator {
	static final String PROPERTY_RANDOM_NUM_FILES = "generator.item.random.num";
	static final String PROPERTY_RANDOM_DATA = "generator.item.random.data";
	static final String PROPERTY_RANDOM_SIZE_FILES = "generator.item.random.size";
	static final String PROPERTY_INPUT_DIRECTORY = "generator.item.input.directory";
	static final String PROPERTY_CONTEXT_ID = "generator.item.context.id";
	static final String PROPERTY_CONTENTMODEL_ID = "generator.item.contentmodel.id";
	static final String PROPERTY_RESULT_PATH = "generator.item.result.path";
	static final String PROPERTY_FILE_TYPES = "generator.item.input.types";
	static final String PROPERTY_STORAGE_TYPE = "generator.item.storage.type";

	private final Properties properties;
	private final Marshaller<Item> itemMarshaller = Marshaller.getMarshaller(Item.class);

	ItemGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}

	public void interactive() {
		try {
			this.questionResultFile();
			this.questionRandomData();
			this.questionContextId();
			this.questionContentModelId();
			this.questionStorageType();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void questionStorageType() throws Exception{
		StorageType storageType;
		switch(poseQuestion(Integer.class, 1, "Which Storage type should be used? [default=1]\n[1 = INTERNAL_MANAGED, 2 = EXTERNAL_MANAGED, 3 = EXTERNAL_URL] ")){
		case 2:
			storageType=StorageType.EXTERNAL_MANAGED;
			break;
		case 3:
			storageType=StorageType.EXTERNAL_URL;
			break;
		default:
			storageType=StorageType.INTERNAL_MANAGED;
		}
		properties.setProperty(PROPERTY_STORAGE_TYPE, storageType.toString());
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
			resultFile = poseQuestion(String.class, properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY)
					+ "/testdaten-i.csv", "What's the path to the result file [default="
					+ properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY) + "/testdaten-i.csv] ?");
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
			this.properties.setProperty(PROPERTY_RANDOM_SIZE_FILES, String.valueOf(size * 1024));
		} else {
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir")), "What's the location of the test files [default="
						+ System.getProperty("java.io.tmpdir") + "] ?");
			} while (!dir.exists() && !dir.canRead());
			this.properties.setProperty(PROPERTY_INPUT_DIRECTORY, dir.getAbsolutePath());
			String types=this.poseQuestion(String.class, "*", "What filetypes should be used for creating the items? Separate multiple types by commata [default=*]");
			if (types.length() == 0){
				types="*";
			}
			this.properties.setProperty(PROPERTY_FILE_TYPES, types);

		}
	}

	public List<File> generateFiles() throws IOException, ParserConfigurationException, InternalClientException {
		final List<File> files = new ArrayList<File>();
		final boolean randomData = Boolean.parseBoolean(properties.getProperty(PROPERTY_RANDOM_DATA));
		final File targetDirectory = new File(properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY));
		final String contextId = properties.getProperty(PROPERTY_CONTEXT_ID);
		final String contentModelId = properties.getProperty(PROPERTY_CONTENTMODEL_ID);
		final StorageType storageType= StorageType.valueOf(properties.getProperty(PROPERTY_STORAGE_TYPE));
		if (randomData) {
			generateRandomData(contextId, contentModelId, targetDirectory, files,storageType);
		} else {
			generateData(contextId, contentModelId, targetDirectory, files, new File(properties.getProperty(PROPERTY_INPUT_DIRECTORY)),this.properties.getProperty(PROPERTY_FILE_TYPES).split(","),storageType);
		}
		ProgressBar.printProgressBar(100, true);
		return files;
	}

	private void generateData(String contextId, String contentModelId, File targetDirectory, List<File> files,
			File inputDirectory, String[] fileTypes,StorageType storageType) throws IOException,ParserConfigurationException,InternalClientException {
		List<File> inputs = getFiles(inputDirectory, fileTypes);
		for (File input : inputs) {
			OutputStream out = null;
			try {
				File outFile = File.createTempFile("item-", ".xml", targetDirectory);
				Item item = EscidocObjects.createItem(contextId, contentModelId, Arrays.asList(EscidocObjects.createComponentFromURI("component-"
						+ UUID.randomUUID().toString(), input.getAbsolutePath(),storageType)));
				String xml = itemMarshaller.marshalDocument(item);
				out = new FileOutputStream(outFile);
				files.add(outFile);
				IOUtils.write(xml, out);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
		System.out.println(":: generated " + files.size() + " items");
	}

	private List<File> getFiles(File inputDirectory, String[] fileTypes) {
		List<File> files = new ArrayList<File>();
		for (String name : inputDirectory.list()) {
			File f = new File(inputDirectory, name);
			if (f.isFile() && f.canRead() && isOfType(f, fileTypes)) {
				files.add(f);
			} else if (f.isDirectory() && f.canRead()) {
				files.addAll(getFiles(f, fileTypes));
			}
		}
		return files;
	}

	private boolean isOfType(File f, String[] fileTypes) {
		for (String type : fileTypes) {
			if (type.equals("*")) {
				return true;
			}else if (f.getName().endsWith("." + type)) {
				return true;
			}
		}
		return false;
	}

	private void generateRandomData(String contextId, String contentModelId, File targetDirectory, List<File> files,StorageType storageType)
			throws IOException,ParserConfigurationException,InternalClientException {
		final int numFiles = Integer.parseInt(properties.getProperty(PROPERTY_RANDOM_NUM_FILES));
		int currentPercent = 0;
		int oldPercent = 0;
		long size = Long.parseLong(properties.getProperty(PROPERTY_RANDOM_SIZE_FILES));

		for (int i = 0; i < numFiles; i++) {
			Item item = EscidocObjects.createItem(contextId, contentModelId, Arrays.asList(EscidocObjects.createComponentFromRandomData(targetDirectory, size,storageType)));
			String xml = itemMarshaller.marshalDocument(item);
			FileOutputStream out = null;
			try {
				File outFile = File.createTempFile("item-", ".xml", targetDirectory);
				out = new FileOutputStream(outFile);
				files.add(outFile);
				IOUtils.write(xml, out);
				oldPercent = currentPercent;
				currentPercent = (int) ((double) i / (double) numFiles * 100d);
				if (currentPercent > oldPercent) {
					ProgressBar.printProgressBar(currentPercent);
				}
			} finally {
				out.close();
			}
		}
		final File result = new File(properties.getProperty(PROPERTY_RESULT_PATH));
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(result, false);
			for (File f : files) {
				out.write(new String("testdaten/daten/" + f.getName() + "," + f.getName() + ",text/xml\n").getBytes("UTF-8"));
				out.flush();
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
}
