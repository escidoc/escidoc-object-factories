package de.fiz.escidoc.factory.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.common.reference.Reference;
import de.escidoc.core.resources.om.contentRelation.ContentRelation;
import de.escidoc.core.resources.om.contentRelation.ContentRelationProperties;

public class ContentRelationGenerator extends Questionary implements Generator{ 
	private static final String PROPERTY_NUMFILES="generator.contentrelation.num";
	private static final String PROPERTY_RESULT_PATH = "generator.contentrelation.result.path";
	private static final String PROPERTY_SUBJECT_ID = "generator.contentrelation.subject.id";

	private final Properties properties;
	private final Marshaller<ContentRelation> marshaller=Marshaller.getMarshaller(ContentRelation.class);
	
	ContentRelationGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}
	
	public List<File> generateFiles() throws IOException,ParserConfigurationException,InternalClientException{
		final List<File> result=new ArrayList<File>();
		final int numFiles=Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory=new File(properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY));
		int oldPercent,currentPercent=0;
		for (int i=0;i<numFiles;i++){
			final ContentRelationProperties cp=new ContentRelationProperties();
			cp.setDescription("test");
			final ContentRelation rel=new ContentRelation.Builder(cp)
				.type(URI.create("http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isConstituentOf"))
				.subject(new ContextRef(properties.getProperty(PROPERTY_SUBJECT_ID)))
				.object(new ContextRef(properties.getProperty(PROPERTY_SUBJECT_ID)))
				.build();
			final File xmlFile=File.createTempFile("contentrelation-", ".xml", targetDirectory);
			final String xml=marshaller.marshalDocument(rel);
			OutputStream out=null;
			try{
				out=new FileOutputStream(xmlFile);
				IOUtils.write(xml,out);
				result.add(xmlFile);
			}finally{
				IOUtils.closeQuietly(out);
			}
			oldPercent=currentPercent;
			currentPercent=(int) ((double)i/(double)numFiles * 100d);
			if (currentPercent > oldPercent){
				ProgressBar.printProgressBar(currentPercent);
			}
		}
		final File resultFile = new File(properties.getProperty(PROPERTY_RESULT_PATH));
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(resultFile, false);
			for (File f : result) {
				out.write(new String(f.getAbsolutePath() + "," + f.getName() + ",text/xml\n").getBytes("UTF-8"));
				out.flush();
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
		ProgressBar.printProgressBar(100,true);
		return result;
	}

	public void interactive() {
		try{
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many content relations should be created [default=10] ? ")));
			properties.setProperty(PROPERTY_SUBJECT_ID, poseQuestion(String.class, "", "What's the context's id for the relations ? "));
			String resultFile;
			do {
				resultFile = poseQuestion(String.class, properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY) + "/testdaten-cr.csv", "What's the path to the result file [default="
						+ properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY) + "/testdaten-cr.csv] ?");
			} while (resultFile.length() == 0);
			properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
