package com.facebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ShareOnFacebook extends Activity {

    private static final String APP_ID = "165131880277343";
    private static final String[] PERMISSIONS = new String[] {"publish_stream"};

    private static final String TOKEN = "access_token";
    private static final String EXPIRES = "expires_in";
    private static final String KEY = "facebook-credentials";

    private Facebook facebook;
    private String messageToPost;
    AsyncFacebookRunner asyncFacebookRunner= new AsyncFacebookRunner(facebook);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        facebook = new Facebook(APP_ID);



        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        String facebookMessage = getIntent().getStringExtra("facebookMessage");
        if (facebookMessage == null){
            facebookMessage = "Test wall post";
        }
        messageToPost = facebookMessage;
    }

    public void doNotShare(View button){
        finish();
    }
    public void share(View button) throws IOException {
        if (! facebook.isSessionValid()) {
            loginAndPostToWall();
        }
        else {
            postToWall(messageToPost);
        }
    }

    public void loginAndPostToWall(){
        facebook.authorize(this, PERMISSIONS, Facebook.FORCE_DIALOG_AUTH, new LoginDialogListener());
    }

    public void postToWall(String message) throws IOException {

        byte[] data = null;
        Bitmap bmp = BitmapFactory.decodeFile("/sdcard/rose.jpeg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();

        Bundle params = new Bundle();
        params.putString (Facebook.TOKEN, facebook.getAccessToken());
        params.putString("message", "Priyanka");
        params.putString("link","http://www.google.com");
        params.putByteArray("source", data);

        try {
            facebook.request("me");
            String response = facebook.request("me/photos", params, "POST");
            Log.d("Tests", "got response: " + response);
            if (response == null || response.equals("") ||
                    response.equals("false")) {
                showToast("Blank response.");
            }
            else {
                showToast("Message posted to your facebook wall!");
            }
            finish();
        } catch (Exception e) {
            showToast("Failed to post to wall!");
            e.printStackTrace();
            finish();
        }
    }

    class LoginDialogListener implements Facebook.DialogListener {
        public void onComplete(Bundle values) throws IOException {
            if (messageToPost != null){
                postToWall(messageToPost);
            }
        }
        public void onFacebookError(FacebookError error) {
            showToast("Authentication with Facebook failed!");
            finish();
        }
        public void onError(DialogError error) {
            showToast("Authentication with Facebook failed!");
            finish();
        }
        public void onCancel() {
            showToast("Authentication with Facebook cancelled!");
            finish();
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            facebook.authorizeCallback(requestCode, resultCode, data);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}