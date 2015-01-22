package com.attivio.maven;

import org.apache.maven.plugin.MojoExecutionException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JarContentsCatalogerTest extends TestCase {
	
	public JarContentsCatalogerTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( JarContentsCatalogerTest.class );
    }
    
    public void testJarContentsCataloger()
    {
    	JarContentsCataloger cataloger = new JarContentsCataloger();
    	
    	try {
    		cataloger.setPathDir("C:/unscanned/devsetup/trunk/tools/class-conflict-detector/testJars/testJars");
        	cataloger.setReport("build/report/");
			cataloger.execute();
		} 
    	catch (MojoExecutionException e) 
		{
			e.printStackTrace();
		}
    
    }

}
