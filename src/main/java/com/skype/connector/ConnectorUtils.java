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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	 * Extract a file for a jarfile on the classpath.
	 * Used to extract library files to the System temp directory.
	 * <code>System.getProperty("java.io.tmpdir")</code>.
	 * @param filename The file to search and extract.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJarToTemp(String filename) {
        return extractFromJar(filename, filename, System.getProperty("java.io.tmpdir"));
	}
	
	/**
	 * Extract a file for a jarfile on the classpath.
	 * Used to extract library files.
	 * @param filename The file to search and extract.
	 * @param destinationDirectory The directory to place it in.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJar(String filename, String destinationDirectory) {
        return extractFromJar(filename, filename, destinationDirectory);
	}
	
	/**
	 * Extract a file from a jarfile on the classpath.
	 * Used to extract library files.
	 * @param searchString The path+filename to search for.
	 * @param filename The file to search and extract.
	 * @param destinationDirectory The directory to place it in.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJar(String searchString, String filename, String destinationDirectory) {
        boolean extracted = extractFromJarZipMethod(filename, filename, destinationDirectory);
        if (!extracted) {
            extractFromJarUsingClassLoader(filename, filename, destinationDirectory);
        }
        return extracted;
    }
    
    private static boolean extractFromJarZipMethod(String searchString, String filename, String destinationDirectory) {
        boolean extracted = false;
    	String classpath = getExtendedClasspath();
    	File jarfile = null;
    	byte[] buf = new byte[1024];
    	String jarFileName;
    	StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    	//Try each jarfile on the classpath.
    	while (st.hasMoreTokens() && !extracted) {
    		jarFileName = st.nextToken();
    		jarfile = new File(jarFileName);
    		//Only try to read the jarfile if it exists.
    		if (jarfile.exists() && jarfile.isFile()) {
    			 //Check the contents of this Jar file for the searchstring.
    			 FileInputStream fis = null;
				try {
					fis = new FileInputStream(jarFileName);
	    	        BufferedInputStream bis=new BufferedInputStream(fis);
	    	        ZipInputStream zis=new ZipInputStream(bis);
	    	        ZipEntry ze=null;
	    	        while ((ze=zis.getNextEntry())!=null) {
					     if (ze.getName().endsWith(searchString)){
					    	 //File found, now try to extract it.
					     	if (!destinationDirectory.endsWith(File.separator)) { 
					     		destinationDirectory = destinationDirectory+File.separator;
					     	}
                            // First delete old file.
                            File destFile = new File(destinationDirectory+filename);
                            if (destFile.exists()) {
                                destFile.delete();
                            }
					    	 int n;
					    	 FileOutputStream fileoutputstream;
				                fileoutputstream = new FileOutputStream(destinationDirectory+filename);             
					                while ((n = zis.read(buf, 0, 1024)) > -1) {
					                    fileoutputstream.write(buf, 0, n);
					                }
					                fileoutputstream.close();
					                extracted=true;
                                    destFile.deleteOnExit();
					     }			         		
					}
				} catch (Exception e) {
					e.printStackTrace();
					extracted = false;
				}
    		}
    	}
		return extracted;
	}
	
    /**
     * Extract a file using the ClassLoader.
     * Used to extract library files.
     * @param searchString The path+filename to search for.
     * @param filename The file to search and extract.
     * @param destinationDirectory The directory to place it in.
     * @return true if file could be found and extracted.
     */
    @SuppressWarnings("rawtypes")
	private static boolean extractFromJarUsingClassLoader(String searchString, String filename, String destinationDirectory) {
        ClassLoader cl = null;
        try {
            Class clClass = Class.forName("com.simontuffs.onejar.JarClassLoader");
            Constructor[] constructor = clClass.getConstructors();
            cl = (ClassLoader) constructor[1].newInstance(ClassLoader.getSystemClassLoader());
            System.out.println("Loaded JarClassLoader. cl="+cl.toString());
        } catch (Throwable e) {
            cl = ClassLoader.getSystemClassLoader();
        }
        URL liburl = cl.getResource(filename);
        if (liburl == null ) {
            return false;
        }
        if (!destinationDirectory.endsWith(File.separator)) { 
            destinationDirectory = destinationDirectory+File.separator;
        }
        
        try {
            // First delete old file.
            File destFile = new File(destinationDirectory+filename);
            if (destFile.exists()) {
                destFile.delete();
            }
            
            InputStream is;
            is = liburl.openStream();
            OutputStream os = new FileOutputStream(destinationDirectory+filename);
            byte[] buf = new byte[4096];
            int cnt = is.read(buf);
            while (cnt > 0) {
                os.write(buf, 0, cnt);
                cnt = is.read(buf);
            }
            os.close();
            is.close();
            destFile.deleteOnExit();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
	/**
	 * Search for a file in the jars of the classpath, return true if found.
	 * @param searchString The path+filename to search for.
	 * @return true if file could be found.
	 */
	public static boolean isInJar(String searchString) {
		boolean found = false;
    	String classpath = getExtendedClasspath();
    	File jarfile = null;
    	String jarFileName;
    	StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    	//Try each jarfile on the classpath.
    	while (st.hasMoreTokens() && !found) {
    		jarFileName = st.nextToken();
    		jarfile = new File(jarFileName);
    		//Only try to read the jarfile if it exists.
    		if (jarfile.exists() && jarfile.isFile()) {
    			 //Check the contents of this Jar file for the searchstring.
    			 FileInputStream fis = null;
				try {
					fis = new FileInputStream(jarFileName);
	    	        BufferedInputStream bis=new BufferedInputStream(fis);
	    	        ZipInputStream zis=new ZipInputStream(bis);
	    	        ZipEntry ze=null;
	    	        while ((ze=zis.getNextEntry())!=null) {
					     if (ze.getName().endsWith(searchString)){
					    	 //File found, now try to extract it.
					     	found = true;
					     }			         		
					}
				} catch (Exception e) {
					found = false;
				}
    		}
    	}
		return found;
	}
	
	/**
	 * Return the extended library path on which the JVM looks for lib files.
	 * @return String with extended lib path.
	 */
    private static String getLibrarySearchPath() {
    	return System.getProperty("java.library.path")+File.pathSeparatorChar+System.getProperty("user.dir")+File.pathSeparatorChar;
    }
    
    /**
     * Return the extended class path on which the JVM looks for jar files.
     * It adds the jar files in the application directory, even when not set in the classpath.
     * @return String with extended lib path.
     */
    private static String getExtendedClasspath() {
        String classpath = System.getProperty("java.class.path")+File.pathSeparatorChar;
        String userDirStr = System.getProperty("user.dir");
        File userDir = new File(userDirStr);
        userDirStr = userDir.getAbsolutePath();
        if (!userDirStr.endsWith(File.separator)) {
            userDirStr = userDirStr+File.separator;
        }
        String[] files = userDir.list();
        for (int i=0;i < files.length; i++) {
            if (files[i].endsWith("jar")) {
                classpath = classpath+File.pathSeparatorChar+userDirStr+files[i];
            }
        }
        return classpath;
    }
    
    /**
     * Check if a lib file is to be found by the JVM.
     * @param libFilename the filename.
     * @return true if the file is found.
     */
    public static boolean checkLibraryInPath(String libFilename) {
    	boolean libfilefound = false;
    	String libpath = getLibrarySearchPath();
    	File libfile = new File("");
    	StringTokenizer st = new StringTokenizer(libpath, File.pathSeparator);
    	while (st.hasMoreTokens() && !libfilefound) {
    		libfile = new File(st.nextToken()+File.separatorChar+libFilename);
    		libfilefound = libfile.exists();
    	}
    	return libfilefound;
    }
	
    /** 
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     * @param dir The directory to delete.
     * @return True if deletion is succesfull.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

    public static void loadLibrary(String libraryName) throws LoadLibraryException {
        synchronized(loadedLibraries) {
            if (loadedLibraries.contains(libraryName)) {
                return;
            }
            
            try {
                System.loadLibrary(libraryName);
            } catch (UnsatisfiedLinkError err) {
                String libraryFileName = System.mapLibraryName(libraryName);
                URL url = ConnectorUtils.class.getResource("/" + libraryFileName);
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
	    	InputStream skypeFrameworkStream = ConnectorUtils.class.getResourceAsStream("/"+"Skype.Framework");
	    	writeStreamToFile(skypeFrameworkStream, skypeFramework);
    	}
    	catch(IOException e) {
    		throw new IllegalStateException(e);
    	}
	}


	private static void writeStreamToFile(InputStream skypeFrameworkStream,
			File skypeFramework) throws FileNotFoundException,
			IOException {
		FileOutputStream out = new FileOutputStream(skypeFramework);
		int count;
		byte[] buffer = new byte[1024];
		while(0 < (count = skypeFrameworkStream.read(buffer))) {
		    out.write(buffer, 0, count);
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
}
