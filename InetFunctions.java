import java.lang.*;
import java.io.*;
import java.net.*;

public class InetFunctions
{
    public static final String GetHTMLPage(String pageURL)
    {
	String hpStr = new String("");

	try {
	    URL hp = new URL(pageURL);
	    URLConnection hpCon = hp.openConnection();
	    String mime_type = hpCon.getContentType();
	    if (!(mime_type.equals("text/html")))
		return "";

	    InputStream hpInput = hpCon.getInputStream();
	
	    // read the contents of the InputStream into the StringBuffer    
	    int c;
	    StringBuffer sb = new StringBuffer();
	    while (((c = hpInput.read()) != -1))
		sb.append((char)c);
	    
	    // fill the String
	    hpStr = sb.toString();

	    hpInput.close();
	}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	    return "";
	}

	return hpStr;
    }

    public static final String GetHost(String pageURL)
    {
	String host;
	
	try {
	    URL hp = new URL(pageURL);
	    host = hp.getHost();
	}
	catch (MalformedURLException mfue) {
	    System.out.println(mfue.getMessage());
	    host = "";
	}

	return host;
    }

    public static final String ConcatenateURLs(String page, String appendage)
    {
	String returnstring;

	try {
	    URL hp = new URL(page);
	    URL retURL = new URL(hp, appendage);
	    returnstring = retURL.toString();
	}
	catch (MalformedURLException mfue) {
	    System.out.println(mfue.getMessage());
	    returnstring = "";
	}

	return returnstring;
    }
}
