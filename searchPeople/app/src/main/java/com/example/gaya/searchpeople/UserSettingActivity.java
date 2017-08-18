package com.example.gaya.searchpeople;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserSettingActivity extends AppCompatActivity {

    private static final int SELECTED_PICTURE = 100;
    Uri uri;
    ImageView ivPicture;
    EditText tvName,tvSurname,tvAge,tvPhone,tvEmail,tvComment;
    boolean flag;
    String lastChar;
    Button bChange, bNewPhoto;

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
        setContentView(R.layout.activity_user_setting);

        flag=false;
         tvName = (EditText) findViewById(R.id.tvName);
         tvSurname = (EditText) findViewById(R.id.tvSurname);
         tvAge = (EditText) findViewById(R.id.tvAge);
         tvPhone = (EditText) findViewById(R.id.tvPhone);
         tvEmail = (EditText) findViewById(R.id.tvEmail);
         tvComment = (EditText) findViewById(R.id.tvComment);
        ivPicture = (ImageView) findViewById(R.id.ivPicture);
         bChange = (Button) findViewById(R.id.bChange);
         bNewPhoto=(Button) findViewById(R.id.bNewPhoto);

        ImageButton bHome = (ImageButton) findViewById(R.id.bHome);
        ImageButton bSearch = (ImageButton) findViewById(R.id.bSearch);
        ImageButton bUsers = (ImageButton) findViewById(R.id.bUsers);
        ImageButton bExit = (ImageButton) findViewById(R.id.bExit);

        lastChar = " ";

        tvName.setText(UserInfo.name );
        tvSurname.setText(UserInfo.surname);
        tvPhone.setText(UserInfo.phone);
        tvEmail.setText(UserInfo.email);
        if(UserInfo.age == null)
            tvAge.setText("-");
        else
            tvAge.setText(UserInfo.age);
        if(UserInfo.comment == null)
            tvComment.setText("-");
        else
            tvComment.setText(UserInfo.comment);
        ImageLoadTask imageLoadTask = new ImageLoadTask(UserInfo.uri, ivPicture, getApplicationContext());
        imageLoadTask.execute();

        bNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        bChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!emailValidator(tvEmail.getText().toString()))
                {
                    tvEmail.setError("This email is not valid");
                }
                if (ConnectionDetector.isConnected(v.getContext())) {

                    NetworkConnector connector = new NetworkConnector(tvName.getText().toString(), tvSurname.getText().toString(),
                            tvAge.getText().toString(), tvEmail.getText().toString(), tvPhone.getText().toString(),
                            tvComment.getText().toString(), getApplicationContext());
                    connector.execute();
                }
            }
        });

        tvPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int digits = tvPhone.getText().toString().length();
                if (digits > 1)
                    lastChar = tvPhone.getText().toString().substring(digits-1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int digits = tvPhone.getText().toString().length();
                Log.d("LENGTH",""+digits);
                if (!lastChar.equals("-")) {
                    if (digits == 3 || digits == 7) {
                        tvPhone.append("-");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSettingActivity.this, UserMainActivity.class);
                UserSettingActivity.this.startActivity(registerIntent);
            }
        });

        bUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSettingActivity.this, UsersInfoActivity.class);
                UserSettingActivity.this.startActivity(registerIntent);
            }
        });

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(UserSettingActivity.this, SearchActivity.class);
                UserSettingActivity.this.startActivity(registerIntent);
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
                Intent registerIntent = new Intent(UserSettingActivity.this, LoginActivity.class);
                UserSettingActivity.this.startActivity(registerIntent);
            }
        });



    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECTED_PICTURE) {
            uri = data.getData();
            flag=true;
            ivPicture.setImageURI(uri);
        }
    }


    private class NetworkConnector extends AsyncTask<Void, Void, String> {
        private String name, surname, age, email, phone, comment;
        private Context context;

        NetworkConnector(String name, String surname, String age, String email, String phone, String comment, Context context)
        {
            this.name=name;
            this.surname=surname;
            this.age=age;
            this.email=email;
            this.phone=phone;
            this.comment=comment;
            this.context=context;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                CloudStorageAccount storageAccount = CloudStorageAccount.parse(UserInfo.storageConnectionString);
                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable = tableClient.getTableReference("userInfo");
                TableOperation retrieveUsernamePassword = TableOperation.retrieve(UserInfo.username,"", DBclass.class);
                DBclass specificEntity = cloudTable.execute(retrieveUsernamePassword).getResultAsType();
                specificEntity.setComment(comment);
                specificEntity.setName(name);
                specificEntity.setSurname(surname);
                specificEntity.setAge(age);
                specificEntity.setEmail(email);
                specificEntity.setPhoneNumber(phone);
                UserInfo.name=name;
                UserInfo.surname=surname;
                UserInfo.age=age;
                UserInfo.email=email;
                UserInfo.phone=phone;
                UserInfo.comment=comment;
                TableOperation replaceEntity = TableOperation.replace(specificEntity);

                cloudTable.execute(replaceEntity);
                if(flag) {

                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                    CloudBlobContainer container = blobClient.getContainerReference("photo");

                    CloudBlockBlob blob = container.getBlockBlobReference(UserInfo.username + ".jpg");
                    blob.deleteIfExists();
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
                    UserInfo.uri = blob.getUri().toString();
                }
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
                Intent phinIntent = new Intent(UserSettingActivity.this, UserMainActivity.class);
                UserSettingActivity.this.startActivity(phinIntent);
            } else if (result.length() != 0) {
            }

        }
    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ImageView imageView;
        private Context context;

        public ImageLoadTask(String url, ImageView imageView, Context context) {
            this.url = url;
            this.imageView = imageView;
            this.context=context;
        }

        @Override
        protected String doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Picasso.with(context)
                    .load(url)
                    .fit()
                    .error(R.drawable.user)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }

    }
}
