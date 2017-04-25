package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ModifyMeActivity extends AppCompatActivity {
    EditText et_name, et_state, et_number, et_id, et_email;
    Button bt_save;
    ImageView iv_back;
    ImageButton ib_profile;
    SharedPreferences preferences;
    String background = "", profile = "", backgroundPath = "", profilePath = "";
    Bitmap bitmap;
    private AQuery aq = new AQuery(this);
    private static final int BACK = 13;
    private static final int PROFILE = 31;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_me);
        et_name = (EditText) findViewById(R.id.modify_m_et_name);
        et_state = (EditText) findViewById(R.id.modify_m_et_state);
        et_number = (EditText) findViewById(R.id.modify_m_et_number);
        bt_save = (Button) findViewById(R.id.modify_m_bt_save);
        iv_back = (ImageView) findViewById(R.id.modify_m_iv_back);
        ib_profile = (ImageButton) findViewById(R.id.modify_m_ib_profile);
        et_id = (EditText) findViewById(R.id.modify_m_et_id);
        et_email = (EditText) findViewById(R.id.modify_m_et_email);

        preferences = getSharedPreferences("user", 0);
        profile = preferences.getString("profilePicture", "");
        background = preferences.getString("backgroundPhoto", "");
        et_name.setText(preferences.getString("name", ""));
        et_state.setText(preferences.getString("stateMessage", ""));
        et_number.setText(preferences.getString("phoneNumber", ""));
        et_id.setText(preferences.getString("id", ""));
        et_email.setText(preferences.getString("Email", ""));
        if(!profile.equals("")) aq.id(ib_profile).image(profile);
        if(!background.equals("")) aq.id(iv_back).image(background);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                startActivityForResult(intent, BACK);
            }
        });
        ib_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                startActivityForResult(intent, PROFILE);

            }
        });

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_name.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!profilePath.equals("")) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.bucket));
                    final StorageReference mountainsRef = storageRef.child(profilePath);

                    bitmap = ((BitmapDrawable) ib_profile.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();

                    UploadTask uploadTask = mountainsRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ModifyMeActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ModifyMeActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                            mountainsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    profile = uri.toString();
                                    putData("putProfile.php");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            });
                        }
                    });
                }
                if (!backgroundPath.equals("")) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.bucket));
                    final StorageReference mountainsRef = storageRef.child(backgroundPath);

                    bitmap = ((BitmapDrawable) iv_back.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();

                    UploadTask uploadTask = mountainsRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ModifyMeActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ModifyMeActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                            mountainsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    background = uri.toString();
                                    putData("putBackground.php");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            });
                        }
                    });
                }
                putData("putMyInfo.php");
            }
        });
    }

    void putData(final String url) {

        final String userId = et_id.getText().toString();
        final String name = et_name.getText().toString();
        final String stateMessage = et_state.getText().toString();
        final String phoneNumber = et_number.getText().toString();
        class InsertData extends AsyncTask<Object, Object, Void> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void s) {
                super.onPostExecute(s);

                preferences = getSharedPreferences("user", 0);
                SharedPreferences.Editor editor = preferences.edit();
                if (url.equals("putProfile.php")) {
                    editor.putString("profilePicture", profile);
                    editor.commit();
                } else if (url.equals("putBackground.php")) {
                    editor.putString("backgroundPhoto", background);
                    editor.commit();
                } else if (url.equals("putMyInfo.php")) {
                    editor.putString("name", name);
                    editor.putString("stateMessage", stateMessage);
                    editor.putString("phoneNumber", phoneNumber);
                    editor.commit();
                    finish();
                }
            }

            @Override
            protected Void doInBackground(Object... params) {
                try {
                    String link = getString(R.string.ip) + url;
                    String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    if (url.equals("putMyInfo.php")) {
                        data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                        data += "&" + URLEncoder.encode("stateMessage", "UTF-8") + "=" + URLEncoder.encode(stateMessage, "UTF-8");
                        data += "&" + URLEncoder.encode("phoneNumber", "UTF-8") + "=" + URLEncoder.encode(phoneNumber, "UTF-8");
                    } else if (url.equals("putBackground.php"))
                        data += "&" + URLEncoder.encode("background", "UTF-8") + "=" + URLEncoder.encode(background, "UTF-8");
                    else if (url.equals("putProfile.php"))
                        data += "&" + URLEncoder.encode("profile", "UTF-8") + "=" + URLEncoder.encode(profile, "UTF-8");


                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                } catch (Exception e) {
                    e.toString();
                }
                return null;
            }
        }
        InsertData task = new InsertData();
        task.execute();
    }


    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ImageView imageView = null;
            String fileName = "";
            if (requestCode == BACK) {
                imageView = iv_back;
                backgroundPath = et_id.getText().toString() + "_background.jpg";
            } else if (requestCode == PROFILE) {
                imageView = ib_profile;
                profilePath = et_id.getText().toString() + "_profile.jpg";
            }

            String[] projectionPath = {MediaStore.Images.Media.DATA};
            Cursor cursorPath = managedQuery(data.getData(), projectionPath, null, null, null);
            int column_index_path = cursorPath.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursorPath.moveToFirst();
            String path = cursorPath.getString(column_index_path);
            bitmap = decodeSampledBitmapFromResource(path, imageView.getWidth(), imageView.getHeight());
            imageView.setImageBitmap(bitmap);
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
