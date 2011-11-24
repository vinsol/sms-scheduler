package com.vinsol.sms_scheduler;

public class Person {
	String personName;
	String personNumber;
	
	public Person(){
		
	}
	
	public Person(String name, String number){
		this.personName = name;
		this.personNumber = number;
	}
	
	public void setName(String name){
		this.personName = name;
	}
	
	public void setNumber(String number){
		this.personNumber = number;
	}
	
	public String getName(){
		return this.personName;
	}
	
	public String getNumber(){
		return this.personNumber;
	}
	
	@Override
	public String toString() {
		return this.personNumber; 
	}
}
