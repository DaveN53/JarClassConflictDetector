# JarClassConflictDetector
Maven plugin for searching through .jar file directories, finding conflicting classes among them and reporting what was found. 

##Maven Plugin Goals:
- **detectClassConflict**: This goal will search for all .jar files contained in a given directory. Generate a JarCatalog for all .jar files found. Save that catalog and then ingest it in order to generate a ClassConflictReport.
- **catalogJarContents**: This goal will search for all .jar files contained in a given directory and the generate and save a JarCatalog for all .jar files found.
- **detectJarContentsConflict**: This goal will ingest a JarCatalog from a given directory and generate a ClassConflictReport.

##Generated Report:
- **ClassConflictReport**:Full report of any conflicting classes found in the processed .jar files. Includes found conflicts, ignored conflicts, and list of processed jars.
- **Jar-Content**: Report on all .jar files processed and the classes found within each.
- **JarCatalog**: XML file containing all information related to .jar files processed. This file is used by the plugin to store data that has been processed for later ingestion. 

