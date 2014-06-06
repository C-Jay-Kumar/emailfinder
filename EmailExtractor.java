import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class EmailExtractor
{
    // to keep track of and not repeat going to the same webpages again
    private Vector public_hyperlink_list = new Vector();
    private boolean email_found = false;

    public void ExtractAllEmails(String homepage, EmailReceiver er , int traversaldepth)
    {
	if (traversaldepth == 0 || email_found == true)
	    return;

	String hpStr = InetFunctions.GetHTMLPage(homepage);
	if (hpStr.equals(""))  // not an HTML document
	    return;

	if (!(er.EnteredWebpage(homepage)))
	    return;

	Vector hyperlink_list = new Vector();  // contains all the hyperlinks on the page
	classbool anchor = new classbool();    // true if hyperlink is obtained from an anchor

	int from_index = 0;  // pointer to present position in the String
	// find all the hyperlinks first.  Determine if the hyperlink is an email 
	// address or a web page and store in the corresponding vector.
	do {
	    // get the position of the first found occurrence, and the first found position
	    // is assigned to from_index.
	    from_index = FindFirstOccurrence(hpStr, from_index, anchor);

	    if (from_index == -1)  break;

	    String hypl = new String();
	    String text = new String();
	    try {	    
		// store the string till the next double-quote
		StringBuffer hl = new StringBuffer();
		while(hpStr.charAt(from_index) != '\"')
		    hl.append(hpStr.charAt(from_index++));
		hypl = hl.toString();
		// if anchor, obtain the text
		StringBuffer txt = new StringBuffer();
		text = null;
		if (anchor.getValue())
		    {
			from_index = hpStr.indexOf(">", from_index);
			while(hpStr.charAt(++from_index) != '<')
			    txt.append(hpStr.charAt(from_index));
			text = txt.toString();
		    }
	    }
	    catch(Exception e) {
		System.out.println(e.getMessage());
		hypl = "";
	    }
	    
	    if (hypl.equals(""))
		continue;
	    
	    // if email address, store in the email_list
	    if (hypl.startsWith("mailto:"))
		{
		    String sbstr = hypl.substring(7);
		    if(!(er.ReceiveEmail(sbstr)))
			email_found = true;
		}
	    // else if hyperlink, store in the hyperlink_list, only if this is not the lowest page.
	    // This caveat was added because it was found that if you added the hyperlinks which are
	    // not going to be referred to anyway, because EmailExtractor is going to return as 
	    // traversaldepth will be zero, then sometimes this prevents the same hyperlinks being
	    // added by another page which does not have its traversaldepth equal to one, which means
	    // that the hyperlink would have been accessed, if not for the fact that it had been
	    // added before.
	    else if(traversaldepth != 1)
		{
		    // absolute path.  just add it to the vector.
		    if (hypl.startsWith("http://"))
			{
			    if(public_hyperlink_list.contains(hypl) == false)
				{
				    if (er.ReceiveHyperlink(hypl, text, traversaldepth))
					{
					    public_hyperlink_list.addElement(hypl);
					    hyperlink_list.addElement(hypl);
					}
				}
			}
		    // relative path.  add to vector after concatenating paths
		    else
			{
			    String con_url = InetFunctions.ConcatenateURLs(homepage, hypl);
			    // continue only if the concatenated URL uses the HTTP protocol
			    if (con_url.startsWith("http://"))
				{
				    if(public_hyperlink_list.contains(con_url) == false)
					{
					    if (er.ReceiveHyperlink(con_url, text, traversaldepth))
						{
						    public_hyperlink_list.addElement(con_url);
						    hyperlink_list.addElement(con_url);
						}
					}
				}
			}
		}
	} while (from_index != -1);

	Enumeration hlinks = hyperlink_list.elements();
	// recurse for all the hyperlinks in the vector
	while(hlinks.hasMoreElements())
	    ExtractAllEmails((String)hlinks.nextElement(), er, traversaldepth - 1);
    }


    // Finds the first Occurrence of a hyperlink.  It considers two types of hyperlinks,
    // HREF's and FRAME SRC's.
    private int FindFirstOccurrence(String hpStr, int from_index, classbool anchor)
    {
	int temp_index, temp2_index, final_index;
	boolean frame;

	// URL or url indicates a refresh page, so skip the rest
	final_index = Min(hpStr.indexOf("URL=", from_index), hpStr.indexOf("url=", from_index));
	if (final_index >= 0)
	    {
		final_index += 4;
		return final_index;
	    }

	// find out the first position of either the HREF's or the SRC's.
	temp_index = Min(hpStr.indexOf("HREF=\"", from_index), hpStr.indexOf("href=\"", from_index));
	temp2_index = Min(hpStr.indexOf("FRAME", from_index), hpStr.indexOf("frame", from_index));
	final_index = Min(temp_index, temp2_index);
	if(final_index == temp_index)
	    {
		anchor.setValue(true);
		frame = false;
	    }
	else
	    {
		int temp3_index = Min(hpStr.indexOf("SRC=\"", final_index), hpStr.indexOf("src=\"", final_index));
		final_index = temp3_index;
		anchor.setValue(false);
		frame = true;
	    }

	// add to from_index, the length of HREF=".
	if(final_index >= 0)
	    {
		if(frame)
		    final_index += 5;
		else
		    final_index += 6;
	    }
	
	return final_index;
    }


    // finds the minimum of two numbers if both are positive or both are negative
    // or returns the positive number if one is positive and one is negative
    int Min(int a, int b)
    {
	if(a >= 0 && b >= 0)
	    return Math.min(a, b);
	else if(a < 0 && b < 0)
	    return Math.min(a, b);
	else if(a >= 0)
	    return a;
	else
	    return b;
    }
}
