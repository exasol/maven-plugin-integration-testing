package com.example;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "hello")
public class DummyPluginMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Hello World!");
    }
}