import java.lang.*;
import java.util.*;
import java.io.*;

interface SharedConstants
{
    int TRAVERSAL_DEPTH = 5;
}

public class FindEmail implements SharedConstants, EmailReceiver
{
    public static void main(String args[])
    {
	if(args.length < 2 || args.length > 4 || (args.length == 3 && !(args[0].equals("-d"))))
	    // there has to be two arguments, an inputfile and an outputfile
	    System.out.println("Usage: java FindEmail [-d] inputfile outputfile");
	else
	    {
		FindEmail fe;
		// start everything by instantiating an object instance and
		// then calling the function that starts it all.
		if(args.length == 3)
		    fe = new FindEmail(args[1], args[2], true);
		else
		    fe = new FindEmail(args[0], args[1], false);
		fe.addEmailsToFile();
	    }
    }

    private String input_file;  // the file containing the information
    private String output_file; // the file to be output
    private String mailkeywords[] = {"job", "career", "resume", "employ", "recruit",
				 "HR", "hr", "apply", "staff", "personnel", "INFO", "Info", "info"};
    private boolean debug;      // whether debug information is to be printed
    private Vector email_list = new Vector(); // email vector to store obtained emails
    private String host = new String();      // hyperlinks of the same host will only be considered
    private String emailhost = new String(); // emails to the same host will only be considered
    private String textkeywords[] = {"Job", "job", "Employ", "employ", "Career", "career", "OPPORTUNIT", 
				     "opportunit", "About", "about", "ABOUT", "Contact", "contact", "CONTACT",
				     "Corporate", "corporate", "CORPORATE", "Info", "info", "INFO"};

    FindEmail(String input_file, String output_file, boolean debug)
    {
	// set the instance variables to the passed-in parameters
	this.input_file = input_file;
	this.output_file = output_file;
	this.debug = debug;
    }

    public void addEmailsToFile()
    {
	Vector company_list = new Vector(); // stores company homepages

	FileInputStream inputfile;          // input stream
	FileOutputStream outputfile;        // output stream
	PrintStream printfile;              // filter to use print and println

	// open the input stream and the output stream
	try {
	    inputfile = new FileInputStream(input_file);
	    outputfile = new FileOutputStream(output_file);
	    // a read-only file returns FileNotFoundException too.
	    printfile = new PrintStream(outputfile);
	}
	catch (FileNotFoundException fnfee) {
	    System.out.println(fnfee.getMessage());
	    return;
	}

	// set StreamTokenizer to tokenize strings enclosed in quotes, separated
	// by commas and newlines.  So commas become whitespace, and end-of-line
	// becomes significant.  The default options in StreamTokenizer tokenize
	// anything enclosed in quotes as strings, so leave that alone.  Don't
	// reset the contents.  
	Reader r = new BufferedReader(new InputStreamReader(inputfile));
	StreamTokenizer istok = new StreamTokenizer(r);
	istok.eolIsSignificant(true);
	istok.whitespaceChars(44, 44);

	int column_number = 0;        // number of the present column
	boolean emailflag = false;    // true indicates email is missing
	int line_number = 0;

	try {
outer:	    while(istok.ttype != StreamTokenizer.TT_EOF)
		{
		    column_number++;
		    // keep getting the tokens
		    switch(istok.nextToken())
			{
			case StreamTokenizer.TT_EOF:
			    // break, and we get out of the while loop
			    break;
			case StreamTokenizer.TT_EOL:
			    if(column_number != 39)
				// eol can only occur after 38 strings
				throw new ParseException(1);
			    else
				{
				    // reset column number, print a line and continue
				    column_number = 0;
				    printfile.println();
				    // every 100 lines, run the garbage collector
				    if((++line_number % 100) == 0)
					{
					    Runtime current_runtime = Runtime.getRuntime();
					    current_runtime.gc();
					}
				    continue outer;
				}
			case 34:
			    // if there exists no email address, set the email flag and continue
			    if(column_number == 17)
				{
				    if(istok.sval.equals(""))
					{
					    emailflag = true;
					    continue outer;
					}
				}
			    // if string is homepage
			    if(column_number == 18)
				{
				    // if no email is present, find it
				    if(emailflag)
					{
					    // if no homepage exists, or homepage already searched, print
					    // empty string for the email
					    if(istok.sval.equals("") || 
					       company_list.contains(istok.sval) == true)
						printfile.print("\"\",");
					    // if homepage exists, extract the email from the homepage
					    else
						{
						    // instantiate EmailExtractor and get email address, and print it
						    printfile.print("\"" + GetEmailAddress(istok.sval) + "\",");
						}
					    // reset email flag for the next line
					    emailflag = false;
					}
				    // add homepage to searched homepages list.
				    company_list.addElement(istok.sval);
				}

			    // print the present token verbatim
			    printfile.print("\"" + istok.sval + "\"");
			    // except for the last token on the line, print a comma after each token
			    if(column_number != 38)
				printfile.print(",");
			    break;
			default:
			    throw new ParseException(2);
			}
		}
	}
	catch (IOException ioe)
	    {
		System.out.println(ioe.getMessage());
	    }
	catch (ParseException pe)
	    {
		System.out.println(pe);
	    }
    }

