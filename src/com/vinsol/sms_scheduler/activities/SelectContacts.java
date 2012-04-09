/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.ContactNumber;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

public class SelectContacts extends Activity {
	
	private TabHost mtabHost;
	private DBAdapter mdba = new DBAdapter(this);
	private Cursor cur;
	private LinearLayout listLayout;
	private LinearLayout blankLayout;
	private Button blankListAddButton;
	private LinearLayout recentsListLayout;
	private LinearLayout recentsBlankLayout;
	
	
	
	//---------------- Variables relating to Contacts tab -----------------------
	private ListView nativeContactsList;
	private EditText filterField;
	private ImageView clearFilterButton;
	private Button doneButton;
	private Button cancelButton;
	
	private ContactsAdapter contactsAdapter;
	private String origin;
	ArrayList<Contact> sortedContacts = new ArrayList<Contact>(); 
	
	int positionOfContact = 0;
	
	private ArrayList<Recipient> RecipientsTemp 	= new ArrayList<Recipient>();
	//---------------------------------------------------------------------------
	
	
	
	//----------- Variables related to Groups Tab-------------------------------
	private ExpandableListView nativeGroupExplList;
	private ExpandableListView privateGroupExplList;
	private ArrayList<ArrayList<HashMap<String, Object>>> nativeChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private ArrayList<HashMap<String, Object>> nativeGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	
	private ArrayList<ArrayList<HashMap<String, Object>>> privateChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private ArrayList<HashMap<String, Object>> privateGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	private ArrayList<ArrayList<HashMap<String, Object>>> groupedPrivateChildDataTemp = new ArrayList<ArrayList<HashMap<String,Object>>>();
	
	private ArrayList<ArrayList<ArrayList<ContactNumber>>> nativeExtraNumbers = new ArrayList<ArrayList<ArrayList<ContactNumber>>>();
	
	private SimpleExpandableListAdapter nativeGroupAdapter;
	private SimpleExpandableListAdapter privateGroupAdapter;
	
	private boolean hasToRefresh;
	//-----------------------------------------------------------------------------
	
	
	
	
	//----------------------Variables relating to Recents Tab-----------------------
	private ArrayList<Long> recentIds = new ArrayList<Long>();
	private ArrayList<Long> recentContactIds = new ArrayList<Long>();
	private ArrayList<String> recentContactNumbers = new ArrayList<String>();
	private ListView recentsList;
	private RecentsAdapter recentsAdapter;
	//------------------------------------------------------------------------------
	
	
	
