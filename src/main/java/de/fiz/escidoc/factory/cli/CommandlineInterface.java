package de.fiz.escidoc.factory.cli;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

public class CommandlineInterface {
	private static final String PROPERTY_VALIDITY="properties.valid";
	
	private static void printUsage(){
		StringBuilder helpBuilder=new StringBuilder();
		helpBuilder.append("Escidoc objects generator\n")
			.append("Create Escidoc XML files for infrastructure testing\n\n")
			.append("Usage:\n")
			.append("java -jar escidoc-objects-gen.jar OPTIONS [-p path to properties]\n\n")
			.append("OPTIONS:\n")
			.append("-h\tprint this help and exit\n")
			.append("-i\tgenerate items\n")
			.append("-c\tgenerate contexts\n")
			.append("-m\tgenerate content models\n")
			.append("-r\tgenerate content relations\n")
			.append("-o\tgenerate organizational unit\n\n")
			.append("The settings will be saved after each run and can be supplied by the -p switch. If -p is ommitted the program will enter interactive mode\n");
		System.out.println(helpBuilder.toString());
	}
	
	public static void main(String[] args) {
		final Properties properties=new Properties();
		final Getopt opt=new Getopt("Escidoc objects generator", args, "hicmrop:");
		boolean items=false,context=false,contentModel=false,contentRelation=false,organizationalUnit=false;
		if (args.length == 0){
			printUsage();
			return;
		}
		int option;
		while ((option=opt.getopt()) != -1){
			switch(option){
			case 'h':
				printUsage();
				return;
			case 'i':
				items=true;
				break;
			case 'c':
				context=true;
				break;
			case 'm':
				contentModel=true;
				break;
			case 'r':
				contentRelation=true;
				break;
			case 'o':
				organizationalUnit=true;
				break;
			case 'p':
				String path=opt.getOptarg();
				try{
					properties.load(new FileInputStream(path));
					properties.setProperty(PROPERTY_VALIDITY, "true");
				}catch(IOException e){
					System.err.println("Unable to load properties file from " + path);
					return;
				}
				break;
			default:
				printUsage();
				return;
			}
		}
		
		if (items){
			generateItems(properties);
		}
		if (context){
			generateContexts(properties);
		}
		if (contentModel){
			generateContentModels(properties);
		}
		if (contentRelation){
			generateContentRelations(properties);
		}
		if (organizationalUnit){
			generateOrganziationalUnits(properties);
		}
		try{
			properties.store(System.out, "printing the setting for convienience");
			final File propFile=new File("generator.properties");
			properties.store(new FileOutputStream(propFile), "created by escidoc-object-generator");
			System.out.println("saved properties to " + propFile.getAbsolutePath());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static void generateOrganziationalUnits(Properties properties) {
		final OrganizationalUnitGenerator gen=new OrganizationalUnitGenerator(properties);
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			gen.interactive();
		}
		try {
			List<File> ous=gen.generateOrganizationalUnit();
			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateContentRelations(Properties properties) {
		final ContentRelationGenerator gen=new ContentRelationGenerator(properties);
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			gen.interactive();
		}
		try {
			List<File> relations=gen.generateContentRelations();
			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateContentModels(Properties properties) {
		final ContentModelGenerator gen=new ContentModelGenerator(properties);
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			gen.interactive();
		}
		try {
			List<File> contentModels=gen.generateContentModels();
			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateContexts(Properties properties) {
		final ContextGenerator gen=new ContextGenerator(properties);
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			gen.interactive();
		}
		try {
			List<File> contexts=gen.generateContexts();
			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateItems(Properties properties) {
		final ItemGenerator itemGenerator = new ItemGenerator(properties);
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			itemGenerator.interactive();
		}
		try {
			final String msg;
			if (properties.getProperty(ItemGenerator.PROPERTY_RANDOM_DATA).equals("true")) {
				final long size = (Long.parseLong(properties.getProperty(ItemGenerator.PROPERTY_RANDOM_NUM_FILES))
						* Long.parseLong(properties.getProperty(ItemGenerator.PROPERTY_RANDOM_SIZE_FILES)) * 1024L);
				msg = "generating " + properties.getProperty(ItemGenerator.PROPERTY_RANDOM_NUM_FILES)
						+ " random files with " + properties.getProperty(ItemGenerator.PROPERTY_RANDOM_SIZE_FILES)
						+ " kb each totaling in " + formatSize(size / 1024L) + " of data";
			} else {
				msg = "generating escidoc objects from " + properties.getProperty(ItemGenerator.PROPERTY_INPUT_DIRECTORY);
			}
			System.out.println(msg);
			List<File> items = itemGenerator.generateItems();
			System.out.println("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String formatSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
