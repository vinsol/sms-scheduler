package com.vinsol.sms_scheduler.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vinsol.sms_scheduler.models.Contact;

public class MyGson {
	GsonBuilder gsonBuilder = new GsonBuilder();
	Gson gson = gsonBuilder.create();

	public String serializer(ArrayList<Contact> contacts){
		String serializedString = gson.toJson(contacts);
		return serializedString;
	}

	public ArrayList<Contact> deserializer(String jsonString){
		Contact[] contacts = gson.fromJson(jsonString, Contact[].class);
		ArrayList<Contact> contactsList = new ArrayList<Contact>();
		for(int i = 0; i< contacts.length; i++){
			contactsList.add(contacts[i]);
		}
		return contactsList;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> deserializeRepeatHash(String hashString){
		HashMap<String, Object> repeatHash = new HashMap<String, Object>();
		repeatHash = gson.fromJson(hashString, repeatHash.getClass());
		return repeatHash;
	}

	public String serializeRepeatHash(HashMap<String, Object> repeatHash){
		String repeatString = gson.toJson(repeatHash);
		return repeatString;
	}
}