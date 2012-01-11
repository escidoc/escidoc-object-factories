package de.fiz.escidoc.factory.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.escidoc.core.client.exceptions.InternalClientException;

public interface Generator {
	public List<File> generateFiles() throws IOException,ParserConfigurationException,InternalClientException;
	public void interactive();
}
