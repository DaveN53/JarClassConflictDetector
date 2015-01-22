package com.attivio.maven.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class JarReference {
	
	Set<String> modules = new HashSet<String>();
	File jarFile;
	
	public File getJarFile() { return jarFile; }
	public void setJarFile(File jarFile) { this.jarFile = jarFile; }

	public JarReference(File jarFile) 
	{
		this.jarFile = jarFile;
	}
  
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for (String module: modules)
		{
			sb.append("  ")
				.append(module)
				.append(" ")
				.append(jarFile.getName())
				.append("\n");
		}
		
		return sb.toString()
				 .replaceFirst("\n$", "");
	}
  
	public Set<String> getModules() 
	{
		return modules;
	}
  
	public void setModules(Set<String> modules) 
	{
		this.modules = modules;
	}

}
