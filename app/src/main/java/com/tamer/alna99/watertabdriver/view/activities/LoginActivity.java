package com.tamer.alna99.watertabdriver.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tamer.alna99.watertabdriver.R;
import com.tamer.alna99.watertabdriver.model.Result;
import com.tamer.alna99.watertabdriver.model.SharedPrefs;
import com.tamer.alna99.watertabdriver.viewmodel.LoginViewModel;
import com.tapadoo.alerter.Alerter;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText et_email, et_password;
    private Button btnLogin;
    private ProgressBar progressBar;
    private String email, password;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = new LoginViewModel();
        initViews();
        String shared = SharedPrefs.getUserEmail(this);
        if (!shared.equals("-1")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initViews() {
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progressBar);
    }

    private boolean checkFields() {
        if (!TextUtils.isEmpty(Objects.requireNonNull(et_email.getText()).toString())) {
            if (!TextUtils.isEmpty(Objects.requireNonNull(et_password.getText()).toString())) {
                email = et_email.getText().toString();
                password = et_password.getText().toString();
                return true;
            } else {
                et_password.setError(getString(R.string.password_empty));
                return false;
            }
        } else {
            et_email.setError(getString(R.string.email_empty));
            return false;
        }
    }

    public void login(View view) {
        btnLogin.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (checkFields()) {
            loginViewModel.loginInfo().addObserver((observable, o) -> {
                Result result = (Result) o;
                switch (result.status) {
                    case SUCCESS:
                        String data = (String) result.data;
                        JsonObject root = new JsonParser().parse(data).getAsJsonObject();
                        boolean success = root.get("loginSuccess").getAsBoolean();
                        if (success) {
                            Log.d("dddd", data);
                            String id = root.get("id").getAsString();
                            String name = root.get("name").getAsString();
                            String email = root.get("email").getAsString();
                            String phone = root.get("phone").getAsString();
                            double rate = root.get("rate").getAsDouble();
                            JsonArray jsonArray = root.get("orders").getAsJsonArray();
                            SharedPrefs.setUserInfo(getApplicationContext(), id, name, email, phone, rate);
                            SharedPrefs.saveOrders(getApplicationContext(), jsonArray);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String message = root.get("message").getAsString();
                            showAlerter(message);
                        }
                        btnLogin.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        showAlerter(getString(R.string.error));
                        btnLogin.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            });
            loginViewModel.requestLogin(email, password);
        }
    }

    private void showAlerter(String message) {
        Alerter.create(this)
                .setText(message)
                .setDuration(3000)
                .setBackgroundColorRes(R.color.teal_200)
                .show();
    }
}