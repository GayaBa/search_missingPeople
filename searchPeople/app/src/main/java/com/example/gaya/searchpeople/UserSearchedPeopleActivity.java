package com.example.gaya.searchpeople;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserSearchedPeopleActivity extends AppCompatActivity {

    TextView etName, etSurname, etAge, etComment, tvSearchedName;
    ImageView ivPhoto;
    boolean bClick;
    Button bDelete;

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
        setContentView(R.layout.activity_user_searched_people);

         etName = (TextView) findViewById(R.id.etName);
         etSurname = (TextView) findViewById(R.id.etSurname);
         etAge = (TextView) findViewById(R.id.etAge);
         etComment = (TextView) findViewById(R.id.etComment);
         tvSearchedName=(TextView) findViewById(R.id.tvSearcherName);
        ivPhoto=(ImageView) findViewById(R.id.ivPhoto);
         bDelete=(Button) findViewById(R.id.bDelete);
        bClick=false;

        if(!UserInfo.username.equals(UserInfo.searchedUser))
        {
            bDelete.setBackgroundColor(Color.GRAY);
            bDelete.setEnabled(false);
        }


        ImageButton bUsers = (ImageButton) findViewById(R.id.bUsers);
        ImageButton bSettings = (ImageButton) findViewById(R.id.bSetting);
        ImageButton bHome = (ImageButton) findViewById(R.id.bHome);
        ImageButton bExit = (ImageButton) findViewById(R.id.bExit);
        ImageButton bSearch = (ImageButton) findViewById(R.id.bSearch);

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, SearchActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
            }
        });

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, UserMainActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, UsersInfoActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, UserSettingActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, LoginActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
            }
        });
        if (ConnectionDetector.isConnected(getApplicationContext())) {

            NetworkConnector connector = new NetworkConnector(getApplicationContext());
            connector.execute();
        }

        tvSearchedName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo.otherusername=UserInfo.searchedUser;
                Intent registerIntent = new Intent(UserSearchedPeopleActivity.this, UserPageActivity.class);
                UserSearchedPeopleActivity.this.startActivity(registerIntent);
            }
        });
        bDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bClick=true;
                if (ConnectionDetector.isConnected(v.getContext())) {

                    NetworkConnector connector = new NetworkConnector(getApplicationContext());
                    connector.execute();
                }
            }
        });

    }

    private class NetworkConnector extends AsyncTask<String, Void, String> {


        private String name, surname, comment, age,searcherName;
        private Context context;
        NetworkConnector(Context context)
        {
            this.context=context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                    final String ROW_KEY = "RowKey";
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(UserInfo.storageConnectionString);
                    CloudTableClient tableClient = storageAccount.createCloudTableClient();
                    CloudTable cloudTable = tableClient.getTableReference("searchedPeopleInfo");
                    String partitionFilter = TableQuery.generateFilterCondition(
                            ROW_KEY,
                            TableQuery.QueryComparisons.EQUAL, UserInfo.searchedUser);

                    TableQuery<SearchedDBclass> partitionQuery =
                            TableQuery.from(SearchedDBclass.class)
                                    .where(partitionFilter);
                    for (SearchedDBclass searchedDBclass : cloudTable.execute(partitionQuery)) {
                        if (searchedDBclass.getId() == UserInfo.searchedId) {
                            if(bClick)
                            {
                                TableOperation retrieveDelete = TableOperation.retrieve("", UserInfo.searchedUser, SearchedDBclass.class);
                                SearchedDBclass entityDelete =
                                        cloudTable.execute(retrieveDelete).getResultAsType();
                               TableOperation deleteSearched = TableOperation.delete(entityDelete);
                                cloudTable.execute(deleteSearched);
                                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                                CloudBlobContainer container = blobClient.getContainerReference("photo");
                                CloudBlockBlob blob = container.getBlockBlobReference(searchedDBclass.getId()+"_"+
                                        searchedDBclass.getRowKey()+".jpg");
                                blob.deleteIfExists();
                                return "Deleted";
                            }
                            else {
                                name = searchedDBclass.getName();
                                surname = searchedDBclass.getSurname();
                                age = searchedDBclass.getAge();
                                comment = searchedDBclass.getComment();
                                CloudTable cloudTableuser = tableClient.getTableReference("userInfo");
                                TableOperation retrieveSearcher =
                                        TableOperation.retrieve(UserInfo.searchedUser, "", DBclass.class);

                                DBclass specificEntity =
                                        cloudTableuser.execute(retrieveSearcher).getResultAsType();
                                if (specificEntity != null) {
                                    searcherName = specificEntity.getName() + "  " + specificEntity.getSurname();
                                }
                            }
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
            if (result.equals("OK")) {
                etName.setText(name);
                etSurname.setText(surname);
                etAge.setText(age+"  "+"years old");
                etComment.setText(comment);
                tvSearchedName.setText(searcherName);
                Picasso.with(context)
                        .load(UserInfo.bitmap)
                        .fit()
                        .error(R.drawable.user)
                        .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                        .into(ivPhoto);
            }
            else
            {
                if(result.equals("Deleted"))
                {
                    Intent phinIntent = new Intent(UserSearchedPeopleActivity.this, UserMainActivity.class);
                    UserSearchedPeopleActivity.this.startActivity(phinIntent);
                }
                else {
                    etSurname.setError("Please, check your connection");
                }
            }
        }
    }
}
