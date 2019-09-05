package com.ull.project.mobiletriageapp_start;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author Juan José Gregorio Díaz Marrero ULL
 * Clase diseñada para asignar un toast personalizado a los avisos
 * que suceden durante la ejecución de la aplicación
 *
 */

class CustomToast {
    private static final String TAG = "CustomToast";

    // Contexto donde se va a mostrar el Toast Personalizado
    private Context toastcontext;
    // Mensaje que se va a mostrar en el Toast Personalizado
    private  String message;

    // Id del Layout Personalizado para el Toast
    private int idviewcustomview;

    public CustomToast( Context toastcontext, String message, int idviewcustomview) {
        this.toastcontext = toastcontext;
        this.message = message;
        this.idviewcustomview = idviewcustomview;
    }
    public Context getToastcontext() {
        return toastcontext;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message != null)
            this.message = message;
    }

    public int getIdviewcustomview() {
        return idviewcustomview;
    }


    /**
     * Método para inflar una vista xml con un Toast Customizado y personalizado
     * @param root :(ViewGroup) Layout raíz en el cúal se va a encapsular   el aviso
     *
     */
    public void inflateToast(ViewGroup root){

        try{
            LayoutInflater inflater  = LayoutInflater.from(this.toastcontext);
            View layout = inflater.inflate(this.getIdviewcustomview(),root,false);
            TextView text =  layout.findViewById(R.id.custom_toast_textview);
            text.setText(this.getMessage());
            Toast toast = new Toast(this.getToastcontext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

    }









}
