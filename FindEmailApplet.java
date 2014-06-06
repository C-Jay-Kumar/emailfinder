/* <applet code="FindEmailApplet" width=810 height=350>
 * </applet>
 */

import java.awt.*;
import java.applet.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.*;

public class FindEmailApplet extends Applet implements Runnable, EmailReceiver
{
    // Components for the GUI
    private Label info, lurl, lmailkeywords, ltextkeywords, ltdepth, lhl, lmail;
    private TextArea taurl, tamailkeywords, tatextkeywords, tahl;
    private Checkbox stoponemailfound;
    private TextField tftdepth;
    private Button startstop, reset;
    private List limail;

    // variables that hold the values implied by the Components
    private String urlst;                        // holds the strings in the URL TextArea
    private String textst;                       // holds the strings in the Text TextArea
    private String mailst;                       // holds the strings in the Mail TextArea
    private boolean stopwhenemailfound;          // Checkbox value
    private int traversal_depth;                 // value of the Traversal Depth TextField

    // instance variables
    private Thread t = null;                             // holds the value of the started Thread
    private boolean proceed = true;                      // should the Thread continue?
    private String mailkeywords = "job\ncareer\nresume\nemploy\nrecruit\nHR\nhr\napply\nstaff\npersonnel\nINFO\nInfo\ninfo\n";
    private String textkeywords = "Job\njob\nEmploy\nemploy\nCareer\ncareer\nOPPORTUNIT\nopportunit\nAbout\nabout\nABOUT\nContact\ncontact\nCONTACT\nCorporate\ncorporate\nCORPORATE\nInfo\ninfo\nINFO\n";
    private String url = "http://www.alpine-la.com/\nhttp://www.tentek.com/\nhttp://www.theboylstongroup.com/\nhttp://www.tadpoleventures.com/\n";
    private Vector textkeys = new Vector();              // holds the text keywords
    private Vector mailkeys = new Vector();              // holds the mail keywords
    private Vector urls = new Vector();                  // holds the urls
    private String emailhost;                            // only emails of the same host will be considered
    private String host;                                 // only webpages of the same host will be traversed

    public void init()
    {
	// not using any of the Java-provided layouts.  will layout the components manually
	setLayout(null);

	// Label Component to display what is going on in the Applet
	info = new Label("Enter Web-page in the URL Edit Box, and click START to search...", Label.LEFT);
	add(info);

	// Label and TextArea Components for the URL
	lurl = new Label("URL", Label.LEFT);
	add(lurl);
	taurl = new TextArea("http://www.careermosaic.com", 0, 0, TextArea.SCROLLBARS_BOTH);
	add(taurl);
	taurl.setText(url);

	// Label and TextArea Components for the Email keywords
	lmailkeywords = new Label("Email keywords", Label.LEFT);
	add(lmailkeywords);
	tamailkeywords = new TextArea("", 10, 20, TextArea.SCROLLBARS_BOTH);
	add(tamailkeywords);
	tamailkeywords.setText(mailkeywords);

	// Label and TextArea Components for the Text keywords
	ltextkeywords = new Label("Text keywords", Label.LEFT);
	add(ltextkeywords);
	tatextkeywords = new TextArea("", 10, 20, TextArea.SCROLLBARS_BOTH);
	add(tatextkeywords);
	tatextkeywords.setText(textkeywords);

	// Checkbox for stopping if an email is found
	stoponemailfound = new Checkbox("Stop when first Email is found.");
	add(stoponemailfound);
	stoponemailfound.setState(true);

	// Label and TextField Components for choosing traversal depth
	ltdepth = new Label("How many pages deep do you want to look?", Label.LEFT);
	add(ltdepth);
	tftdepth = new TextField("3", 3);
	add(tftdepth);

	// Button to start and stop the search
	startstop = new Button("START");
	add(startstop);

	// Button to reset to initial values
	reset = new Button("Reset");
	add(reset);

	// Label and List Components for displaying the Emails entered
	lmail = new Label("Emails Found", Label.LEFT);
	add(lmail);
	limail = new List(0, false);
	add(limail);

	// Label and TextArea Components for displaying the Hyperlinks found
	lhl = new Label("Hyperlinks Visited", Label.LEFT);
	add(lhl);
	tahl = new TextArea("", 15, 40, TextArea.SCROLLBARS_BOTH);
	tahl.setEditable(false);
	add(tahl);

	// A Forms Designer is badly needed here.
	// after much trial and error, the following values are set...
	Rectangle r = new Rectangle(0, 0, 20, 18);
	lurl.setBounds(r);
	r.setBounds(0, 18, 400, 95);
	taurl.setBounds(r);
	r.setBounds(0, 118, 150, 18);
	lmailkeywords.setBounds(r);
	r.setBounds(0, 136, 200, 100);
	tamailkeywords.setBounds(r);
	r.setBounds(200, 118, 150, 18);
	ltextkeywords.setBounds(r);
	r.setBounds(200, 136, 200, 100);
	tatextkeywords.setBounds(r);
	r.setBounds(25, 241, 225, 18);
	stoponemailfound.setBounds(r);
	r.setBounds(25, 263, 275, 18);
	ltdepth.setBounds(r);
	r.setBounds(300, 258, 60, 25);
	tftdepth.setBounds(r);
	r.setBounds(155, 298, 70, 23);
	startstop.setBounds(r);
	r.setBounds(50, 298, 70, 23);
	reset.setBounds(r);
	r.setBounds(0, 331, 400, 18);
	info.setBounds(r);
	r.setBounds(400, 0, 100, 18);
	lmail.setBounds(r);
	r.setBounds(400, 18, 400, 95);
	limail.setBounds(r);
	r.setBounds(400, 118, 150, 17);
	lhl.setBounds(r);
	r.setBounds(400, 135, 400, 215);
	tahl.setBounds(r);
    }

