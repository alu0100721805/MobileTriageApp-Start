package com.ull.project.mobiletriageapp_start;


import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class FragmentWriteNFC extends DialogFragment{

    public static final String TAG = FragmentWriteNFC.class.getSimpleName();
    public static FragmentWriteNFC newInstance() {

        return new FragmentWriteNFC();
    }
    private TextView txtviewmsg;
    private ProgressBar progressBar;
    private FragmentViewListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_nfc,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        txtviewmsg =  view.findViewById(R.id.textview_message_fragment);
        progressBar =  view.findViewById(R.id.progressbar_nfc);
        getDialog().setTitle(getString(R.string.txt_Title_Dialog));
        txtviewmsg.setText(getString(R.string.txt_Info_Progress_Dialog_Write_TAG));
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Activity_Result)context;
        listener.onDialogDisplayed();

    }
    @Override
    public void onDetach() {
        super.onDetach();
        listener.onDialogDismissed();
    }

    /**
     * Método encargado de detectar y escribir un determinado mensaje en formato NDEF en la etiqueta pasada por parámetro
     * @param nfctag : (Tag) Etiqueta en la que se va a escribir el mensaje
     * @param colour : (String) Recibe el color de la etiqueta
     */


    public void onNfcDetected(Tag nfctag,String colour,byte[] id){

        progressBar.setVisibility(View.VISIBLE);
        if(colour !=null && nfctag !=null)
        {
            DataNFC datos = new DataNFC(colour);
            String payload =  hmacSha256(colour,new String(id));
            datos.setTagId(payload);
            NdefMessage message = createNdefMessage(payload,id);
            writeTagTech(message,nfctag,datos);
        }
    }
    /**
     *
     * Función encargada de transformar el texto codificado con utf8,
     * con el color de la etiqueta a un mensaje del tipo ndef
     * @param payload :(String) Datos que se codificarán en un paquete NdefMessage
     * @param id : (String) Id de la etiqueta
     * @return  NdefMessage
     *
     */

    private NdefMessage createNdefMessage(String payload,byte[] id) {
        try {

            byte[] lang = Locale.getDefault().getLanguage().getBytes(StandardCharsets.UTF_8);
            byte[] text = payload.getBytes(StandardCharsets.UTF_8);
            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payloadraw = new ByteArrayOutputStream(1 + langSize + textLength);

            payloadraw.write((byte) (langSize & 0x1F));
            payloadraw.write(lang, 0, langSize);
            payloadraw.write(text, 0, textLength);

            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, id,
                    payloadraw.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});

        }catch (NullPointerException e){
            Log.d("Error:",e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Método síncronizado para escribir el contenido de un mensaje Ndef en una etiqueta con tecnología
     * compatible o sin formato
     * @param message : (Ndef) Mensaje tipo Ndef el cuál será escrito en la etiqueta
     * @param nfctag  : (Tag) Etiqueta tipo Tag en la cuál se escribirá el mensaje
     * @param datos :(DataNFC) Datos completados a enviar a la actividad de resultados
     *
     **/

    private synchronized void writeTagTech( NdefMessage message , @NonNull Tag nfctag,@NonNull DataNFC datos)  {
         if (message != null){
            try {
                Ndef ndefTag = Ndef.get(nfctag);
                if (ndefTag == null) {
                    // Se intenta dar formato ndef  a la etiqueta
                    NdefFormatable nForm = NdefFormatable.get(nfctag);
                    nForm.connect();
                    nForm.format(message);
                    nForm.close();
                }
                else {

                    // En caso en el que la etiqueta tenga el formato Ndef se escribe un mensaje directamente
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                }
                txtviewmsg.setText(getString(R.string.txt_WriteTagSuccess));
                // Se envia el estado de escritura en la etiqueta con éxito
                Intent i = new Intent(getActivity(),Activity_Result.class);
                i.putExtra("ok",true);
                i.putExtra("Datos",datos);
                startActivity(i);

            }
            catch(FormatException | IOException e) {
                Log.d("Error:",e.getLocalizedMessage());
            }
        }

    }

    /***
     * Método para generar un id único de etiqueta con HMAC SHA256 /DUDA
     * @param msg : (String) Resultado + Id de la etiqueta
     * @param secret : (String) Palabra secreta del servidor
     *
     */
    private String  hmacSha256(String msg,String secret){

        Mac sha256_hmac;
        try {
            sha256_hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretkey =  new SecretKeySpec(secret.trim().getBytes(),"HmacSHA256");
            sha256_hmac.init(secretkey);
            return Base64.encodeToString(sha256_hmac.doFinal(msg.getBytes()),Base64.DEFAULT).trim();
        } catch (Exception e) {
            return null;
        }

    }


}
