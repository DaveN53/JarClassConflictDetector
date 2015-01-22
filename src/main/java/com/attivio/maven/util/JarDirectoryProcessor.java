package com.attivio.maven.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class JarDirectoryProcessor {

	private DupJarClassMap theClasses = new DupJarClassMap();
	private List<Pattern> jarFilters = null;
	private String buildKitPath = new File( "target/kit" ).getPath();
	
	FilenameFilter jarFilenameFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith(".jar");
		}
	};
	
	public JarDirectoryProcessor()
	{
		if (jarFilters == null)
		{
			jarFilters = new ArrayList<Pattern>();
			jarFilters.add( Pattern.compile( "^aie-.*"));
		}
	}
	
	public void setKitPath(String kitPath)
	{
		buildKitPath = new File(kitPath).getPath();
	}
	
	public void setClassMap(DupJarClassMap classMap)
	{
		theClasses = classMap;
	}
	
 	public DupJarClassMap search(File... locations) throws IOException
	{
		for (File loc : locations)
		{
			internalSearch(loc, loc);
		}
		
		return theClasses;
	}

	public void internalSearch(File location, File baseDir) throws IOException
	{
		if (location.getAbsolutePath().endsWith(buildKitPath))
		{
			processKitDir(location, baseDir);
			return;
		}
	
		for (File f : location.listFiles())
		{
			if (!f.isDirectory()) continue;
			if (".svn".equals(f.getName())) continue;
			
			internalSearch(f, baseDir);
		}
	}
	
	private void processKitDir(File kitDir, File baseDir) throws IOException 
	{
		processJarDir(kitDir, baseDir, new File(kitDir, "lib"));
		processJarDir(kitDir, baseDir, new File(kitDir, "lib-override"));
	}

	private void processJarDir(File kitDir, File baseDir, File kitChildDir) throws IOException 
	{
		if (!kitChildDir.exists()) return;
		
		String modDir = kitDir.getParentFile()
							.getParentFile()
							.getCanonicalPath()
							.substring(baseDir
									.getParentFile()
									.getCanonicalPath()
									.length() + 1);

		for (File jar : kitChildDir.listFiles(jarFilenameFilter)) 
		{
			boolean process = true;
			for (Pattern p : jarFilters)
			{
				if (p.matcher(jar.getName()).matches())
				{
					process = false;
					break;
				}
			}
			if (!process) continue;

			try 
			{
				theClasses.processJar(modDir, jar);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				continue;
			}
		}
	}

	public List<Pattern> getJarFilters() 
	{
		return jarFilters;
	}

	public void setJarFilters(List<Pattern> jarFilters) 
	{
		this.jarFilters = jarFilters;
	}

	public void GenerateReport(OutputStream os) throws IOException
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
	}
	
	private void createHead(Element html)
	{
		Element head = html.addElement("head");
		Element metaCt = head.addElement("meta");
	    metaCt.addAttribute("http-equiv", "Content-Type");
	    metaCt.addAttribute("content", "text/html; charset=utf-8");
	    head.addElement("title").setText("Jar Content Report");
	
	    addCSS(head);
	    addJS(head);
	}
	
	private void createBody(Element html)
	{
		Element body = html.addElement("body");
		Element heading = body.addElement("h1");
		
	    setupJarPaths(body); 
	    
	    heading.setText("Jar Content Report: " 
					    + Calendar.getInstance().getTime() 
					    + " Jars: " + theClasses.getJarPaths().size());
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
		        ".jar {\n"+
		        "  width: 350px;\n"+
		        "  margin-left:20px;\n"+
		        "  clear: left;\n"+
		        "}\n"+
		        ".class {\n"+
		        "  padding-left: 4px;\n"+
		        "  background-color: #eee;\n"+
		        "  color: red;\n"+
		        "}\n"+
		        ".jarContent {\n"+
		        "  padding: 4px;\n"+
		        "  background-color: #eee;\n"+
		        "}\n"+
		        ".jarName {\n"+
		        "  padding-left: 4px;\n"+
		        "  background-color: #ccc;\n"+
		        "  color: #222;\n"+
		        "  font-size: 20px;\n"+
		        "}\n"+
		        ".jarClass {\n"+
		        "  padding-left: 30px;\n"+
		        "  background-color: #eee;\n"+
		        "  color: #333;\n"+
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
	
	private void setupJarPaths(Element body)
	{
		Element jarPaths = body.addElement("div");
		
		jarPaths.addAttribute("class","jarpath");
	    jarPaths.addElement("h2").setText("Jars Cataloged");
	    
	    addJarData(jarPaths);
	}
	
	private void addJarData(Element jarPaths)
	{
		List<JarInfo> jarInformation = theClasses.getJarInformation();
		int count = 0;
		
		for (JarInfo info: jarInformation) 
		{
			addJarDescription(info, jarPaths, count);	
			count++;
		}
	}
	
	private void addJarDescription(JarInfo info, Element parent, int count)
	{
		Element mDiv = parent.addElement("div");
		mDiv.addAttribute("class", "jarContent");
		
		Element title = mDiv.addElement("div");
		title.addAttribute("class", "jarName");
		Element boldTitle = title.addElement("b");
		boldTitle.setText(info.GetName());
		
		Element path = mDiv.addElement("div");
		path.addAttribute("class", "jarClass");
		path.setText("PATH: " + info.GetPath());
		
		Element toggleButton = mDiv.addElement("button");
		toggleButton.addAttribute("id", "displayText");
		toggleButton.addAttribute("style", "padding:0px");
		toggleButton.addAttribute("onclick", "javascript:toggle('" + "classes" + count + "');");
		toggleButton.setText("Classes +/-");
		
		Element classBody = mDiv.addElement("div");
		classBody.addAttribute("class", "jarClass");
		classBody.addAttribute("id", "classes" + count);
		classBody.addAttribute("style", "display: none;");
		
		addClassNames(info.GetClassNames(), classBody);
	}
	
	private void addClassNames(List<String> classNames, Element parentDiv)
	{
		for(String className: classNames)
		{
			Element classDiv = parentDiv.addElement("div");
			classDiv.addAttribute("class", "jarClass");
			classDiv.setText(className);
		}
	}
}
