package com.ull.project.mobiletriageapp_start;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;


import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;


public class Activity_Result extends AppCompatActivity implements View.OnClickListener, FragmentViewListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    public static final int REQUEST_CODE_NFC = 0;
    public static final int REQUEST_CODE_FINE_LOCATION = 1;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private int id_bt_write_;
    private int id_bt_main_;
    private int id_bt_classify_;
    private NfcAdapter nfcAdapter_ = null;
    private DataNFC datos_;
    private ViewGroup custom_toast_ = null;
    private boolean isDialogDisplayed_ = false;
    private boolean isWrite_ = false;
    private FragmentWriteNFC fragmentWrite_ = null;
    private GoogleApiClient mGoogleApiClient_ = null;
    private SingletonVolleyClass volley_ = null;
    Location location_;
    private boolean ok_;
    private FusedLocationProviderClient mFusedLocationClient_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__result);
        TextView txtvw_winfo_ =  findViewById(R.id.textview_colour_result);
        Button bt_write =  findViewById(R.id.button_write_result);
        Button bt_main =  findViewById(R.id.bt_main_result);
        Button bt_clasify =  findViewById(R.id.button_classify_result);
        custom_toast_ = findViewById(R.id.relative_layout_toast_result);
        volley_ = SingletonVolleyClass.getInstance(this);

             try {
                 bt_write.setOnClickListener(this);
                 bt_main.setOnClickListener(this);
                 bt_clasify.setOnClickListener(this);
                 mFusedLocationClient_ = LocationServices.getFusedLocationProviderClient(this);
                 id_bt_main_ = bt_main.getId();
                 id_bt_write_ = bt_write.getId();
                 id_bt_classify_ = bt_clasify.getId();

                 checkPermission(Activity_Result.this);

                 //Se inicializa la configuración del apideGoogle
                 initApiGoogle();

                     // Se comprueba que el dispositivo sea compatible con el adaptador nfc
                     // y que este esté habilitado

                 nfcAdapter_ = NfcAdapter.getDefaultAdapter(this);
                 // Llega la clasificación del color determinado por el algoritmo de clasificación por pantallas
                 datos_ = getIntent().getParcelableExtra("Datos");
                 // Si se ha realizado la escritura de en una etiqueta entonces ok tomará el valor true en caso contrario false
                 ok_ = getIntent().getBooleanExtra("ok",false);

                 if (nfcAdapter_ == null){

                     CustomToast customToast = new CustomToast(this,getString(R.string.txt_Err_NosupportNFC),R.layout.custom_toast);
                     customToast.inflateToast(custom_toast_);
                     throw new RuntimeException(getString(R.string.txt_Err_NosupportNFC));
                 }
                 if(nfcAdapter_.isEnabled()){
                        if(datos_ !=null)
                        {
                            txtvw_winfo_.setText(datos_.getColor_());
                        }

                 }else{
                     CustomToast customToast = new CustomToast(this,getString(R.string.txt_AdapterNFC_Explication),R.layout.custom_toast);
                     customToast.inflateToast(custom_toast_);

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
            else if(id_bt_write_ == v.getId()){
                this.showWriteFragment();
            }else if (id_bt_classify_ == v.getId()){
                refreshCoordinates(true);
                clasify();

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
    protected void onNewIntent(Intent intent) {
        Tag NFCTAG = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(NFCTAG != null) {
            if (isDialogDisplayed_) {
                if (isWrite_ ) {
                    // Se escribe el mensaje de la id interna con el color del trige en la etiqueta
                    String messageToWrite = datos_.getColor_();
                   fragmentWrite_ = (FragmentWriteNFC) getSupportFragmentManager().findFragmentByTag(FragmentWriteNFC.TAG);
                   fragmentWrite_.onNfcDetected(NFCTAG,messageToWrite, NFCTAG.getId());
                }
            }
        }
    }

    /**
     * Método para configurar el GOOGLE API CLIENT
     */
    private void initApiGoogle(){

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient_ == null) {
            mGoogleApiClient_ = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }
    private void showWriteFragment(){
        isWrite_ = true;
        fragmentWrite_ = (FragmentWriteNFC) this.getSupportFragmentManager().findFragmentByTag(FragmentWriteNFC.TAG);
        if (fragmentWrite_ == null) {

            fragmentWrite_ = FragmentWriteNFC.newInstance();
        }
        fragmentWrite_.show(this.getSupportFragmentManager(),FragmentWriteNFC.TAG);

    }

    /**
     *  Método de clase que se encarga de configurar el adaptador para que los distintos intentos
     *  de detección etiquetas NFC se ejecuten con los permisos de nuestra aplicación
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
     * Método privado que deshabilita la adquisición de los nuevos intentos que llegan del adaptador cada vez que este detecta una
     * @param activity : (Activity) Actividad en la que se encuentra el adaptador
     * @param adapter : (NfcAdapter) La instancia del objeto adaptador
     */


   private void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
       if(adapter != null)
         adapter.disableForegroundDispatch(activity);
    }


    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed_ = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed_ = false;
        isWrite_ = false;
    }
    /**
     *  Función encarga da de comprobar los permisos necesarios para la aplicación
     *  @param context : (Context) Contexto en el cual se van a comprobar y solicitar los permisos
     */

    private void checkPermission (Context context){
        // Si la version es  igual o superior a la Marshmalloew se piden los permisos en tiempo de ejecucion
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            int idperchecknfc = ContextCompat.checkSelfPermission(context, android.Manifest.permission.NFC);
            int idperchecklocalitation = ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION);
            if (idperchecknfc == PackageManager.PERMISSION_DENIED) {

                // Se explica que se necesita el permiso
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NFC)) {
                    CustomToast customToast = new CustomToast(context,getString(R.string.txt_ExplainedPermissionNFC),R.layout.custom_toast);
                    customToast.inflateToast(custom_toast_);
                    requestPermissions(new String[]{Manifest.permission.NFC}, REQUEST_CODE_NFC);
                }

            }
            if (idperchecklocalitation == PackageManager.PERMISSION_DENIED){

                // Se explica que se necesita el permiso
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    CustomToast customToast = new CustomToast(context,getString(R.string.txt_ExplainedPermissionAccessFine),R.layout.custom_toast);
                    customToast.inflateToast(custom_toast_);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
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
            }case REQUEST_CODE_FINE_LOCATION:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    // Como no se concedieron los permisos se vuelve a la ventana principal dado que no tendria ninguna utilidad
                    Intent intent = new Intent(this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }
    @Override
    protected void onStart()
    {
        mGoogleApiClient_.connect();
        super.onStart();
        // Llega la clasificación del color determinado por el algoritmo de clasificación por pantallas
        datos_ = getIntent().getParcelableExtra("Datos");
        // Si se ha realizado la escritura de en una etiqueta entonces ok tomará el valor true en caso contrario false
        ok_ = getIntent().getBooleanExtra("ok",false);
    }
    @Override
    protected void onStop()
    {
        mGoogleApiClient_.disconnect();
        volley_.stop();
        super.onStop();

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshCoordinates(false);
    }

    @Override
    public void onConnectionSuspended(int i) {
        CustomToast customToast = new CustomToast(this,getString(R.string.txt_Warning_SuspendConnection),R.layout.custom_toast);
        customToast.inflateToast(custom_toast_);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        CustomToast customToast = new CustomToast(this,getString(R.string.txt_Warning_FailedConnection),R.layout.custom_toast);
        customToast.inflateToast(custom_toast_);
    }

    /**
     * Método para obtener la ubicación y refrescar las coordenadas
     *  @param modificar :(boolean) Propiedad establecida para asignar el valor de las coordenadas al
     *                  objeto datos_ que se va a enviar al servidor
     */
    private void refreshCoordinates(boolean modificar){
        try
        {
            checkPermission(Activity_Result.this);
            mFusedLocationClient_.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                location_ = location;
                            }
                        }
                    });
            if(modificar){
                datos_.setLongitude(location_.getLongitude());
                datos_.setLatitude(location_.getLatitude());
            }
        }catch (Exception e){
            CustomToast customToast = new CustomToast(this,e.getMessage(),R.layout.custom_toast);
            customToast.inflateToast(custom_toast_);
        }
    }

    /***
     *  Método encargado de construir el objeto json que se enviará al servidor con el contenido de la etiqueta
     * ya clasificada e identificada
     */
    private void clasify(){
        if ( ok_ ) {

            try {
                String url = getString(R.string.txt_url_sendTagJSON);
                //Se crea el objeto que contendrá los datos de la etiqueta para transmitirlos en la petición
                JSONObject json = new JSONObject();
                json.put("id",datos_.getTagId_());
                json.put("color",datos_.getColor_());
                json.put("latitud",datos_.getLatitude());
                json.put("longitud",datos_.getLongitude_());
                EditText aux = findViewById(R.id.txt_msg_to_send_result);
                String strdata = "";
                strdata = strdata + "ID: " + datos_.getTagId_()+"\n";
                strdata = strdata + "COLOR: " + datos_.getColor_() +"\n";
                strdata = strdata + "LAT: " + String.valueOf(datos_.getLatitude())+"\n";
                strdata = strdata + "LONG: " + String.valueOf(datos_.getLongitude_())+"\n";
                aux.setText(strdata);
                aux.setVisibility(View.VISIBLE);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                CustomToast customToast = null;
                                try {
                                    customToast = new CustomToast(getApplicationContext(),response.getString("message"), R.layout.custom_toast);
                                } catch (JSONException e) {
                                    Log.e("Error JSONException",e.getMessage());
                                }
                                customToast.inflateToast(custom_toast_);
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
                Log.e("Error Server:",e.toString());
            }


        }else{
            CustomToast customToast = new CustomToast(getApplicationContext(),getString(R.string.txt_Warnning_Send_Server),R.layout.custom_toast);
            customToast.inflateToast(custom_toast_);
        }

    }

}





