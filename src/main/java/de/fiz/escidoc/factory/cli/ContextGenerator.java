package de.fiz.escidoc.factory.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.common.reference.OrganizationalUnitRef;
import de.escidoc.core.resources.om.context.AdminDescriptor;
import de.escidoc.core.resources.om.context.AdminDescriptors;
import de.escidoc.core.resources.om.context.Context;
import de.escidoc.core.resources.om.context.ContextProperties;
import de.escidoc.core.resources.om.context.OrganizationalUnitRefs;

public class ContextGenerator extends Questionary implements Generator {
	private static final String PROPERTY_NUMFILES = "generator.context.num";
	private static final String PROPERTY_ORGANIZATIONAL_UNIT_ID = "generator.context.ou.id";
	private static final String PROPERTY_RESULT_PATH = "generator.context.result.path";

	private final Properties properties;
	private final Marshaller<Context> marshaller = Marshaller.getMarshaller(Context.class);

	ContextGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}

	public List<File> generateFiles() throws Exception {
		final List<File> result = new ArrayList<File>();
		final int numFiles = Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory = new File(properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY));
		int oldPercent, currentPercent = 0;
		for (int i = 0; i < numFiles; i++) {
			final OrganizationalUnitRefs ouRefs = new OrganizationalUnitRefs();
			ouRefs.add(new OrganizationalUnitRef(properties.getProperty(PROPERTY_ORGANIZATIONAL_UNIT_ID)));
			final AdminDescriptor desc = new AdminDescriptor("admin");
			desc.setContent("<void />");
			final AdminDescriptors adms = new AdminDescriptors();
			adms.add(desc);
			final ContextProperties cp = new ContextProperties();
			cp.setName("context-" + UUID.randomUUID().toString());
			cp.setPublicStatus(PublicStatus.PENDING);
			cp.setType("type1");
			cp.setOrganizationalUnitRefs(ouRefs);
			cp.setDescription("test-description");
			final Context ctx = new Context();
			ctx.setProperties(cp);
			ctx.setAdminDescriptors(adms);
			final File xmlFile = File.createTempFile("context-", ".xml", targetDirectory);
			final String xml = marshaller.marshalDocument(ctx);
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
				out.write(new String("testdaten/daten/" + f.getName() + "," + f.getName() + ",text/xml\n")
						.getBytes("UTF-8"));
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
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10,
					"How many Contexts should be created [default=10] ? ")));
			properties.setProperty(PROPERTY_ORGANIZATIONAL_UNIT_ID, String.valueOf(poseQuestion(String.class, "",
					"What's the id of the organizational unit to associate the context with? ")));
			String resultFile;
			do {
				resultFile = poseQuestion(String.class,
						properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY)
								+ "/testdaten-ctx.csv", "What's the path to the result file [default="
								+ properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY)
								+ "/testdaten-ctx.csv] ?");
			} while (resultFile.length() == 0);
			properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
