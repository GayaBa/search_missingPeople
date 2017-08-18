package com.example.gaya.searchpeople;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;

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

public class SearchActivity extends AppCompatActivity {

    boolean users;
    EditText etName, etSurname;

    GridView androidGridView;

    ArrayList<String> usersName;
    ArrayList<String> userPhotos;
    ArrayList<String> userBitmaps;
    ArrayList<String> usernames;
    ArrayList<Integer> ids;

    Button bSearch;
    ImageButton bUsers, bSettings, bHome, bExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);



        users=true;

         etName=(EditText) findViewById(R.id.etName);
         etSurname=(EditText) findViewById(R.id.etSurname);
         bSearch = (Button) findViewById(R.id.bSearch);

         bUsers = (ImageButton) findViewById(R.id.bUsers);
         bSettings = (ImageButton) findViewById(R.id.bSetting);
         bHome = (ImageButton) findViewById(R.id.bHome);
         bExit = (ImageButton) findViewById(R.id.bExit);

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(SearchActivity.this, UserMainActivity.class);
                SearchActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(SearchActivity.this, UsersInfoActivity.class);
                SearchActivity.this.startActivity(registerIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(SearchActivity.this, UserSettingActivity.class);
                SearchActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(SearchActivity.this, LoginActivity.class);
                SearchActivity.this.startActivity(registerIntent);
            }
        });


        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersName = new ArrayList<>();
                userPhotos = new ArrayList<>();
                userBitmaps = new ArrayList<>();
                usernames = new ArrayList<>();
                ids = new ArrayList<>();

                if (ConnectionDetector.isConnected(v.getContext())) {

                    NetworkConnector connector = new NetworkConnector(etName.getText().toString(), etSurname.getText().toString());
                    connector.execute();
                }
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

    public void onRadioButtonClicked(View view) {

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.users:
                if (checked)
                    users=true;
                break;
            case R.id.searchedPeople:
                if (checked)
                    users=false;
                    break;
        }
    }

    private class NetworkConnector extends AsyncTask<String, Void, String> {
        private String name, surname;
        NetworkConnector(String name, String surname)
        {
            this.name=name;
            this.surname=surname;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                CloudStorageAccount storageAccount =
                        CloudStorageAccount.parse(UserInfo.storageConnectionString);

                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable;
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                CloudBlobContainer container = blobClient.getContainerReference("photo");
                if(users) {
                     cloudTable = tableClient.getTableReference("userInfo");
                    TableQuery<DBclass> partitionQuery =
                            TableQuery.from(DBclass.class);

                    for (DBclass dBclass : cloudTable.execute(partitionQuery)) {

                        boolean flag=false;
                        if(name!=null && surname!=null && dBclass.getName().equals(name) &&  dBclass.getSurname().equals(surname))
                        {
                            flag=true;
                        }
                        if(flag || dBclass.getName().equals(name) || dBclass.getSurname().equals(surname))
                        {
                            usersName.add(dBclass.getName() + " " + dBclass.getSurname());
                            usernames.add(dBclass.getPartitionKey());
                            CloudBlockBlob blob = container.getBlockBlobReference(dBclass.getPartitionKey() + ".jpg");
                            userPhotos.add(blob.getUri().toString());
                            userBitmaps.add(blob.getUri().toString());
                            return "OK";
                        }
                    }
                    return "";
                }
                else
                {
                     cloudTable = tableClient.getTableReference("searchedPeopleInfo");
                    TableQuery<SearchedDBclass> partitionQuery =
                            TableQuery.from(SearchedDBclass.class);


                    for (SearchedDBclass searchedDBclass : cloudTable.execute(partitionQuery)) {

                        boolean flag=false;
                        if(name!=null && surname!=null && searchedDBclass.getName().equals(name) &&  searchedDBclass.getSurname().equals(surname))
                        {
                            flag=true;
                        }
                        if(flag || searchedDBclass.getName().equals(name) || searchedDBclass.getSurname().equals(surname))
                        {
                            usersName.add(searchedDBclass.getName() + " " + searchedDBclass.getSurname());
                            ids.add(searchedDBclass.getId());
                            usernames.add(searchedDBclass.getRowKey());
                            CloudBlockBlob blob = container.getBlockBlobReference(searchedDBclass.getId() + "_"+
                                    searchedDBclass.getRowKey()+ ".jpg");
                            userPhotos.add(blob.getUri().toString());
                            userBitmaps.add(blob.getUri().toString());
                            return "OK";
                        }
                    }
                    return "";
                }



            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {
                GridImageTextActivity adapterViewAndroid = new GridImageTextActivity(SearchActivity.this, usersName, userBitmaps);
                androidGridView = (GridView) findViewById(R.id.gvSearchResult);
                androidGridView.setAdapter(adapterViewAndroid);
                androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        if(users) {
                            UserInfo.otherusername = usernames.get(+i);
                            Intent registerIntent = new Intent(SearchActivity.this, UserPageActivity.class);
                            SearchActivity.this.startActivity(registerIntent);
                        }
                        else{
                            UserInfo.searchedUser = usernames.get(+i);
                            UserInfo.searchedId=ids.get(+i);
                            UserInfo.bitmap=userBitmaps.get(+i);
                            Intent registerIntent = new Intent(SearchActivity.this, UserSearchedPeopleActivity.class);
                            SearchActivity.this.startActivity(registerIntent);
                        }
                    }
                });

            }
            else
            {
                etName.setError("Nothing to show");
            }
        }
    }
}
