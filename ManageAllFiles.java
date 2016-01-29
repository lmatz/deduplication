package MyDedup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import MyDedup.Reference.StorageType;

public class ManageAllFiles implements Serializable {

	public class Statistics implements Serializable {
		public int totalChunks;
		
		public int uniqueChunks;
		
		public long bytesWithDedup;
		
		public long bytesWithoutDedup;
		
		public double spaceSaving;
		
		public Statistics() {
			this.totalChunks = 0;
			this.uniqueChunks = 0;
			this.bytesWithDedup = 0;
			this.bytesWithoutDedup = 0;
		}
		
		public void increaseTotalChunks(int increase) {
			this.totalChunks += increase;
		}
		
		public void decreaseTotalChunks(int decrease) {
			this.totalChunks -= decrease;
		}
		
		public void increaseUniqueChunks(int increase) {
			this.uniqueChunks += increase;
		}
		
		public void decreaseUniqueChunks(int decrease) {
			this.uniqueChunks -= decrease;
		}
		
		public void increaseBytesWithDedup(int increase) {
			this.bytesWithDedup += increase;
		}
		
		public void decreaseBytesWithDedup(int decrease) {
			this.bytesWithDedup -= decrease;
		}
		
		public void setBytesWithDedup(long newUpload) {
			this.bytesWithDedup = newUpload;
		}
		
		public void increaseBytesWithoutDedup(int increase) {
			this.bytesWithoutDedup += increase;
		}
		
		public void decreaseBytesWithoutDedup(int decrease) {
			this.bytesWithoutDedup -= decrease;
		}
		
		public void setBytesWithoutDedup(long newUpload) {
			this.bytesWithoutDedup = newUpload;
		}
		
	}
	
	// File name -> File recipe(File recipe is used by one file to manage its related chunks)
	
	public Statistics dataToReport;
	
	public HashMap<String, ManageOneFile> files;
	
	public ManageAllChunks metaStore;
	
	public ManageAllFiles() {
		dataToReport = new Statistics();
		files = new HashMap<String, ManageOneFile>();
		metaStore = new ManageAllChunks();
	}
	
	public ManageAllChunks getMetaStore() {
		return metaStore;
	}
	
	public void setMetaStore(ManageAllChunks metaStore) {
		this.metaStore = metaStore;
	}
	
	public Save getUploadChunk() {
		return metaStore.getUploadChunkStore();
	}

	public void setStorageType(StorageType storageType) {
		metaStore.setStorageType(storageType);
	}
	
	public StorageType getStorageType() {
		return metaStore.getStorageType();
	}

	public ManageOneFile getFileRecipe(String fileName) {
		return files.get(fileName);
	}
	
	
	// create a new index structure for the file  to manage its chunks and update the hashmap
	public ManageOneFile createFile(String fileName) {
		ManageOneFile fileRecipe = new ManageOneFile(metaStore);
		files.put(fileName,fileRecipe);
		return fileRecipe;
	}
	
	
	// add a file. 
	public void addFile(String fileToUpload, int min, int avg, int max, int baseParameter, int anchorValue) {
        // try to read the file we want to upload
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(fileToUpload);
        }
        catch (FileNotFoundException e){
            System.err.println("Error: Can not open file: " + fileToUpload);
            e.printStackTrace();
            System.exit(1);
        }
        
        // Chunker is a handler which is responsible for the process of chunking
        CHandler chunker = new CHandler(fileStream, min, avg, max, baseParameter,anchorValue);
        long count = 0, uniqueCount = 0, byteDedupCount = 0, byteNoDedupCount = 0; // for the statistics report
        try {
        	// Since we want to add a new file, So we need to create a index structure for this file to manage its chunks
            ManageOneFile fileRecipe = createFile(fileToUpload);
            for (Content chunkData : chunker) {
                Reference uploadChunkInfo = fileRecipe.appendChunk(chunkData.bbb, chunkData.os, chunkData.len);
                  dataToReport.increaseTotalChunks(1);
//                count++;// how many chunks altogether?
                byteNoDedupCount += uploadChunkInfo.getSize(); // the bytes we have to upload cause no duplication yet
                  dataToReport.increaseBytesWithoutDedup(uploadChunkInfo.getSize());
                if (uploadChunkInfo.getReferenceCount() <= 1){
                    byteDedupCount += uploadChunkInfo.getSize(); // the bytes we can save cause duplication
//                    uniqueCount++;
                	dataToReport.increaseBytesWithDedup(uploadChunkInfo.getSize());
                	dataToReport.increaseUniqueChunks(1);
                }
            }
            dataToReport.setBytesWithDedup(byteDedupCount);
            dataToReport.setBytesWithoutDedup(byteNoDedupCount);
            chunker.close();
        }
        catch (Exception e){
            System.err.println("Error: Uploading file "+ fileToUpload);
            e.printStackTrace();
            System.exit(1);
        }
        
	}
	
	// first get the index structure for the file and get all chunks of this file 
	// (if chunk is only referenced by this file, then delete it. otherwise decrement the reference count
	public List<Reference> removeFile(String fileName) {
        ManageOneFile fileRecipe = files.get(fileName);
        if (fileRecipe == null) {
        	return null;
        }

        ArrayList<Reference> removedChunks = new ArrayList<Reference>();
        Reference chunkInfo;
        for (SHA fingerPrint : fileRecipe) {
            chunkInfo = metaStore.removeChunk(fingerPrint);
            dataToReport.decreaseTotalChunks(1);
            dataToReport.decreaseBytesWithoutDedup(chunkInfo.getSize());
            if (chunkInfo.getReferenceCount() == 0) {
            	removedChunks.add(chunkInfo);
            	dataToReport.decreaseUniqueChunks(1);
            	dataToReport.decreaseBytesWithDedup(chunkInfo.getSize());
            }
        }
        files.remove(fileName);
        return removedChunks;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.writeObject(dataToReport);
        out.writeObject(files);
        out.writeObject(metaStore);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Statistics _dataToReport = (Statistics)in.readObject();
        dataToReport = _dataToReport;
        HashMap<String, ManageOneFile> _files = (HashMap<String, ManageOneFile>)in.readObject();
        files = _files;
        metaStore = (ManageAllChunks)in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        files = new HashMap<String, ManageOneFile>();
        metaStore = new ManageAllChunks();
        dataToReport = new Statistics();
    }
	
}
