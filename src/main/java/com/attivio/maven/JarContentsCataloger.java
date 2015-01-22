package com.attivio.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.attivio.maven.util.DupJarClassMap;
import com.attivio.maven.util.JarDirectoryProcessor;
import com.attivio.maven.util.XmlSerializer;

/**
*
* @goal catalogJarContents
* 
*/
public class JarContentsCataloger extends AbstractMojo{

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
	
	private DupJarClassMap theClasses = new DupJarClassMap();
	
	@Override
	public void execute() throws MojoExecutionException {
		
		validateParameters();
		
		processDirectoryForJarCatalog();
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
	
	private void processDirectoryForJarCatalog() throws MojoExecutionException
	{
		JarDirectoryProcessor processor = new JarDirectoryProcessor();
		XmlSerializer serializer = new XmlSerializer();
		
		File folder = new File(pathDir);
		
		try 
		{
			if (kitPath != null) processor.setKitPath(kitPath);
			theClasses = processor.search(folder);
			serializer.SerializeClassMapToXml(theClasses, "build/report/JarCatalog.xml");
			handleJarContentResults(processor);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void handleJarContentResults(JarDirectoryProcessor processor) throws MojoExecutionException
	{
		FileOutputStream out = null;
		try 
		{
			out = new FileOutputStream(reportDir + "jar-content.html");
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
	
	public void setReport(String report) 
 	{
 		this.reportDir = report;
 	}
 	
 	public void setPathDir(String pathDir)
 	{
 		this.pathDir = pathDir;
 	}

}
