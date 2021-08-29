package common;

import java.io.Serializable;
import java.util.Date;

/**
* Client-Server reply message.
* The object that is being transferred from the server to the client.
*/
public class ReplyMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public RequestType type;
	public Reply reply;
	public int intField;
	public String stringField;
	public boolean boolField1;
	public boolean boolField2;
	public Date dateField;
}
