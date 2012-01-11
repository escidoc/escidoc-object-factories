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
import de.escidoc.core.resources.cmm.ContentModel;
import de.escidoc.core.resources.cmm.ContentModelProperties;

public class ContentModelGenerator extends Questionary implements Generator{
	private static final String PROPERTY_NUMFILES="generator.contentmodel.num";
	private static final String PROPERTY_TARGET_DIRECTORY="generator.contentmodel.target.directory";
	private static final String PROPERTY_RESULT_PATH = "generator.contentmodel.result.path";


	private final Properties properties;
	private final Marshaller<ContentModel> marshaller=Marshaller.getMarshaller(ContentModel.class);
	
	ContentModelGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}
	
	public List<File> generateFiles() throws IOException,ParserConfigurationException,InternalClientException{
		final List<File> result=new ArrayList<File>();
		final int numFiles=Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory=new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		int oldPercent,currentPercent=0;
		for (int i=0;i<numFiles;i++){
			final ContentModel model=new ContentModel();
			final ContentModelProperties cp=new ContentModelProperties();
			cp.setName("contentmodel-" + UUID.randomUUID().toString());
			model.setProperties(cp);
			final File xmlFile=File.createTempFile("contentmodel-", ".xml", targetDirectory);
			final String xml=marshaller.marshalDocument(model);
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
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many content models should be created [default=10] ? ")));
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the content model xml files be written to [default=" + System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
				if (!dir.exists()){
					if (poseQuestion(Boolean.class, true, "Create directory " + dir.getAbsolutePath() + " [default=yes] ?")){
						dir.mkdir();
					}
				}
			} while (!dir.exists() && !dir.canWrite());
			this.properties.setProperty(PROPERTY_TARGET_DIRECTORY, dir.getAbsolutePath());
			String resultFile;
			do {
				resultFile = poseQuestion(String.class, properties.getProperty(PROPERTY_TARGET_DIRECTORY) + "/testdaten-cm.csv", "What's the path to the result file [default="
						+ properties.getProperty(PROPERTY_TARGET_DIRECTORY) + "/testdaten-cm.csv] ?");
			} while (resultFile.length() == 0);
			properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
