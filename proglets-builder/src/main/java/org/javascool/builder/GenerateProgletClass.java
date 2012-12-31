package org.javascool.builder;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.javascool.core.Proglet;

/**
 * Cette tâche génère la classe Proglet pour la compilation.
 * <p>
 * Cette classe est ensuite utilisé pour détecter la présence de Proglet par le
 * {@link ServiceLoader}
 * </p>
 * <p>
 * La classe généré étant de {@link Proglet}
 * </p>
 * 
 * @author PhilippeGeek
 * @since 5.0
 * 
 */
@Mojo(name = "generate-proglet-class")
public class GenerateProgletClass extends AbstractMojo {

	@Parameter(property = "project.artifactId")
	private String progletId;

	@Parameter(property = "project.groupId")
	private String groupId;

	@Parameter(property = "project.build.outputDirectory")
	private File outputDirectory;

	@Parameter(property = "basedir")
	private File directory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		File progletJavaFile = FileUtils.getFile(directory, "Proglet.java");
		try {
			FileUtils.writeStringToFile(progletJavaFile, getJavaCode(),
					Charsets.UTF_8);
		} catch (IOException e) {
			throw new MojoFailureException(
					"Impossible d'ecrire l'objet de la Proglet", e);
		}
	}

	private String getJavaCode() {
		StringBuilder builder = new StringBuilder();
		{ // Ajout d'une déclaration de package
			builder.append("package ").append(groupId).append(".")
					.append(progletId).append(";\n");
		}
		{ // Ajout des import
			builder.append(importGenerate("org.javascool.core.Translator"))
					.append(importGenerate("javax.swing.*"));
		}
		{ // Déclaration de la classe
			builder.append("\n")
					.append("public class Proglet extends org.javascool.core.Proglet{\n");
			builder.append("\tpublic Class<? extends javax.swing.JPanel> getPanelClass(){\n");
			if (FileUtils.getFile(directory, "Panel.java").exists()) {
				builder.append("\t\treturn Panel.class;\n");
			} else {
				builder.append("\t\treturn null;\n");
			}
			builder.append("\t}\n");
			builder.append("\tpublic Class<?> getFunctionsClass(){\n");
			if (FileUtils.getFile(directory, "Functions.java").exists()) {
				builder.append("\t\treturn Functions.class;\n");
			} else {
				builder.append("\t\treturn null;\n");
			}
			builder.append("\t}\n");
			builder.append("\tpublic Class<? extends Translator> getTranslatorClass(){\n");
			if (FileUtils.getFile(directory, "Translator.java").exists()) {
				builder.append("\t\treturn Translator.class;\n");
			} else {
				builder.append("\t\treturn null;\n");
			}
			builder.append("\t}\n");
			builder.append("\tpublic String getProgletPackage(){\n");
			builder.append("\t\treturn \"").append(groupId).append('.')
					.append(progletId).append("\";\n");
			builder.append("\t}\n");
			builder.append("}\n");
		}
		return builder.toString();
	}

	private String importGenerate(String pac) {
		return "import " + pac + ";\n";
	}

}