    public boolean action(Event e, Object o)
    {
	if(e.target == startstop) {
	    // if the user wants to start the search
	    if(o.equals("START")) {
		// set the button text to Stop
		startstop.setLabel("STOP!");
		ModifyInfoLabel("Starting Search for Emails.  Please wait...");

		// clear the Mail ListBox and Hyperlink TextArea
		tahl.setText("");
		limail.removeAll();
		
		// obtain the values from the Components
		urlst = taurl.getText();
		mailst = tamailkeywords.getText();
		textst = tatextkeywords.getText();
		try {
		    traversal_depth = Integer.parseInt(tftdepth.getText());
		}
		catch (NumberFormatException nfee) {
		    traversal_depth = 3;
		    tahl.append("**** Error reading Traversal Depth.  Searching 3 levels as the default...\n");
		    tahl.append("********************\n");
		}
		stopwhenemailfound = stoponemailfound.getState();

		// place the TextArea values into the Vectors
		urls.removeAllElements();
		textkeys.removeAllElements();
		mailkeys.removeAllElements();
		StringTokenizer st = new StringTokenizer(urlst);
		while(st.hasMoreTokens())
		    urls.addElement(st.nextToken());
		st = new StringTokenizer(textst);
		while(st.hasMoreTokens())
		    textkeys.addElement(st.nextToken());
		st = new StringTokenizer(mailst);
		while(st.hasMoreTokens())
		    mailkeys.addElement(st.nextToken());

		// create and start the Thread
		t = new Thread(this, "EmailExtractor");
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		t.start();
	    }
	    else if(o.equals("STOP!")) {
		ModifyInfoLabel("Stopping Web Search.  Please wait...");

		// indicate to Thread t that it is not required to continue
		proceed = false;

		// wait for Thread t to die
		try { t.join();	}
		catch(InterruptedException iee) {}

		// reset the Button text and the Info label
		startstop.setLabel("START");
		ModifyInfoLabel("Enter Web-page in the URL Edit Box, and click START to search...");
	    }

	    return true;
	}
	else if(e.target == reset) {
	    taurl.setText(url);
	    tamailkeywords.setText(mailkeywords);
	    tatextkeywords.setText(textkeywords);
	    return true;
	}
	else if(e.target == limail) {
	    try {
		getAppletContext().showDocument(new URL("mailto:" + (String)o), "_self");
	    }
	    catch (MalformedURLException mfue) {
		ModifyInfoLabel(mfue.getMessage());
	    }
	    return true;
	}

	return false;
    }

    public void run()
    {
	// set the thread priority to the minimum as this is a background thread doing 
	// both background networking and background screen output
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

	// call EmailExtractor to extract the emails
	for(int i = 0; i < urls.size(); i++)
	    if(proceed)
		{
		    // set the instance variables to their current values
		    host = InetFunctions.GetHost((String)urls.elementAt(i));
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

		    // extract emails
		    EmailExtractor ex = new EmailExtractor();
		    ex.ExtractAllEmails((String)urls.elementAt(i), this, traversal_depth);

		    // a distinguishing asterisk line
		    tahl.append("********************\n");
		}

	// reset the Button text and the Info label
	startstop.setLabel("START");
	ModifyInfoLabel("Enter Web-page in the URL Edit Box, and click START to search...");
    }

    public boolean ReceiveEmail(String email)
    {
	if(!proceed)
	    return false;

	try {
	    if(email.indexOf(emailhost) != -1)
		{
		    if(mailkeys.size() == 0)
			return true;

		    for(int i = 0; i < mailkeys.size(); i++)
			if(email.indexOf((String)mailkeys.elementAt(i)) != -1)
			    {
				limail.addItem(email);
				if(stopwhenemailfound == true)
				    return false;
				else
				    break;
			    }
		}
	}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	}

	return true;
    }

    public boolean ReceiveHyperlink(String webpage, String text, int traversaldepth)
    {
	if(!proceed)
	    return false;

	// return false  only if the hyperlink is the same host as this web page
	String wphost = InetFunctions.GetHost(webpage);
	if (wphost.equals(host))
	    {
		// for only the first page, check if hyperlink texts contain the text keywords
		if (traversaldepth == traversal_depth)
		    {
			if(textkeys.size() == 0)
			    return true;

			for(int i = 0; i < textkeys.size(); i++)
			    {
				try {
				    if(text.indexOf((String)textkeys.elementAt(i)) != -1)
					return true;
				}
				catch (NullPointerException npe) {
				    System.out.println(npe.getMessage());
				    return false;
				}
			    }
			return false;
		    }
		// for all other pages, go to all the hyperlinks
		else
		    return true;
	    }
	else
	    return false;
    }

    public boolean EnteredWebpage(String webpage)
    {
	if(!proceed)
	    return false;

	tahl.append(webpage + "\n");

	return true;
    }

    synchronized private void ModifyInfoLabel(String str)
    {
	info.setText(str);
    }
}