	@Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, this.getResources().getString(R.string.flurry_key_test));
    	FlurryAgent.onEvent("Selecting from Contacts");
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
    
	
	
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contacts);
		
		for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
			sortedContacts.add(SmsSchedulerApplication.contactsList.get(i));
		}
		
		filterField = (EditText) findViewById(R.id.filter_text);
		clearFilterButton = (ImageView) findViewById(R.id.clear_filter_button);
		
		//----------------------Setting up the Tabs--------------------------------
		final TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        
        tabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.contacts_tab);
        spec1.setIndicator("Contacts", getResources().getDrawable(R.drawable.tab_icon_contacts));

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Groups", getResources().getDrawable(R.drawable.tab_icon_group));
        spec2.setContent(R.id.group_tabs);

        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Recents", getResources().getDrawable(R.drawable.tab_icon_recents));
        spec3.setContent(R.id.contacts_tabs_recents_layout);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        
        for(int i=0;i<tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg_selector));
        }
    	//----------------------------------------------------end of Tabs Setup-----------
        
        
        
        filterField.addTextChangedListener(new TextWatcher() {
			
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			
			public void afterTextChanged(Editable s) {
				if(s.equals("")){
					sortedContacts.clear();
					for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
						sortedContacts.add(SmsSchedulerApplication.contactsList.get(i));
					}
					contactsAdapter.notifyDataSetChanged();
				}else{
					sortedContacts.clear();
					for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
						Pattern p = Pattern.compile(s.toString(), Pattern.CASE_INSENSITIVE);
						
						Matcher m = p.matcher(SmsSchedulerApplication.contactsList.get(i).name);
						if(m.find()) {
							sortedContacts.add(SmsSchedulerApplication.contactsList.get(i));
						} else {
							for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
								SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number = AbstractScheduleSms.refineNumber(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number);
								m = p.matcher(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number);
								if(m.find()) {
									sortedContacts.add(SmsSchedulerApplication.contactsList.get(i));
									break;
								}
							}
							
						}
					}
					contactsAdapter.notifyDataSetChanged();
				}
			}
		});
        
        
        
        clearFilterButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				filterField.setText("");
				filterField.setHint("Filter");
				sortedContacts.clear();
				for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
					sortedContacts.add(SmsSchedulerApplication.contactsList.get(i));
				}
				contactsAdapter.notifyDataSetChanged();
			}
		});
        
        
        
		
		Intent intent = getIntent();
		origin = intent.getStringExtra("ORIGIN");
		
		for(int i = 0; i < AbstractScheduleSms.Recipients.size(); i++){
			RecipientsTemp.add(AbstractScheduleSms.Recipients.get(i));
		}
		for(int groupCount = 0; groupCount< AbstractScheduleSms.nativeGroupData.size(); groupCount++){
			boolean hasAChild = false;
			HashMap<String, Object> group = new HashMap<String, Object>();
			group.put(Constants.GROUP_ID, AbstractScheduleSms.nativeGroupData.get(groupCount).get(Constants.GROUP_ID));
			group.put(Constants.GROUP_NAME, AbstractScheduleSms.nativeGroupData.get(groupCount).get(Constants.GROUP_NAME));
			group.put(Constants.GROUP_IMAGE, AbstractScheduleSms.nativeGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
			group.put(Constants.GROUP_TYPE, AbstractScheduleSms.nativeGroupData.get(groupCount).get(Constants.GROUP_TYPE));
			group.put(Constants.GROUP_CHECK, false);
				
				
			ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
			for(int childCount = 0; childCount< AbstractScheduleSms.nativeChildData.get(groupCount).size(); childCount++){
					
				HashMap<String, Object> childParams = new HashMap<String, Object>();
				childParams.put(Constants.CHILD_CONTACT_ID, AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
				childParams.put(Constants.CHILD_NAME, AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
				childParams.put(Constants.CHILD_NUMBER, AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
				childParams.put(Constants.CHILD_IMAGE, AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
				childParams.put(Constants.CHILD_CHECK, AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
				child.add(childParams);
				if((Boolean) AbstractScheduleSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
					hasAChild = true;
				}
			}
			if(hasAChild){
				group.put(Constants.GROUP_CHECK, true);
			}
			nativeGroupDataTemp.add(group);
			nativeChildDataTemp.add(child);
		}
			
		for(int groupCount = 0; groupCount< AbstractScheduleSms.privateGroupData.size(); groupCount++){
			boolean hasAChild = false;
			HashMap<String, Object> group = new HashMap<String, Object>();
			group.put(Constants.GROUP_ID, AbstractScheduleSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
			group.put(Constants.GROUP_NAME, AbstractScheduleSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
			group.put(Constants.GROUP_IMAGE, AbstractScheduleSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
			group.put(Constants.GROUP_TYPE, AbstractScheduleSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
			group.put(Constants.GROUP_CHECK, false);
				
				
			ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
			for(int childCount = 0; childCount< AbstractScheduleSms.privateChildData.get(groupCount).size(); childCount++){
					
				HashMap<String, Object> childParams = new HashMap<String, Object>();
				childParams.put(Constants.CHILD_CONTACT_ID, AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
				childParams.put(Constants.CHILD_NAME, AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
				childParams.put(Constants.CHILD_NUMBER, AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
				childParams.put(Constants.CHILD_IMAGE, AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
				childParams.put(Constants.CHILD_CHECK, AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
				child.add(childParams);
			if((Boolean) AbstractScheduleSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
					hasAChild = true;
				}
			}
			
			if(hasAChild){
				group.put(Constants.GROUP_CHECK, true);
			}
			privateGroupDataTemp.add(group);
			privateChildDataTemp.add(child);
		}
		
		
		
		groupedPrivateChildDataTemp = organizeChildData(AbstractScheduleSms.privateGroupData, AbstractScheduleSms.privateChildData);
		
		Log.d("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		Log.d("Groups : " + groupedPrivateChildDataTemp.size());
		for(int groupCount = 0; groupCount< AbstractScheduleSms.privateGroupData.size(); groupCount++){
			Log.d("+++++++++++++++++++++++++++++++" + AbstractScheduleSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME) + "++++++++++++++++++++++++++++++++++++++++"); 
			
			for(int childCount = 0; childCount< groupedPrivateChildDataTemp.get(groupCount).size(); childCount++){
				Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				Log.d("Name : " + (String)groupedPrivateChildDataTemp.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
				Log.d("Contact Id : " + (Long)groupedPrivateChildDataTemp.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
				Log.d("Numbers-----------------------------------------");
				ArrayList<ContactNumber> numbers = new ArrayList<ContactNumber>();
				numbers = (ArrayList<ContactNumber>)groupedPrivateChildDataTemp.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER);
				for(int i = 0; i< numbers.size(); i++){
					Log.d(i + ". " + numbers.get(i).type + ": " + numbers.get(i).number);
				}
				Log.d("------------------------------------------------");
				Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			}
		}
		Log.d("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
		
        doneButton			= (Button) 		findViewById(R.id.contacts_tab_done_button);
        cancelButton		= (Button) 		findViewById(R.id.contacts_tab_cancel_button);
        
        
        doneButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				Intent intent = new Intent();
				AbstractScheduleSms.nativeGroupData.clear();
				AbstractScheduleSms.nativeChildData.clear();
				
				AbstractScheduleSms.nativeGroupData = nativeGroupDataTemp;
				AbstractScheduleSms.nativeChildData = nativeChildDataTemp;
				
				AbstractScheduleSms.privateGroupData.clear();
				AbstractScheduleSms.privateChildData.clear();

				AbstractScheduleSms.privateGroupData = privateGroupDataTemp;
				AbstractScheduleSms.privateChildData = privateChildDataTemp;
					
				AbstractScheduleSms.Recipients.clear();
				for(int i = 0; i< RecipientsTemp.size(); i++){
					AbstractScheduleSms.Recipients.add(RecipientsTemp.get(i));
				}
					
				setResult(2, intent);
				SelectContacts.this.finish();
			}
		});
        
        
        
        
        
        cancelButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(2, intent);
				SelectContacts.this.finish();
			}
		});
        
        
        
        //------------------Setting up the Contacts Tab ---------------------------------------------
        nativeContactsList 	= (ListView) findViewById(R.id.contacts_tabs_native_contacts_list);
		contactsAdapter = new ContactsAdapter(this, sortedContacts);
        nativeContactsList.setAdapter(contactsAdapter);
        //------------------------------------------------------------end of setting up Contacts Tab--------
        
        
        
        
        
        listLayout = (LinearLayout) findViewById(R.id.list_layout);
        blankLayout = (LinearLayout) findViewById(R.id.blank_layout);
        blankListAddButton = (Button) findViewById(R.id.blank_list_add_button);
        
        blankListAddButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				Intent intent = new Intent(SelectContacts.this, ContactsList.class);
				intent.putExtra("ORIGINATOR", "Group Add Activity");
				startActivity(intent);
			}
		});
        
        mdba.open();
		cur = mdba.fetchAllGroups();
		if(cur.getCount()==0){
			listLayout.setVisibility(LinearLayout.GONE);
			blankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			listLayout.setVisibility(LinearLayout.VISIBLE);
			blankLayout.setVisibility(LinearLayout.GONE);
		}
		cur.close();
		mdba.close();
		
        LinearLayout groupTabs = (LinearLayout)findViewById( R.id.group_tabs );
        mtabHost = (TabHost)groupTabs.findViewById( android.R.id.tabhost );
        
        mtabHost.setup( );
        mtabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        setupTab(new TextView(this), "Phone Groups");
    	setupTab(new TextView(this), "My Groups");

        privateGroupExplList = (ExpandableListView) findViewById(R.id.private_list);
        nativeGroupExplList = (ExpandableListView) groupTabs.findViewById(R.id.native_list);
        
        nativeGroupsAdapterSetup();
		privateGroupsAdapterSetup();
		
		nativeGroupExplList.setAdapter(nativeGroupAdapter);
		privateGroupExplList.setAdapter(privateGroupAdapter);
		//----------------------------------------------------end of Groups Tab setup-------------------
        
        
		
		//--------------------setting up Recents Tab--------------------------------
		recentsListLayout  = (LinearLayout) findViewById(R.id.contacts_tabs_recents_list_layout);
		recentsBlankLayout = (LinearLayout) findViewById(R.id.contacts_tabs_recents_blank_layout);
		
		mdba.open();
		Cursor cur = mdba.fetchAllRecents();
		startManagingCursor(cur);
		if(cur.getCount()==0){
			recentsListLayout.setVisibility(LinearLayout.GONE);
			recentsBlankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			recentsBlankLayout.setVisibility(LinearLayout.GONE);
			recentsListLayout.setVisibility(LinearLayout.VISIBLE);
			recentIds.clear();
			recentContactIds.clear();
			recentContactNumbers.clear();
			recentsList = (ListView) findViewById(R.id.contacts_tabs_recents_list);
			
			if(cur.moveToFirst()){
				do{
					recentIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_ID)));
					recentContactIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_CONTACT_ID)));
					recentContactNumbers.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_NUMBER)));
				}while(cur.moveToNext());
			}
			recentsAdapter = new RecentsAdapter();
			recentsList.setAdapter(recentsAdapter);
		}
		mdba.close();
		//---------------------------------------------------------------------------
	}
	
	
	public ArrayList<ArrayList<HashMap<String, Object>>> organizeChildData(ArrayList<HashMap<String, Object>> privateGroupData, ArrayList<ArrayList<HashMap<String, Object>>> privateChildData){
		ArrayList<ArrayList<HashMap<String, Object>>> data = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		for(int groupCount = 0; groupCount< privateGroupData.size(); groupCount++){
			ArrayList<HashMap<String, Object>> groupMembers = new ArrayList<HashMap<String,Object>>();
			for(int childCount = 0; childCount< privateChildData.get(groupCount).size(); childCount++){
				boolean isPresent = false;
				for(int i =0; i< groupMembers.size(); i++){

					if(groupMembers.get(i).get(Constants.CHILD_CONTACT_ID).equals(privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID))){
						isPresent = true;
					}
				}
				if(!isPresent){
					HashMap<String, Object> child = new HashMap<String, Object>();
					child.put(Constants.CHILD_NAME, privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					child.put(Constants.CHILD_CONTACT_ID, privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					child.put(Constants.CHILD_IMAGE, privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					ArrayList<ContactNumber> numbers = new ArrayList<ContactNumber>();
					ContactNumber number = new ContactNumber((Long)child.get(Constants.CHILD_CONTACT_ID), (String)privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER), getType((Long)child.get(Constants.CHILD_CONTACT_ID), (String)privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER)));
					numbers.add(number);
					for(int childCountExt=childCount+1; childCountExt< privateChildData.get(groupCount).size(); childCountExt++){
						
						if(((Long)privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID)).equals((Long)privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID))){
							number = new ContactNumber((Long)child.get(Constants.CHILD_CONTACT_ID), (String)privateChildData.get(groupCount).get(childCountExt).get(Constants.CHILD_NUMBER), getType((Long)child.get(Constants.CHILD_CONTACT_ID), (String)privateChildData.get(groupCount).get(childCountExt).get(Constants.CHILD_NUMBER)));
							if(number.type!=null)
								numbers.add(number);
						}
					}
					Log.d("Numbers : " + numbers.size());
					child.put(Constants.CHILD_NUMBER, numbers);
					groupMembers.add(child);
				}
			}
			data.add(groupMembers);
		}
		
		return data;
	}
	
	
	
	private String getType(long id, String number){
		for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
			if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == id){
				for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
					if(number.equals(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number)){
						return SmsSchedulerApplication.contactsList.get(i).numbers.get(j).type;
					}
				}
			}
		}
		return null;
	}
	
	

	private void setupTab(final View view, final String tag) {
		View tabview = createTabView(mtabHost.getContext(), tag);
		TabSpec setContent = null;
		if(tag.equals("Phone Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.native_list);
		}else if(tag.equals("My Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.private_list_parent_layout);
		}
		mtabHost.addTab(setContent);
	}

	
	
	private View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	
	
	
	
	protected void onPause() {
		super.onPause();
		if(privateGroupDataTemp.size() == 0){
			hasToRefresh = true;
		}else{
			hasToRefresh = false;
		}
	}
	
	
	
	
	protected void onResume() {
		super.onResume();
		if(hasToRefresh){
			mdba.open();
			cur = mdba.fetchAllGroups();
			if(cur.getCount()==0){
				listLayout.setVisibility(LinearLayout.GONE);
				blankLayout.setVisibility(LinearLayout.VISIBLE);
			}else{
				listLayout.setVisibility(LinearLayout.VISIBLE);
				blankLayout.setVisibility(LinearLayout.GONE);
			}
			mdba.close();
			reloadPrivateGroupData();
			privateGroupAdapter.notifyDataSetChanged();
			hasToRefresh = false;
		}
	}
	
	
	
	
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		setResult(2, intent);
		
		SelectContacts.this.finish();
	}
	
	
	
	//************************* Adapter for the list *****************************************
	//**************************** in Contacts Tab ********************************************
	
	private class ContactsAdapter extends ArrayAdapter<Contact> implements SectionIndexer{
		
		HashMap<String, Integer> alphaIndexer;
        String[] sections;
        ArrayList<Contact> contacts;
		ContactsAdapter(Context context, ArrayList<Contact> _contacts){
    		super(SelectContacts.this, R.layout.contacts_list_row, _contacts);
    		
    		contacts = _contacts;
    		
    		alphaIndexer = new HashMap<String, Integer>();
            int size = contacts.size();
 
            for (int x = 0; x < size; x++) {
                Contact c = contacts.get(x);
                String ch =  c.name.substring(0, 1);
                ch = ch.toUpperCase();
                alphaIndexer.put(ch, x);
            }
 
            Set<String> sectionLetters = alphaIndexer.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters); 
 
            Collections.sort(sectionList);
 
            sections = new String[sectionList.size()];
 
            sectionList.toArray(sections);
    	}
		
		
		
		
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ContactsListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
	    		holder = new ContactsListHolder();
				holder.contactImage 		= (ImageView) 		convertView.findViewById(R.id.contact_list_row_contact_pic);
	    		holder.nameText 			= (TextView) 		convertView.findViewById(R.id.contact_list_row_contact_name);
	    		holder.numberText 			= (TextView) 		convertView.findViewById(R.id.contact_list_row_contact_number);
	    		holder.contactCheck     	= (CheckBox) 		convertView.findViewById(R.id.contact_list_row_contact_check);
	    		holder.primaryContactLayout = (RelativeLayout) 	convertView.findViewById(R.id.contact_list_primary_contact_space);
//	    		holder.extraContacts	= (ListView) 	convertView.findViewById(R.id.extra_numbers_list);
	    		
	    		
	    		
			}else{
				holder = (ContactsListHolder) convertView.getTag();
			}
			positionOfContact = position - 1;
    		holder.contactImage.setImageBitmap(contacts.get(position).image);
    		holder.nameText.setText(contacts.get(position).name);
    		holder.numberText.setText(contacts.get(position).numbers.get(0).type + ": " + contacts.get(position).numbers.get(0).number);//TODO
    		
    		holder.extraContactsLayout = (LinearLayout) convertView.findViewById(R.id.extra_numbers_layout);
    		holder.extraContactsViews = new ArrayList<View>();
    		
    		if(contacts.get(position).numbers.size()>1){
    			holder.extraContactsLayout.setVisibility(View.VISIBLE);
    			holder.extraContactsLayout.removeAllViews();
    			holder.extraContactsViews.clear();
    			ArrayList<ContactNumber> extraNumbers = new ArrayList<ContactNumber>();
        		for(int i=1; i< contacts.get(position).numbers.size(); i++){
        			extraNumbers.add(contacts.get(position).numbers.get(i));
        		}
        		for(int i = 0; i< extraNumbers.size(); i++){
        			View view = createView(extraNumbers.get(i), contacts.get(position), getLayoutInflater());
        			holder.extraContactsViews.add(view);
        			
//        			holder.extraContactsLayout.refreshDrawableState();
        		}
        		for(int i = 0; i< holder.extraContactsViews.size(); i++){
        			holder.extraContactsLayout.addView(holder.extraContactsViews.get(i));
        		}
    		}else{
    			holder.extraContactsLayout.setVisibility(View.GONE);
    		}
    		
    		convertView.setTag(holder);
    		
