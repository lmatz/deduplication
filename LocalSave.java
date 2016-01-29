package MyDedup;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class LocalSave extends Save 
{
	
	// This is the class for chunk stored in local. One chunk is one file. One file is one chunk.
	// A chunk's name is just its fingerprint (hash)
	
	
	public String dataDir;

	public LocalSave(String dataDir) 
	{
		this.dataDir = dataDir+"/";
	}
	
	// add a new chunk ( which is a file on local disk) from byte[] data from 'offset' with 'length'
	@Override
	public boolean addChunk(SHA fingerPrint, byte[] data, int offset, int length) {
		String chunkName = fingerPrint.toString();
		try 
		{
			FileOutputStream fileOutput = new FileOutputStream(dataDir + chunkName,true);
			fileOutput.write(data, offset, length);
            fileOutput.close();
            return true;
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}

	// get the chunk which has the same hash as fingerPrint into buf
	@Override
	public int getChunk(SHA fingerPrint, byte[] buf, int offset) {
		String chunkName = fingerPrint.toString();
        try
		{
            FileInputStream fileInput;
			fileInput = new FileInputStream(dataDir + chunkName);
            fileInput.skip(offset);
            int read;
			read = fileInput.read(buf);
            fileInput.close();
            return read;
        }
		catch(Exception e)
		{
            e.printStackTrace();
            return 0;
        }
	}
	
	// find the size of the chunk which has the same hash as fingerPrint
	@Override
	public int sizeOfChunk(SHA fingerPrint) 
	{
		String chunkName;
		File chunkFile;
		chunkName = fingerPrint.toString();

        chunkFile = new File(dataDir + chunkName);
        if( chunkFile.isFile() && chunkFile.exists() )
		{
			int tmp = (int)chunkFile.length();
            return tmp;
        } 
        else 
		{
            return 0;
        }
	}

	
	//remove the chunk which has the same hash as fingerPrint
	@Override
	public boolean removeChunk(SHA fingerPrint) 
	{
		String chunkName;
		File chunkFile;
		chunkName = fingerPrint.toString();

        chunkFile = new File(dataDir + chunkName);
        if( chunkFile.isFile() && chunkFile.exists() )
		{
            chunkFile.delete();
            return true;
        }
        else
		{
            return false;
        }
	}
}

