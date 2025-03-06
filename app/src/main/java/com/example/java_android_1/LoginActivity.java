package com.example.java_android_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;


    private static final String LOGIN_URL = "http://10.0.2.2:5000/api/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi komponen
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        // Event tombol login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Event klik register
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Go to Register Page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan Password harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan ProgressBar dan disable tombol login sementara
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Buat JSON body untuk request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Response: " + response.toString());

                        // Sembunyikan ProgressBar dan aktifkan tombol login kembali
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        try {
                            if (response.has("token")) { // Cek apakah respons mengandung token
                                String token = response.getString("token");

                                // Simpan token ke SharedPreferences
                                getSharedPreferences("USER_PREF", MODE_PRIVATE)
                                        .edit()
                                        .putString("token", token)
                                        .apply();

                                Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                                // Arahkan ke DashboardActivity
                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Gagal! Periksa kembali username dan password.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Kesalahan JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Sembunyikan ProgressBar dan aktifkan tombol login kembali
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        String errorMessage = "Terjadi Kesalahan!";
                        if (error.networkResponse != null) {
                            errorMessage = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        }
                        Log.e("API_ERROR", "Error: " + errorMessage);
                        Toast.makeText(LoginActivity.this, "Kesalahan: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Kirim request ke backend
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}
