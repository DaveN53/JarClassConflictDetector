package com.attivio.maven;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.maven.plugin.MojoExecutionException;

import com.attivio.maven.util.Ignore;
import com.attivio.maven.util.Ignores;

public class JarContentsConflictDetectorTest extends TestCase {
	
	public JarContentsConflictDetectorTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( JarContentsConflictDetectorTest.class );
    }
    
    public void testJarContentsCataloger()
    {
    	JarContentsConflictDetector detector = new JarContentsConflictDetector();
    	List<Ignore> ignoreList = new ArrayList<Ignore>();
        Ignore ignore = new Ignore("javax/.xml/.namespace/..*", "jaxrpc-.*/.jar", "xml-apis-.*/.jar");
        Ignore ignore2 = new Ignore ("test.TestData.IgnoreThis", "TestJar3.jar", "TestJar4.jar");
        ignoreList.add(ignore);
        ignoreList.add(ignore2);
        Ignores is = new Ignores();
        is.setIgnores(ignoreList);
        
    	try {
    		detector.setPathDir("C:/unscanned/devsetup/trunk/tools/class-conflict-detector/testJars/testJars");
    		detector.addIgnores(is);
    		detector.setReport("build/report/");
    		detector.execute();
		} 
    	catch (MojoExecutionException e) 
		{
			e.printStackTrace();
		}
    
    }
}
