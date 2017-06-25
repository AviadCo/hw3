package parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import basicClasses.Journal;
import basicClasses.Register;

/**
 * This class parse the csv data to list of Registers & Journals.
 * 
 * @author Aviad
 *
 */
public class SubscribersParserCsv {
	
	private static final String CANCEL_TYPE_TAG       = "cancel";
	private static final String SUBSCRIPTION_TYPE_TAG = "subscription";
	private static final String JOURNAL_TYPE_TAG      = "journal";
		
	/**
	 * 
	 * @param csvData - the csv string of the Registers & Journals.
	 * @return - list of registerations
	 */
	public static List<Register> createListOfRegisters(String csvData)
	{
		List<String> allData = Arrays.asList(csvData.split("\n"));
		List<Register> registerations = new ArrayList<Register>();
		
		for (String element : allData) {
			String elementArr[] = element.split(",");
			
			if (elementArr[0].equals(SUBSCRIPTION_TYPE_TAG) || elementArr[0].equals(CANCEL_TYPE_TAG)) {
			    registerations.add(new Register (elementArr[1], elementArr[2],
			    		 elementArr[0].equals(SUBSCRIPTION_TYPE_TAG) ? 
						 Register.SUBSCRIPTION_TYPE : Register.CANCEL_TYPE));			}
		}
		
	    return registerations;
	}

	/**
	 * 
	 * @param csvData - the csv string of the journals
	 * @return - list of Journal
	 */
	public static List<Journal> createListOfJournals(String csvData)
	{
		List<String> allData = Arrays.asList(csvData.split("\n"));
		Map<String, Journal> journalsMap = new HashMap<String, Journal>();

		for (String element : allData) {
			String elementArr[] = element.split(",");
			
			if (elementArr[0].equals(JOURNAL_TYPE_TAG)) {
				journalsMap.put(elementArr[1], new Journal(elementArr[1], Long.parseLong(elementArr[2])));
			}
		}
		
	    return new ArrayList<Journal>(journalsMap.values());
	}
}