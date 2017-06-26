package basicClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {

	private final static String USER_SPLITER = ",";
	
	private String userID;
	private List<Register> registerations;
	
	public User(String userID) {
		this.userID = userID;
		this.registerations = new ArrayList<Register>();
	}
	public User(String userID, List<Register> registerationList) {
		this.userID = userID;
		this.registerations = registerationList;
	}
	public List<Register> getRegisterations() {
		return registerations;
	}
	public void setRegisterations(List<Register> registerations) {
		this.registerations = registerations;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public void addRegisteration(Register register) {
		registerations.add(register);
	}
	
	public static User createObject(String s) {
		String arr[] = s.split(USER_SPLITER);
		
		List<Register> registerationList = new ArrayList<Register>();
		for (String registerStr : Arrays.copyOfRange(arr, 2, arr.length)) {			
			registerationList.add(Register.createObject(registerStr));
		}
				
		return new User(arr[0], registerationList);
	}

	public static String createString(User u) {
		String userStr = "";
		
		userStr += u.userID;
		
		for (Register register : u.registerations) {
			userStr += USER_SPLITER + Register.createString(register);
		}
		
		return userStr;
	}
}
