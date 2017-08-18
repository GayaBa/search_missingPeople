package com.example.gaya.searchpeople;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddSearchedActivity extends AppCompatActivity {

    private static final int SELECTED_PICTURE = 100;
    Uri uri;
    ImageView iv;
    EditText etSurname, etName, etAge,etComment;
    Button bPhoto, bAdd;
    ImageButton bUsers, bSettings, bHome, bExit, bSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_searched);


          etName = (EditText) findViewById(R.id.etName);
          etSurname = (EditText) findViewById(R.id.etSurname);
          etAge = (EditText) findViewById(R.id.etAge);
          etComment = (EditText) findViewById(R.id.etComment);

        iv = (ImageView) findViewById(R.id.ivPhoto);
         bPhoto = (Button) findViewById(R.id.bPhoto);
         bAdd = (Button) findViewById(R.id.bAddInfo);

         bUsers = (ImageButton) findViewById(R.id.bUsers);
         bSettings = (ImageButton) findViewById(R.id.bSetting);
         bHome = (ImageButton) findViewById(R.id.bHome);
         bExit = (ImageButton) findViewById(R.id.bExit);
         bSearch = (ImageButton) findViewById(R.id.bSearch);

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(AddSearchedActivity.this, SearchActivity.class);
                AddSearchedActivity.this.startActivity(registerIntent);
            }
        });

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(AddSearchedActivity.this, UserMainActivity.class);
                AddSearchedActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(AddSearchedActivity.this, UsersInfoActivity.class);
                AddSearchedActivity.this.startActivity(registerIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(AddSearchedActivity.this, UserSettingActivity.class);
                AddSearchedActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(AddSearchedActivity.this, LoginActivity.class);
                AddSearchedActivity.this.startActivity(registerIntent);
            }
        });

        bPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionDetector.isConnected(v.getContext())) {
                    if (TextUtils.isEmpty(etName.getText().toString())) {
                        etName.setError("The name cannot be empty");
                        return;
                    }
                    if (TextUtils.isEmpty(etSurname.getText().toString())) {
                        etSurname.setError("The surname cannot be empty");
                        return;
                    }

                    if (TextUtils.isEmpty(etAge.getText().toString())) {
                        etAge.setError("The age cannot be empty");
                        return;
                    }

                    if (TextUtils.isEmpty(etComment.getText().toString())) {
                        etComment.setError("The comment cannot be empty");
                        return;
                    }
                    if (ConnectionDetector.isConnected(v.getContext())) {

                        NetworkConnector connector = new NetworkConnector(etName.getText().toString(), etSurname.getText().toString(),
                                etComment.getText().toString(), etAge.getText().toString(), getApplicationContext());
                        connector.execute();
                    }
                }

            }
        });

    }

    private class NetworkConnector extends AsyncTask<String, Void, String> {


        private String name, surname, comment, age;
        private Context context;

        NetworkConnector(String name, String surname, String comment, String age, Context context) {
            this.name = name;
            this.surname = surname;
            this.comment=comment;
            this.age=age;
            this.context=context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                CloudStorageAccount storageAccount = CloudStorageAccount.parse(UserInfo.storageConnectionString);
                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable = tableClient.getTableReference("searchedPeopleInfo");

                SearchedDBclass searchedDBclass = new SearchedDBclass(UserInfo.username, "");

                searchedDBclass.setName(name);
                searchedDBclass.setSurname(surname);
                searchedDBclass.setComment(comment);
                searchedDBclass.setAge(age);
                searchedDBclass.setId(UserInfo.id++);
                TableOperation insertCustomer1 = TableOperation.insertOrReplace(searchedDBclass);
                cloudTable.execute(insertCustomer1);

                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                CloudBlobContainer container = blobClient.getContainerReference("photo");
                CloudBlockBlob blob = container.getBlockBlobReference(Integer.toString(searchedDBclass.getId()) + "_"+
                        searchedDBclass.getRowKey()+ ".jpg");
                BlobOutputStream blobOutputStream = blob.openOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream s = cr.openInputStream(uri);
                byte[] arr = convertInputStreamToByteArray(s);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(arr);
                int next = inputStream.read();
                while (next != -1) {
                    blobOutputStream.write(next);
                    next = inputStream.read();
                }

                blobOutputStream.close();
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        }

        public byte[] convertInputStreamToByteArray(InputStream inputStream)
        {
            byte[] bytes= null;

            try
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte data[] = new byte[1024];
                int count;

                while ((count = inputStream.read(data)) != -1)
                {
                    bos.write(data, 0, count);
                }

                bos.flush();
                bos.close();
                inputStream.close();

                bytes = bos.toByteArray();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return bytes;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {
                Intent phinIntent = new Intent(AddSearchedActivity.this, UserMainActivity.class);
                AddSearchedActivity.this.startActivity(phinIntent);
            }
            else
            {
                etSurname.setError("Please, correct enter");
            }
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECTED_PICTURE);
    }

    @Override
    public void finish() {
        System.out.println("finish activity");
        System.runFinalizersOnExit(true) ;
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECTED_PICTURE)
        {
            uri = data.getData();
            iv.setImageURI(uri);
        }
    }
}
