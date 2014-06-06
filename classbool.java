// encapsulates a boolean so that it can be passed by reference
// Java's Boolean does not allow you to change the value
public class classbool
{
    private boolean value;

    public classbool()
    {
    }

    public classbool(boolean value)
    {
	this.value = value;
    }

    public boolean getValue()
    {
	return value;
    }

    public void setValue(boolean value)
    {
	this.value = value;
    }

    public String toString()
    {
	if (value)
	    return "true";
	else
	    return "false";
    }
}
