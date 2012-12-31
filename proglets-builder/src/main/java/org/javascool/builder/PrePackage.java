package org.javascool.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.javascool.core.JarManager;

@Mojo(name = "prepackage")
public class PrePackage extends AbstractMojo {

	private static final String[] IGNORED_EXTENTIONS_FOR_RESOURCE = { "java",
			"jar" };

	@Parameter(property = "project.artifactId")
	private String progletId;

	@Parameter(property = "project.groupId")
	private String groupId;

	@Parameter(property = "project.build.outputDirectory")
	private File outputDirectory;

	@Parameter(property = "basedir")
	private File directory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// On enlève la classe Proglet
		getLog().info("Nettoyage ...");
		FileUtils.getFile(directory, "Proglet.java").delete();
		// On liste les resources
		getLog().info("Copie des resources");
		File[] resources = directory.listFiles(new FilenameFilter() {

			public boolean accept(File directory, String file) {
				if (file.startsWith("."))
					return false;
				for (String ext : IGNORED_EXTENTIONS_FOR_RESOURCE)
					if (file.endsWith("." + ext))
						return false;
				if (FileUtils.getFile(directory, file).isDirectory())
					return false;
				return true;
			}
		});
		File dest = FileUtils.getFile(
				FileUtils.getFile(outputDirectory, groupId.split("\\.")),
				progletId);
		for (File resource : resources) {
			try {
				getLog().debug("Copie de " + resource);
				FileUtils.copyFileToDirectory(resource, dest, true);
			} catch (IOException e) {
				getLog().error("Impossible de copier la resource " + resource);
				getLog().error(e);
			}
		}
		// On définit le fichier pour le ServiceLoader
		getLog().info("Définition des Méta Informations du Jar");
		File serviceFile = FileUtils.getFile(outputDirectory, "META-INF",
				"services", "org.javascool.core.Proglet");
		serviceFile.getParentFile().mkdirs();
		try {
			FileUtils.write(serviceFile, groupId + "." + progletId
					+ ".Proglet\n", Charsets.UTF_8);
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Impossible de créer le fichier de service", e);
		}
		// On extrait les dépendances placés à la racine du projet
		for (File jar : directory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		})) {
			getLog().info("Extraction de "+jar+" ...");
			JarManager.jarExtract(jar.getAbsolutePath(), outputDirectory.getAbsolutePath());
		}
	}

}
