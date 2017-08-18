package com.example.gaya.searchpeople;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UsersInfoActivity extends AppCompatActivity {

    GridView androidGridView;

    ArrayList<String> usersName;
    ArrayList<String> userPhotos;
    ArrayList<String> userBitmaps;
    ArrayList<String> usernames;

    @Override
    public void finish() {
        System.out.println("finish activity");
        System.runFinalizersOnExit(true) ;
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view_image_text);

        usersName = new ArrayList<>();
        userPhotos = new ArrayList<>();
        userBitmaps = new ArrayList<>();
        usernames = new ArrayList<>();



        ImageButton bUsers = (ImageButton) findViewById(R.id.bUsers);
        ImageButton bSettings = (ImageButton) findViewById(R.id.bSetting);
        ImageButton bHome = (ImageButton) findViewById(R.id.bHome);
        ImageButton bExit = (ImageButton) findViewById(R.id.bExit);
        ImageButton bSearch = (ImageButton) findViewById(R.id.bSearch);

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UsersInfoActivity.this, SearchActivity.class);
                UsersInfoActivity.this.startActivity(registerIntent);
            }
        });

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UsersInfoActivity.this, UserMainActivity.class);
                UsersInfoActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UsersInfoActivity.this, UsersInfoActivity.class);
                UsersInfoActivity.this.startActivity(registerIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UsersInfoActivity.this, UserSettingActivity.class);
                UsersInfoActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(UsersInfoActivity.this, LoginActivity.class);
                UsersInfoActivity.this.startActivity(registerIntent);
            }
        });
        if (ConnectionDetector.isConnected(getApplicationContext())) {

            NetworkConnector connector = new NetworkConnector();
            connector.execute();
        }


    }

    private class NetworkConnector extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            try {

                CloudStorageAccount storageAccount =
                        CloudStorageAccount.parse(UserInfo.storageConnectionString);

                // Create the table client.
                CloudTableClient tableClient = storageAccount.createCloudTableClient();

                // Create a cloud table object for the table.
                CloudTable cloudTable = tableClient.getTableReference("userInfo");
                TableQuery<DBclass> partitionQuery =
                        TableQuery.from(DBclass.class);
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                CloudBlobContainer container = blobClient.getContainerReference("photo");


                // Loop through the results, displaying information about the entity.
                for (DBclass dBclass : cloudTable.execute(partitionQuery)) {

                    usersName.add(dBclass.getName() + " " + dBclass.getSurname());
                    usernames.add(dBclass.getPartitionKey());
                    CloudBlockBlob blob = container.getBlockBlobReference(dBclass.getPartitionKey() + ".jpg");
                    userPhotos.add(blob.getUri().toString());
                    userBitmaps.add(blob.getUri().toString());
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
            if (result.equals("OK")) {
                GridImageTextActivity adapterViewAndroid = new GridImageTextActivity(UsersInfoActivity.this, usersName, userBitmaps);
                androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
                androidGridView.setAdapter(adapterViewAndroid);
                androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        UserInfo.otherusername=usernames.get(+i);
                        Intent registerIntent = new Intent(UsersInfoActivity.this, UserPageActivity.class);
                        UsersInfoActivity.this.startActivity(registerIntent);
                    }
                });

            }
            else
            {

            }
        }
    }
}