    String GetEmailAddress(String homepage)
    {
	if(debug)
	    System.out.println(homepage);

	// set the instance variables to their current values
	host = InetFunctions.GetHost(homepage);
	try {
	    if (host.startsWith("www."))
		emailhost = host.substring(4);
	    else
		emailhost = host;
	}
	catch(Exception e) {
	    System.out.println(e.getMessage());
	    emailhost = host;
	}

	// purge email_list
	email_list.removeAllElements();

	EmailExtractor es = new EmailExtractor();
	// extract all emails from the homepage upto a depth of TRAVERSAL_DEPTH using
	// a breadth-first search of the web pages.
	es.ExtractAllEmails(homepage, this, TRAVERSAL_DEPTH);

	String returnstring;
	try {
	    // return the first element on the assumption that RearrangeEmailList()
	    // has placed the most likely candidate in the front of the list.
	    returnstring = (String) email_list.firstElement();
	    System.out.println(returnstring + " has been extracted from " + homepage + ".");
	}
	catch (NoSuchElementException nsee) {
	    System.out.println(homepage + " does not have an Email Address.");
	    returnstring = "";
	}
	return returnstring;
    }


    // returns false when an email is obtained
    public boolean ReceiveEmail(String email)
    {
	if(debug)
	    System.out.println("MAIL: " + email);

	boolean returnvalue = true;
	try {
	    if(email.indexOf(emailhost) != -1)
		{
		    int keyword = 0;
		    while (keyword < mailkeywords.length)
			{
			    if(email.indexOf(mailkeywords[keyword]) != -1)
				{
				    email_list.addElement(email);
				    returnvalue = false;
				}
			    ++keyword;
			}
		}
	}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	}
	return returnvalue;
    }

    public boolean ReceiveHyperlink(String webpage, String text, int traversaldepth)
    {
	// return false  only if the hyperlink is the same host as this web page
	String wphost = InetFunctions.GetHost(webpage);
	if (wphost.equals(host))
	    {
		// for only the first page, check if hyperlink texts contain the text keywords
		if (traversaldepth == TRAVERSAL_DEPTH)
		    {
			int keyword = 0;
			while (keyword < textkeywords.length)
			    {
				try {
				    if(text.indexOf(textkeywords[keyword]) != -1)
					{
					    if(debug)
						{
						    System.out.println(webpage + " <" + text + ">");
						}
					    // IMPORTANT: return true if you want the weblinks which contain the 
					    // keywords to be added
					    return true;
					}
				}
				catch (NullPointerException npe) {
				    System.out.println(npe.getMessage());
				    return false;
				}
				++keyword;
			    }
			// IMPORTANT: return true if you want the weblinks which do NOT contain the keywords to be
			// added to the list
			return true;
		    }
		// for all other pages, go to all the hyperlinks
		else
		    {
			if(debug)
			    {
				System.out.println(webpage + " <" + text + ">");
			    }
			return true;
		    }
	    }
	else
	    return false;
    }

    // return true always because this function is not doing anything
    public boolean EnteredWebpage(String webpage)
    {
	return true;
    }
}


class ParseException extends Exception
{
    private int exception_code;

    ParseException(int code)
    {
	exception_code = code;
    }

    public String toString()
    {
	String exception_string;

	switch(exception_code)
	    {
	    case 1:
		exception_string = "Unexpected End of Line.";
	    case 2:
		exception_string = "Unknown Token.";
	    default:
		exception_string = "Unknown ParseException.";
	    }

	return "ParseException: " + exception_string;
    }
}
