package pt.utl.ist.thesis.datacollector.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileUtils {

	/**
	 * Moves all the files in an origin folder to 
	 * a given destination folder.
	 * 
	 * @param origFolder The origin folder containing 
	 * 					 all the files to be moved.
	 * @param destFolder The destination folder to receive
	 * 					 the moved files.
	 */
	public static void moveAllFilesToDir(File origFolder, Object destFolder) {
		for(File f : origFolder.listFiles()){
			if(f.isFile()){
				try {
					FileUtils.copy(f, new File(destFolder + f.getName()));
					f.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Copies a given file into the specified directory.
	 * (Source: http://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android)
	 * 
	 * @param src The source file to be copied.
	 * @param dst The destination folder to receive the file.
	 * @throws IOException
	 */
	public static void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	
	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

	/**
	 * Deletes all the files inside a given folder.
	 * Does not delete directories inside that folder.
	 *  
	 * @param dir The directory containing the files 
	 * 			  to be deleted.
	 */
	public static void deleteFilesFromDir(File dir) {
		for(File f : dir.listFiles()){
			if(f.isFile()){
				f.delete();
			}
		}
	}

}
