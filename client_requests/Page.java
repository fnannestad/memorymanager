/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

public class Page
{
	private int id;
	private String contents;
	
	public Page(int id, String contents)
	{
		this.id = id;
		this.contents = contents;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getContents()
	{
		return contents;
	}
	
	public void setContents(String contents)
	{
		this.contents = contents;
	}
	
	public String toString()
	{
		return id + " " + contents;
	}
	
	public Page clone()
	{
		return new Page(id, contents);
	}
}
