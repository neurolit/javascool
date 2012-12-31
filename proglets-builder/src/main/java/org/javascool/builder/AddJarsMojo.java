/*
 * AddJars Maven Plugin
 * Copyright (C) 2012 Vasily Karyaev <v.karyaev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javascool.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.WriterFactory;

/**
 * Ajoute les Jars du dossier courant comme dépendance au projet.
 * 
 * @author Philippe VIENNE <PhilippeGeek@gmail.com>
 */
@Mojo(name = "add-jars", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AddJarsMojo extends AbstractMojo {

	@Parameter(property = "project", readonly = true, required = true)
	private MavenProject project;

	@Component
	private ArtifactFactory artifactFactory;

	@Component
	private ArtifactInstaller artifactInstaller;
	
	@Parameter(property = "basedir")
	private File basedir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			executeInt();
		} catch (MojoFailureException e) {
			throw e;
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void executeInt() throws Exception {
		File workdir = new File(project.getBuild().getDirectory(), getClass()
				.getName());
		workdir.mkdirs();

		Set<Artifact> newDependenciesArtifacts = new HashSet<Artifact>();
		Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();

		final String groupIdForDeps = project.getGroupId() + "."
				+ project.getArtifactId();
		for (final File jar : project.getBasedir().listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.getName().endsWith(".jar"))
					return true;
				return false;
			}
		})) {
			boolean publishLocaly = true, newDep=true;
			final String depArtifact = jar.getName().substring(0,
					jar.getName().length() - 4);
			final String version = Long.toString(jar.lastModified());
			final Artifact artifact = artifactFactory.createArtifact(
					groupIdForDeps, depArtifact, version, "compile", "jar");
			for (Object o : project.getOriginalModel().getDependencies()) {
				final Dependency dep = (Dependency) o;
				if (dep.getGroupId().equals(groupIdForDeps)
						&& dep.getArtifactId().equals(depArtifact)) {
					publishLocaly = !dep.getVersion().equals(version);
					dep.setVersion(version);
					newDep=false;
				}
			}
			if (publishLocaly) {
				getLog().info(
						"On ajoute " + depArtifact
								+ " au dépendances de l'ordinateur.");
				artifact.addMetadata(new ProjectArtifactMetadata(artifact,
						createArtifactPom(artifact)));
				artifactInstaller.install(jar, artifact, null);
				getLog().info("Ajout au classpath de " + jar.getName());
				if(newDep) newDependenciesArtifacts.add(artifact);
			}
		}

		dependencyArtifacts.addAll(newDependenciesArtifacts);

		project.setDependencyArtifacts(dependencyArtifacts);

		for (Artifact dependency : newDependenciesArtifacts)
			project.getOriginalModel().addDependency(
					createDependency(dependency));

		
		writePom(new File(basedir,"pom-tmp.xml"), project.getOriginalModel());
		project.setFile(new File(basedir,"pom-tmp.xml"));
	}

	private Dependency createDependency(Artifact a) {
		Dependency d = new Dependency();
		d.setGroupId(a.getGroupId());
		d.setArtifactId(a.getArtifactId());
		d.setVersion(a.getVersion());
		d.setScope(a.getScope());
		d.setType(a.getType());
		return d;
	}

	private void writePom(File pom, Model model) throws IOException {
		Writer writer = WriterFactory.newXmlWriter(pom);
		new MavenXpp3Writer().write(writer, model);
		writer.close();
	}

	private File createArtifactPom(Artifact a) throws IOException {
		File pomFile = File.createTempFile(a.getArtifactId(), ".pom");
		writePom(pomFile, createModel(a));
		return pomFile;
	}

	private Model createModel(Artifact a) {
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(a.getGroupId());
		model.setArtifactId(a.getArtifactId());
		model.setVersion(a.getVersion());
		model.setPackaging(a.getType());
		return model;
	}
}
