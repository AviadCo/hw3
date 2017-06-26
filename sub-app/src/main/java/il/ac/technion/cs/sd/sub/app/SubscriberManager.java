package il.ac.technion.cs.sd.sub.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import basicClasses.Journal;
import basicClasses.Register;
import basicClasses.User;
import library.Dict;
import parsers.SubscribersParserCsv;
import parsers.SubscribersParserJson;

public class SubscriberManager implements SubscriberInitializer, SubscriberReader {

	/* Databases names */
	public static final String USERS_DICT_NAME    = "USERS_DICT";
	public static final String JOURNALS_DICT_NAME = "JOURNALS_DICT";
	
	@Inject
	public SubscriberManager(@Named(USERS_DICT_NAME) Dict users, @Named(JOURNALS_DICT_NAME) Dict journals) {
		this.users    = users;
		this.journals = journals;
	}
	
	Dict users;
	Dict journals;
	
	private CompletableFuture<Void> initializeDictionaries(List<Register> registerations,
										Map<String, Journal> journalsWithoutRegisterations) {	
		List<Register> validRegisterations = new ArrayList<Register>();
		Map <String, User> usersMap = new HashMap<String, User>();
		
		for (Register register : registerations) {
			if (journalsWithoutRegisterations.containsKey(register.getJournalID())) {
				Journal journal = journalsWithoutRegisterations.get(register.getJournalID());
				
				register.setJournalPrice(journal.getPrice());
				
				journal.addRegisteration(register);
				journalsWithoutRegisterations.put(register.getJournalID(), journal);
				
				/* Registration is valid only journalID exists */
				validRegisterations.add(register);
			}
		}
		
		for (Register register : validRegisterations) {
			if (usersMap.containsKey(register.getUserID())) {
				User user = usersMap.get(register.getUserID());
				
				user.addRegisteration(register);
				
				usersMap.put(register.getUserID(), user);
			} else {
				User user = new User(register.getUserID());
				
				user.addRegisteration(register);
				
				usersMap.put(register.getUserID(), user);
			}
		}
				
		users.addAll(usersMap
				.entrySet()
			     .stream()
			     .collect(Collectors.toMap(e -> e.getKey(), e -> User.createString(e.getValue()))));
		journals.addAll(journalsWithoutRegisterations
				.entrySet()
			     .stream()
			     .collect(Collectors.toMap(e -> e.getKey(), e -> Journal.createString(e.getValue()))));
		
		//TODO check failures
		users.store();
		journals.store();
		
		return null;
	}
	
	@Override
	public CompletableFuture<Void> setupCsv(String csvData) {
		List<Register> registerations = SubscribersParserCsv.createListOfRegisters(csvData);
		Map<String, Journal> journalsWithoutRegisterations = SubscribersParserCsv.createListOfJournals(csvData);
		
		return initializeDictionaries(registerations, journalsWithoutRegisterations);
	}

	@Override
	public CompletableFuture<Void> setupJson(String jsonData) {
		List<Register> registerations = SubscribersParserJson.createListOfRegisters(jsonData);
		Map<String, Journal> journalsWithoutRegisterations = SubscribersParserJson.createListOfJournals(jsonData);
		
		return initializeDictionaries(registerations, journalsWithoutRegisterations);
	}
	
	private CompletableFuture<Optional<Boolean>> isWasSubscribed(String userId, String journalId, boolean was) {
		return users.find(userId).thenApply(userStr -> {
			if (!userStr.isPresent()) {
				return Optional.empty();
			}
			
			User user = User.createObject(userStr.get());
			List<Register> relevantRegisteration = user.getRegisterations()
					.stream()
					.filter(r -> r.getJournalID().equals(journalId))
					.collect(Collectors.toList());
			
			if (relevantRegisteration.isEmpty()) {
				return Optional.of(false);
			} else if (was) {
				/* wasSubscribed */
				return Optional.of(true);
			} else {
				/* isSubscribed */
				return Optional.of(relevantRegisteration.get(relevantRegisteration.size() - 1)
										.getType()
										.equals(Register.SUBSCRIPTION_TYPE));
			}			
		});
	}
	
	private CompletableFuture<Optional<Boolean>> isWasCanceled(String userId, String journalId, boolean was) {
		return users.find(userId).thenApply(userStr -> {
			if (!userStr.isPresent()) {
				return Optional.empty();
			}
			
			User user = User.createObject(userStr.get());
			List<Register> relevantRegisteration = user.getRegisterations()
					.stream()
					.filter(r -> r.getJournalID().equals(journalId))
					.collect(Collectors.toList());
			
			if (relevantRegisteration.isEmpty()) {
				return Optional.of(false);
			} else if (was) {
				/* wasCanceled */
				return Optional.of(relevantRegisteration
						.stream()
						.filter(r -> r.getType().equals(Register.CANCEL_TYPE))
						.count() > 0);
			} else {
				/* isCanceled */
				return Optional.of(relevantRegisteration.get(relevantRegisteration.size() - 1)
										.getType()
										.equals(Register.CANCEL_TYPE));
			}			
		});
	}
	
	@Override
	public CompletableFuture<Optional<Boolean>> isSubscribed(String userId, String journalId) {
		return isWasSubscribed(userId, journalId, false);
	}

	@Override
	public CompletableFuture<Optional<Boolean>> wasSubscribed(String userId, String journalId) {
		return isWasSubscribed(userId, journalId, true);
	}

