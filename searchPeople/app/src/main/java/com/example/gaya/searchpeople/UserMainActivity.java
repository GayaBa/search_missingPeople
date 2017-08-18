package com.example.gaya.searchpeople;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UserMainActivity extends AppCompatActivity {

    ArrayList<String> usersName;
    ArrayList<String> usernames;
    ArrayList<String> userPhotos;
    ArrayList<String> userBitmaps;
    ArrayList<Integer> ids;

    TextView tvName, tvAge, tvPhone, tvEmail, tvComment;
    Button bAdd;

    GridView androidGridView;
    ImageView ivPicture;

    ImageButton bUsers, bSettings,  bExit, bSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        usernames = new ArrayList<>();
        usersName = new ArrayList<>();
        userPhotos = new ArrayList<>();
        userBitmaps = new ArrayList<>();
        ids = new ArrayList<>();

         tvName = (TextView) findViewById(R.id.tvName);
         tvAge = (TextView) findViewById(R.id.tvAge);
         tvPhone = (TextView) findViewById(R.id.tvPhone);
         tvEmail = (TextView) findViewById(R.id.tvEmail);
         tvComment = (TextView) findViewById(R.id.tvComment);
        ivPicture = (ImageView) findViewById(R.id.ivPicture);
         bAdd = (Button) findViewById(R.id.bAdd);

         bUsers = (ImageButton) findViewById(R.id.bUsers);
         bSearch = (ImageButton) findViewById(R.id.bSearch);
         bSettings = (ImageButton) findViewById(R.id.bSetting);
         bExit = (ImageButton) findViewById(R.id.bExit);

        tvName.setText(UserInfo.name + " " + UserInfo.surname);
        tvAge.setText("Age: " + UserInfo.age);
        tvEmail.setText("Email: " + UserInfo.email);
        if(UserInfo.phone == null)
            tvPhone.setText("Phone: -");
        else
            tvPhone.setText("Phone: " + UserInfo.phone);
        if(UserInfo.comment == null)
            tvComment.setText("-");
        else
            tvComment.setText(UserInfo.comment);
        if (ConnectionDetector.isConnected(getApplicationContext())) {

            NetworkConnector connector = new NetworkConnector(getApplicationContext());
            connector.execute();
        }

        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserMainActivity.this, AddSearchedActivity.class);
                UserMainActivity.this.startActivity(registerIntent);

            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserMainActivity.this, UsersInfoActivity.class);
                UserMainActivity.this.startActivity(registerIntent);
            }
        });
        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserMainActivity.this, UserSettingActivity.class);
                UserMainActivity.this.startActivity(registerIntent);
            }
        });

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserMainActivity.this, SearchActivity.class);
                UserMainActivity.this.startActivity(registerIntent);
            }
        });
        bExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserInfo.name="";
                UserInfo.surname="";
                UserInfo.username="";
                UserInfo.password="";
                UserInfo.email="";
                UserInfo.age="";
                UserInfo.phone="";
                UserInfo.comment="";
                UserInfo.othername="";
                UserInfo.othersurname="";
                UserInfo.otherusername="";
                UserInfo.otheremail="";
                UserInfo.otherage="";
                UserInfo.otherphone="";
                UserInfo.othercomment="";
                UserInfo.id=1;
                UserInfo.uri="";
                UserInfo.otheruri="";
                UserInfo.searchedUser="";
                UserInfo.searchedId=0;
                Intent registerIntent = new Intent(UserMainActivity.this, LoginActivity.class);
                UserMainActivity.this.startActivity(registerIntent);
            }
        });



    }

    @Override
    public void finish() {
        System.out.println("finish activity");
        System.runFinalizersOnExit(true) ;
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private class NetworkConnector extends AsyncTask<String, Void, String> {

        private  int id;
        private Context context;
        NetworkConnector(Context context)
        {
            this.context=context;
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                CloudStorageAccount storageAccount =
                        CloudStorageAccount.parse(UserInfo.storageConnectionString);

                // Create the table client.
                CloudTableClient tableClient = storageAccount.createCloudTableClient();

                // Create a cloud table object for the table.
                CloudTable cloudTable = tableClient.getTableReference("searchedPeopleInfo");

                cloudTable.createIfNotExists();

                TableQuery<SearchedDBclass> partitionQuery =
                        TableQuery.from(SearchedDBclass.class);
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                id=0;
                CloudBlobContainer container = blobClient.getContainerReference("photo");
                for (SearchedDBclass searchedDBclass : cloudTable.execute(partitionQuery)) {
                    if(searchedDBclass.getRowKey().equals(UserInfo.username))
                    {
                        if(searchedDBclass.getId()>id)
                        {
                            id=searchedDBclass.getId();
                        }
                        usersName.add(searchedDBclass.getName()+" "+ searchedDBclass.getSurname());
                        usernames.add(searchedDBclass.getRowKey());
                        ids.add(searchedDBclass.getId());
                        CloudBlockBlob blob = container.getBlockBlobReference( Integer.toString(searchedDBclass.getId())+"_"+
                                searchedDBclass.getPartitionKey()  + ".jpg");
                        String URL=blob.getUri().toString();
                        userPhotos.add(URL.substring(0,URL.length()-4)+searchedDBclass.getRowKey()+URL.substring(URL.length()-4, URL.length()));
                        userBitmaps.add(URL.substring(0,URL.length()-4)+searchedDBclass.getRowKey()+URL.substring(URL.length()-4, URL.length()));
                    }
                }

                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Picasso.with(context)
                    .load(UserInfo.uri)
                    .fit()
                    .error(R.drawable.user)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(ivPicture);

            if (result.equals("OK")) {
                UserInfo.id=id;

                GridImageTextActivity adapterViewAndroid = new GridImageTextActivity(UserMainActivity.this, usersName, userBitmaps);
                androidGridView = (GridView) findViewById(R.id.gvSearched);
                androidGridView.setAdapter(adapterViewAndroid);
                androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        UserInfo.searchedUser=usernames.get(+i);
                        UserInfo.searchedId=ids.get(+i);
                        UserInfo.bitmap=userBitmaps.get(+i);
                        Intent registerIntent = new Intent(UserMainActivity.this, UserSearchedPeopleActivity.class);
                        UserMainActivity.this.startActivity(registerIntent);
                    }
                });

            }

        }
    }


}