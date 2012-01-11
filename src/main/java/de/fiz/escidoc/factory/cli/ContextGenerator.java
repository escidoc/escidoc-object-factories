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
import de.escidoc.core.resources.om.context.Context;
import de.escidoc.core.resources.om.context.ContextProperties;

public class ContextGenerator extends Questionary{
	private static final String PROPERTY_NUMFILES="generator.context.num";
	private static final String PROPERTY_TARGET_DIRECTORY="generator.context.target.directory";

	private final Properties properties;
	private final Marshaller<Context> marshaller=Marshaller.getMarshaller(Context.class);
	
	ContextGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}
	
	List<File> generateContexts() throws Exception{
		final List<File> result=new ArrayList<File>();
		final int numFiles=Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory=new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		for (int i=0;i<numFiles;i++){
			final ContextProperties cp=new ContextProperties.Builder("context-" + UUID.randomUUID().toString(), PublicStatus.PENDING)
				.build();
			final Context ctx=new Context();
			ctx.setProperties(cp);
			final File xmlFile=File.createTempFile("context-", ".xml", targetDirectory);
			final String xml=marshaller.marshalDocument(ctx);
			OutputStream out=null;
			try{
				out=new FileOutputStream(xmlFile);
				IOUtils.write(xml,out);
			}finally{
				IOUtils.closeQuietly(out);
			}
		}
		return result;
	}

	void interactive() {
		try{
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many Contexts should be created [default=10] ? ")));
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the context xml files be written to [default=" + System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
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
