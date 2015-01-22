package com.attivio.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import com.attivio.maven.ClassConflictDetector;
import com.attivio.maven.util.Ignores;
import com.attivio.maven.util.Ignore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ClassConflictDetectorTest extends TestCase {
	
	public ClassConflictDetectorTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( ClassConflictDetectorTest.class );
    }
    
    public void testClassConflictDetector()
    {
        ClassConflictDetector conflictDetector = new ClassConflictDetector();
        List<Ignore> ignoreList = new ArrayList<Ignore>();
        Ignore ignore = new Ignore("javax/.xml/.namespace/..*", "jaxrpc-.*/.jar", "xml-apis-.*/.jar");
        Ignore ignore2 = new Ignore ("test.TestData.IgnoreThis", "TestJar3.jar", "TestJar4.jar");
        ignoreList.add(ignore);
        ignoreList.add(ignore2);
        Ignores is = new Ignores();
        is.setIgnores(ignoreList);
        try 
        {
        	//conflictDetector.setPathDir("C:/unscanned/devsetup/trunk/tools/class-conflict-detector/testJars/testJars");
        	conflictDetector.addIgnores(is);
        	conflictDetector.setReport("build\\report\\");
			conflictDetector.execute();
		} 
        catch (MojoExecutionException e) 
        {
			e.printStackTrace();
		}
    }

}
