/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

public class Request implements Comparable<Request>
{
	private int clientId;
	private int number;
	private String type;
	private Page page;
	
	public Request(int clientId, int number, String type, int pageId)
	{
		this.clientId = clientId;
		this.number = number;
		this.type = type;
		this.page = new Page(pageId, "");
	}
	
	public Request(int clientId, int number, String type, int pageId, String contents)
	{
		this.clientId = clientId;
		this.number = number;
		this.type = type;
		this.page = new Page(pageId, contents);
	}
	
	public int getClientId()
	{
		return clientId;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public String getType()
	{
		return type;
	}
	
	public Page getPage()
	{
		return page;
	}
	
	public int compareTo(Request r)
	{
		if (r.getNumber() > number)
			return -1;
		else if (r.getNumber() < number)
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return "Request number: " + number + ", Client: " + clientId + ", Type: " 
			+ type + ", Page: " + page.toString();
	}
}
