package com.attivio.maven.util;

import java.util.Set;

import com.attivio.maven.util.JarReference;

public class DupJarClassDefinition {
	
	JarReference  JarRef;
	String        SHA1sum;
  
	public DupJarClassDefinition(JarReference JarRef, String SHA1sum) 
	{
		this.JarRef = JarRef;
		this.SHA1sum = SHA1sum;
	}

 	public String getJarName() 
 	{ 
 		return JarRef.getJarFile().getName(); 
 	}
  
 	public Set<String> getModules() 
 	{ 
 		return JarRef.getModules(); 
 	}
  
 	public JarReference getJarReference() 
 	{ 
 		return JarRef; 
 	}
  
 	public void setJarReference(JarReference JarRef) 
 	{ 
 		this.JarRef = JarRef; 
 	}

 	public String getSHA1sum() 
 	{ 
 		return SHA1sum; 
 	}
  
 	public void setSHA1sum(String SHA1sum) 
 	{ 
 		this.SHA1sum = SHA1sum; 
 	}

 	@Override
 	public String toString()
 	{
 		return this.JarRef.toString() + SHA1sum;
 	}
}