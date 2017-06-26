package basicClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Journal {

	private final static String JOURNAL_SPLITER = ",";
	
	private String journalID;
	private Long price;
	private List<Register> registerations;
	
	public Journal(String journalID, Long price, List<Register> registerationList) {
		this.journalID = journalID;
		this.price = price;
		this.registerations = registerationList;
	}
	public Journal(String journalID, Long price) {
		this.journalID = journalID;
		this.price = price;
		this.registerations = new ArrayList<Register>();;
	}
	public String getJournalID() {
		return journalID;
	}
	public void setJournalID(String journalID) {
		this.journalID = journalID;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public List<Register> getRegisterations() {
		return registerations;
	}
	public void setRegisterations(List<Register> registerations) {
		this.registerations = registerations;
	}
	public void addRegisteration(Register register) {
		registerations.add(register);
	}
	
	public static Journal createObject(String s) {
		String arr[] = s.split(JOURNAL_SPLITER);
		
		List<Register> registerationList = new ArrayList<Register>();
		for (String registerStr : Arrays.copyOfRange(arr, 2, arr.length)) {			
			registerationList.add(Register.createObject(registerStr));
		}
				
		return new Journal(arr[0], Long.parseLong(arr[1]), registerationList);
	}

	public static String createString(Journal m) {
		String journalStr = "";
		
		journalStr += m.journalID + JOURNAL_SPLITER + m.price;
		
		for (Register register : m.registerations) {
			journalStr += JOURNAL_SPLITER + Register.createString(register);
		}
		
		return journalStr;
	}
}
