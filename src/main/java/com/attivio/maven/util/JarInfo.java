package com.attivio.maven.util;

import java.util.List;

public class JarInfo
{
	private String Name;
	private List<String> ClassNames;
	private String Path;
	
	public JarInfo(String Name, List<String> ClassNames, String Path)
	{
		this.Name = Name;
		this.ClassNames = ClassNames;
		this.Path = Path;
	}
	
	public String GetName() { return Name; }
	public List<String> GetClassNames() { return ClassNames; }
	public String GetPath() { return Path; }
}
