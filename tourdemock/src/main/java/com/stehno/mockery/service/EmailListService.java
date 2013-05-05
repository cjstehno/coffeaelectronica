package com.stehno.mockery.service;

import java.io.IOException;
import java.util.List;

public interface EmailListService {

	public static final String KEY = "com.stehno.mockery.service.EmailListService";

	/**
	 * Retrieves the list of email addresses with the specified name. If no list
	 * exists with that name an IOException is thrown.
	 * 
	 * @param listName
	 * @return
	 * @throws IOException
	 */
	List<String> getListByName(String listName) throws IOException;
}
