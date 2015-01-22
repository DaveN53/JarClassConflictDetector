package com.attivio.maven.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlSerializer {
	
	private Document doc;
	
	public void SerializeClassMapToXml(DupJarClassMap classMap, String path)
	{
		try 
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("DupJarClassMap");
			doc.appendChild(rootElement);
			
			serializeClassMap(classMap.getClassMap(), rootElement);
			serializeJarMap(classMap.getJarMap(), rootElement);
			serializeJarInformation(classMap.getJarInformation(), rootElement);
			
			saveXMLtoFile(path);
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveXMLtoFile(String path) throws TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(path));
 
		transformer.transform(source, result);
 
		System.out.println("Jar Catalog saved as XML");
	}
	
	private void serializeJarInformation(List<JarInfo> jarInformation, Element root)
	{
		Element jarInfoList = doc.createElement("jarInformation");
		
		for(JarInfo ji: jarInformation)
		{
			serializeJarInfo(ji, jarInfoList);
		}
		
		root.appendChild(jarInfoList);
	}
	
	private void serializeJarInfo(JarInfo ji, Element parent)
	{
		Element jarInfo = doc.createElement("jarInfo");
		
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(ji.GetName()));
		jarInfo.appendChild(name);
		
		Element path = doc.createElement("path");
		path.appendChild(doc.createTextNode(ji.GetPath()));
		jarInfo.appendChild(path);
		
		List<String> classNames = ji.GetClassNames();
		Element classNameList = doc.createElement("classNames");
		
		for(String cName: classNames)
		{
			Element className = doc.createElement("className");
			className.appendChild(doc.createTextNode(cName));
			classNameList.appendChild(className);
		}
		
		jarInfo.appendChild(classNameList);
		parent.appendChild(jarInfo);
	}
	
	private void serializeJarMap(Map<String, JarReference> jarMap, Element root)
	{
		Element jm = doc.createElement("jarMap");
		
		for (Entry<String, JarReference> entry: jarMap.entrySet())
		{
			serializeJarMapEntry(entry, jm);
		}
		
		root.appendChild(jm);
	}
	
	private void serializeJarMapEntry(Entry<String, JarReference> entry, Element parent)
	{
		Element jmEntry = doc.createElement("entry");
		
		String key = entry.getKey();
		Element entryKey = doc.createElement("key");
		entryKey.appendChild(doc.createTextNode(key));
		jmEntry.appendChild(entryKey);
		
		JarReference ref = entry.getValue();
		serializeJarReference(ref, jmEntry);
		
		parent.appendChild(jmEntry);
	}
	
	private void serializeClassMap(LinkedHashMap<String, Set<DupJarClassDefinition>> classMap, Element root)
	{
		Element cm = doc.createElement("classMap");
		
		for (Entry<String, Set<DupJarClassDefinition>> entry: classMap.entrySet()) 
		{ 
			serializeClassMapEntry(entry, cm);
		}
		
		root.appendChild(cm);
	}
	
	private void serializeClassMapEntry(Entry<String, Set<DupJarClassDefinition>> entry, Element parent)
	{
		Element cmEntry = doc.createElement("entry");	
		
		String key = entry.getKey();
		Element entryKey = doc.createElement("key");
		entryKey.appendChild(doc.createTextNode(key));
		cmEntry.appendChild(entryKey);
		
		Set<DupJarClassDefinition> defs = entry.getValue();
		Element dupJarClassDefSet = doc.createElement("DupJarClassDefinitionSet");
		
		for(DupJarClassDefinition def: defs)
		{
			serializeDupJarClassDefinition(def, dupJarClassDefSet);
		}
		
		cmEntry.appendChild(dupJarClassDefSet);
		parent.appendChild(cmEntry);
	}
	
	private void serializeDupJarClassDefinition(DupJarClassDefinition def, Element parent)
	{
		Element dupJarClassdef = doc.createElement("DupJarClassDefinition");
		
		String SHA1sum = def.getSHA1sum();
		Element SHAel = doc.createElement("SHA1sum");
		SHAel.appendChild(doc.createTextNode(SHA1sum));
		dupJarClassdef.appendChild(SHAel);
		
		JarReference ref = def.getJarReference();
		serializeJarReference(ref, dupJarClassdef);
		
		parent.appendChild(dupJarClassdef);
	}
	
	private void serializeJarReference(JarReference ref, Element parent)
	{
		Element jarDef = doc.createElement("JarReference");
		
		File jarFile = ref.getJarFile();
		Set<String> modules = ref.getModules();

		String workingPath = System.getProperty("user.dir");
		String jarAbsPath = jarFile.getAbsolutePath();
		String relativePath = new File(workingPath).toURI().relativize(new File(jarAbsPath).toURI()).getPath();
		
		Element f = doc.createElement("File");
		f.appendChild(doc.createTextNode(relativePath));
		jarDef.appendChild(f);	
		
		serializeModules(modules, jarDef);
		
		parent.appendChild(jarDef);
	}
	
	private void serializeModules(Set<String> modules, Element parent)
	{
		Element mods = doc.createElement("modules");
		
		for(String m: modules)
		{
			Element mod = doc.createElement("module");
			mod.appendChild(doc.createTextNode(m));
			
			mods.appendChild(mod);
		}
		
		parent.appendChild(mods);
	}
}
