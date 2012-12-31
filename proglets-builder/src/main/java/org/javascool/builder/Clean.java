package org.javascool.builder;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="clean")
public class Clean extends AbstractMojo {
	
	@Parameter(property="basedir",required=true,readonly=true)
	private File basedir;
	
	@Parameter(property = "project", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		new File(basedir,"Proglet.java").delete();
		new File(basedir,"pom-tmp.xml").delete();
		project.setFile(new File(basedir,"pom.xml"));
	}

}
