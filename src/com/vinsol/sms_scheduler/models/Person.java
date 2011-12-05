package com.vinsol.sms_scheduler.models;

public class Person {
	public String personName;
	public String personNumber;
	
	public Person(){
		
	}
	
	public Person(String name, String number){
		this.personName = name;
		this.personNumber = number;
	}
	
	@Override
	public String toString() {
		return this.personNumber; 
	}
}
