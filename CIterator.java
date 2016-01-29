package MyDedup;

import java.io.IOException;
import java.util.Iterator;


public class CIterator implements Iterator<Content> {
	
	//This class serves as a iterator for iteration which is used in -> for( Type a: XXX) {something()};
	
	public Stream stream;
    
	public Content content;
    
	public int windowSize, avgChunkSize, maxChunkSize, baseParameter, interestedRFP, anchorMask;
    
	public long[] precomputations;
    
	public long rfp;
    
	public boolean endOfFile;
	
	public CIterator(Stream stream, int windowSize, int avgChunkSize, int maxChunkSize, int baseParameter, int interestedRFP) {
        this.interestedRFP = interestedRFP ;
        this.content = new Content(new byte[maxChunkSize]);
        this.precomputations = new long[256];
        this.maxChunkSize = maxChunkSize;
        this.baseParameter = baseParameter;
        this.rfp = -1;
        this.endOfFile = false;
        this.stream = stream;
        this.windowSize = windowSize;
        this.avgChunkSize = avgChunkSize;
        this.anchorMask = avgChunkSize - 1;
        computerAheadOfTime();
    }

	public void computerAheadOfTime() {
        long firstMultiplier = ModExp.modExpOpt(baseParameter, windowSize, avgChunkSize);
        for (int i = 0; i < 256; i++)  {
//        	multiplier[i] = (int)(i * firstCharMultiplier) & moduloMask;
        	// Compute d^e mod q
        	precomputations[i] = ModExp.modExpOpt(i*firstMultiplier, 1, avgChunkSize);
        }
    }
	
	// iterator
	@Override
	public boolean hasNext() {
		if ( endOfFile ) {
			return false;
		}
        try {
            if ( stream.hasMoreBytes() ) {
            	return true;
            }
            else {
                endOfFile = true;
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
	}

	
	@Override
	public Content next() {
		try {
            firstWindow();
            while (!endOfFile && rfp != interestedRFP && content.len < maxChunkSize)  {
            	nextWindow();
            }
        } catch (IOException ex) {
            System.out.println("Error during chunking.");
        }
        rfp = -1;
        return content;
	}
	
	public void firstWindow() throws IOException {
        rfp = 0;
        content.len = stream.fill(content.bbb, content.os, windowSize);
        if (content.len < windowSize) {
            endOfFile = true;
            return;
        }

        for (int i = 0; i < windowSize; i++) {
//            rfp = (rfp * baseParameter) & anchorMask;
        	rfp = ModExp.modExpOpt(rfp*baseParameter,1,avgChunkSize);
//            rfp = (rfp + byteToInt(content.buf[content.offset + i])) & anchorMask;
        	rfp = ModExp.modExpOpt(rfp + byteToInt(content.bbb[content.os + i]), 1, avgChunkSize);
        }
    }
	
	public void nextWindow() throws IOException {
        int nextByte = stream.read();
        if (nextByte < 0) {
            endOfFile = true;
            return;
        }
//        rfp = (rfp * baseParameter - multiplier[byteToInt(content.buf[content.offset + content.length - windowSize])] + nextByte) & anchorMask;
        rfp = ModExp.modExpOpt(rfp * baseParameter - precomputations[byteToInt(content.bbb[content.os + content.len - windowSize])] + nextByte, 1, avgChunkSize);
        content.bbb[content.os + content.len++] = (byte)nextByte;
    }
	
	// turn btyes to int
	public static int byteToInt(byte b) {
        return (int)b & 0xff;
    }

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
