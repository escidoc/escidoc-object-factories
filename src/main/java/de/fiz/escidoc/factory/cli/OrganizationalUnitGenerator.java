package de.fiz.escidoc.factory.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.OrganizationalUnitProperties;
import de.fiz.escidoc.factory.EscidocObjects;

public class OrganizationalUnitGenerator extends Questionary implements Generator {
	private static final String PROPERTY_NUMFILES = "generator.organizationalunit.num";
	private static final String PROPERTY_TARGET_DIRECTORY = "generator.organizationalunit.target.directory";
	private static final String PROPERTY_RESULT_PATH = "generator.organizationalunit.result.path";

	private final Properties properties;
	private final Marshaller<OrganizationalUnit> marshaller = Marshaller.getMarshaller(OrganizationalUnit.class);

	public OrganizationalUnitGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}

	public List<File> generateFiles() throws IOException, ParserConfigurationException, InternalClientException {
		final List<File> result = new ArrayList<File>();
		final int numFiles = Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory = new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		int oldPercent, currentPercent = 0;
		for (int i = 0; i < numFiles; i++) {
			final OrganizationalUnitProperties op = new OrganizationalUnitProperties.Builder("ou-"
					+ UUID.randomUUID().toString(), PublicStatus.PENDING).build();
			final OrganizationalUnit ou = new OrganizationalUnit.Builder(op).build();
			ou.setMetadataRecords(EscidocObjects.createMetadataRecords("test-ou","ou"));
			final File xmlFile = File.createTempFile("ou-", ".xml", targetDirectory);
			final String xml = marshaller.marshalDocument(ou);
			OutputStream out = null;
			try {
				out = new FileOutputStream(xmlFile);
				IOUtils.write(xml, out);
				result.add(xmlFile);
			} finally {
				IOUtils.closeQuietly(out);
			}
			oldPercent = currentPercent;
			currentPercent = (int) ((double) i / (double) numFiles * 100d);
			if (currentPercent > oldPercent) {
				ProgressBar.printProgressBar(currentPercent);
			}
		}
		final File resultFile = new File(properties.getProperty(PROPERTY_RESULT_PATH));
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(resultFile, false);
			for (File f : result) {
				out.write(new String("testdaten/daten/" + f.getName()  + "," + f.getName() + ",text/xml\n").getBytes("UTF-8"));
				out.flush();
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
		ProgressBar.printProgressBar(100, true);
		return result;
	}

	public void interactive() {
		try {
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many organizational units should be created [default=10] ? ")));
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the organizational unit xml files be written to [default="
						+ System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
				if (!dir.exists()) {
					if (poseQuestion(Boolean.class, true, "Create directory " + dir.getAbsolutePath()
							+ " [default=yes] ?")) {
						dir.mkdir();
					}
				}
			} while (!dir.exists() && !dir.canWrite());
			this.properties.setProperty(PROPERTY_TARGET_DIRECTORY, dir.getAbsolutePath());
			String resultFile;
			do {
				resultFile = poseQuestion(String.class, properties.getProperty(PROPERTY_TARGET_DIRECTORY)
						+ "/testdaten-ou.csv", "What's the path to the result file [default="
						+ properties.getProperty(PROPERTY_TARGET_DIRECTORY) + "/testdaten-ou.csv] ?");
			} while (resultFile.length() == 0);
			properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
