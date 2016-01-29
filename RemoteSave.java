package MyDedup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.microsoft.windowsazure.services.blob.client.*;
import com.microsoft.windowsazure.services.core.storage.*;


public class RemoteSave extends Save {
	
	// This class is for remote storage. Configure the key for Azure access.
	// Basically most of this part refers to the tutorial notes.
	

	public static final String storageConnectionString =
		    "DefaultEndpointsProtocol=http;" +
		    "AccountName=group8;" +
		    "AccountKey=COzsBJE1+Su9oCYqBsspnS2AF2K/UZ++JG2R/4BH7F7l+x6sTQQqkl6qUE1un8owLt42tPxk/R72V2J4XhwItA==";
	
	private CloudBlobContainer container;
	
	public RemoteSave(String containerName){
        try{
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            container = blobClient.getContainerReference(containerName);
            container.createIfNotExist();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
	

	@Override
	public boolean addChunk(SHA fingerPrint, byte[] data, int offset,
			int length) {
		String chunkName = fingerPrint.toString();

        try{
            CloudBlockBlob blob = container.getBlockBlobReference(chunkName);
            ByteArrayInputStream chunkStream = new ByteArrayInputStream(data, offset, length);
            blob.upload(chunkStream, length);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
	}

	@Override
	public int getChunk(SHA fingerPrint, byte[] buf, int offset) {
		String chunkName = fingerPrint.toString();

        try{
           CloudBlockBlob blob = container.getBlockBlobReference(chunkName);
           ByteArrayOutputStream bOut = new ByteArrayOutputStream(buf.length);
           blob.download(bOut);
           byte[] tempBuf = bOut.toByteArray();
           int copyLength = Math.min(tempBuf.length - offset, buf.length);
           System.arraycopy(tempBuf, offset, buf, 0, copyLength);
           return copyLength;
       }catch(Exception e){
           e.printStackTrace();
           return 0;
       }
	}

	@Override
	public int sizeOfChunk(SHA fingerPrint) {
		String chunkName = fingerPrint.toString();

        try{
            CloudBlockBlob blob = container.getBlockBlobReference(chunkName);
            blob.downloadAttributes();
            long size =  blob.getProperties().getLength();
            return (int)size;
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
	}

	@Override
	public boolean removeChunk(SHA fingerPrint) {
		String chunkName = fingerPrint.toString();

        try{
            CloudBlockBlob blob = container.getBlockBlobReference(chunkName);
            blob.deleteIfExists();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
	}

}
