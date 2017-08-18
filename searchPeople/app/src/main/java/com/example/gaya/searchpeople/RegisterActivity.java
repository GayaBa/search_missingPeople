package com.example.gaya.searchpeople;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    EditText etUsername, etName, etAge, etEmail, etPassword, etTryPassword, etPhone, etSurname;
    Button bRegister;
    String lastChar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        lastChar = " ";

          etAge = (EditText) findViewById(R.id.etAge);
        etUsername = (EditText) findViewById(R.id.etUsername);
          etPassword = (EditText) findViewById(R.id.etPassword);
          etTryPassword = (EditText) findViewById(R.id.etTryPassword);
          etEmail = (EditText) findViewById(R.id.etEmail);
          etName = (EditText) findViewById(R.id.etName);
          etSurname = (EditText) findViewById(R.id.etSurname);
        etPhone = (EditText) findViewById(R.id.etPhone);
          bRegister = (Button) findViewById(R.id.bRegister);

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int digits = etPhone.getText().toString().length();
                if (digits > 1)
                    lastChar = etPhone.getText().toString().substring(digits-1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int digits = etPhone.getText().toString().length();
                Log.d("LENGTH",""+digits);
                if (!lastChar.equals("-")) {
                    if (digits == 3 || digits == 7) {
                        etPhone.append("-");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etTryPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!etPassword.getText().toString().equals(etTryPassword.getText().toString())) {
                    ShapeDrawable shape = new ShapeDrawable(new RectShape());
                    shape.getPaint().setColor(0x55FF0000);
                    shape.getPaint().setStyle(Paint.Style.STROKE);
                    shape.getPaint().setStrokeWidth(3);

                    etTryPassword.setBackground(shape);
                } else {
                    ShapeDrawable shape = new ShapeDrawable(new RectShape());
                    shape.getPaint().setColor(0xFF12FF45);
                    shape.getPaint().setStyle(Paint.Style.STROKE);
                    shape.getPaint().setStrokeWidth(3);

                    etTryPassword.setBackground(shape);
                }

            }
        });
        bRegister.setOnClickListener(new View.OnClickListener() {
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
                    if (TextUtils.isEmpty(etEmail.getText().toString())) {
                        etEmail.setError("The email cannot be empty");
                        return;
                    }
                    if(!emailValidator(etEmail.getText().toString()))
                    {
                        etEmail.setError("This email is not valid");
                    }

                    if (TextUtils.isEmpty(etPhone.getText().toString())) {
                        etPhone.setError("The phone cannot be empty");
                        return;
                    }
                    if (etPhone.getText().length()!=12) {
                        etPhone.setError("The phone number format is wrong.");
                        return;
                    }
                    if (TextUtils.isEmpty(etUsername.getText().toString())) {
                        etUsername.setError("The username cannot be empty");
                        return;
                    }
                    if (TextUtils.isEmpty(etPassword.getText().toString())) {
                        etPassword.setError("The password cannot be empty");
                        return;
                    }
                    if (TextUtils.isEmpty(etTryPassword.getText().toString())) {
                        etTryPassword.setError("Please, confirm password");
                        return;
                    }

                    if(!etPassword.getText().equals(etTryPassword.getText()))
                    {
                        etTryPassword.setError("Password is wrong");
                        return;
                    }
                    if (ConnectionDetector.isConnected(v.getContext())) {

                        NetworkConnector connector = new NetworkConnector(etUsername.getText().toString(), etPassword.getText().toString(),
                                etName.getText().toString(), etSurname.getText().toString(), etAge.getText().toString(),
                                etEmail.getText().toString(), etPhone.getText().toString());
                        connector.execute();
                    }
                }
            }
        });

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

    @Override
    public void finish() {
        System.out.println("finish activity");
        System.runFinalizersOnExit(true) ;
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    class NetworkConnector extends AsyncTask<Void, Void, String> {

        private String userName, userPassword, name, surname, email, age, phone;
        private UserInfo user;

        NetworkConnector(String userName, String userPassword, String name, String surname, String age, String email, String phone) {
            this.userName = userName;
            this.userPassword = userPassword;
            this.name = name;
            this.surname = surname;
            this.age = age;
            this.email = email;
            this.phone = phone;
        }


        @Override
        protected String doInBackground(Void... params) {
            try {

                CloudStorageAccount storageAccount =
                        CloudStorageAccount.parse(UserInfo.storageConnectionString);
                CloudTableClient tableClient = storageAccount.createCloudTableClient();
                CloudTable cloudTable = tableClient.getTableReference("userInfo");
                TableOperation retrieveUsernamePassword =
                        TableOperation.retrieve(userName,"", DBclass.class);
                DBclass specificEntity =
                        cloudTable.execute(retrieveUsernamePassword).getResultAsType();
                if (specificEntity != null) {
                    return "OK";
                } else {
                    DBclass newuser = new DBclass(userName);
                    newuser.setEmail(email);
                    newuser.setAge(age);
                    newuser.setName(name);
                    newuser.setSurname(surname);
                    newuser.setPhoneNumber(phone);
                    newuser.setPassword(userPassword);
                    UserInfo.username = userName;
                    UserInfo.password = userPassword;
                    UserInfo.email = email;
                    UserInfo.age = age;
                    UserInfo.name = name;
                    UserInfo.surname = surname;
                    UserInfo.phone = phone;
                    TableOperation insertNewuser = TableOperation.insertOrReplace(newuser);

                    cloudTable.execute(insertNewuser);
                    return "New user";
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
                etUsername.setError("This username is already exists.");
            } else if (result.length() != 0) {
                Intent phinIntent = new Intent(RegisterActivity.this, PhotoInfoActivity.class);
                RegisterActivity.this.startActivity(phinIntent);
            }
        }
    }

}
