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
import android.widget.Button;
import android.widget.EditText;
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
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UserPageActivity extends AppCompatActivity {

    ImageView ivPicture;
    TextView tvName, tvAge, tvPhone, tvEmail, tvComment;

    ArrayList<String> usersName;
    ArrayList<String> usernames;
    ArrayList<String> userPhotos;
    ArrayList<String> userBitmaps;
    ArrayList<Integer> ids;

    GridView androidGridView;

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
        setContentView(R.layout.activity_user_page);

         tvName = (TextView) findViewById(R.id.tvName);
         tvAge = (TextView) findViewById(R.id.tvAge);
         tvPhone = (TextView) findViewById(R.id.tvPhone);
         tvEmail = (TextView) findViewById(R.id.tvEmail);
         tvComment = (TextView) findViewById(R.id.tvComment);
         ivPicture = (ImageView) findViewById(R.id.ivPicture);

        usernames = new ArrayList<>();
        usersName = new ArrayList<>();
        userPhotos = new ArrayList<>();
        userBitmaps = new ArrayList<>();
        ids = new ArrayList<>();

        ImageButton bUsers = (ImageButton) findViewById(R.id.bUsers);
        ImageButton bSettings = (ImageButton) findViewById(R.id.bSetting);
        ImageButton bHome = (ImageButton) findViewById(R.id.bHome);
        ImageButton bExit = (ImageButton) findViewById(R.id.bExit);
        ImageButton bSearch = (ImageButton) findViewById(R.id.bSearch);

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserPageActivity.this, SearchActivity.class);
                UserPageActivity.this.startActivity(registerIntent);
            }
        });

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserPageActivity.this, UserMainActivity.class);
                UserPageActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserPageActivity.this, UsersInfoActivity.class);
                UserPageActivity.this.startActivity(registerIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserPageActivity.this, UserSettingActivity.class);
                UserPageActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(UserPageActivity.this, LoginActivity.class);
                UserPageActivity.this.startActivity(registerIntent);
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

                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable = tableClient.getTableReference("userInfo");
                TableOperation retrieveUsernamePassword = TableOperation.retrieve(UserInfo.otherusername,"", DBclass.class);
                DBclass specificEntity = cloudTable.execute(retrieveUsernamePassword).getResultAsType();
                if (specificEntity != null) {
                    UserInfo.otheremail = specificEntity.getEmail();
                    UserInfo.otherage = specificEntity.getAge();
                    UserInfo.othername = specificEntity.getName();
                    UserInfo.othersurname = specificEntity.getSurname();
                    UserInfo.otherphone = specificEntity.getPhoneNumber();
                    UserInfo.othercomment = specificEntity.getComment();
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                    CloudBlobContainer container = blobClient.getContainerReference("photo");
                    CloudBlockBlob blob = container.getBlockBlobReference(UserInfo.otherusername + ".jpg");
                    UserInfo.otheruri = blob.getUri().toString();
                    }

                // Create a cloud table object for the table.
                CloudTable cloudTableuser = tableClient.getTableReference("searchedPeopleInfo");

                cloudTable.createIfNotExists();

                TableQuery<SearchedDBclass> partitionQuery =
                        TableQuery.from(SearchedDBclass.class);
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                CloudBlobContainer container = blobClient.getContainerReference("photo");
                for (SearchedDBclass searchedDBclass : cloudTableuser.execute(partitionQuery)) {
                    if(searchedDBclass.getRowKey().equals(UserInfo.otherusername))
                    {
                        usersName.add(searchedDBclass.getName()+" "+ searchedDBclass.getSurname());
                        usernames.add(searchedDBclass.getRowKey());
                        ids.add(searchedDBclass.getId());
                        CloudBlockBlob blob = container.getBlockBlobReference( Integer.toString(searchedDBclass.getId())+"_"+
                                searchedDBclass.getPartitionKey()  + ".jpg");
                        String URL=blob.getUri().toString();
                        userPhotos.add(URL.substring(0,URL.length()-4)+searchedDBclass.getRowKey()+URL.substring(URL.length()-4, URL.length()));
                        URL urlConnection1 = new URL(blob.getUri().toString());
                        HttpURLConnection connection1 = (HttpURLConnection) urlConnection1
                                .openConnection();
                        connection1.setDoInput(true);
                        connection1.connect();
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
            if (result.equals("OK")) {
                tvName.setText(UserInfo.othername+" "+UserInfo.othersurname);
                tvAge.setText("Age: " + UserInfo.otherage);
                tvEmail.setText("Email: " + UserInfo.otheremail);
                if(UserInfo.otherphone == null)
                    tvPhone.setText("Phone: -");
                else
                    tvPhone.setText("Phone: " + UserInfo.otherphone);
                if(UserInfo.othercomment == null)
                    tvComment.setText(" -");
                else
                    tvComment.setText(UserInfo.othercomment);
                ImageLoadTask imageLoadTask = new ImageLoadTask(getApplicationContext(), ivPicture);
                imageLoadTask.execute();


            }
            else
            {
                tvName.setError("Please, check your connection");
            }
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, String> {

        private ImageView imageView;
        private Context context;

        public ImageLoadTask(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
               return UserInfo.otheruri;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Picasso.with(context)
                    .load(result)
                    .fit()
                    .error(R.drawable.user)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(imageView);
            GridImageTextActivity adapterViewAndroid = new GridImageTextActivity(UserPageActivity.this, usersName, userBitmaps);
            androidGridView = (GridView) findViewById(R.id.lvSearched);
            androidGridView.setAdapter(adapterViewAndroid);
            androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    UserInfo.searchedUser=usernames.get(+i);
                    UserInfo.searchedId=ids.get(+i);
                    UserInfo.bitmap=userBitmaps.get(+i);
                    Intent registerIntent = new Intent(UserPageActivity.this, UserSearchedPeopleActivity.class);
                    UserPageActivity.this.startActivity(registerIntent);
                }
            });
        }

    }

}
