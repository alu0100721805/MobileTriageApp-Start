package com.ull.project.mobiletriageapp_start;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class Activity_Info_Tag extends AppCompatActivity implements View.OnClickListener, FragmentViewListener{
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final int REQUEST_CODE_NFC = 0;
    private int id_bt_read_;
    private int id_bt_check_;
    private int id_bt_main_;
    private boolean isDialogDisplayed_ = false;
    private boolean isRead_ = false;
    private NfcAdapter nfcAdapter_ = null;
    private DataNFC datos_;
    private ViewGroup custom_toast_ = null;
    private FragmentReadNFC fragmentRead_ = null;
    private SingletonVolleyClass volley_ = null;
    private TextView text_view_id_tag_ = null;
    private TextView text_view_state_ = null;
    private boolean ok_;
    Button bt_check_ = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__info__tag);
        Button bt_read =  findViewById(R.id.button_read_info_tag);
        bt_check_  = findViewById(R.id.button_check_info_tag);
        Button bt_main =  findViewById(R.id.bt_main_info_tag);
        custom_toast_ = findViewById(R.id.relative_layout_toast_info_tag);
        volley_ = SingletonVolleyClass.getInstance(this);
        text_view_id_tag_ = findViewById(R.id.textview_id_info_tag);
        text_view_state_ = findViewById(R.id.txt_msg_to_send_result_info_tag);
        try {
            bt_read.setOnClickListener(this);
            bt_main.setOnClickListener(this);
            bt_check_.setOnClickListener(this);

            id_bt_main_ = bt_main.getId();
            id_bt_read_ = bt_read.getId();
            id_bt_check_ = bt_check_.getId();

            checkPermission(this);

            // Se comprueba que el dispositivo sea compatible con el adaptador nfc
            // y que este esté habilitado

            nfcAdapter_ = NfcAdapter.getDefaultAdapter(this);
            // Si se ha realizado la escritura de en una etiqueta entonces ok tomará el valor true en caso contrario false
            ok_ = getIntent().getBooleanExtra("ok",false);

            // Si el dispositivo móvil no tiene una adaptador para leer etiquetas nfc se lanza un error
            if (nfcAdapter_ == null){

                CustomToast customToast = new CustomToast(this,getString(R.string.txt_Err_NosupportNFC),R.layout.custom_toast);
                customToast.inflateToast(custom_toast_);
                throw new RuntimeException(getString(R.string.txt_Err_NosupportNFC));
            }
        } catch (Exception e) {
            Log.d(e.getLocalizedMessage(),e.getMessage());
            Intent i = new Intent (this,MainActivity.class);
            startActivity(i);
        }

    }

    @Override
    public void onClick(View v) {

        if (id_bt_main_ == v.getId())
        {
            Intent i = new Intent (this,MainActivity.class);
            startActivity(i);
            this.finish();
        }
        else if(id_bt_read_ == v.getId()){
            this.showReadFragment();
        }else if (id_bt_check_ == v.getId()){
            this.checkState();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this,nfcAdapter_);


    }
    @Override
    protected void onPause(){
        stopForegroundDispatch(this, nfcAdapter_);
        super.onPause();
    }

    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed_ = true;
    }
    @Override
    public void onDialogDismissed() {
        isDialogDisplayed_ = false;
    }

    /**
     *
     * @param intent:(Intent) Cada vez que se detecta una etiqueta se envia un nuevo intent que lleva los datos de la misma
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Tag NFCTAG = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(NFCTAG != null) {
            if (isDialogDisplayed_) {
                if (isRead_ ) {
                    fragmentRead_ = (FragmentReadNFC) getSupportFragmentManager().findFragmentByTag(FragmentReadNFC.TAG);
                    fragmentRead_.onNfcDetected(NFCTAG);
                }
            }
        }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        // Si se ha realizado la lectura de una etiqueta se recupera el valor de ok_
        ok_ = getIntent().getBooleanExtra("ok",false);
        datos_ = getIntent().getParcelableExtra("Datos");
        if (ok_ && datos_ != null){
            text_view_id_tag_.setText(datos_.getTagId());
            bt_check_.setEnabled(true);
        }else{
            bt_check_.setEnabled(false);
        }
    }
    @Override
    protected void onStop()
    {
        volley_.stop();
        super.onStop();

    }

    private void showReadFragment(){
        isRead_ = true;
        fragmentRead_ = (FragmentReadNFC) this.getSupportFragmentManager().findFragmentByTag(FragmentReadNFC.TAG);

        if (fragmentRead_ == null) {

            fragmentRead_= FragmentReadNFC.newInstance();
        }
        fragmentRead_.show(this.getSupportFragmentManager(),FragmentReadNFC.TAG);

    }



    /**
     *  Método para verificar el estado de la etiqueta en el servidor
     */
    private void checkState(){

        try {
            String url = getString(R.string.txt_url_getCheckTagJSON);
            //Se crea el objeto que contendrá los datos de la etiqueta para transmitirlos en la petición
            JSONObject json = new JSONObject();
            json.put("id",datos_.getTagId());
            json.put("color",datos_.getColor());
            json.put("long",datos_.getLongitude());
            json.put("lat",datos_.getLatitude());
            json.put("sign",datos_.getSignature());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    CustomToast customToast;
                    String strdata = "";
                    try {
                        if(!response.isNull("tag")) {
                            JSONObject obj = response.getJSONObject("tag");
                            strdata = strdata + "ID: " +  obj.getString("id") + "\n";
                            strdata = strdata + "COLOR: " + obj.getString("color") +"\n";
                            strdata = strdata + "LAT: " +   obj.getString("lat") +"\n";
                            strdata = strdata + "LONG: " + obj.getString("long") +"\n";
                            strdata = strdata + "SING: " + obj.get("sign")+ "\n";
                            text_view_state_.setText(strdata);
                            text_view_state_.setVisibility(View.VISIBLE);
                            customToast = new CustomToast(getApplicationContext(),getString(R.string.txt_Result_Search_TagOnServer),R.layout.custom_toast);
                        }else
                        {
                            customToast = new CustomToast(getApplicationContext(),response.getString("message"),R.layout.custom_toast);
                        }
                        customToast.inflateToast(custom_toast_);
                    } catch (JSONException e) {
                        customToast = new CustomToast(getApplicationContext(),e.toString(),R.layout.custom_toast);
                        customToast.inflateToast(custom_toast_);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomToast customToast = new CustomToast(getApplicationContext(),error.toString(),R.layout.custom_toast);
                    customToast.inflateToast(custom_toast_);

                }
            });

            SingletonVolleyClass.getInstance(this).addToRequestQueue(jsObjRequest);

        }catch(Exception e){
            CustomToast customToast = new CustomToast(getApplicationContext(),e.getCause().toString(),R.layout.custom_toast);
            customToast.inflateToast(custom_toast_);
            Log.e("Error Server:",e.getCause().toString());
        }
}



    /**
     * Método privado que deshabilita la adquisición de los nuevos intentos que llegan del adaptador cada vez que este detecta una
     * @param activity : (Activity) Actividad en la que se encuentra el adaptador
     * @param adapter : (NfcAdapter) La instancia del objeto adaptador
     */


    private void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if(adapter != null)
            adapter.disableForegroundDispatch(activity);
    }

    /**
     *  Método de clase que se encarga de configurar el adaptador para que los distintos intentos
     *  de detección de etiquetas NFC se ejecuten con los permisos de nuestra aplicación
     **/
    private static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        // Se crea el intent en la primera posición de la pila
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        try{
            // Se añade al manifiesto la configuracion necesaria para atender Acciones NFDEF DISCOVERED
            IntentFilter[] filters = new IntentFilter[2];
            // Añadimos filtros de las distintas acción para NDEF_DISCOVERED y NDEF_TECH_DISCOVERED y las distintas tecnologías
            filters[0] = new IntentFilter();
            filters[1] = new IntentFilter();
            String[][] techList = new String[2][1];
            techList[0][0] = Ndef.class.getName();
            techList[1][0] = NdefFormatable.class.getName();
            try {
                filters[0].addDataType(MIME_TEXT_PLAIN);
                filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
                filters[0].addCategory(Intent.CATEGORY_DEFAULT);
            } catch (Exception e) {
                Log.d("IntentFilterNDEF",e.getLocalizedMessage(),e.getCause());
            }
            try {
                filters[1].addDataType("*/*");
                filters[1].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
                filters[1].addCategory(Intent.CATEGORY_DEFAULT);
            } catch (Exception e){
                Log.d("IntentFilterTech",e.getLocalizedMessage(),e.getCause());
            }

            adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);

        }catch (NullPointerException e){
            Log.d("NULL",e.getLocalizedMessage());
        }

    }

    /**
     *  Función encarga da de comprobar los permisos necesarios para la aplicación
     *  @param context : (Context) Contexto en el cual se van a comprobar y solicitar los permisos
     */

    private void checkPermission (Context context){
        // Si la version es  igual o superior a la Marshmalloew se piden los permisos en tiempo de ejecucion
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int idperchecknfc = ContextCompat.checkSelfPermission(context, android.Manifest.permission.NFC);
            if (idperchecknfc == PackageManager.PERMISSION_DENIED) {

                // Se explica que se necesita el permiso
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NFC)) {
                    CustomToast customToast = new CustomToast(context,getString(R.string.txt_ExplainedPermissionNFC),R.layout.custom_toast);
                    customToast.inflateToast(custom_toast_);
                    requestPermissions(new String[]{Manifest.permission.NFC}, REQUEST_CODE_NFC);
                }

            }

        }
    }
    /**
     * Método encargado de manejar la respuesta a la solicitud de permisos una vez expuesto su uso y necesidad
     * @param requestCode : (int) Código que identifica la petición
     * @param permissions : (String []) Array que contiene el texto de los permisos necesarios para la actividad a realizar
     * @param grantResults :  (int []) Array que contiene los códigos de los permisos necesitados
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        switch(requestCode){
            case REQUEST_CODE_NFC:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    // Como no se concedieron los permisos se vuelve a la ventana principal dado que no tendria ninguna utilidad
                    Intent intent = new Intent(this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }







}