//    		holder.extraNumbersAdapter = new ExtraNumbersAdapter(SelectContacts.this, extraNumbers);
//    		holder.extraContacts.setAdapter(holder.extraNumbersAdapter);
    		
    		for(int i = 0; i< RecipientsTemp.size(); i++){
//    			Log.d("Recipient detail : " + RecipientsTemp.get(i).contactId + " : " + RecipientsTemp.get(i).number);
//    			Log.d("Contact detail : " + contacts.get(position).content_uri_id + " : " + contacts.get(position).numbers.get(0).);
        		if(contacts.get(position).content_uri_id == RecipientsTemp.get(i).contactId && contacts.get(position).numbers.get(0).number.equals(RecipientsTemp.get(i).number)){
        			holder.contactCheck.setChecked(true);
        			break;
        		}else{
        			holder.contactCheck.setChecked(false);
        		}
        	}
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						boolean isPresent = false;
						for(int i = 0; i< RecipientsTemp.size(); i++){
							if(RecipientsTemp.get(i).contactId == contacts.get(position).content_uri_id && contacts.get(position).numbers.get(0).number.equals(RecipientsTemp.get(i).number)){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Contacts List");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							Recipient recipient = new Recipient(-1, 2, contacts.get(position).name, contacts.get(position).content_uri_id, -1, -1, -1, contacts.get(position).numbers.get(0).number);
							recipient.groupIds.add((long) -1);
							recipient.groupTypes.add(-1);
							RecipientsTemp.add(recipient);
						}
					}else{
						for(int i = 0; i<RecipientsTemp.size(); i++){
				    		if(contacts.get(position).content_uri_id == RecipientsTemp.get(i).contactId && contacts.get(position).numbers.get(0).number.equals(RecipientsTemp.get(i).number)){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if((Long)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if((Long)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			HashMap<String, String> params = new HashMap<String, String>();
								params.put("From", "Contacts List");
								FlurryAgent.logEvent("Recipient Removed", params);
								
				    			RecipientsTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged(); 
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		
    		
    		
    		holder.primaryContactLayout.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						boolean isPresent = false;
						for(int i = 0; i< RecipientsTemp.size(); i++){
							if(RecipientsTemp.get(i).contactId == contacts.get(position).content_uri_id && contacts.get(position).numbers.get(0).number.equals(RecipientsTemp.get(i).number)){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Contacts List");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							Recipient recipient = new Recipient(-1, 2, contacts.get(position).name, contacts.get(position).content_uri_id, -1, -1, -1, contacts.get(position).numbers.get(0).number);
							recipient.groupIds.add((long) -1);
							recipient.groupTypes.add(-1);
							RecipientsTemp.add(recipient);
						}
					}else{	
						holder.contactCheck.setChecked(false);
						for(int i = 0; i<RecipientsTemp.size(); i++){
				    		if(contacts.get(position).content_uri_id == RecipientsTemp.get(i).contactId && contacts.get(position).numbers.get(0).number.equals(RecipientsTemp.get(i).number)){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if((Long)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if((Long)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			HashMap<String, String> params = new HashMap<String, String>();
								params.put("From", "Contacts List");
								FlurryAgent.logEvent("Recipient Removed", params);
								
				    			RecipientsTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged();
				    		}
				    	}
					}
				}
			});
    		
    		return convertView;
		}



		
		public int getPositionForSection(int section) {
			return alphaIndexer.get(sections[section]);
		}



		
		public int getSectionForPosition(int position) {
			return 1;
		}



		
		public Object[] getSections() {
			return sections;
		}
	}
	//************************************************************** end of ContactsAdapter******************
	

	
	public View createView(final ContactNumber contactNumber, final Contact contact, LayoutInflater inflater){
		
		View view = inflater.inflate(R.layout.extra_numbers_list_row, null);
		
		TextView tv = (TextView) view.findViewById(R.id.extra_number);
		final CheckBox cb = (CheckBox) view.findViewById(R.id.extra_number_checkbox);
		
		tv.setText(contactNumber.type + ": " + contactNumber.number);
		
		for(int i = 0; i< RecipientsTemp.size(); i++){
			Log.d(i + " : " + RecipientsTemp.get(i).contactId + ", " + RecipientsTemp.get(i).number);
		}
		Log.d("\nView Details : " + contactNumber.contactId + ", " + contactNumber.number);
		
		for(int i = 0; i< RecipientsTemp.size(); i++){
			
    		if(contactNumber.contactId == RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
    			cb.setChecked(true);
    			break;
    		}else{
    			cb.setChecked(false);
    		}
    	}
		
		view.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				if(!cb.isChecked()){
					cb.setChecked(true);
					boolean isPresent = false;
					for(int i = 0; i< RecipientsTemp.size(); i++){
						if(RecipientsTemp.get(i).contactId == contactNumber.contactId && RecipientsTemp.get(i).number.equals(contactNumber.number)){
							isPresent = true;
							break;
						}
					}
					if(!isPresent){
						int k;
						for(k = 0; k < SmsSchedulerApplication.contactsList.size(); k++){
							Log.d(SmsSchedulerApplication.contactsList.get(k).content_uri_id + " and " + contactNumber.contactId);
							if(SmsSchedulerApplication.contactsList.get(k).content_uri_id==contactNumber.contactId){
								break;
							}
						}
						
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Contacts List");
						params.put("Is Primary Number", "no");
						FlurryAgent.logEvent("Recipient Added", params);
						
						Recipient recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, contactNumber.contactId, -1, -1, -1, contactNumber.number);
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						RecipientsTemp.add(recipient);
					}
				}else{
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Contacts List");
					FlurryAgent.logEvent("Recipient Removed", params);
					
					cb.setChecked(false);
					for(int i = 0; i<RecipientsTemp.size(); i++){
			    		if(contactNumber.contactId == RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
			    			RecipientsTemp.remove(i);
			    		}
					}
				}
			}
		});
		
		cb.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				
				if(cb.isChecked()){
					boolean isPresent = false;
					for(int i = 0; i< RecipientsTemp.size(); i++){
						if(RecipientsTemp.get(i).contactId == contactNumber.contactId && RecipientsTemp.get(i).number.equals(contactNumber.number)){
							isPresent = true;
							break;
						}
					}
					if(!isPresent){
						int k;
						for(k = 0; k < SmsSchedulerApplication.contactsList.size(); k++){
							Log.d(SmsSchedulerApplication.contactsList.get(k).content_uri_id + " and " + contactNumber.contactId);
							if(SmsSchedulerApplication.contactsList.get(k).content_uri_id==contactNumber.contactId){
								break;
							}
						}
						
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Contacts List");
						params.put("Is Primary Number", "no");
						FlurryAgent.logEvent("Recipient Added", params);
						
						Recipient recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, contactNumber.contactId, -1, -1, -1, contactNumber.number);
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						RecipientsTemp.add(recipient);
					}
				}else{
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Contacts List");
					FlurryAgent.logEvent("Recipient Removed", params);
					
					for(int i = 0; i<RecipientsTemp.size(); i++){
			    		if(contactNumber.contactId == RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
			    			RecipientsTemp.remove(i);
			    		}
					}
				}
			}
		});
		
		return view;
	}
	
	
	
