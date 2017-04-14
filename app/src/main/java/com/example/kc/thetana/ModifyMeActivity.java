package com.example.kc.thetana;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class ModifyMeActivity extends AppCompatActivity {
    EditText et_before, et_after;
    Button bt_modify;
    DBHelper dbHelper = new DBHelper(ModifyMeActivity.this, "thetana.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_me);
        et_before = (EditText) findViewById(R.id.modify_f_et_before);
        et_after = (EditText) findViewById(R.id.modify_f_et_after);
        bt_modify = (Button) findViewById(R.id.modify_f_bt_modify);
    }
}
