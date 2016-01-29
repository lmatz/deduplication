package MyDedup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class ManageOneFile implements Serializable, Iterable<SHA> {
	
	// this class 

	public ArrayList<SHA> chunks;
	
	public ManageAllChunks metaStore;
	
	public ManageOneFile(ManageAllChunks metaStore) {
		chunks = new ArrayList<SHA>();
		this.metaStore = metaStore;
	}
	
	public ManageAllChunks getMetaStore() {
		return metaStore;
	}
	
	// add byte[] data from its 'offset' with 'length' as a chunk and update the arraylist with adding the fingerPrint 
	public Reference appendChunk(SHA fingerPrint, byte[] data, int offset, int length) {
		Reference chunkInfo = metaStore.addChunk(fingerPrint, data, offset, length);
        chunks.add(fingerPrint);
        return chunkInfo;
	}
	
	// wrapper 
	public Reference appendChunk(byte[] data, int offset, int length) {
        return appendChunk(new SHA(data, offset, length), data, offset, length);
    }
	
	@Override
	public Iterator<SHA> iterator() {
		return chunks.iterator();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(chunks);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        ArrayList<SHA> _chunks = (ArrayList<SHA>)in.readObject();
        chunks = _chunks;
        metaStore = null;
    }

    private void readObjectNoData() throws ObjectStreamException {
        chunks = new ArrayList<SHA>();
        metaStore = null;
    }
	
}
