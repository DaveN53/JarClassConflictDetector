/**
* Copyright 2013 Attivio Inc., All rights reserved.
*/
package com.attivio.maven.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class DupJarClassMap implements Iterable<Entry<String, Set<DupJarClassDefinition>>> 
{
	private final String unreadableSHA1 = "Unreadable SHA1";
	private LinkedHashMap<String, Set<DupJarClassDefinition>> classMap = new LinkedHashMap<String, Set<DupJarClassDefinition>>();
	private Map<String, JarReference> jarMap = new HashMap<String, JarReference>();
	private final Set<String> jreClasses = new HashSet<String>();
	private List<JarInfo> jarInformation = new ArrayList<JarInfo>();
  
	public Iterator<Entry<String, Set<DupJarClassDefinition>>> iterator() 
	{
		return classMap.entrySet().iterator();
	}

	public List <String> getJarPaths() 
	{
		List <String> jarPaths = new ArrayList<String>();
    
		for (Entry<String, JarReference> entry: jarMap.entrySet()) 
		{
			JarReference ref = entry.getValue();
			jarPaths.add(ref.getJarFile().getAbsolutePath());
		}
		
		Collections.sort(jarPaths);
		return jarPaths;
	}
  
	public Set<Entry<String, Set<DupJarClassDefinition>>> getDuplicates() 
	{
		LinkedHashMap<String, Set<DupJarClassDefinition>> duplicates = new LinkedHashMap<String, Set<DupJarClassDefinition>>();
		
		for (Entry<String, Set<DupJarClassDefinition>> entry: classMap.entrySet()) 
		{ 	
		 	Set<DupJarClassDefinition> defs = entry.getValue();
		 	if (!isDuplicate(defs)) continue;
		 	
	 		String className = entry.getKey();
	 		Set<String> dupKeys = duplicates.keySet();
	 		if (dupKeys.contains(className)) continue;
	 		
	 		duplicates.put(className, defs);
		}
		return duplicates.entrySet();
	}

	private boolean isDuplicate(Set<DupJarClassDefinition> defs) 
	{
		if (defs.size() <= 1) return false;
			
		java.util.Iterator<DupJarClassDefinition> it = defs.iterator();
      
		// Get First Object
		DupJarClassDefinition def = it.next();
		String lastSeenSHA1 = def.getSHA1sum();
  
		// Check Against Next - No Dup if they all match 
		while(it.hasNext())
		{
			def = it.next();
			if (!def.getSHA1sum().equals(lastSeenSHA1)) return true; 
			
			lastSeenSHA1 = def.getSHA1sum();
			if (unreadableSHA1.equals(lastSeenSHA1)) return true; 
		}
		
		return false;
  }

	/**
	 * Add Instance of class definition into tracking
	 * @param module    module where found
	 * @param jarFile   jar file where found
	 * @param jarEntry  jar entry
	 */	
	private void addClassDefinition(String module, File jarFile, JarEntry jarEntry, JarInputStream jarStream)
	{
		String className = classNameFromEntry(jarEntry);
		if (!classDefinitionIsValid(jarEntry, className)) return;
    
		String sha1sum = getSHA1ForClass(jarEntry, jarFile);
    
		JarReference ref = getJarRefFromFile(jarFile);
		ref.getModules().add(module);
    
		DupJarClassDefinition def = new DupJarClassDefinition(ref,sha1sum);  
		Set<DupJarClassDefinition> defs =  getClassRefFromClassName(className);
		
		defs.add(def);
		defs =  getClassRefFromClassName(className);
	}
	
	private boolean classDefinitionIsValid(JarEntry jarEntry, String className)
	{
		if (!entryIsClass(jarEntry)) return false;
    
		if (classDefinedByJRE(className)) 
		{
			System.out.println("JRE Defines.  Skipping " + className);
			return false;
		}
		
		return true;
	}

	/**
	 * Scan jar for classes and add them to collection
	 * @param module    module where found
	 * @param jarFile   jar file where found
	 */  
	public void processJar(String module, File jarFile) throws IOException 
	{   
		if (jreClasses.size() == 0) loadClassesDefinitionsFromJRE();
    
		JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile));
		JarEntry jarEntry;

		// Add Jar to Global List of Jars and Attach Module
		jarEntry = jarStream.getNextJarEntry();
    
		List<String> classNames = new ArrayList<String>();
		
		while (jarEntry != null)
		{
			if (entryIsClass(jarEntry))
			{
				// Add Jar Reference to ClassName
				this.addClassDefinition(module, jarFile, jarEntry, jarStream);
				classNames.add(jarEntry.getName());
			}
			jarEntry = jarStream.getNextJarEntry();
		}
		
		jarInformation.add(new JarInfo(jarFile.getName(), classNames, jarFile.getAbsolutePath()));
	}
  
	private boolean classDefinedByJRE(String className) 
	{
		return jreClasses.contains(className);
	}

	private void loadClassesDefinitionsFromJRE() throws IOException 
	{
		System.out.println("Load");
		
		String rtPath = System.getProperty("java.home") 
						+ System.getProperty("file.separator") 
						+ "lib" 
						+ System.getProperty("file.separator") 
						+ "rt.jar";

		JarInputStream jarStream = new JarInputStream(new FileInputStream(rtPath));
		JarEntry jarEntry;

		// Add Jar to Global List of Jars and Attach Module
		jarEntry = jarStream.getNextJarEntry();
    
		while (jarEntry != null)
		{
			if (entryIsClass(jarEntry)) jreClasses.add(classNameFromEntry(jarEntry));
			
			jarEntry = jarStream.getNextJarEntry();
		} 
		
		jarStream.close();
  }
  
	private String getSHA1ForClass(JarEntry jarEntry, File jarFile) 
	{
		MessageDigest md;
    
		try 
		{
			md = MessageDigest.getInstance("SHA1");
			md = updateMessageDigest(md, jarEntry, jarFile);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			return unreadableSHA1;
		} 
		catch (IOException e)  
		{
			return unreadableSHA1;
		} 
    
		String result = digestMessageDigest(md);
		
		return result;
	}  
	
	private MessageDigest updateMessageDigest(MessageDigest md, JarEntry jarEntry, File jarFile) throws IOException
	{
		// Setup Stream for Data
		JarFile jar = new JarFile(jarFile);
		InputStream in = jar.getInputStream(jarEntry);

		int buffer = 8192;
		byte [] classData = new byte[buffer];
  
		int nread = 0;
  
		while ((nread = in.read(classData, 0, buffer)) != -1) 
		{
			md.update(classData, 0, nread);
		}
  
		in.close();
		jar.close();
		
		return md;
	}

	private String digestMessageDigest(MessageDigest md)
	{
		byte[] mdbytes = md.digest();    
		StringBuffer sb = new StringBuffer("");
		
		for (int i = 0; i < mdbytes.length; i++) 
		{
			String val = Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
								.substring(1);
			sb.append(val);
		}
		
		return sb.toString();
	}
	
	private boolean entryIsClass(JarEntry jarEntry) 
	{
		String entryName = jarEntry.getName();
		return entryName.endsWith(".class");
	}
  
	private JarReference getJarRefFromFile(File jarFile) 
	{
		JarReference ref = jarMap.get(jarFile.getName());
		if (ref == null)
		{
			ref = new JarReference(jarFile);
			jarMap.put(jarFile.getName(), ref);
		}
		return ref;    
	}
  
	private String classNameFromEntry(JarEntry jarEntry) 
	{
		String entryName = jarEntry.getName();
		String className = entryName.replace('/', '.')
									.replaceFirst("\\.class$", "");
		return className;
	}
	
	private Set<DupJarClassDefinition> getClassRefFromClassName(String className) 
	{
		Set<DupJarClassDefinition> defs = classMap.get(className);
		if (defs == null)
		{
			defs = new HashSet<DupJarClassDefinition>();
			classMap.put(className, defs);      
		}
		return defs;
	}
	
	public List<JarInfo> getJarInformation()
	{
		return jarInformation;
	}
	
	public LinkedHashMap<String, Set<DupJarClassDefinition>> getClassMap()
	{
		return classMap;
	}
	
	public void setClassMap(LinkedHashMap<String, Set<DupJarClassDefinition>> classMap)
	{
		this.classMap = classMap;
	}
	
	public Map<String, JarReference> getJarMap()
	{
		return jarMap;
	}
	
	public void setJarMap(Map<String, JarReference> jarMap)
	{
		this.jarMap = jarMap;
	}
	
	public void setJarInformation(List<JarInfo> jarInformation)
	{
		this.jarInformation = jarInformation;
	}
}
