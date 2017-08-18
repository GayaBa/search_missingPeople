package com.example.gaya.searchpeople;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoInfoActivity extends AppCompatActivity {

    private static final int SELECTED_PICTURE = 100;
    Uri uri;
    ImageView iv;
    EditText etComment;
    Boolean addPhoto;
    Button bAddPhoto, bNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_info);
         bAddPhoto = (Button) findViewById(R.id.bAddPhoto);
         bNext = (Button) findViewById(R.id.bNext);
        etComment = (EditText) findViewById(R.id.etComment);
        iv = (ImageView) findViewById(R.id.imageView);
        addPhoto=false;

        bAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionDetector.isConnected(v.getContext())) {

                    NetworkConnector connector = new NetworkConnector(getApplicationContext(), etComment.getText().toString());
                    connector.execute(etComment.getText().toString());
                }
            }
        });
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
        if (resultCode == RESULT_OK && requestCode == SELECTED_PICTURE) {
            addPhoto=true;
            uri = data.getData();
            iv.setImageURI(uri);
        }
    }

    private class NetworkConnector extends AsyncTask<String, Void, String> {

        Context context;
        String comment;

        NetworkConnector(Context c, String comment) {
            context = c;
            this.comment=comment;
        }


        @Override
        protected String doInBackground(String... params) {
            try {
                if(!addPhoto) {
                    uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.user)
                            + '/' + getResources().getResourceTypeName(R.drawable.user) + '/'
                            + getResources().getResourceEntryName(R.drawable.user) );
                }
                    CloudStorageAccount storageAccount =
                            CloudStorageAccount.parse(UserInfo.storageConnectionString);
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                    CloudBlobContainer container = blobClient.getContainerReference("photo");
                    CloudBlockBlob blob = container.getBlockBlobReference(UserInfo.username + ".jpg");
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
                CloudTableClient tableClient = storageAccount.createCloudTableClient();

                CloudTable cloudTable = tableClient.getTableReference("userinfo");

                TableOperation retrieveComment =
                        TableOperation.retrieve(UserInfo.username, "", DBclass.class);

                DBclass specificEntity =
                        cloudTable.execute(retrieveComment).getResultAsType();

                specificEntity.setComment(comment);
                TableOperation replaceEntity = TableOperation.replace(specificEntity);
                cloudTable.execute(replaceEntity);

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
                Intent phinIntent = new Intent(PhotoInfoActivity.this, UserMainActivity.class);
                PhotoInfoActivity.this.startActivity(phinIntent);
            }
            else {
                etComment.setError("Please, check your connection");
            }
        }
    }
}