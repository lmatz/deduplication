package MyDedup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Stream extends BufferedInputStream 
{
	
	// This class serves as a stream to read from chunk
	
	public boolean rnb, mnbr;
	public int nb, mnb;	
	
	public Stream(InputStream in) 
	{
		super(in);
		rnb = false;
	}
	
	public Stream(InputStream in, int size) 
	{
		super(in,size);
		rnb = false;
	}
	
	public boolean isNextByteBuffered() 
	{
        return rnb && nb >= 0;
    }
	
	public boolean hasMoreBytes() throws IOException 
	{
        if ( !rnb )
		{
            nb = super.read();
            rnb = true;
        }
        return nb >= 0;
    }

	public synchronized int fill(byte[] buf, int offset, int len) throws IOException 
	{
		int get;
		int temp;
		get = 0;
		while( len >= 1 ) 
		//while( len > 0 ) 
		{
			temp = read(buf,offset,len);
			//if ( r==-1 ) {
			if ( temp > -2 && temp < 0 ) 
			{
				if (!(get != 0))
					return get-1;
				else
					return get;
				// return read == 0 ? -1 : read;
			}
			get +=temp;
			len -=temp;
			offset +=temp;
		}
		return get;
	}
	
	@Override
    public synchronized int read(byte[] buf, int offset, int len) throws IOException 
	{
		int get;
		get = 0;
        //int read = 0;
        if (rnb) 
		{
            if (nb < 0) 
			{
				rnb = false;
				return -1;
			}
			else
			{
				get++;
				rnb = false;
				len--;
				buf[offset] = (byte)nb;
				offset++;
			}
        }
        if (get > 0 && offset >= buf.length) 
		{
			System.out.println("yes");
			return get;
		}
        else 
		{
            int r = super.read(buf, offset, len);
            if (r < 0) 
			{
                if (get == 0) 
				{
					return -1;
				}
                else 
				{
                    rnb = true;
                    nb = -1;
                    return get;
                }
            } 
			else 
			{
				int go = r+get;
				return go;
			}
        }
    }
	
}

