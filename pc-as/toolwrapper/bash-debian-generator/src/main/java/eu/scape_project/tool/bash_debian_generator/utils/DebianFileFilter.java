package eu.scape_project.tool.bash_debian_generator.utils;

import java.io.File;
import java.io.FileFilter;

public class DebianFileFilter implements FileFilter {
	@Override
	public boolean accept(File file) {
		return file.getName().endsWith("deb");
	}
}
