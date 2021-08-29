package common;

import java.io.Serializable;
import java.util.Date;

/**
* Client-Server request message.
* The object that is being transferred from the client to the server.
*/
public class RequestMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public RequestType type;
	public int intField;
	public String stringField1;
	public String stringField2;
	public Date dateField;
}
