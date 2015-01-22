package com.attivio.maven.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlDeserializer {

	public DupJarClassMap DeserializeXmlToClassMap(String path)
	{
		DupJarClassMap dupJarClassMap = new DupJarClassMap();
		
		try 
		{
			File xmlFile = new File(path);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(xmlFile);
			
			Element rootElement = doc.getDocumentElement();
			
			NodeList nl = rootElement.getElementsByTagName("classMap");
			Node cm = nl.item(0);
			
			nl = rootElement.getElementsByTagName("jarMap");
			Node jm = nl.item(0);
			
			nl = rootElement.getElementsByTagName("jarInformation");
			Node ji = nl.item(0);
			
			LinkedHashMap<String, Set<DupJarClassDefinition>> classMap = deserializeClassMap((Element)cm);
			dupJarClassMap.setClassMap(classMap);
			
			Map<String, JarReference> jarMap = deserializeJarMap((Element)jm);
			dupJarClassMap.setJarMap(jarMap);
			
			List<JarInfo> jarInformation = deserializeJarInformation((Element)ji);
			dupJarClassMap.setJarInformation(jarInformation);
			
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return dupJarClassMap;
	}
	
	private List<JarInfo> deserializeJarInformation(Element ji)
	{
		List<JarInfo> jarInformation = new ArrayList<JarInfo>();
		
		if (ji == null) return jarInformation;
		
		NodeList nl = ji.getElementsByTagName("jarInfo");
		for (int i=0; i < nl.getLength(); i++)
		{
			Node n = nl.item(i);
			deserializeJarInfo((Element)n, jarInformation);
		}
		
		return jarInformation;
	}
	

	private void deserializeJarInfo(Element ji, List<JarInfo> jarInformation)
	{
		String name = ji.getElementsByTagName("name").item(0).getTextContent();
		String path = ji.getElementsByTagName("path").item(0).getTextContent();
		
		List<String> classNameList = new ArrayList<String>();
		Element classNames = (Element) ji.getElementsByTagName("classNames").item(0);
		NodeList nl = classNames.getElementsByTagName("className");
		for (int i=0; i < nl.getLength(); i++)
		{
			String className = nl.item(i).getTextContent();
			classNameList.add(className);
		}
		
		JarInfo jarInfo = new JarInfo(name,classNameList, path);
		jarInformation.add(jarInfo);
	}
	
	private Map<String, JarReference> deserializeJarMap(Element jm)
	{
		Map<String, JarReference> jarMap = new HashMap<String, JarReference>();
		
		if (jm == null) return jarMap;
		
		NodeList nl = jm.getElementsByTagName("entry");
		for(int i=0; i < nl.getLength(); i++)
		{
			Node n = nl.item(i);
			deserializeJarMapEntry((Element)n, jarMap);
		}
		
		return jarMap;
	}
	
	private void deserializeJarMapEntry(Element entry, Map<String, JarReference> jarMap)
	{
		Node key = entry.getElementsByTagName("key").item(0);
		String keyValue = key.getTextContent();
		
		Element jarRef = (Element) entry.getElementsByTagName("JarReference").item(0);
	    Node fRef = jarRef.getElementsByTagName("File").item(0);	
		File f = new File(fRef.getTextContent());
		JarReference jf = new JarReference(f);
		
		Element modules = (Element) jarRef.getElementsByTagName("modules").item(0);
		Set<String> moduleList = deserializeModules(modules);
		jf.setModules(moduleList);
		
		jarMap.put(keyValue, jf);
	}
	
	private LinkedHashMap<String, Set<DupJarClassDefinition>> deserializeClassMap(Element cm)
	{
		LinkedHashMap<String, Set<DupJarClassDefinition>> classMap = new LinkedHashMap<String, Set<DupJarClassDefinition>>();
		
		if (cm == null) return classMap;
		
		NodeList nl = cm.getElementsByTagName("entry");
		for(int i=0; i < nl.getLength(); i++)
		{
			Node n = nl.item(i);
			deserializeClassMapEntry((Element)n, classMap);
		}
		
		return classMap;
	}
	
	private void deserializeClassMapEntry(Element entry, LinkedHashMap<String, Set<DupJarClassDefinition>> classMap)
	{
		Node key = entry.getElementsByTagName("key").item(0);
		String keyValue = key.getTextContent();
		
		Element dupJarClassDefSet = (Element) entry.getElementsByTagName("DupJarClassDefinitionSet").item(0);
		NodeList classDefNl = dupJarClassDefSet.getElementsByTagName("DupJarClassDefinition");
		Set<DupJarClassDefinition> classDefSet = new HashSet<DupJarClassDefinition>();
		
		for(int i=0; i < classDefNl.getLength(); i++)
		{
			Element e = (Element) classDefNl.item(i);
			DupJarClassDefinition def = deserializeDupJarClassDef(e);
			classDefSet.add(def);
		}
		
		classMap.put(keyValue, classDefSet);
	}
	
	private DupJarClassDefinition deserializeDupJarClassDef(Element def)
	{
		Node shaSum = def.getElementsByTagName("SHA1sum").item(0);
		Element jRef = (Element) def.getElementsByTagName("JarReference").item(0);
		
		Node fRef = jRef.getElementsByTagName("File").item(0);	
		File f = new File(fRef.getTextContent());
		JarReference jf = new JarReference(f);
		
		Element modules = (Element) jRef.getElementsByTagName("modules").item(0);
		Set<String> moduleList = deserializeModules(modules);
		jf.setModules(moduleList);
		
		DupJarClassDefinition dupJarClassDef = new DupJarClassDefinition(jf, shaSum.getTextContent());
		
		return dupJarClassDef;
	}

	private Set<String> deserializeModules(Element modules)
	{
		Set<String> moduleList = new HashSet<String>();
		
		NodeList nl = modules.getElementsByTagName("module");
		
		for(int i=0; i<nl.getLength(); i++)
		{
			Node n = nl.item(i);
			moduleList.add(n.getTextContent());
		}
		
		return moduleList;
	}
} 
