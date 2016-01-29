package MyDedup;

import MyDedup.Reference.StorageType;

public abstract class Save {
	
	// This class is the base class for localChunkStore and AzureChunkStore.
	
	private static Save localChunkStore = null;
	
	private static Save remoteChunkStore = null;
	
	public final static Save getChunkStore(StorageType storageType) {
		switch(storageType) {
			case LOCAL:
				if ( localChunkStore==null ) {
					localChunkStore = new LocalSave("chunks");
				}
				return localChunkStore;
			case REMOTE:
				if ( remoteChunkStore == null ) {
					remoteChunkStore = new RemoteSave("yourcontainer");
				}
				return remoteChunkStore;
			default:
				return null;
		}
	}
	
	public abstract boolean addChunk(SHA fingerPrint, byte[] data, int offset, int length);
	
	// a wrapper
	public boolean addChunk(SHA fingerPrint, byte[] data) {
		return addChunk(fingerPrint, data, 0, data.length);
	}
	
	public abstract int getChunk(SHA fingerPrint, byte[] buf, int offset);
		
	// a wrapper
	public byte[] getChunk(SHA fingerPrint) {
		byte[] chunk = new byte[sizeOfChunk(fingerPrint)];
		getChunk(fingerPrint, chunk, 0);
		return chunk;
	}
	
	public abstract int sizeOfChunk(SHA fingerPrint);
	
	public abstract boolean removeChunk(SHA fingerPrint);

}