//	private class ExtraNumbersAdapter extends ArrayAdapter<ContactNumber>{
//		ArrayList<ContactNumber> extraNumbers;
//		
//		ExtraNumbersAdapter(Context context, ArrayList<ContactNumber> _extraNumbers){
//    		super(SelectContacts.this, R.layout.contacts_list_row, _extraNumbers);
//    		extraNumbers = _extraNumbers;
//		}
//		
//		
//		
//		public View getView(final int position, View convertView, ViewGroup parent) {
//			final ExtraNumbersListHolder holder;
//			if(convertView==null){
//				LayoutInflater inflater = getLayoutInflater();
//	    		convertView = inflater.inflate(R.layout.extra_numbers_list_row, parent, false);
//	    		holder = new ExtraNumbersListHolder();
//	    		holder.extraNumber 			= (TextView) convertView.findViewById(R.id.extra_number);
//	    		holder.extraNumberCheckbox  = (CheckBox) convertView.findViewById(R.id.extra_number_checkbox);
//	    		convertView.setTag(holder);
//			}else{
//				holder = (ExtraNumbersListHolder) convertView.getTag();
//			}
//			
//			holder.extraNumber.setText(extraNumbers.get(position).type + ": " + extraNumbers.get(position).number);
//			
//			for(int i = 0; i< RecipientsTemp.size(); i++){
//    			
//        		if(extraNumbers.get(position).contactId == RecipientsTemp.get(i).contactId && extraNumbers.get(position).number.equals(RecipientsTemp.get(i).number)){
//        			holder.extraNumberCheckbox.setChecked(true);
//        			break;
//        		}else{
//        			holder.extraNumberCheckbox.setChecked(false);
//        		}
//        	}
//			
//			
//			holder.extraNumberCheckbox.setOnClickListener(new OnClickListener() {
//				
//				
//				public void onClick(View v) {
//					if(holder.extraNumberCheckbox.isChecked()){
//						boolean isPresent = false;
//						for(int i = 0; i< RecipientsTemp.size(); i++){
//							if(RecipientsTemp.get(i).contactId == extraNumbers.get(position).contactId){
//								isPresent = true;
//								break;
//							}
//						}
//						if(!isPresent){
//							int k;
//							for(k = 0; k < SmsSchedulerApplication.contactsList.size(); k++){
//								Log.d(SmsSchedulerApplication.contactsList.get(k).content_uri_id + " and " + extraNumbers.get(position).contactId);
//								if(SmsSchedulerApplication.contactsList.get(k).content_uri_id==extraNumbers.get(position).contactId){
//									break;
//								}
//							}
//							Recipient recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, extraNumbers.get(position).contactId, -1, -1, -1, contacts.get(position).numbers.get(0).number);
//							recipient.groupIds.add((long) -1);
//							recipient.groupTypes.add(-1);
//							RecipientsTemp.add(recipient);
//						}
//					}else{
//						for(int i = 0; i<RecipientsTemp.size(); i++){
//				    		if(extraNumbers.get(position).contactId == RecipientsTemp.get(i).contactId){
//				    			RecipientsTemp.remove(i);
//				    		}
//						}
//					}
//				}
//			});
//			return convertView;
//		}
//	}
	
	
	
	private void nativeGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		nativeGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	nativeGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	nativeChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			public Object getChild(int groupPosition, int childPosition) {
			   return nativeChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += nativeChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		
    		public int getChildrenCount(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition).size();
    		}
    		 
    		
    		public Object getGroup(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition);
    		}
    		 
    		
    		public int getGroupCount() {
    		   return nativeGroupDataTemp.size();
    		}
    		 
    		
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.select_contacts_expandable_list_group, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));
    			
    			nativeExtraNumbers.add(new ArrayList<ArrayList<ContactNumber>>());
    			
    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									addCheck(groupPosition, i, nativeChildDataTemp, nativeGroupDataTemp);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}else{
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									HashMap<String, String> params = new HashMap<String, String>();
									params.put("From", "Native Group");
									FlurryAgent.logEvent("Recipient Removed", params);
									
									removeCheck(groupPosition, i, nativeChildDataTemp, nativeGroupDataTemp);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(isExpanded){
							nativeGroupExplList.collapseGroup(groupPosition);
						}else{
							nativeGroupExplList.expandGroup(groupPosition);
						}
					}
				});
    			return convertView;
    		}



			
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				final ChildListHolder holder;
				if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  		convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) 		convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  		convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  		convertView.findViewById(R.id.contact_list_row_contact_check);
        			holder.primaryNumberLayout  = (RelativeLayout) 	convertView.findViewById(R.id.contact_list_primary_contact_space);
        			
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
				long contactId = (Long)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID);
				
				Contact contact = null;
    			for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
    				if(SmsSchedulerApplication.contactsList.get(i).content_uri_id==contactId){
    					contact = SmsSchedulerApplication.contactsList.get(i);
    				}
    			}
    			
    			
				
    			holder.childNameText.setText((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME));
    			holder.childNumberText.setText(contact.numbers.get(0).type + ": " + (String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
    			holder.childContactImage.setImageBitmap((Bitmap)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			holder.childCheck.setChecked((Boolean)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK));
    			
    			holder.extraNumbersLayout	= (LinearLayout)convertView.findViewById(R.id.extra_numbers_layout);
    			
    			nativeExtraNumbers.get(groupPosition).add(new ArrayList<ContactNumber>());
    			
    			ArrayList<ContactNumber> prunedList = new ArrayList<ContactNumber>();
    			for(int i = 1; i< contact.numbers.size(); i++){
    				prunedList.add(contact.numbers.get(i));
//    				nativeExtraNumbers.get(groupPosition).get(childPosition).add(prunedList.get(i));
    			}
    			if(prunedList.size()>0){
    				holder.extraNumbersLayout.setVisibility(View.VISIBLE);
        			holder.extraNumbersLayout.removeAllViews();
//        			holder.extraNumbersViews.clear();
        			for(int i = 0; i< prunedList.size(); i++){
        				View view = createNativeExtraNumberView(groupPosition, childPosition, prunedList.get(i), contact, getLayoutInflater(), (Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
        				holder.extraNumbersLayout.addView(view);
        			}
    			}else{
    				holder.extraNumbersLayout.setVisibility(View.GONE);
    			}
    			
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Native Group");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							addCheck(groupPosition, childPosition, nativeChildDataTemp, nativeGroupDataTemp);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Native Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							removeCheck(groupPosition, childPosition, nativeChildDataTemp, nativeGroupDataTemp);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			holder.primaryNumberLayout.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							holder.childCheck.setChecked(false);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Native Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							removeCheck(groupPosition, childPosition, nativeChildDataTemp, nativeGroupDataTemp);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(true);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Native Group");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							addCheck(groupPosition, childPosition, nativeChildDataTemp, nativeGroupDataTemp);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			
			
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			
			public boolean hasStableIds() {
			   return false;
			}
			 
			
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    		
    	};
    }
	
	
	
	
	
	public View createNativeExtraNumberView(final int groupPosition, final int childPosition, final ContactNumber contactNumber, final Contact contact, LayoutInflater inflater, final long groupId){
		
		View view = inflater.inflate(R.layout.extra_numbers_list_row, null);
		
		TextView tv = (TextView) view.findViewById(R.id.extra_number);
		final CheckBox cb = (CheckBox) view.findViewById(R.id.extra_number_checkbox);
		
		tv.setText(contactNumber.type + ": " + contactNumber.number);
		//TODO
		for(int i = 0; i< RecipientsTemp.size(); i++){
			Log.d(i + " : " + RecipientsTemp.get(i).contactId + ", " + RecipientsTemp.get(i).number);
		}
		Log.d("\nView Details : " + contactNumber.contactId + ", " + contactNumber.number);
		
		boolean gotChecked = false;
		for(int i = 0; i< RecipientsTemp.size() && !gotChecked; i++){
			
    		if(contactNumber.contactId == RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
    			for(int j=0; j<RecipientsTemp.get(i).groupIds.size(); j++){
    				if(RecipientsTemp.get(i).groupIds.get(j)==groupId && RecipientsTemp.get(i).groupTypes.get(j)==2){
    					cb.setChecked(true);
    					gotChecked = true;
    					break;
    				}
    			}
    		}
    	}
		
		view.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				if(!cb.isChecked()){
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Native Group");
					params.put("Is Primary Number", "no");
					FlurryAgent.logEvent("Recipient Added", params);
					
					cb.setChecked(true);
					addExtraCheck(groupPosition, childPosition, cb, contact.name, contact.content_uri_id, contactNumber, groupId);
					contactsAdapter.notifyDataSetChanged();
				}else{
					cb.setChecked(false);
					
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Native Group");
					FlurryAgent.logEvent("Recipient Removed", params);
					
					removeExtraCheck(groupPosition, childPosition, cb, contactNumber, groupId);
					contactsAdapter.notifyDataSetChanged();
				}
			}
		});
		
		cb.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				if(cb.isChecked()){
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Native Group");
					params.put("Is Primary Number", "no");
					FlurryAgent.logEvent("Recipient Added", params);
					
					addExtraCheck(groupPosition, childPosition, cb, contact.name, contact.content_uri_id, contactNumber, groupId);
					contactsAdapter.notifyDataSetChanged();
				}else{
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Native Group");
					FlurryAgent.logEvent("Recipient Removed", params);
					
					removeExtraCheck(groupPosition, childPosition, cb, contactNumber, groupId);
					contactsAdapter.notifyDataSetChanged();
				}
			}
		});
		
		return view;
	}
	
	
	
	
	
	private void privateGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		privateGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	privateGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	groupedPrivateChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			
			public Object getChild(int groupPosition, int childPosition) {
			   return groupedPrivateChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += groupedPrivateChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		
    		public int getChildrenCount(int groupPosition) {
    		   return groupedPrivateChildDataTemp.get(groupPosition).size();
    		}
    		 
    		
    		public Object getGroup(int groupPosition) {
    		   return privateGroupDataTemp.get(groupPosition);
    		}
    		 
    		
    		public int getGroupCount() {
    		   return privateGroupDataTemp.size();
    		}
    		 
    		
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.select_contacts_expandable_list_group, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));

    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									addCheck(groupPosition, i, privateChildDataTemp, privateGroupDataTemp);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}else{
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									HashMap<String, String> params = new HashMap<String, String>();
									params.put("From", "Private Group");
									FlurryAgent.logEvent("Recipient Removed", params);
									
									removeCheck(groupPosition, i, privateChildDataTemp, privateGroupDataTemp);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(isExpanded){
							privateGroupExplList.collapseGroup(groupPosition);
						}else{
							privateGroupExplList.expandGroup(groupPosition);
						}
					}
				});
    			
    			return convertView;
    			
    		}



			
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				privateChildDataTemp.get(groupPosition).get(childPosition);
				final ChildListHolder holder;
    			if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  		convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) 		convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  		convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  		convertView.findViewById(R.id.contact_list_row_contact_check);
        			holder.primaryNumberLayout  = (RelativeLayout) 	convertView.findViewById(R.id.contact_list_primary_contact_space);
        			
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
    			Long contactId = (Long)groupedPrivateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID);
    			
    			String contactName = (String)groupedPrivateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME);
    			holder.childNameText.setText(contactName);
    			@SuppressWarnings("unchecked")
				ArrayList<ContactNumber> numbers = (ArrayList<ContactNumber>)groupedPrivateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER);
    			
    			holder.childNumberText.setText(numbers.get(0).type + ": " + numbers.get(0).number);
    			holder.childContactImage.setImageBitmap((Bitmap)groupedPrivateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			boolean isChecked = false;
    			for(int i = 0; i< privateChildDataTemp.get(groupPosition).get(childPosition).size(); i++){
    				if(((Long)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)).equals(contactId) &&((String) privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER)).equals(numbers.get(0).number)){
    					if((Boolean)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK)){
    						isChecked = true;
    						break;
    					}
    				}
    			}
    			holder.childCheck.setChecked(isChecked);
    			
    			holder.extraNumbersLayout	= (LinearLayout)convertView.findViewById(R.id.extra_numbers_layout);
    			
    			nativeExtraNumbers.get(groupPosition).add(new ArrayList<ContactNumber>());
    			
    			ArrayList<ContactNumber> prunedList = new ArrayList<ContactNumber>();
    			for(int i = 1; i< numbers.size(); i++){
    				prunedList.add(numbers.get(i));
//    				nativeExtraNumbers.get(groupPosition).get(childPosition).add(prunedList.get(i));
    			}
    			if(prunedList.size()>0){
    				holder.extraNumbersLayout.setVisibility(View.VISIBLE);
        			holder.extraNumbersLayout.removeAllViews();
//        			holder.extraNumbersViews.clear();
        			for(int i = 0; i< prunedList.size(); i++){
        				View view = createPrivateExtraNumberView(groupPosition, childPosition, prunedList.get(i), contactName, contactId, getLayoutInflater(), Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
        				holder.extraNumbersLayout.addView(view);
        			}
    			}else{
    				holder.extraNumbersLayout.setVisibility(View.GONE);
    			}
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							addCheck(groupPosition, childPosition, privateChildDataTemp, privateGroupDataTemp);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
								
						}else{
							removeCheck(groupPosition, childPosition, privateChildDataTemp, privateGroupDataTemp);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			holder.primaryNumberLayout.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(!holder.childCheck.isChecked()){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							params.put("Is Primary Number", "yes");
							FlurryAgent.logEvent("Recipient Added", params);
							
							holder.childCheck.setChecked(true);
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							addCheck(groupPosition, childPosition, privateChildDataTemp, privateGroupDataTemp);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(false);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							removeCheck(groupPosition, childPosition, privateChildDataTemp, privateGroupDataTemp);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			private View createPrivateExtraNumberView(final int groupPosition, final int childPosition, final ContactNumber contactNumber, final String contactName, final long contactId, LayoutInflater inflater, final Long groupId) {
				View view = inflater.inflate(R.layout.extra_numbers_list_row, null);
				
				TextView tv = (TextView) view.findViewById(R.id.extra_number);
				final CheckBox cb = (CheckBox) view.findViewById(R.id.extra_number_checkbox);
				
				tv.setText(contactNumber.type + ": " + contactNumber.number);
				
				cb.setChecked(false);
				boolean gotChecked = false;
				for(int i = 0; i< RecipientsTemp.size() && !gotChecked; i++){
					
		    		if(contactNumber.contactId == RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
		    			for(int j=0; j<RecipientsTemp.get(i).groupIds.size(); j++){
		    				if(RecipientsTemp.get(i).groupIds.get(j)==groupId && RecipientsTemp.get(i).groupTypes.get(j)==2){
		    					cb.setChecked(true);
		    					gotChecked = true;
		    					break;
		    				}
		    			}
		    		}
		    	}
				
				view.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(!cb.isChecked()){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Priavte Group");
							params.put("Is Primary Number", "no");
							FlurryAgent.logEvent("Recipient Added", params);
							
							cb.setChecked(true);
							
							addExtraCheck(groupPosition, childPosition, cb, contactName, contactId, contactNumber, groupId);
						}else{
							cb.setChecked(false);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Priavte Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							removeExtraCheck(groupPosition, childPosition, cb, contactNumber, groupId);
						}
						contactsAdapter.notifyDataSetChanged();
					}
				});
				
				cb.setOnClickListener(new OnClickListener() {
					
					
					public void onClick(View v) {
						if(cb.isChecked()){
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							params.put("Is Primary Number", "no");
							FlurryAgent.logEvent("Recipient Added", params);
							
							addExtraCheck(groupPosition, childPosition, cb, contactName, contactId, contactNumber, groupId);
						}else{
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Private Group");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							removeExtraCheck(groupPosition, childPosition, cb, contactNumber, groupId);
						}
						contactsAdapter.notifyDataSetChanged();
					}
				});
				
				return view;
			}


			
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			
			public boolean hasStableIds() {
			   return false;
			}
			 
			
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    	};
    }
	
	
	private void addExtraCheck(int groupPosition, int childPosition, CheckBox cb, String contactName, long contactId, ContactNumber contactNumber, long groupId){
		Log.d("Entering extra number add check");
		cb.setChecked(true);
		boolean recipientExist = false;
		for(int i = 0; i< RecipientsTemp.size(); i++){
			if(RecipientsTemp.get(i).contactId == contactNumber.contactId && RecipientsTemp.get(i).number.equals(contactNumber.number)){
				recipientExist = true;
				RecipientsTemp.get(i).groupIds.add(groupId);
				RecipientsTemp.get(i).groupTypes.add(2);
				break;
			}
		}
		if(!recipientExist){
			Recipient recipient = new Recipient(-1, 2, contactName, contactId, -1, -1, -1, contactNumber.number);
			recipient.groupIds.add(groupId);
			recipient.groupTypes.add(2);
			RecipientsTemp.add(recipient);
			contactsAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	private void addCheck(int groupPosition, int childPosition, ArrayList<ArrayList<HashMap<String, Object>>> ChildDataTemp, ArrayList<HashMap<String, Object>> GroupDataTemp){
		Log.d("entering childcheck is checked true listner");
		ChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, true);
		boolean spanExist = false;
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if(RecipientsTemp.get(i).contactId == (Long) ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID) && RecipientsTemp.get(i).number.equals((String)ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER))){
				spanExist = true;
				try{
					RecipientsTemp.get(i).groupIds.add(Long.parseLong((String)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}catch (ClassCastException e) {
					RecipientsTemp.get(i).groupIds.add(((Long)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}
				RecipientsTemp.get(i).groupTypes.add(2);
				break;
			}
		}
		if(!spanExist){
			Recipient recipient = new Recipient(-1, 2, (String)ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME), (Long)ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID), -1, -1, -1, (String)ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
			try{
				recipient.groupIds.add(((Long)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}catch (ClassCastException e) {
				recipient.groupIds.add(Long.parseLong((String)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}
			recipient.groupTypes.add(2);
			RecipientsTemp.add(recipient);
			contactsAdapter.notifyDataSetChanged();
		}
	}	
		
	
	
	private void removeExtraCheck(int groupPosition, int childPosition, CheckBox cb, ContactNumber contactNumber, long groupId){
		cb.setChecked(false);
		for(int i = 0; i< RecipientsTemp.size(); i++){
			if(contactNumber.contactId==RecipientsTemp.get(i).contactId && contactNumber.number.equals(RecipientsTemp.get(i).number)){
				Long groupIdToRemove = groupId;
				int groupTypeToRemove = 2;
				for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
					if(RecipientsTemp.get(i).groupIds.get(j) == groupIdToRemove && RecipientsTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						RecipientsTemp.get(i).groupIds.remove(j);
						if(RecipientsTemp.get(i).groupIds.size()==0){
							RecipientsTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	private void removeCheck(int groupPosition, int childPosition, ArrayList<ArrayList<HashMap<String, Object>>> ChildDataTemp, ArrayList<HashMap<String, Object>> GroupDataTemp){
		ChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, false);
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if(RecipientsTemp.get(i).contactId == (Long) ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID) && RecipientsTemp.get(i).number.equals((String)ChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER))){
				for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)GroupDataTemp.get(groupPosition).get(Constants.GROUP_ID);
					}
					groupTypeToRemove = 2;
					if(RecipientsTemp.get(i).groupIds.get(j) == groupIdToRemove && RecipientsTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						RecipientsTemp.get(i).groupIds.remove(j);
						if(RecipientsTemp.get(i).groupIds.size()==0){
							RecipientsTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	private class RecentsAdapter extends ArrayAdapter {
		
		@SuppressWarnings("unchecked")
		RecentsAdapter(){
    		super(SelectContacts.this, R.layout.contacts_list_row, recentIds);
    	}
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
			final RecentsListHolder holder;
			final int _position  = position;
    		if(convertView == null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
    			holder = new RecentsListHolder();
    			holder.contactImage 		= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
        		holder.nameText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
        		holder.numberText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
        		holder.contactCheck 		= (CheckBox) 	convertView.findViewById(R.id.contact_list_row_contact_check);
        		holder.primaryNumberLayout  = (RelativeLayout) convertView.findViewById(R.id.contact_list_primary_contact_space);
    			convertView.setTag(holder);
    		}else{
    			holder = (RecentsListHolder) convertView.getTag();
    		}
    		
    		int i = 0;
    		
    		if(recentContactIds.get(position)> -1){
    			
    			for(i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
    				if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == recentContactIds.get(position)){
    					holder.contactImage.setImageBitmap(SmsSchedulerApplication.contactsList.get(i).image);
    		    		holder.nameText.setText(SmsSchedulerApplication.contactsList.get(i).name);
    		    		holder.numberText.setText(recentContactNumbers.get(position)); //TODO
    		    		
    		    		for(int j = 0; j< RecipientsTemp.size(); j++){
    		        		if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == RecipientsTemp.get(j).contactId){//TODO
    		        			holder.contactCheck.setChecked(true);
    		        			Log.d(">-1");
    		        			break;
    		        		}else{
    		        			holder.contactCheck.setChecked(false);
    		        		}
    		        	}
    		    		break;
    				}
    			}
    		}else if(recentContactIds.get(position) == -1){
    			
    			holder.contactImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
    			holder.nameText.setText(recentContactNumbers.get(position));
    			holder.numberText.setText("");
    			for(int j = 0; j< RecipientsTemp.size(); j++){
    				if(RecipientsTemp.get(j).displayName.equals(recentContactNumbers.get(position))){
    					holder.contactCheck.setChecked(true);
    					Log.d("==-1");
    					break;
    				}else{
    					holder.contactCheck.setChecked(false);
    				}
    			}
    		}
    		holder.primaryNumberLayout.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						Recipient recipient = new Recipient();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).content_uri_id == recentContactIds.get(_position)){
									recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, SmsSchedulerApplication.contactsList.get(k).content_uri_id, -1, -1, -1, SmsSchedulerApplication.contactsList.get(k).numbers.get(0).number);//TODO
									break;
								}
							}
						}else{
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).name.equals(recentContactNumbers.get(_position))){
									
								}
							}
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Recents");
							params.put("Is Primary Number", "no");
							FlurryAgent.logEvent("Recipient Added", params);
							
							recipient = new Recipient(-1, 1, recentContactNumbers.get(_position), -1, -1, -1, -1, null); //TODO
						}
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						RecipientsTemp.add(recipient);
						contactsAdapter.notifyDataSetChanged();
					}else{
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Recents");
						FlurryAgent.logEvent("Recipient Removed", params);
						
						holder.contactCheck.setChecked(false);
						for(int i = 0; i< RecipientsTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == RecipientsTemp.get(i).contactId){
				    				RecipientsTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(RecipientsTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				RecipientsTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						Recipient recipient = new Recipient();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).content_uri_id == recentContactIds.get(_position)){
									recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, SmsSchedulerApplication.contactsList.get(k).content_uri_id, -1, -1, -1, SmsSchedulerApplication.contactsList.get(k).numbers.get(0).number);
									break;
								}
							}
						}else{
							recipient = new Recipient(-1, 1, recentContactNumbers.get(_position), -1, -1, -1, -1, null);
						}
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Recents");
						params.put("Is Primary Number", "no");
						FlurryAgent.logEvent("Recipient Added", params);
						
						RecipientsTemp.add(recipient);
						
						contactsAdapter.notifyDataSetChanged();
					}else{
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Recents");
						FlurryAgent.logEvent("Recipient Removed", params);
						
						for(int i = 0; i< RecipientsTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == RecipientsTemp.get(i).contactId){
				    				RecipientsTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(RecipientsTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				RecipientsTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		return convertView;
		}
	}
	
	
	
	private class ContactsListHolder{
		ImageView 			contactImage;
		TextView 			nameText;
		TextView 			numberText;
		CheckBox 			contactCheck;
		LinearLayout		extraContactsLayout;
		RelativeLayout		primaryContactLayout;
		ArrayList<View> extraContactsViews;
//		ExtraNumbersAdapter extraNumbersAdapter;
	}
	
	
	private class GroupListHolder{
		TextView groupHeading;
		CheckBox groupCheck;
	}
	
	
	private class ChildListHolder{
		TextView childNameText;
		ImageView childContactImage;
		TextView childNumberText;
		CheckBox childCheck;
		LinearLayout extraNumbersLayout;
		RelativeLayout primaryNumberLayout;
	}
	
	
	private class RecentsListHolder{
		ImageView 	contactImage;
		TextView 	nameText;
		TextView 	numberText;
		CheckBox 	contactCheck;
		RelativeLayout primaryNumberLayout;
	}
	
	
	
	
	@SuppressWarnings("static-access")
	private void reloadPrivateGroupData(){
		mdba.open();
		
		privateChildDataTemp.clear();
		privateGroupDataTemp.clear();
		
		if(origin.equals("new")){
			ScheduleNewSms.privateGroupData.clear();
			ScheduleNewSms.privateChildData.clear();
		}else{
			EditScheduledSms.privateGroupData.clear();
			EditScheduledSms.privateChildData.clear();
		}
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> spanIdsForGroup = mdba.fetchRecipientsForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		if(spanIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< RecipientsTemp.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==RecipientsTemp.get(i).recipientId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		ArrayList<String> contactNumbers = mdba.fetchNumbersForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		AbstractScheduleSms.privateGroupData.add(group);
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
        				if(contactIds.get(i)==SmsSchedulerApplication.contactsList.get(j).content_uri_id){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(j).name);
        					ArrayList<ContactNumber> numbers = SmsSchedulerApplication.contactsList.get(j).numbers;
        					String number = "";
        					Log.d("Numbers size : " + numbers.size());
        					for(int m = 0; m< numbers.size(); m++){
        						if(numbers.get(m).number.equals(contactNumbers.get(i))){
        							number = numbers.get(m).number;
        							break;
        						}
        					}
        					childParameters.put(Constants.CHILD_NUMBER, number);//TODO
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsSchedulerApplication.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< RecipientsTemp.size(); m++){
       								if(RecipientsTemp.get(m).recipientId == spanIdsForGroup.get(k) && RecipientsTemp.get(m).recipientId == contactIds.get(i)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}
        					
        					child.add(childParameters);
        				}
        			}
        		}
        		AbstractScheduleSms.privateChildData.add(child);
        	}while(groupsCursor.moveToNext());
        }
        
        mdba.close();
        
        if(origin.equals("new")){
        	for(int groupCount = 0; groupCount< ScheduleNewSms.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				for(int i = 0; i< RecipientsTemp.size(); i++){
        			for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
        				if((RecipientsTemp.get(i).groupIds.get(j)==group.get(Constants.GROUP_ID)) && RecipientsTemp.get(i).groupTypes.get(j) == 2){
        					group.put(Constants.GROUP_CHECK, true);
        					break;
        				}
        			}
        		}
				
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< ScheduleNewSms.privateChildData.get(groupCount).size(); childCount++){
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));

					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}else{
			
			for(int groupCount = 0; groupCount< EditScheduledSms.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount < EditScheduledSms.privateChildData.get(groupCount).size(); childCount++){
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}
        
        
        groupedPrivateChildDataTemp = organizeChildData(privateGroupDataTemp, privateChildDataTemp);
	}
}