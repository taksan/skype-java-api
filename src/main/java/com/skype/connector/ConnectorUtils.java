/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - ExtractFormJar methods.
 * Gabriel Takeuchi - mac osx native library improvements
 ******************************************************************************/
package com.skype.connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Connector helper class.
 * Generic helper methods for all connectors.
 */
public final class ConnectorUtils {
    private static List<String> loadedLibraries = new ArrayList<String>();
    
	/**
	 * Check an object if its not null.
	 * If it is a NullPointerException will be thrown.
	 * @param name Name of the object, used in the exception message.
	 * @param value The object to check.
	 */
	public static void checkNotNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("The " + name + " must not be null.");
        }
    }

	/**
	 * Will load the given native library, extracting it from the resources if needed
	 * to a temporary file. This is just a auxiliary method and not to be used outside
	 * the API.
	 * 
	 * @param libraryName
	 * @throws LoadLibraryException If the file could not be loaded for any reason.
	 */
    public static void loadLibrary(String libraryName) throws LoadLibraryException {
        synchronized(loadedLibraries) {
            if (loadedLibraries.contains(libraryName)) {
                return;
            }
            
            try {
                System.loadLibrary(libraryName);
            } catch (UnsatisfiedLinkError err) {
                String libraryFileName = libraryName;
                URL url = ConnectorUtils.class.getResource("/" + libraryFileName);
                if (url == null) {
                	throw new IllegalStateException("Library " + libraryFileName + " is not in the resource path! This is a bug!");
                }
                File libraryFile;
                if(url.getProtocol().toLowerCase().equals("file")) {
                    try {
                        libraryFile = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
                    } catch(UnsupportedEncodingException e) {
                        throw new LoadLibraryException("UTF-8 is not supported encoding.");
                    }
                } else {
                    cleanUpOldLibraryFiles(libraryFileName);
                    libraryFile = createTempLibraryFile(libraryFileName);
                }
                rehydrateFrameworkAtLibraryPath(libraryFile);
                
                try {
                    System.load(libraryFile.getAbsolutePath());            
                } catch (UnsatisfiedLinkError e) {
                    throw new LoadLibraryException("Loading " + libraryFileName + " failed.\n"+e.getMessage());
                }
            }

            loadedLibraries.add(libraryName);
        }
    }

    private static void rehydrateFrameworkAtLibraryPath(File libraryFile) {
    	if (!libraryFile.getName().endsWith("jnilib"))
    		return;
    	
    	try {
	    	File skypeFramework = new File(libraryFile.getCanonicalFile().getParentFile(), "Skype.Framework");
	    	
	    	URL skypeFrameworkResourceUrl = ConnectorUtils.class.getResource("/"+"Skype.Framework");
	    	if(!skypeFramework.getAbsolutePath().equals(skypeFrameworkResourceUrl.getPath())){
	    		InputStream skypeFrameworkStream = ConnectorUtils.class.getResourceAsStream("/"+"Skype.Framework");
	    		writeStreamToFile(skypeFrameworkStream, skypeFramework);
	    	}
    	}
    	catch(IOException e) {
    		throw new IllegalStateException(e);
    	}
	}


	private static void writeStreamToFile(InputStream skypeFrameworkStream,
			File skypeFramework) throws FileNotFoundException,
			IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(skypeFramework);
			int count;
			byte[] buffer = new byte[1024];
			while(0 < (count = skypeFrameworkStream.read(buffer))) {
			    out.write(buffer, 0, count);
			}
		}finally
		{
			if (out != null) {
				out.close();
			}
		}
	}

	private static void cleanUpOldLibraryFiles(final String libraryFileName) {
        final String fileNamePrefix = libraryFileName.substring(0, libraryFileName.indexOf('.'));
        final String extension = libraryFileName.substring(libraryFileName.lastIndexOf('.'));
        for(File file: new File(System.getProperty("java.io.tmpdir")).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(fileNamePrefix) && name.endsWith(extension);
            }
        })) {
            file.delete();
        }
    }

    private static File createTempLibraryFile(String libraryFileName) throws LoadLibraryException {
        InputStream in = ConnectorUtils.class.getResourceAsStream("/" + libraryFileName);
        if(in == null) {
            throw new LoadLibraryException(libraryFileName + " is not contained in the jar.");
        }
        FileOutputStream out = null;
        try {
            final String fileNamePrefix = libraryFileName.substring(0, libraryFileName.indexOf('.'));
            final String extension = libraryFileName.substring(libraryFileName.lastIndexOf('.'));
            File libraryFile = File.createTempFile(fileNamePrefix, extension);
            libraryFile.deleteOnExit();
            writeStreamToFile(in, libraryFile);
            
            return libraryFile;
        } catch(IOException e) {
            throw new LoadLibraryException("Writing " + libraryFileName + " failed.");
        } finally {
            try {
                in.close();
            } catch(IOException e) {
            }
            if(out != null) {
                try {
                    out.close();
                } catch(IOException e) {
                }
            }
        }
    }
    
	/**
	 * The methods of this class should be used staticly.
	 * That is why the constructor is private.
	 */
    private ConnectorUtils() {
    }


    private static String skypeApiTempDir = null;
	public static String getSkypeTempDir() {
		if (skypeApiTempDir != null) {
			if (new File(skypeApiTempDir).exists())
				return skypeApiTempDir;
		}
		
		
		File directory = new File(System.getProperty("java.io.tmpdir"));
		File tempDir;
		try {
			tempDir = File.createTempFile("skype-java-api", "", directory);
			tempDir.delete();
			tempDir.mkdir();
			skypeApiTempDir =  tempDir.getCanonicalPath();
			return skypeApiTempDir;
		} catch (IOException e) {
			throw new RuntimeException("Could not create temporary directory to extract required libraries", e);
		}
	}
}
