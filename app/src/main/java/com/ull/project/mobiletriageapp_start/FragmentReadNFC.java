package com.ull.project.mobiletriageapp_start;

import android.content.Context;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class FragmentReadNFC extends DialogFragment{



    public static final String TAG = FragmentReadNFC.class.getSimpleName();

    public static FragmentReadNFC  newInstance() {

        return new FragmentReadNFC ();
    }

    private TextView txtviewmsg;
    private ProgressBar progressBar;
    private FragmentViewListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_nfc,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        getDialog().setTitle(getString(R.string.txt_Title_Dialog));
        txtviewmsg = view.findViewById(R.id.textview_message_fragment);
        txtviewmsg.setText(getString(R.string.txt_Info_Progress_Dialog_Read_TAG));
        progressBar =  view.findViewById(R.id.progressbar_nfc);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Activity_Info_Tag)context;
        listener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener.onDialogDismissed();
    }


    /**
     * Método encargado de detectar y escribir un determinado mensaje en formato NDEF en la etiqueta pasada por parámetro
     * @param nfctag : (Tag) Etiqueta  que contiene el mensaje para leer
     *
     */
    public void onNfcDetected(Tag nfctag){
        progressBar.setVisibility(View.VISIBLE);
        if (nfctag != null){
            Ndef ndef = Ndef.get(nfctag);
            readFromNFC(ndef);
        }

    }

    private void readFromNFC(Ndef ndef) {
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if(ndefMessage != null){
                byte[] payload = ndefMessage.getRecords()[0].getPayload();
                int languageCodeLength = payload[0] & 51;
                String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, StandardCharsets.UTF_8);
                DataNFC datos_ = new DataNFC(text);
                txtviewmsg.setText(getString(R.string.txt_ReadTagSucces));
                datos_.setTagId(text);
                Intent i = new Intent(this.getContext(),Activity_Info_Tag.class);
                i.putExtra("ok",true);
                i.putExtra("Datos", datos_);
                startActivity(i);
            }
            ndef.close();
        } catch (IOException | FormatException | NullPointerException e) {
           Log.d("Error readFromNFC: ",e.getCause().toString());
        }
    }


























}
