package com.gmail.wazappdotgithub.ships;


import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.comms.ComModule;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UserInput extends Activity implements Observer, OnClickListener{

	public static final String tag = "Ships UserInput ";
	public static final String SMS_RECIPIENT_EXTRA = "com.gmail.wazappdotgithub.ships.SMS_RECIPIENT";
	public static final String ACTION_SMS_SENT = "com.gmail.wazappdotgithub.ships.SMS_SENT_ACTION";
	protected String MESSAGES_TO_FRIEND = "http://ships.com/Ships, press if you want to pla.."; //Maybe store the message in the string.xml....
	protected String PHONE_NUMBER;


	private Button challengeAFriend = null;
	private Button letsContinueButton = null;
	private Button fleeHome = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.userinput);

		challengeAFriend = (Button) findViewById(R.id.button3);

		letsContinueButton = (Button) findViewById(R.id.button1);

		fleeHome = (Button) findViewById(R.id.button2);

		challengeAFriend.setOnClickListener(this);
		letsContinueButton.setOnClickListener(this);
		fleeHome.setOnClickListener(this);


	}

	@Override
	public void onClick(View arg0) {

		if (arg0 == challengeAFriend) {

			try {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, 1);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Error in intent : ", e.toString());
			}


			//this.onActivityResult(1, RESULT_OK, getIntent());


		}

		else if (arg0 == letsContinueButton) {

			try {
				Log.d(tag, tag + "launching Communication Module");
				//ComModule.serve_from_tcp(Constants.DEFAULT_PORT);
				ComModule.serve_computer(Constants.DEFAULT_PORT);
				//ComModule.connect_to_tcp(Inet4Address.getByName("87.227.1.232"), Constants.DEFAULT_PORT);

				Log.d(tag, tag + "creating remoteclient");
				RemoteClient.newInstance(UserInput.this, true);
				Log.d(tag, tag + "completed remoteclient");

				RemoteClient.getInstance().playerCompletedUserInput();

			} catch (IOException e) {
				e.printStackTrace();
				AlertDialog.Builder builder = new AlertDialog.Builder(UserInput.this);

				builder.setMessage("An error occurred establishing the connection\n" + e.getMessage())
				.setCancelable(false)
				.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						UserInput.this.finish();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}
		}




		else if (arg0 == fleeHome) {

			finish();

		}	

	}


	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {

		super.onActivityResult(reqCode, resultCode, data);

		try {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor cur = managedQuery(contactData, null, null, null, null);
				ContentResolver contect_resolver = getContentResolver();

				if (cur.moveToFirst()) {
					String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String name = "";
					String no = "";

					Cursor phoneCur = contect_resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);

					if (phoneCur.moveToFirst()) {
						name = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						no = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}

					Log.e("Phone no & name :***: ", name + " : " + no);
					//txt.append(name + " : " + no + "\n");

					id = null;
					//name = null;
					//no = null;
					phoneCur = null;
					PHONE_NUMBER = no;
				}
				contect_resolver = null;
				cur = null;


				//populateContacts();

				this.textFriend(PHONE_NUMBER, MESSAGES_TO_FRIEND);

			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log.e("IllegalArgumentException :: ", e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Error :: ", e.toString());
		}

	}



	public void textFriend(String phonenumber, String messageToFriend) {



		SmsManager sms = SmsManager.getDefault();

		List<String> messages = sms.divideMessage(messageToFriend);

		//String recipient = recipientTextEdit.getText().toString();

		for (String message : messages) {

			sms.sendTextMessage(phonenumber, null, message, PendingIntent.getBroadcast(UserInput.this, 0, new Intent(ACTION_SMS_SENT), 0), null);

		}

		/* To add:  A method (Or own activity) taking PHONE_NUMBER from above and sending an actionable text message, sms, (with actionable text) to the person/friend in question. 
	   The text message should contain some kind of ip-number/other relevant information to which the friend can connect through the net. -- Almost done.*/

		/* ALSO to add: Progress bar showing "Trying to contact friend" or something... + code to establish connection before moving on to the next Activity*/


	}


	@Override
	public void update(Observable observable, Object data) {
		Statename s = ((Statename) data);
		if (s == Statename.PREGAME) {
			Log.d(tag, tag + "Received " + s + " event");
			RemoteClient.getInstance().removeAsObserver(this);
			Log.d(tag, tag + "Creating intent");
			Intent next = new Intent(UserInput.this, PreGame.class);
			Log.d(tag, tag + "starting activity");
			startActivity(next);

			//this shall not finish because postgame will return to it
		}
	}

}
