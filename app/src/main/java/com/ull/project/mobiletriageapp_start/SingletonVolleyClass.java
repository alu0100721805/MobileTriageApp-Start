package com.ull.project.mobiletriageapp_start;

import android.annotation.SuppressLint;
import android.content.Context;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Clase singleton que contiene la configuración necesaria para realizar peticiones mediante http  mediante el
 * uso de la librería Volley. Dicha clase deberá existir a lo largo del ciclo de vida de la aplicación y no de
 * la actividad.
 */

class SingletonVolleyClass {

        private static SingletonVolleyClass instance_;
        private RequestQueue requestqueue_;
        private static Context ctx_;

        private SingletonVolleyClass(Context context) {
            ctx_ = context;
            requestqueue_ = getRequestQueue();
        }

        public static synchronized SingletonVolleyClass getInstance(Context context) {
            if (instance_ == null) {
                instance_ = new SingletonVolleyClass (context);
            }
            return instance_;
        }

        public RequestQueue getRequestQueue() {
            if (requestqueue_ == null) {
                requestqueue_= Volley.newRequestQueue(ctx_.getApplicationContext());
            }
            return requestqueue_;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }
        public void stop(){
            getRequestQueue().stop();
            ctx_ = null;
            instance_ = null;

        }

}
