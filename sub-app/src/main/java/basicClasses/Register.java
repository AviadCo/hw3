package basicClasses;

public class Register {
	
	final static String REGISTER_SPLITER = " ";
	
	private String userID;
	private String journalID;
	private Long journalPrice;
	private String type;
	
	public static final String SUBSCRIPTION_TYPE = "SUBSCRIPTION_TYPE";
	public static final String CANCEL_TYPE       = "CANCEL_TYPE";
	
	public Register(String userID, String journalID, Long journalPrice, String type) {
		this.userID = userID;
		this.journalID = journalID;
		this.journalPrice = journalPrice;
		this.type = type;
	}
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getJournalID() {
		return journalID;
	}
	public void setJournalID(String journalID) {
		this.journalID = journalID;
	}
	public Long getJournalPrice() {
		return journalPrice;
	}

	public void setJournalPrice(Long journalPrice) {
		this.journalPrice = journalPrice;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public static Register createObject(String s) {
		String arr[] = s.split(REGISTER_SPLITER);
		
		return new Register(arr[0], arr[1], Long.parseLong(arr[2]), arr[3]);
	}

	public static String createString(Register r) {
		return r.userID + REGISTER_SPLITER + r.journalID + REGISTER_SPLITER + r.journalPrice + REGISTER_SPLITER + r.type;
	}
}
