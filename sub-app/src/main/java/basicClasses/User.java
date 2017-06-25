package basicClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {

	private final static String USER_SPLITER = ",";
	
	private String userID;
	private String magazineID;
	private List<Register> registerations;
	
	public User(String userID, String magazineID, List<Register> registerationList) {
		this.userID = userID;
		this.magazineID = magazineID;
		this.registerations = registerationList;
	}
	public String getMagazineID() {
		return magazineID;
	}
	public void setMagazineID(String magazineID) {
		this.magazineID = magazineID;
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
	
	public static User createObject(String s) {
		String arr[] = s.split(USER_SPLITER);
		
		List<Register> registerationList = new ArrayList<Register>();
		for (String registerStr : Arrays.copyOfRange(arr, 2, arr.length)) {			
			registerationList.add(Register.createObject(registerStr));
		}
				
		return new User(arr[0], arr[1], registerationList);
	}

	public static String createString(User u) {
		String userStr = "";
		
		userStr += u.userID + USER_SPLITER + u.magazineID;
		
		for (Register register : u.registerations) {
			userStr += USER_SPLITER + Register.createString(register);
		}
		
		return userStr;
	}
}
