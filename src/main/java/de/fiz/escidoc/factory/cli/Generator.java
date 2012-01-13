package de.fiz.escidoc.factory.cli;

import java.io.File;
import java.util.List;

public interface Generator {
	public List<File> generateFiles() throws Exception;
	public void interactive();
}