	@Override
	public CompletableFuture<Optional<Boolean>> isCanceled(String userId, String journalId) {
		return isWasCanceled(userId, journalId, false);
	}

	@Override
	public CompletableFuture<Optional<Boolean>> wasCanceled(String userId, String journalId) {
		return isWasCanceled(userId, journalId, true);
	}

	@Override
	public CompletableFuture<List<String>> getSubscribedJournals(String userId) {
		return users.find(userId).thenApply(userStr -> {
			List<String> journals = new ArrayList<String>();
			
			if (!userStr.isPresent()) {
				return journals;
			}
			
			User user = User.createObject(userStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = user.getRegisterations().stream().collect(Collectors.groupingBy(Register::getJournalID));
			
			for (List<Register> registerations : registerationsPerJournalID.values()) {
				if (registerations.get(registerations.size() - 1).getType().equals(Register.SUBSCRIPTION_TYPE)) {
					journals.add(registerations.get(0).getJournalID());
				}
			}
			
			return journals;		
		});
	}

	@Override
	public CompletableFuture<Map<String, List<Boolean>>> getAllSubscriptions(String userId) {
		return users.find(userId).thenApply(userStr -> {
			Map<String, List<Boolean>> journals = new HashMap<String, List<Boolean>>();
			
			if (!userStr.isPresent()) {
				return journals;
			}
			
			User user = User.createObject(userStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = user.getRegisterations().stream().collect(Collectors.groupingBy(Register::getJournalID));
			
			for (List<Register> registerations : registerationsPerJournalID.values()) {
				List<Boolean> journalList = new ArrayList<Boolean>();
				
				for (Register registeration : registerations) {
					if (registeration.getType().equals(Register.SUBSCRIPTION_TYPE)) {
						journalList.add(true);
					} else if (journalList.isEmpty()) {
						/* Registration is cancel type and the list is empty */
						journalList.add(false);
					} else if (journalList.get(journalList.size() - 1)) {
						/* Registration is cancel type and the list is not empty and last registeration wasn't cancel */
						journalList.add(false);
					}
				}
				
				journals.put(registerations.get(0).getJournalID(), journalList);
			}
			
			return journals;		
		});
	}

	@Override
	public CompletableFuture<OptionalInt> getMonthlyBudget(String userId) {
		return users.find(userId).thenApply(userStr -> {
			Integer budget = 0;
			
			if (!userStr.isPresent()) {
				return OptionalInt.empty();
			}
			
			User user = User.createObject(userStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = user.getRegisterations().stream().collect(Collectors.groupingBy(Register::getJournalID));

			for (List<Register> registerations : registerationsPerJournalID.values()) {
				if (registerations.get(registerations.size() - 1).getType().equals(Register.SUBSCRIPTION_TYPE)) {
					budget += registerations.get(0).getJournalPrice().intValue();
				}
			}
			
			return OptionalInt.of(budget);
		});
	}

	@Override
	public CompletableFuture<List<String>> getSubscribedUsers(String journalId) {
		return journals.find(journalId).thenApply(journalStr -> {
			List<String> users = new ArrayList<String>();
			
			if (!journalStr.isPresent()) {
				return users;
			}
			
			Journal journal = Journal.createObject(journalStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = journal.getRegisterations().stream().collect(Collectors.groupingBy(Register::getUserID));

			for (List<Register> registerations : registerationsPerJournalID.values()) {
				if (registerations.get(registerations.size() - 1).getType().equals(Register.SUBSCRIPTION_TYPE)) {
					users.add(registerations.get(0).getUserID());
				}
			}
			
			return users.stream().sorted().collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<OptionalInt> getMonthlyIncome(String journalId) {
		return journals.find(journalId).thenApply(journalStr -> {
			Integer budget = 0;
			
			if (!journalStr.isPresent()) {
				return OptionalInt.empty();
			}
			
			Journal journal = Journal.createObject(journalStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = journal.getRegisterations().stream().collect(Collectors.groupingBy(Register::getUserID));

			for (List<Register> registerations : registerationsPerJournalID.values()) {
				if (registerations.get(registerations.size() - 1).getType().equals(Register.SUBSCRIPTION_TYPE)) {
					budget += registerations.get(0).getJournalPrice().intValue();
				}
			}
			
			return OptionalInt.of(budget);
		});
	}

	@Override
	public CompletableFuture<Map<String, List<Boolean>>> getSubscribers(String journalId) {
		return journals.find(journalId).thenApply(jounralStr -> {
			Map<String, List<Boolean>> journals = new HashMap<String, List<Boolean>>();
			
			if (!jounralStr.isPresent()) {
				return journals;
			}
			
			Journal jounral = Journal.createObject(jounralStr.get());
			
			Map<String, List<Register>> registerationsPerJournalID = jounral.getRegisterations().stream().collect(Collectors.groupingBy(Register::getUserID));
			
			for (List<Register> registerations : registerationsPerJournalID.values()) {
				List<Boolean> journalList = new ArrayList<Boolean>();
				
				for (Register registeration : registerations) {
					if (registeration.getType().equals(Register.SUBSCRIPTION_TYPE)) {
						journalList.add(true);
					} else if (journalList.isEmpty()) {
						/* Registration is cancel type and the list is empty */
						journalList.add(false);
					} else if (journalList.get(journalList.size() - 1)) {
						/* Registration is cancel type and the list is not empty and last registeration wasn't cancel */
						journalList.add(false);
					}
				}
				
				journals.put(registerations.get(0).getJournalID(), journalList);
			}
			
			return journals;		
		});
	}
}
