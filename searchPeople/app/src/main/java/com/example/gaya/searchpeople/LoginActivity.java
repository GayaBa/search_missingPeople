package com.example.gaya.searchpeople;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;

public class LoginActivity extends AppCompatActivity {


    EditText etPassword, etUsername;
    Button bRegister, bLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etSurname);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        bRegister = (Button) findViewById(R.id.tvRegister);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionDetector.isConnected(v.getContext())) {
                    if (TextUtils.isEmpty(etUsername.getText().toString())) {
                        etUsername.setError("The username cannot be empty");
                        return;
                    }
                    if (TextUtils.isEmpty(etPassword.getText().toString())) {
                        etPassword.setError("The password cannot be empty");
                        return;
                    }
                    if (ConnectionDetector.isConnected(v.getContext())) {

                        NetworkConnector connector = new NetworkConnector(etUsername.getText().toString(), etPassword.getText().toString());
                        connector.execute();
                    }
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

    private class NetworkConnector extends AsyncTask<Void, Void, String> {

        private String userName, userPassword;

        NetworkConnector(String userName, String userPassword) {
            this.userName = userName;
            this.userPassword = userPassword;
        }


        @Override
        protected String doInBackground(Void... params) {
            try {

                CloudStorageAccount storageAccount = CloudStorageAccount.parse(UserInfo.storageConnectionString);
                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable = tableClient.getTableReference("userInfo");
                TableOperation retrieveUsernamePassword = TableOperation.retrieve(userName,"", DBclass.class);
                DBclass specificEntity = cloudTable.execute(retrieveUsernamePassword).getResultAsType();
                if (specificEntity != null) {
                    if (specificEntity.getPassword().equals(userPassword)) {
                        UserInfo.username = userName;
                        UserInfo.password = specificEntity.getPassword();
                        UserInfo.email = specificEntity.getEmail();
                        UserInfo.age = specificEntity.getAge();
                        UserInfo.name = specificEntity.getName();
                        UserInfo.surname = specificEntity.getSurname();
                        UserInfo.phone = specificEntity.getPhoneNumber();
                        UserInfo.comment = specificEntity.getComment();

                        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                        CloudBlobContainer container = blobClient.getContainerReference("photo");
                        CloudBlockBlob blob = container.getBlockBlobReference(UserInfo.username + ".jpg");
                        UserInfo.uri = blob.getUri().toString();
                        return "OK";
                    }
                    else
                    {
                        return "Username and/or password is wrong!";
                    }
                } else {
                    return "Username and/or password is wrong!";
                }
            } catch (Exception e) {
                // Output the stack trace.
                e.printStackTrace();
                return "";
            }
        }

        /**
         * finish the loading
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {
                Intent phinIntent = new Intent(LoginActivity.this, UserMainActivity.class);
                LoginActivity.this.startActivity(phinIntent);
            } else if (result.length() != 0) {
                etPassword.setError(result);
            }

        }
    }

}
