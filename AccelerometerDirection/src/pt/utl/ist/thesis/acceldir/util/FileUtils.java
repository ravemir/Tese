package pt.utl.ist.thesis.acceldir.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


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

	/**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     * <i> Note2: Code copied from http://stackoverflow.com/questions/6540906/android-simple-export-import-of-sqlite-database</i>
     * 
     * 
     * @param fromFile	FileInputStream for the file to copy from.
     * @param toFile	FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

	/**
	 * Function used to return the current date, ready to be used in a filename.
	 * 
	 * @return A string representing the date in a "YYYY-MM-DD_HH:MM" format
	 */
	public static String getDateForFilename() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = (SimpleDateFormat) java.text.DateFormat.getDateTimeInstance();
		sdf.applyPattern("yyyy-MM-dd_HH'h'mm");
		String date = sdf.format(c.getTime());
	
		return date;
	}
}
