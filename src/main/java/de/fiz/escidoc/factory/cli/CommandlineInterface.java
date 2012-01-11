package de.fiz.escidoc.factory.cli;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CommandlineInterface {
	private static final String PROPERTY_VALIDITY = "properties.valid";

	private static void printUsage() {
		StringBuilder helpBuilder = new StringBuilder();
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
		final Properties properties = new Properties();
		final Getopt opt = new Getopt("Escidoc objects generator", args, "hicmrop:");
		if (args.length == 0) {
			printUsage();
			return;
		}
		int option;
		final List<Generator> generators = new ArrayList<Generator>();
		while ((option = opt.getopt()) != -1) {
			switch (option) {
			case 'h':
				printUsage();
				return;
			case 'i':
				generators.add(new ItemGenerator(properties));
				break;
			case 'c':
				generators.add(new ContextGenerator(properties));
				break;
			case 'm':
				generators.add(new ContentModelGenerator(properties));
				break;
			case 'r':
				generators.add(new ContentRelationGenerator(properties));
				break;
			case 'o':
				generators.add(new OrganizationalUnitGenerator(properties));
				break;
			case 'p':
				String path = opt.getOptarg();
				try {
					properties.load(new FileInputStream(path));
					properties.setProperty(PROPERTY_VALIDITY, "true");
				} catch (IOException e) {
					System.err.println("Unable to load properties file from " + path);
					return;
				}
				break;
			default:
				printUsage();
				return;
			}
		}

		// get the settings for the generators through an interactive user
		// session
		if (!Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
			System.out.println();
			for (final Generator gen : generators) {
				System.out.println(":: Settings for " + gen.getClass().getSimpleName());
				gen.interactive();
			}
		}
		
		// store the properties for convenience
		System.out.println();
		try {
			// properties.store(System.out, "printing the setting for convienience");
			final File propFile = new File("generator.properties");
			properties.store(new FileOutputStream(propFile), "created by escidoc-object-generator");
			System.out.println("saved properties to " + propFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("\nGenerating xml files...");
		// and finally generate the files
		for (final Generator gen : generators) {
			try {
				System.out.println(":: running generator " + gen.getClass().getSimpleName());
				gen.generateFiles();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nFinished!\n");
	}
}
