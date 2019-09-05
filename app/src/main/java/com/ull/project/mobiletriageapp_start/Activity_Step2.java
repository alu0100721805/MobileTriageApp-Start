package com.ull.project.mobiletriageapp_start;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



public class Activity_Step2 extends AppCompatActivity implements View.OnClickListener {
    private int id_bt_yes_;
    private int id_bt_no_;
    private int id_bt_main_;
    private ViewGroup root_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__step2);
        Button yes_ = findViewById(R.id.bt_yes_step2);
        Button no_ =  findViewById(R.id.bt_no_step2);
        Button main_ = findViewById(R.id.bt_main_step2);
        root_ = findViewById(R.id.activity_step2_rel_layout1);
        if(yes_  != null && no_ != null && main_ != null && root_ != null){
            yes_.setOnClickListener(this);
            no_.setOnClickListener(this);
            main_.setOnClickListener(this);
            id_bt_yes_ = yes_.getId();
            id_bt_no_ = no_.getId();
            id_bt_main_ = main_.getId();
        }else
        {  Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }

    }
    @Override
    public void onClick (View v){
         if(v != null)
         {

             if (v.getId() == id_bt_main_)
             {
                 Intent i = new Intent (this,MainActivity.class);
                 startActivity(i);
                 finish();
             }else if (v.getId() == id_bt_yes_){
                 Intent i = new Intent(this,Activity_Step3.class);
                 startActivity(i);
             }else if (v.getId() == id_bt_no_){
                 Intent i = new Intent(this,Activity_Step6.class);
                 CustomToast customToast = new CustomToast(this,getString(R.string.txt_MandibularRetraction),R.layout.custom_toast);
                 customToast.inflateToast(root_);
                 startActivity(i);
             }

         }

    }

}
