package com.ull.project.mobiletriageapp_start;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class Activity_Step4 extends AppCompatActivity implements View.OnClickListener {
    private int id_bt_yes_;
    private int id_bt_no_;
    private int id_bt_main_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__step4);
        Button yes_ =  findViewById(R.id.bt_yes_step4);
        Button no_ = findViewById(R.id.bt_no_step4);
        Button main_ = findViewById(R.id.bt_main_step4);
        if (yes_ != null && no_ != null && main_ != null) {
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
    public void onClick(View v) {


        if (v != null) {

            if (v.getId() == id_bt_main_) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            } else if (v.getId() == id_bt_yes_) {
                Intent i = new Intent(this, Activity_Step5.class);
                startActivity(i);


            } else if (v.getId() == id_bt_no_) {
                Intent i = new Intent(this, Activity_Result.class);
                Resources res = getResources();
                String[] colores = res.getStringArray(R.array.arr_txt_color);
                DataNFC dato = new DataNFC(colores[1]);
                i.putExtra("Datos", dato);
                startActivity(i);

            }

        }

    }
}