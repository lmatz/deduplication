package MyDedup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class Reference implements Serializable {
	
	// FYI: all object implements Serializable is used to write and read this object into binary file
	
	
	/* ChunkInfo tells  
	 * 1 this chunk is stored by local or remote
	 * 2 this chunk is referenced by how many files
	 * 3 this chunk's size
	 */
	
	public enum StorageType 
	{
		LOCAL,
		REMOTE,
	}
	
	public StorageType storageType;
	public int referCnt, size;
	
	public Reference(StorageType type, int size) 
	{
		this.referCnt = 1;
		this.size = size;
		this.storageType = type;
	}
	
	public StorageType getStorageType() 
	{
		return storageType;
	}
	
	public int getSize() 
	{
		return size;
	}
	
	
    public void increment_ReferCnt() 
	{
        referCnt++;
    }

    public void decrement_ReferCnt() 
	{
        referCnt--;
    }
    
    public void writeObject(ObjectOutputStream out) throws IOException 
	{
        out.writeObject(storageType);
        out.writeInt(size);
        out.writeInt(referCnt);
    }
	
	
	public int getReferenceCount() 
	{
        return referCnt;
    }
	
	public Save getChunkStore() 
	{
		return Save.getChunkStore(storageType);
	}
	
    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException 
	{
        this.storageType = (StorageType)in.readObject();
        this.size = in.readInt();
        referCnt = in.readInt();
    }
    
    public void readObjectNoData() throws ObjectStreamException 
	{
        referCnt = -1;
        size = referCnt;
    	storageType = null;
    }

}
