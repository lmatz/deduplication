package MyDedup;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class MyDedup {
	
	// This is the main class.
	
	private static final String dataFolder = "chunks";
    private static final String metaFileName = "mydedup.meta";
    
    private static final int interestedRFP=0;
    
    public static void main(String[] args) {

    	// the index structure for files. 
    	// It's a map from filename to the index structures of the file(which is fileRecipe) to manages its chunks.
        ManageAllFiles metaData = null;
        
        System.setProperty("http.proxyHost","proxy.cse.cuhk.edu.hk"); 
        System.setProperty("http.proxyPort","8000");

        if(args.length > 0){
            createDataDIRIfNotExist();
        	
        	// open the metadata file
            File meta = new File(metaFileName);
            if( meta.exists() ) {
                metaData = loadMetaData();
            }
            else {
                metaData = new ManageAllFiles();
            }
            
       	    if( args[0].equals("download") ){
            	String downloadFileName = args[1];
            	// check whether the file we want to download has been uploaded
            	ManageOneFile fileRecipe = metaData.getFileRecipe(downloadFileName);
            	if (fileRecipe == null){
                	System.out.println("Error: file not exists");
                	System.exit(0);
	        }
	            ManageAllChunks metaStore = metaData.getMetaStore();
        	    HashMap<SHA,Object> uniqueChunk = new HashMap<SHA,Object>();
	            Object trueValue = new Object();
        	    int numberOfUniqueChunk = 0;
	            int numberOfUniqueBytes = 0;
	            int numberOfBytesReconstructed = 0;
	            Reference chunkInfo = null;
	            byte[] chunk = new byte[20480];
	            int readLength;
	            int chunkSize;
	            try{
	                FileOutputStream fileOutput = new FileOutputStream(downloadFileName, false);
	                for(SHA fingerPrint : fileRecipe){        	
	                	chunkInfo = metaStore.getChunkInfo(fingerPrint);
        	            chunkSize = chunkInfo.getSize();
                    
                	    if (chunk.length < chunkSize) {
                    		chunk = new byte[chunkSize];
                    	    }
             	       readLength = chunkInfo.getChunkStore().getChunk(fingerPrint, chunk, 0);
                	    if (readLength != chunkSize) System.out.println("Inconsistent chunk size. The downloaded file may be corrupted.");
	                    numberOfBytesReconstructed += chunkSize;
	                    fileOutput.write(chunk, 0, chunkSize);

        	            if( !uniqueChunk.containsKey(fingerPrint) ){
	                        uniqueChunk.put(fingerPrint,trueValue);
	                        numberOfUniqueChunk++;
	                        numberOfUniqueBytes += chunkSize;
	                    }
	                }
	                fileOutput.close();
	                printDownloadReport(numberOfUniqueChunk, numberOfUniqueBytes, numberOfBytesReconstructed);
	            }catch(Exception e){
	                System.out.println("Error: downloading file: " + downloadFileName);
	                e.printStackTrace();
	                System.exit(1);
	            }
	    }
            // upload file
            else if( args[0].equals("upload") ){
                int min = Integer.parseInt(args[1]); // min Chunk
                int avg = Integer.parseInt(args[2]); // avg Chunk
                int max = Integer.parseInt(args[3]); // max Chunk
                int baseParameter = Integer.parseInt(args[4]); // d
                String fileToUpload = args[5]; // file to upload
                String localOrRemote = args[6]; // use local disk or Azure cloud storage

                // FileRecipe  . Check whether the file already exists.
                ManageOneFile fileExist = metaData.getFileRecipe(fileToUpload);
                if (fileExist != null){
                	// the file we want to upload already exists! Contradict with the assumption!
                    System.out.println("Error: File exits");
                    System.exit(0);
                }
                
                // use local disk
                if ( localOrRemote.equals("local") ){
                    metaData.setStorageType(Reference.StorageType.LOCAL);
                }
                // use Azure cloud
                else if ( localOrRemote.equals("remote") ){
                    metaData.setStorageType(Reference.StorageType.REMOTE);
                }
                               
                // add the file
                metaData.addFile(fileToUpload, min, avg, max, baseParameter, interestedRFP);
                
                // update our metaData for the new file
                updateMetaData(metaData);
                // report the statistics
                printUploadReport(metaData.dataToReport.totalChunks, metaData.dataToReport.uniqueChunks,
                		metaData.dataToReport.bytesWithDedup, metaData.dataToReport.bytesWithoutDedup);
            }
            else if( args[0].equals("delete") ){
                String deleteFileName = args[1];
                List<Reference> chunksInfo = null;
                try{
                	// get all the chunks currently only referenced by the file 
                	// ( which means the reference count is 1) we need these chunks information for the calculation below
                    chunksInfo = metaData.removeFile(deleteFileName);
                }
                catch (Exception e){
                    System.out.println("Error: deleting file: " + deleteFileName);
                    e.printStackTrace();
                    System.exit(1);
                }
                // the file we want to remove doesn't exist
                if (chunksInfo == null){
                    System.out.println("Error: file not exists");
                    System.exit(0);
                }
                
                // calculate how many chunks and bytes we remove
                long count=0, sizeCount=0;
                for (Reference chunkInfo: chunksInfo){
                    count += 1;
                    sizeCount += chunkInfo.getSize();
                }
                // update the metaData
                updateMetaData(metaData);
                // report the statistics
                printDeleteReport(count, sizeCount);
            }
	    else{
                System.out.println("Invalid command: " + args[0]);
            }
        }
        else{
        	// when the parameter is wrong
            printUsage();
        }
    }
    
    private static void createDataDIRIfNotExist(){
        File dataDIR = new File(dataFolder);
        if(!dataDIR.exists()){
            try{
                dataDIR.mkdir();
            }catch(Exception e){
                System.out.println("Error: Creating data directory");
                e.printStackTrace();
            }
        }
    }
    
    // read the 'mydedup.meta' to get the index structure
    private static ManageAllFiles loadMetaData(){
        try{
        	// open the input of mydedup.meta
            FileInputStream fileInput = new FileInputStream(metaFileName);
            ObjectInputStream objectInput = new ObjectInputStream(fileInput);
            // read the index structure into recipeStore
            ManageAllFiles recipeStore = (ManageAllFiles)objectInput.readObject();
            objectInput.close();
            fileInput.close();
            return recipeStore;
        }catch(Exception e){
            System.out.println("Error: Loading metadata from mydedup.meta");
            e.printStackTrace();
            return new ManageAllFiles();
        }
    }
    
    // 
    private static void updateMetaData(ManageAllFiles recipeStore) {
    	try{
    		// open the output of mydedup.meta
            FileOutputStream fileOutput = new FileOutputStream(metaFileName, false);
            ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
            // write the index structure into file
            objectOutput.writeObject(recipeStore);
            objectOutput.close();
            fileOutput.close();
        }catch(Exception e){
            System.out.println("Error: Updating metadata file");
            e.printStackTrace();
        }
    }
    
    private static void printUploadReport(long totalChunks, long uniqueChunks, long bytesWithDedup, long bytesWithoutDedup) {
    	System.out.println("Report Output:");
        System.out.println("Total number of chunks in storage: " + totalChunks);
        System.out.println("Number of unique chunks in storage: " + uniqueChunks);
        System.out.println("Number of bytes in storage with deduplication: " + bytesWithDedup);
        System.out.println("Number of bytes in storage without deduplication: " + bytesWithoutDedup);
        if (bytesWithoutDedup != 0) {
            System.out.printf("Deduplication Ratio: %.2f%%\n", 100-(double)bytesWithDedup /bytesWithoutDedup * 100);
        }
    }
    
    private static void printDownloadReport(int numberOfUniqueChunk,int numberOfUniqueBytes,int numberOfBytesReconstructed) {
    	System.out.println("Report Output:");
        System.out.println("Number of chunks downloaded: " + numberOfUniqueChunk);
        System.out.println("Number of bytes downloaded: " + numberOfUniqueBytes);
        System.out.println("Number of bytes reconstructed: " + numberOfBytesReconstructed);
    }
    
    private static void printDeleteReport(long count, long sizeCount ) {
    	System.out.println("Report Output:");
    	System.out.println("Number of chunks deleted: " + count);
        System.out.println("Number of bytes deleted: " + sizeCount);
    }
     
    private static void printUsage() {
    	System.out.println("Usage:");
        System.out.println("java Mydedup upload <min_chunck> <avg_chunk> <max_chunk> <d> <file_to_upload> <local|remote>");
        System.out.println("java Mydedup download <file_to_download>");
        System.out.println("java Mydedup delete <file_to_delete>");
    }
    
}
