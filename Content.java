package MyDedup;


public class Content 
{
	public int os, len;
	public byte[] bbb;
	
	public Content(byte[] bbb) 
	{
		this.os = 0;
		this.len = this.os;
		this.bbb = bbb;
	}
	
	public Content() 
	{
		this.os = 0;
		this.len = this.os;
		this.bbb = null;
	}
	
	
}
