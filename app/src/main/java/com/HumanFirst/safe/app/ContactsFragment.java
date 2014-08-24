package com.HumanFirst.safe.app;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends ListFragment {

    String[] name;
    String[] phonenumber;

    ContactCustomList adapter;

    ArrayList<Contact> contact_list;
    List<Contact> list ;
    DatabaseHandler db;

    NotificationManager mNotificationManager;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false ;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_list , menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Clear notification when fragment started
        mNotificationManager = (NotificationManager)getActivity().getApplication() .getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager!=null) {
            mNotificationManager.cancelAll();
        }


        contact_list = new ArrayList<Contact>();
        contact_list = getListData();

        name = new String[contact_list.size()];
        phonenumber = new String[contact_list.size()];


        int i = 0;

        for ( Contact contact : contact_list){

            name[i] = contact.getName();
            phonenumber[i] = contact.getPhone();
            i++ ;

        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new ContactCustomList(getActivity().getApplicationContext(), name, phonenumber);
        }

        setListAdapter(adapter);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
		return rootView;
	}

    private ArrayList<Contact> getListData() {
        // TODO Auto-generated method stub

        db = new DatabaseHandler(getActivity().getApplicationContext());

        ArrayList<Contact> results = new ArrayList<Contact>();
        results.clear();
        List<Contact> contacts = db.getAllContacts();

        for (Contact cnt : contacts)
            results.add(cnt);

        return results;
    }
}
