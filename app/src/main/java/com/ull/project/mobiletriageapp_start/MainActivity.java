package com.ull.project.mobiletriageapp_start;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int id_bt_start_;
    private int id_bt_getInfoTag_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btstart_ = findViewById(R.id.bt_main_start);
        Button btgetInfoTag_ = findViewById(R.id.bt_main_get_info_tag);
        if(btstart_!= null && btgetInfoTag_ != null ){
            btstart_.setOnClickListener(this);
            btgetInfoTag_.setOnClickListener(this);
            id_bt_start_ = btstart_.getId();
            id_bt_getInfoTag_ = btgetInfoTag_.getId();
        }else {
            this.finish();
        }

    }

    @Override
    public void onClick(View v){
        if (v.getId()==id_bt_start_)
        {
            Intent i = new Intent (this,Activity_Step1.class);
            startActivity(i);
        }else if (v.getId()== id_bt_getInfoTag_){
            Intent i = new Intent (this,Activity_Info_Tag.class);
            startActivity(i);
        }

    }
}
