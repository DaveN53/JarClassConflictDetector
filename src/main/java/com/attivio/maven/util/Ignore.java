package com.attivio.maven.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Ignore {
	
	public static class JarName 
	{
		private String name;
		
		public void setName(String name){ this.name = name; }		
		public String getName() { return name; }
	}
	
	private String className;
	private List<String> jarNames = new ArrayList<String>();

	private Pattern classNamePat = null;
	private List<Pattern> jarNamesPat = null;

	public Ignore(){}

	public Ignore(String className, String... jars)
	{
		this.className = className;
		jarNames = new ArrayList<String>();
    
		for (String jar : jars) 
			jarNames.add(jar);
	}

	public boolean matches(String className, Set<DupJarClassDefinition> defs)
	{
		if (classNamePat == null) classNamePat = Pattern.compile(this.className);
		if (jarNamesPat == null) compilePatternsFromJarFiles();

		if (classNamePat.matcher(className).matches()) return calculateMatches(defs);
		return false;
	}
	
	private void compilePatternsFromJarFiles()
	{
		jarNamesPat = new ArrayList<Pattern>();
		for (String pat : jarNames)
		{
			jarNamesPat.add( Pattern.compile( pat ) );
		}
	}

	private boolean calculateMatches(Set<DupJarClassDefinition> defs)
	{
		int cnt = 0;
		Matcher m = null;
		HashSet<String> vBuf = new HashSet<String>();
		
		for (DupJarClassDefinition def : defs)
		{
			for (Pattern pat : jarNamesPat)
			{
				m = pat.matcher(def.getJarName());
				if (!m.matches()) continue;
				
				if (m.groupCount() > 0) 
					vBuf.add(m.group(1));
				
				cnt++;
			}
		}
		
		if (cnt == defs.size() && vBuf.size() < 2) return true;
		
		return false;
	}
	
	public String getClassName() 
	{
		return className;
	}
	
	public void setClassName(String className) 
	{
		this.className = className;
	}

	public List<String> getJarNames() 
	{
		return jarNames;
	}
	
	public void setJarNames(List<String> jarNames) 
	{
		this.jarNames = jarNames;
	}

	public void addConfiguredJar( JarName jar )
	{
		this.jarNames.add( jar.getName() );
	}

}
