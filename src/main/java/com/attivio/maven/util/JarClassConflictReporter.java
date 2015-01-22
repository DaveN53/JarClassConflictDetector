package com.attivio.maven.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class JarClassConflictReporter {
	
	private DupJarClassMap theClasses = new DupJarClassMap();
	private List<Ignore> ignores = new ArrayList<Ignore>();
	private boolean hasDupes = false;
	private int dupCount = 0;
	
	Map<String,List<String>> jarConflicts = new HashMap<String,List<String>>();

	FilenameFilter jarFilenameFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith(".jar");
		}
	};

	public JarClassConflictReporter() {}
	
	public boolean dumpHtml(OutputStream os, DupJarClassMap theClasses) throws IOException
	{
		this.theClasses = theClasses;
		return dumpHtml(os);
	}
	
	public boolean dumpHtml(OutputStream os) throws IOException
	{
	    Document doc = DocumentFactory.getInstance().createDocument();
	    Element html = doc.addElement("html");
    
	    createHead(html);
	    createBody(html);
	
	    OutputFormat of = new OutputFormat(null, false);
	    of.setEncoding("UTF-8");
	    of.setIndent(false);
	    of.setSuppressDeclaration(false);
	    of.setNewlines(false);
	
	    XMLWriter writer = new XMLWriter(os, of);
	    writer.setResolveEntityRefs(false);
	    writer.write(doc);
	    writer.close();
	
	    return hasDupes;
	}
	
	private void createHead(Element html)
	{
		Element head = html.addElement("head");
		Element metaCt = head.addElement("meta");
	    metaCt.addAttribute("http-equiv", "Content-Type");
	    metaCt.addAttribute("content", "text/html; charset=utf-8");
	    head.addElement("title").setText("Duplicate Class Report");
	
	    addCSS(head);
	    addJS(head);
	}
	
	private void createBody(Element html)
	{
		dupCount = 0;
		hasDupes = false;
		
		Element body = html.addElement("body");
		Element heading = body.addElement("h1");
		
		setupIgnoredClasses(body);
	    setupClassesToBeIgnored(body);
	    setupJarPaths(body); 
	    
	    heading.setText("Class Conflict Detection Report: " 
					    + Calendar.getInstance().getTime() 
					    + " Count: " +  dupCount 
					    + " Jars: " + theClasses.getJarPaths().size());
	}
	
	private void setupIgnoredClasses(Element body)
	{
		Element cDiv = body.addElement("div");
		cDiv.addAttribute("class", "class-file");
		cDiv.addElement("h2").setText("Conflicting Classes");
		
		Element toggleButton = cDiv.addElement("button");
		toggleButton.addAttribute("id", "displayText");
		toggleButton.addAttribute("style", "padding:0px");
		toggleButton.addAttribute("onclick", "javascript:toggle('" + "sortByClass" + "');"
											+"javascript:toggle('" + "sortByJar" + "');");
		toggleButton.setText("Sort By Class/Jar");
		
		Element ignored = body.addElement("div");
		
		ignored.addAttribute("class","ignored");
	    ignored.addElement("h2").setText("Ignored Conflicted Classes");
	    
	    addClassConflictResults(ignored, cDiv);
	}
	
	private void setupClassesToBeIgnored(Element body)
	{
		Element classesIgnored = body.addElement("div");
		
		classesIgnored.addAttribute("class", "ignored");
	    classesIgnored.addElement("h2").setText("Class Conflicts to Ignore");
	    
	    addIgnoreData(classesIgnored);
	}
	
	private void setupJarPaths(Element body)
	{
		Element jarPaths = body.addElement("div");
		
		jarPaths.addAttribute("class","jarpath");
	    jarPaths.addElement("h2").setText("Jars Evaluated");
	    
	    addJarData(jarPaths);
	}
	
	private void addCSS(Element head)
	{
		Element style = head.addElement("style");
	    style.addAttribute("type", "text/css");
	    style.addText(
	    		"body {\n"+
		        "  font-family: Courier;\n"+
		        "  font-size: 14px;\n"+
		        "}\n"+
		        "h1 {\n"+
		        "}\n"+
		        "h2 {\n"+
		        "  margin-top: 5px;\n"+
		        "}\n"+
		        ".class-file {\n"+
		        "  width: 1200px;\n"+
		        "  margin-top: 5px;\n"+
		        "  border: 1px solid #ccc;\n" +
		        "  padding: 4px;\n"+
		        "  clear: left;\n"+
		        "}\n"+
		        ".jar-ref {\n"+
		        "  border-top: 1px solid #ccc;\n"+
		        "  clear: left;\n"+
		        "}\n"+
		        ".jar {\n"+
		        "  width: 1000px;\n"+
		        "  margin-left:20px;\n"+
		        "  clear: left;\n"+
		        "}\n"+
		        ".module {\n"+
		        "}\n"+
		        ".modules {\n"+
		        "  margin-left: 40px;\n"+
		        "  border-left: 1px solid #369;\n"+
		        "  padding-left: 4px;\n"+
		        "  font-size: 11px;\n"+
		        "  margin-bottom: 4px;\n"+
		        "}\n"+
		        ".class {\n"+
		        "  padding-left: 4px;\n"+
		        "  background-color: #eee;\n"+
		        "  color: red;\n"+
		        "}\n"+
		        ".ignored {\n" +
		        "  width: 1200;\n"+
		        "  border: 1px solid #ccc;\n" +
		        "  padding: 4px;\n"+
		        "  margin-top: 5px;\n"+
		        "}\n"+
		        ".classesIgnored {\n" +
		        "  border: 1px solid #ccc;\n" +
		        "  margin-top: 5px;\n"+
		        "  background-color: #EEE;;\n"+
		        "}\n"+
		        ".classesIgnoredP {\n" +
		        "  margin: 4px;\n"+
		        "}\n"+
		        ".jarpath {\n" +
		        "  width: 1200;\n"+
		        "  border: 1px solid #ccc;\n" +
		        "  padding: 4px;\n"+
		        "  margin-top: 5px;\n"+
		        "}\n" );
	}
	
	private void addJS(Element head)
	{
		Element style = head.addElement("script");
	    style.addAttribute("language", "javascript");
	    style.addText(
	    		"function toggle(elementId) {\n" +
	    		"var ele = document.getElementById(elementId);\n" +
	    		"if(ele.style.display == \"block\"){\n" +
	    		"ele.style.display = \"none\";\n" +
	    		"}\n" +
	    		"else {\n" +
	    		"ele.style.display = \"block\"\n" +
	    		"}\n" +
	    		"}\n"
	    		);
	}
	
	private void addClassConflictResults(Element ignored,Element cDiv)
	{
	    if (theClasses.getDuplicates().size() <= 0) return;
	    
		int count = 0;
		
		Element sortByClass = cDiv.addElement("div");
		sortByClass.addAttribute("Id", "sortByClass");
		sortByClass.addAttribute("style", "display:none;");
		Element sortByJar = cDiv.addElement("div");
		sortByJar.addAttribute("style", "display:block;");
		sortByJar.addAttribute("Id", "sortByJar");
		
		for (Entry<String, Set<DupJarClassDefinition>> entry: theClasses.getDuplicates() ) 
	    {
	    	String className = entry.getKey();
	    	Set<DupJarClassDefinition> defs = entry.getValue();
	
			if (isIgnored(className, defs) )
			{
				Element c = ignored.addElement("div");
		    	c.addAttribute("class", "ignore-class");
		    	addClassConflictDiv(className, c, count, defs);
		    	count++;
				continue;
			}
			
			hasDupes = true;
		    dupCount++;
		    
		    addClassConflictDiv(className, sortByClass, count, defs);
		    buildJarConflictDef(className, defs);
			count++;
	    }
		
		addJarConflictDiv(sortByJar);
	}
	
	private void buildJarConflictDef(String className, Set<DupJarClassDefinition> defs)
	{
		for(DupJarClassDefinition def: defs)
		{
			List<String> existingClassConflict;
			String jarName = def.getJarName();
			
			if (jarConflicts.containsKey(jarName))
				existingClassConflict = jarConflicts.get(jarName);
			else
				existingClassConflict = new ArrayList<String>();
			
			if (existingClassConflict.contains(className)) continue;
			
			existingClassConflict.add(className);
			jarConflicts.put(jarName, existingClassConflict);
		}
	}
	
	private void addJarConflictDiv(Element parentDiv)
	{
		int count = 0;
		
		List<String> keys = new ArrayList<String>();
		keys.addAll(jarConflicts.keySet());
		Collections.sort(keys);
		
		for(String jarName: keys)
		{
			Element cnDiv = parentDiv.addElement("div");
			cnDiv.addAttribute("class", "class");
			cnDiv.setText(jarName);
			
			Element toggleButton = cnDiv.addElement("button");
			toggleButton.addAttribute("id", "displayText");
			toggleButton.addAttribute("style", "padding:0px");
			toggleButton.addAttribute("onclick", "javascript:toggle('" + "jarConflicts" + count + "');");
			toggleButton.setText("+/-");
			
			Element jrDiv = parentDiv.addElement("div");
			jrDiv.addAttribute("class", "jar-ref");
			jrDiv.addAttribute("id", "jarConflicts" + count);
			jrDiv.addAttribute("style", "display: none;");
			
			addJarClasses(jarConflicts.get(jarName), jrDiv);
			
			count++;
		}
	}
	
	private void addJarClasses(List<String> classes, Element parentDiv)
	{
		for(String className: classes)
		{
			Element classDiv = parentDiv.addElement("div");
			classDiv.setText(className);
		}
	}
	
	private void addClassConflictDiv(String className, Element parentDiv, int count, Set<DupJarClassDefinition> defs)
	{
		Element cnDiv = parentDiv.addElement("div");
		cnDiv.addAttribute("class", "class");
		cnDiv.setText( className );
		
		Element toggleButton = cnDiv.addElement("button");
		toggleButton.addAttribute("id", "displayText");
		toggleButton.addAttribute("style", "padding:0px");
		toggleButton.addAttribute("onclick", "javascript:toggle('" + "classes" + count + "');");
		toggleButton.setText("+/-");
		
		Element jrDiv = parentDiv.addElement("div");
		jrDiv.addAttribute("class", "jar-ref");
		jrDiv.addAttribute("id", "classes" + count);
		jrDiv.addAttribute("style", "display: none;");
		
		addJarSha1sumDiv(defs, jrDiv);
	}
	
	private void addJarSha1sumDiv(Set<DupJarClassDefinition> defs, Element parentDiv)
	{
		List<DupJarClassDefinition> definitions = new ArrayList<DupJarClassDefinition>();
		definitions.addAll(defs);
		Collections.sort(definitions, new classDefinitionComparator());
		
		for ( DupJarClassDefinition def : definitions )
		{
			Element jDiv = parentDiv.addElement("div");
			jDiv.addAttribute("class", "jar");
			jDiv.setText(def.getJarName() + " SHA1sum: " + def.getSHA1sum());
			Element msDiv = parentDiv.addElement("div");
			msDiv.addAttribute("class", "modules");
	  
			addModuleDiv(def.getModules(), msDiv);
	    }
	}
	
	private void addModuleDiv(Set<String> modules, Element parentDiv)
	{
		for (String module : modules)
		{
			Element mDiv = parentDiv.addElement("div");
			mDiv.addAttribute("class", "module");
			mDiv.setText(module);
		}
	}
	
	private void addIgnoreData(Element classesIgnored)
	{
		for(Ignore i: ignores)
	    {
	    	Element iDiv = classesIgnored.addElement("div");
	    	iDiv.addAttribute("class", "classesIgnored");
	    	
	    	Element p = iDiv.addElement("p");
	    	p.addAttribute("class", "classesIgnoredP");
	    	Element b = p.addElement("b");
	    	b.setText(i.getClassName() + ":");
	    	for(String j: i.getJarNames())
    		{
	    		Element p1 = iDiv.addElement("p");
	    		p1.addAttribute("class", "classesIgnoredP");
		    	p1.setText("   " + j);
    		}
	    }
	}
	
	private void addJarData(Element jarPaths)
	{
		List<String> processedJars = theClasses.getJarPaths();
		int count = 0;
		for (String path : processedJars) 
	    {
	    	Element mDiv = jarPaths.addElement("div");
			mDiv.addAttribute("class", "jar-path");
			mDiv.setText(path);
			
			if(count % 2 ==0)
				mDiv.addAttribute("style", "background-color:#DDD");
			
			count++;
	    }
	}
	
	private boolean isIgnored(String className, Set<DupJarClassDefinition> defs) 
	{
		for (Ignore ignore : ignores)
		{
			if (ignore.matches(className, defs)) 
				return true;
		}
		return false;
	}

	public List<Ignore> getIgnores() 
	{
		return ignores;
	}

	public void setIgnores(List<Ignore> ignores) 
	{
		this.ignores = ignores;
	}
	
	class classDefinitionComparator implements Comparator<DupJarClassDefinition>
	{
		@Override
		public int compare(DupJarClassDefinition a, DupJarClassDefinition b) {
			return a.getSHA1sum().compareToIgnoreCase(b.getSHA1sum());
		}
	}
}
