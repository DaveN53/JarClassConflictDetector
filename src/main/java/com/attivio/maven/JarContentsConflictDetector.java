package com.attivio.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.attivio.maven.util.ClassMapAggregator;
import com.attivio.maven.util.DupJarClassMap;
import com.attivio.maven.util.JarClassConflictReporter;
import com.attivio.maven.util.Ignore;
import com.attivio.maven.util.Ignores;
import com.attivio.maven.util.XmlDeserializer;

/**
 * 
 * @goal detectJarContentsConflict
 *
 */
public class JarContentsConflictDetector extends AbstractMojo{

	/**
	 * @parameter
	 */
	private Ignores ignores;
	/**
	 * @parameter
	 */
	private String pathDir = System.getProperty("user.dir");
	/**
	 * @parameter
	 */
	private String reportDir = "build\\report\\";
	/**
	 * @parameter
	 */
	private String kitPath = "target\\kit";
	/**
	 * @parameter
	 */
	private boolean failOnDuplicate = true;
	
	private DupJarClassMap theClasses = new DupJarClassMap();
	private JarClassConflictReporter reporter = new JarClassConflictReporter();
	@Override
	public void execute() throws MojoExecutionException 
	{	
		validateParameters();
		
		
		for(Ignore ign: ignores.getIgnores())
			reporter.getIgnores().add(ign);
	
		retrieveClassCatalog();
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
	
	private void retrieveClassCatalog()
	{
		XmlDeserializer deserializer = new XmlDeserializer();
		ClassMapAggregator aggregator = new ClassMapAggregator();
		theClasses = deserializer.DeserializeXmlToClassMap("build/report/JarCatalog.xml");

		String trunkClassCatalogPath = getTrunkJarCatalogPath();
		if (trunkClassCatalogPath != null)
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
	
	private void handleClassConflictResults(JarClassConflictReporter reporter) throws MojoExecutionException
	{
		boolean dupes;
		FileOutputStream out = null;
		try 
		{
			out = new FileOutputStream(reportDir + "ClassConflictReport.html");
			dupes = reporter.dumpHtml(out, theClasses);
		} 
		catch (IOException e) 	
		{
			throw new MojoExecutionException("Failed during jar report", e);
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
			throw new MojoExecutionException("Duplicate jars were found, failing build");
	}
	
	public void addIgnores(Ignores ignores) throws MojoExecutionException
 	{
 		if (this.ignores != null) 
 			throw new MojoExecutionException("Only one 'ignores' element allowed");
    
 		this.ignores = ignores;
 	}

 	public void setReport(String report) 
 	{
 		this.reportDir = report;
 	}
 	
 	public void setPathDir(String pathDir)
 	{
 		this.pathDir = pathDir;
 	}

}
