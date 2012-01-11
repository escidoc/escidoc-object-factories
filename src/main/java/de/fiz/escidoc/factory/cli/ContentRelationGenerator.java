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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;

import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.om.contentRelation.ContentRelation;
import de.escidoc.core.resources.om.contentRelation.ContentRelationProperties;

public class ContentRelationGenerator extends Questionary implements Generator{ 
	private static final String PROPERTY_NUMFILES="generator.contentrelation.num";
	private static final String PROPERTY_TARGET_DIRECTORY="generator.contentrelation.target.directory";

	private final Properties properties;
	private final Marshaller<ContentRelation> marshaller=Marshaller.getMarshaller(ContentRelation.class);
	
	ContentRelationGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}
	
	public List<File> generateFiles() throws IOException,ParserConfigurationException,InternalClientException{
		final List<File> result=new ArrayList<File>();
		final int numFiles=Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory=new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		int oldPercent,currentPercent=0;
		for (int i=0;i<numFiles;i++){
			final ContentRelationProperties cp=new ContentRelationProperties();
			final ContentRelation rel=new ContentRelation.Builder(cp).build();
			final File xmlFile=File.createTempFile("contentrelation-", ".xml", targetDirectory);
			final String xml=marshaller.marshalDocument(rel);
			OutputStream out=null;
			try{
				out=new FileOutputStream(xmlFile);
				IOUtils.write(xml,out);
			}finally{
				IOUtils.closeQuietly(out);
			}
			oldPercent=currentPercent;
			currentPercent=(int) ((double)i/(double)numFiles * 100d);
			if (currentPercent > oldPercent){
				ProgressBar.printProgressBar(currentPercent);
			}
		}
		ProgressBar.printProgressBar(100,true);
		return result;
	}

	public void interactive() {
		try{
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many content relations should be created [default=10] ? ")));
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the content relation xml files be written to [default=" + System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
				if (!dir.exists()){
					if (poseQuestion(Boolean.class, true, "Create directory " + dir.getAbsolutePath() + " [default=yes] ?")){
						dir.mkdir();
					}
				}
			} while (!dir.exists() && !dir.canWrite());
			this.properties.setProperty(PROPERTY_TARGET_DIRECTORY, dir.getAbsolutePath());
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
