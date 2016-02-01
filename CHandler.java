package MyDedup;

import java.io.Closeable;
import java.io.IOException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;
import java.util.Iterator;


// This class servers as a handler for chunking 
public class CHandler implements Iterable<Content>, Closeable {
	
	public Stream input;

    public int windowSize; 
    public int avgChunkSize;
    public int maxChunkSize;
    public int baseParameter;
    public int anchorValue;
    
    public CHandler(InputStream inputStream, int windowSize, int avgChunkSize, int maxChunkSize, int baseParameter, int anchorValue) {
        this.maxChunkSize = maxChunkSize;
        this.baseParameter = baseParameter;
        this.anchorValue = anchorValue;
        this.input = new Stream(inputStream);
        this.windowSize = windowSize;
        this.avgChunkSize = avgChunkSize;
    }
	
	@Override
	public void close() throws IOException 
	{
		input.close();
	}

	@Override
	public Iterator<Content> iterator() 
	{
		return new CIterator(input, windowSize, avgChunkSize, maxChunkSize, baseParameter, anchorValue);
	}
	
}

