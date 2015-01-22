# JarClassConflictDetector
Maven plugin for searching through .jar file directories, finding conflicting classes among them and reporting what was found. 

##Maven Plugin Goals:
- **detectClassConflict:** This goal will search for all .jar files contained in a given directory. Generate a JarCatalog for all .jar files found. Save that catalog and then ingest it in order to generate a ClassConflictReport.
- **catalogJarContents:** This goal will search for all .jar files contained in a given directory and the generate and save a JarCatalog for all .jar files found.
- **detectJarContentsConflict:** This goal will ingest a JarCatalog from a given directory and generate a ClassConflictReport.

##Plugin Parameters:
- **reportDir:** Directory where all generated report files will be saved
- **pathDir:** Directory to search for .jar files for cataloging and conflict detection 
- **JarCatalogDir:** Directory where existing JarCatalog.xml file can be found for ingestion.
- **kitPath:** SubDirectory to be used under pathDir where .jar files exist. Plugin will search for .jar files under all found instances of this directory under the pathDir directory.
- **failOnDuplicate:** If true build will fail on any found class conflicts. If false execution will continue as normal.
- **ignores:** List of ignore, each of which specify class and jar files to be ignored.
- **ignore:** Parameter holding className and a list of jars to ignore.

##Generated Report:
- **ClassConflictReport:**Full report of any conflicting classes found in the processed .jar files. Includes found conflicts, ignored conflicts, and list of processed jars.
- **Jar-Content:** Report on all .jar files processed and the classes found within each.
- **JarCatalog:** XML file containing all information related to .jar files processed. This file is used by the plugin to store data that has been processed for later ingestion. 

