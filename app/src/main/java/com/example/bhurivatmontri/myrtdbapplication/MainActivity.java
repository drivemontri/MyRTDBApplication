package com.example.bhurivatmontri.myrtdbapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName;
    private EditText etQuote;

    private DatabaseReference mRootRef;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private String user_id;

    private boolean chk_signIn = false;
    private boolean chk_show = false;

    private static final int RC_SIGN_IN = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mGoogleSignInClient.signOut();

        mAuth = FirebaseAuth.getInstance();
        //signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        //FirebaseAuth.getInstance().signOut();
        mAuth.signOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("sss", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("sss", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sss", "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            user_id = user.getUid();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("sss", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    public void saveRecord() {
        etName = findViewById(R.id.et_name);
        etQuote = findViewById(R.id.et_quote);

        String stringName = etName.getText().toString();
        String stringQuote = etQuote.getText().toString();

        if (stringName.equals("") || stringQuote.equals("")) {
            return;
        }

        DatabaseReference mNameRef = mRootRef
                .child("records").child(user_id).child("name");
        DatabaseReference mQuoteRef = mRootRef
                .child("records").child(user_id).child("quote");
        mNameRef.setValue(stringName);
        mQuoteRef.setValue(stringQuote);
    }

    public void fetchQuote() {
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stringName = dataSnapshot.child("records").child(user_id).child("name").getValue().toString();
                String stringQuote = dataSnapshot.child("records").child(user_id).child("quote").getValue().toString();

                TextView tv_quote = findViewById(R.id.tv_quote);
                tv_quote.setText(stringName + " : "+ stringQuote);

                if(chk_show == false){
                    tv_quote.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initView(){
        // To register click event to view
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_show).setOnClickListener(this);
        findViewById(R.id.btn_signIn).setOnClickListener(this);
        findViewById(R.id.btn_signOut).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        //FirebaseUser user = mAuth.getCurrentUser();
        switch (v.getId()) {
            case R.id.btn_signIn:
                if(chk_signIn == false){
                    signIn();
                    chk_signIn = true;
                }
                break;
            case R.id.btn_signOut:
                if(chk_signIn != false){
                    signOut();
                    chk_signIn = false;
                }
                break;
            case R.id.btn_save:
                if(chk_signIn != false){
                    saveRecord();
                }
                break;
            case R.id.btn_show:
                if(chk_signIn != false){
                    if(chk_show == false){
                        chk_show = true;
                    }else {
                        chk_show = false;
                    }
                    Toast.makeText(this,"Sign out",Toast.LENGTH_SHORT).show();
                    fetchQuote();
                }
                break;
        }
    }
}
