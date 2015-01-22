package com.attivio.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.attivio.maven.util.ClassMapAggregator;
import com.attivio.maven.util.DupJarClassMap;
import com.attivio.maven.util.JarClassConflictReporter;
import com.attivio.maven.util.Ignore;
import com.attivio.maven.util.Ignores;
import com.attivio.maven.util.JarDirectoryProcessor;
import com.attivio.maven.util.XmlDeserializer;
import com.attivio.maven.util.XmlSerializer;

/**
 *
 * @goal detectClassConflict
 * 
 */
public class ClassConflictDetector extends AbstractMojo
{	
	/**
	 * @parameter
	 */
	private String reportDir = "build\\report\\";
	/**
	 * @parameter
	 */
	private String pathDir = "";
	/**
	 * @parameter
	 */
	private Ignores ignores;
	/**
	 * @parameter
	 */
	private boolean failOnDuplicate = true;
	/**
	 * @parameter
	 */
	private String kitPath = "target\\kit";
	
	private DupJarClassMap theClasses = new DupJarClassMap();
	private JarClassConflictReporter reporter = new JarClassConflictReporter();

	@Override
	public void execute() throws MojoExecutionException
	{	
		validateParameters();
		
		processJarDirectories();
		
		reporter = getJarFinder();	
		handleClassConflictResults(reporter);
	}
	
	private void validateParameters() throws MojoExecutionException
	{
		getLog().info("'reportDir' attribute set using: " + reportDir);
		getLog().info("'pathDir' attribute set using: " + pathDir);
		getLog().info("'kitPath' attribute set using: " + kitPath);
		checkReportDirExist();
	}
	
	private void checkReportDirExist()
	{
		File outputFile = new File(reportDir);
		if(!outputFile.exists())		
			outputFile.mkdirs();
	}
	
	private void processJarDirectories() throws MojoExecutionException
	{
		JarDirectoryProcessor processor = new JarDirectoryProcessor();
		XmlSerializer serializer = new XmlSerializer();
		List<File> dirsToProcess = getDirsToProcess();
		
		try 
		{
			if (kitPath != null) processor.setKitPath(kitPath);
			theClasses = processor.search(dirsToProcess.toArray(new File[dirsToProcess.size()] ));
			serializer.SerializeClassMapToXml(theClasses, reportDir + "JarCatalog.xml");
			
			retrieveTrunkClassCatalog();
			handleJarContentResults(processor);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void retrieveTrunkClassCatalog()
	{
		XmlDeserializer deserializer = new XmlDeserializer();
		ClassMapAggregator aggregator = new ClassMapAggregator();

		String trunkClassCatalogPath = getTrunkJarCatalogPath();
		if (trunkClassCatalogPath != null && new File(trunkClassCatalogPath).exists())
		{	
			getLog().info("Retrieving Trunk Class Catalog");
			DupJarClassMap entireTrunkClassCatalog = deserializer.DeserializeXmlToClassMap(trunkClassCatalogPath);
			theClasses = aggregator.AggregateDupJarClassMap(theClasses, entireTrunkClassCatalog);
		}
	}
	
	private String getTrunkJarCatalogPath()
	{
		String currentDir = System.getProperty("user.dir");
		if(currentDir.contains("trunk"))
		{
			File folder = new File(currentDir);
			while(!folder.getName().equals("trunk"))
			{
				folder = folder.getParentFile();
			}
			return folder.getAbsolutePath() + "\\" + reportDir + "JarCatalog.xml";
		}
		else return null;
	}
	
	private JarClassConflictReporter getJarFinder()
	{
		JarClassConflictReporter finder = new JarClassConflictReporter();
		
		for (Ignore ign : ignores.getIgnores())
			finder.getIgnores().add(ign);
		
		return finder;
	}
	
	private void handleClassConflictResults(JarClassConflictReporter reporter) throws MojoExecutionException
	{
		boolean dupes;
		FileOutputStream out = null;
		try 
		{
			File outputFile = new File(reportDir + "ClassConflictReport.html");
			if(!outputFile.exists())		
				outputFile.getParentFile().mkdirs();
				
			out = new FileOutputStream(reportDir + "ClassConflictReport.html");
			dupes = reporter.dumpHtml(out, theClasses);
		} 
		catch (IOException e) 	
		{
			throw new MojoExecutionException( "Failed during jar report", e);
		} 
		finally 
		{
			if (out != null)
				try 
				{
					out.close();
				} 
				catch (IOException e) {
					// NOP
				}
		}

		if (dupes == true && failOnDuplicate)
			throw new MojoExecutionException( "Duplicate jars were found, failing build");
	}

	private List<File> getDirsToProcess()
	{
		List<File> dirsToProcess = new ArrayList<File>();
		File folder = new File(pathDir);
		dirsToProcess.add(folder);
		
		return dirsToProcess;
	}
	
	private void handleJarContentResults(JarDirectoryProcessor processor) throws MojoExecutionException
	{
		FileOutputStream out = null;
		try 
		{		
			out = new FileOutputStream(reportDir + "jar-content.html");
			processor.setClassMap(theClasses);
			processor.GenerateReport(out);
		} 
		catch (IOException e) 	
		{
			throw new MojoExecutionException( "Failed during jar report", e);
		} 
		finally 
		{
			if (out != null)
				try 
				{
					out.close();
				} 
				catch (IOException e) {
					// NOP
				}
		}
	}
	
 	public void addIgnores(Ignores ignores) throws MojoExecutionException
 	{
 		if (this.ignores != null) 
 			throw new MojoExecutionException( "Only one 'ignores' element allowed");
    
 		this.ignores = ignores;
 	}

 	public String getReport() 
 	{
 		return reportDir;
 	}

 	public void setReport(String report) 
 	{
 		this.reportDir = report;
 	}
 	
 	public boolean isFailOnDuplicate() 
 	{
 		return failOnDuplicate;
 	}

 	public void setFailOnDuplicate(boolean failOnDuplicate) 
 	{
 		this.failOnDuplicate = failOnDuplicate;
 	}
 	
 	public void setPathDir(String pathDir)
 	{
 		this.pathDir = pathDir;
 	}

}
