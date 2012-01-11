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
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.OrganizationalUnitProperties;

public class OrganizationalUnitGenerator extends Questionary{
	private static final String PROPERTY_NUMFILES="generator.organizationalunit.num";
	private static final String PROPERTY_TARGET_DIRECTORY="generator.organizationalunit.target.directory";

	private final Properties properties;
	private final Marshaller<OrganizationalUnit> marshaller=Marshaller.getMarshaller(OrganizationalUnit.class);
	
	public OrganizationalUnitGenerator(final Properties properties) {
		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
		this.properties = properties;
	}
	
	List<File> generateOrganizationalUnit() throws Exception{
		final List<File> result=new ArrayList<File>();
		final int numFiles=Integer.parseInt(properties.getProperty(PROPERTY_NUMFILES));
		final File targetDirectory=new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		for (int i=0;i<numFiles;i++){
			final OrganizationalUnitProperties op=new OrganizationalUnitProperties.Builder("ou-" + UUID.randomUUID().toString(), PublicStatus.PENDING).build();
			final OrganizationalUnit ou=new OrganizationalUnit.Builder(op).build();
			final File xmlFile=File.createTempFile("ou-", ".xml", targetDirectory);
			final String xml=marshaller.marshalDocument(ou);
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
			properties.setProperty(PROPERTY_NUMFILES, String.valueOf(poseQuestion(Integer.class, 10, "How many organizational units should be created [default=10] ? ")));
			File dir;
			do {
				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir") + "/escidoc-test"), "Where should the organizational unit xml files be written to [default=" + System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
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
