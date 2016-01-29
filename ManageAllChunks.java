package MyDedup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

import MyDedup.Reference.StorageType;

public class ManageAllChunks implements Serializable {
	
	// FingerPrint -> ChunkInfo
	// This class serves to manage all the information about chunks 

	public HashMap<SHA,Reference> chunks;
	
	public StorageType storageType;
	
	public Save chunkStore;
	
	public ManageAllChunks() {
		chunks = new HashMap<SHA,Reference>();
	}
	
	public Save getUploadChunkStore() {
		return chunkStore;
	}
	
	public StorageType getStorageType() {
		return storageType;
	}
	
	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
        this.chunkStore = Save.getChunkStore(storageType);
	}
	
	public Reference getChunkInfo(SHA fingerPrint) {
		return chunks.get(fingerPrint);
	}
	
	public Reference addChunk(SHA fingerPrint, byte[] data, int offset, int length) {
		Reference chunkInfo = getChunkInfo(fingerPrint);
		if ( chunkInfo!= null ) {
			chunkInfo.increment_ReferCnt();
            return chunkInfo;
		}
		
		chunkStore.addChunk(fingerPrint,data,offset,length);
		chunkInfo = new Reference(storageType,length);
		chunks.put(fingerPrint, chunkInfo);
		return chunkInfo;
	}
	
	public Reference addChunk(byte[] data, int offset, int length) {
        return addChunk(new SHA(data, offset, length), data, offset, length);
    }

	// remove the chunk which has the same hash as fingerPrint. 
	// (decrement the reference count, only truly remove it when its reference count is 0)
	public Reference removeChunk(SHA fingerPrint) {
		Reference chunkInfo = getChunkInfo(fingerPrint);
        if (chunkInfo == null) return null;

        chunkInfo.decrement_ReferCnt();
        if (chunkInfo.getReferenceCount() == 0) {
            chunkInfo.getChunkStore().removeChunk(fingerPrint);
            chunks.remove(fingerPrint);
        }
        return chunkInfo;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(chunks);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        HashMap<SHA, Reference> _chunks = (HashMap<SHA, Reference>)in.readObject();
        chunks = _chunks;
    }

    private void readObjectNoData() throws ObjectStreamException {
        chunks = new HashMap<SHA, Reference>();
    }
	
	
	
}

