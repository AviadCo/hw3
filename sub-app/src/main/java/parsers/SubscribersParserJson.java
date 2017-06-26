package parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import basicClasses.Journal;
import basicClasses.Register;

/**
 * This class parse the json data to list of Registers & Journals.
 * 
 * @author Aviad
 *
 */
public class SubscribersParserJson {
	
	private static final String TYPE_TAG              = "type";
	private static final String CANCEL_TYPE_TAG       = "cancel";
	private static final String SUBSCRIPTION_TYPE_TAG = "subscription";
	private static final String JOURNAL_TYPE_TAG      = "journal";
	
	/* Other tags */
	private static final String USER_ID_TAG      = "user-id";
	private static final String JOURNAL_ID_TAG   = "journal-id";
	private static final String PRICE_TAG        = "price";
		
	/**
	 * 
	 * @param jsonData - the json string of the Registers & Journals.
	 * @return - list of registerations
	 */
	@SuppressWarnings("unchecked")
	public static List<Register> createListOfRegisters(String jsonData)
	{
		List<Register> registerations = new ArrayList<Register>();
		
		try {
			((JSONArray) new JSONParser().parse(jsonData)).forEach(item -> {
			    final JSONObject obj = (JSONObject) item;
			    final String type    = (String) obj.get(TYPE_TAG);
			    
			    if (type.equals(SUBSCRIPTION_TYPE_TAG) || type.equals(CANCEL_TYPE_TAG)) {
				    final String userID    = (String) obj.get(USER_ID_TAG);
				    final String journalID = (String) obj.get(JOURNAL_ID_TAG);
				    
				    registerations.add(new Register (userID, journalID, Long.valueOf(0),
				    								 type.equals(SUBSCRIPTION_TYPE_TAG) ? 
				    								 Register.SUBSCRIPTION_TYPE : Register.CANCEL_TYPE));
			    }
			});
		} catch (ParseException e) {
			throw new RuntimeException();
		}
		
	    return registerations;
	}

	/**
	 * 
	 * @param jsonData - the json string of the journals
	 * @return - list of Journal
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Journal> createListOfJournals(String jsonData)
	{
		Map<String, Journal> journalsMap = new HashMap<String, Journal>();
		
		try {
			((JSONArray) new JSONParser().parse(jsonData)).forEach(item -> {				
			    final JSONObject obj = (JSONObject) item;
			    final String type    = (String) obj.get(TYPE_TAG);
			    
			    if (type.equals(JOURNAL_TYPE_TAG)) {
				    final String journalID    = (String) obj.get(JOURNAL_ID_TAG);
				    final Long price   = (Long) obj.get(PRICE_TAG);
				    
				    journalsMap.put(journalID, new Journal(journalID, price));
			    }
			});
		} catch (ParseException e) {
			throw new RuntimeException();
		}
		
	    return journalsMap;
	}
}