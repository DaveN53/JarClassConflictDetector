package com.attivio.maven.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassMapAggregator {

	public DupJarClassMap AggregateDupJarClassMap(DupJarClassMap... dupClassMaps)
	{
		DupJarClassMap finalMap = new DupJarClassMap();
		
		List<LinkedHashMap<String, Set<DupJarClassDefinition>>> classMaps = new ArrayList<LinkedHashMap<String, Set<DupJarClassDefinition>>>();
		List<Map<String, JarReference>> jarMaps = new ArrayList<Map<String, JarReference>>();
		List<List<JarInfo>> jarInformation = new ArrayList<List<JarInfo>>();
		
		for (DupJarClassMap dupClassMap: dupClassMaps)
		{
			LinkedHashMap<String, Set<DupJarClassDefinition>> classMap = dupClassMap.getClassMap();
			classMaps.add(classMap);
			
			Map<String, JarReference> jarMap = dupClassMap.getJarMap();
			jarMaps.add(jarMap);
			
			List<JarInfo> jarInfoList = dupClassMap.getJarInformation();
			jarInformation.add(jarInfoList);
		}
		
		LinkedHashMap<String, Set<DupJarClassDefinition>> finalClassMap = aggregateClassMap(classMaps);
		Map<String, JarReference> finalJarMap = aggregateJarMap(jarMaps);
		List<JarInfo> finalJarInformation = aggregateJarInformation(jarInformation);
		
		finalMap.setClassMap(finalClassMap);
		finalMap.setJarMap(finalJarMap);
		finalMap.setJarInformation(finalJarInformation);
		
		return finalMap;
	}
	
	private LinkedHashMap<String, Set<DupJarClassDefinition>> aggregateClassMap(List<LinkedHashMap<String, Set<DupJarClassDefinition>>> classMaps)
	{
		LinkedHashMap<String, Set<DupJarClassDefinition>> finalClassMap = classMaps.get(0);
		classMaps.remove(0);

		for(LinkedHashMap<String, Set<DupJarClassDefinition>> map: classMaps)
		{
			Set<String> finalKeys = finalClassMap.keySet();
			Set<String> keys = map.keySet();
			for(String key: keys)
			{
				if (finalKeys.contains(key))
				{
					Set<DupJarClassDefinition> finalDefs = finalClassMap.get(key);
					Set<DupJarClassDefinition> newDefs = map.get(key);
					finalDefs = aggregateDefs(finalDefs, newDefs);
					
					finalClassMap.remove(key);
					finalClassMap.put(key, finalDefs);
				}
				else
				{
					finalClassMap.put(key, map.get(key));
					finalKeys = finalClassMap.keySet();
				}
			}
		}
		
		Set<DupJarClassDefinition> test = finalClassMap.get("test.TestData.TestData");
		
		return finalClassMap;
	}
	
	private Set<DupJarClassDefinition> aggregateDefs(Set<DupJarClassDefinition> finalDefs, Set<DupJarClassDefinition> newDefs)
	{
		List<String> paths = new ArrayList<String>();
		
		for (DupJarClassDefinition def: finalDefs)
		{
			JarReference ref = def.getJarReference();
			String path = ref.getJarFile().getPath();
			paths.add(path);
		}
		
		for (DupJarClassDefinition def: newDefs)
		{
			JarReference ref = def.getJarReference();
			String path = ref.getJarFile().getPath();
			
			if (pathisFound(paths,path)) continue;
			
			finalDefs.add(def);
			paths.add(path);
		}
		
		return finalDefs;
	}
	
	private boolean pathisFound(List<String> paths, String path)
	{
		for (String p: paths)
		{
			if (pathsAreEqual(p,path)) return true;
		}
		
		return false;
	}
	
	private boolean pathsAreEqual(String path1, String path2)
	{
		if (path1.length() > path2.length())
		{
			int difference = path1.length() - path2.length();
			String temp = path1.substring(difference);
			
			if (temp.equals(path2)) return true;
		}
		else
		{
			int difference = path2.length() - path1.length();
			String temp = path2.substring(difference);
			
			if (temp.equals(path1)) return true;
		}
		
		return false;
	}
	
	
	private Map<String, JarReference> aggregateJarMap(List<Map<String, JarReference>> jarMaps)
	{
		Map<String, JarReference> finalJarMap = new HashMap<String, JarReference>();
		
		for(Map<String, JarReference> map: jarMaps)
		{
			Set<String> finalKeys = finalJarMap.keySet();
			Set<String> keys = map.keySet();
			
			for(String key: keys)
			{
				if (!finalKeys.contains(key))
					finalJarMap.put(key, map.get(key));
			}
		}
		
		return finalJarMap;
	}
	
	private List<JarInfo> aggregateJarInformation(List<List<JarInfo>> jarInformation)
	{
		List<JarInfo> finalJarInformation = new ArrayList<JarInfo>();
		List<String> pathList = new ArrayList<String>();
		
		for (List<JarInfo> jarinfo: jarInformation)
		{
			for (JarInfo ji: jarinfo)
			{
				if (pathList.contains(ji.GetPath())) continue;
				finalJarInformation.add(ji);
				pathList.add(ji.GetPath());
			}
		}
		
		Collections.sort(finalJarInformation, new JarInfoComparator());
		
		return finalJarInformation;
	}
	
	class JarInfoComparator implements Comparator<JarInfo>
	{
		@Override
		public int compare(JarInfo a, JarInfo b) {
			return a.GetName().compareToIgnoreCase(b.GetName());
		}
	}
}
