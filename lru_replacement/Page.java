/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

public class Page
{
	private int id;
	private String contents;
   private boolean dirtied;
	
	public Page(int id, String contents)
	{
		this.id = id;
		this.contents = contents;
      this.dirtied = false;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getContents()
	{
		return contents;
	}

   public boolean isDirtied()
   {
      return dirtied;
   }
	
	public void setContents(String contents)
	{
      dirtied = true;
		this.contents = contents;
	}
	
	public String toString()
	{
		return id + " " + contents;
	}
}
