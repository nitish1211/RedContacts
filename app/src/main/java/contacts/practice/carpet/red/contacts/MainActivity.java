package contacts.practice.carpet.red.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import contacts.practice.carpet.red.contacts.Data.Contact;

public class MainActivity extends AppCompatActivity {

    int currentapiVersion;
    final int MY_PERMISSIONS = 5;

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    //CSV file header
    private static final String FILE_HEADER = "name,phoneNumber";


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentapiVersion = android.os.Build.VERSION.SDK_INT;

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                Log.d("Button","Clicked");
            }
        });


    }

    public void checkPermission(){
        if(currentapiVersion < 23)
            return;
        requestAllPermission();

    }

    public void getContacts() {

        String phoneNumber = null;

        ArrayList<Contact> contacts = new ArrayList<>();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;


        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {
                    Log.d("name",name);

                    // Query and loop for every phone number of the contact

                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        Log.d("number",phoneNumber);

                    }
                    contacts.add(new Contact(name,phoneNumber));

                    phoneCursor.close();

                }

            }
        }

        storeContacts(contacts);


    }


    public void requestAllPermission(){
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String[] permList = listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]);

        if(permList.length > 0)
        ActivityCompat.requestPermissions(MainActivity.this,permList,MY_PERMISSIONS);
        else
            performWork();

        }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED ) {

                    performWork();

                } else {

                    Toast.makeText(MainActivity.this, "Permission not Granted", Toast.LENGTH_LONG).show();

                }
                return;
            }

        }
    }

    public void storeContacts(ArrayList<Contact> contacts) {

        String data = "";
        String fileName = "contacts.csv";
        File file;

        file = new File(Environment.getExternalStorageDirectory(), fileName);
                //Write the CSV file header
                data = data + FILE_HEADER;

                //Add a new line separator after the header
                data = data + NEW_LINE_SEPARATOR;

                for (Contact contact : contacts) {
                    data = data + contact.getName();
                    data = data + COMMA_DELIMITER;
                    data = data + contact.getPhoneNumber();
                    data = data + NEW_LINE_SEPARATOR;
                }
                Log.d("CSV ", data);

        FileOutputStream outputStream = null;
        try {

            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

            Log.d("Done","with file" +file.getName()+ file.getAbsolutePath());

    }

    public void performWork(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        };
        new Thread(runnable).start();
    }

}
