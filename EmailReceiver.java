interface EmailReceiver
{
    // a return value of true indicates that EmailExtractor continues to look
    // for more emails on the same page.  false, means that EmailExtractor does 
    // not look for any more emails on that webpage.
    boolean ReceiveEmail(String email);

    // a return value of true indicates that EmailExtractor will add that 
    // webpage if it is within the traversaldepth.  false, indicates that 
    // EmailExtractor will ignore that webpage and not add it.
    boolean ReceiveHyperlink(String webpage, String text, int traversaldepth);

    // a return value of true indicates that EmailExtractor will continue
    // with that webpage, otherwise it will not continue
    boolean EnteredWebpage(String webpage);
}
