package it.polito.mad.project.commons.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

open class NotificationFragment : Fragment() {
    private val fcmApi = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAACeyupyA:APA91bGPFuVmAyKArL732RJ6CPNDV4_Che2JlqeEjLASSv2kTzMT_FT6JB3Wv1SC6CAEvc5uU1cAVmHZ2fdTnEk4LnE5Tzv9UrjdsqjM73KJejb5P-l-y_dsGFJxiy83zlTq85ZBD19K"
    private val contentType = "application/json"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(requireActivity().applicationContext)
    }

    fun sendNotification(topic: String, title: String, message: String, notificationBody: JSONObject = JSONObject()) {
        val notification = JSONObject()

        try {
            notificationBody.put("title", title)
            notificationBody.put("message", message)
            notification.put("to", "/topics/$topic")
            notification.put("data", notificationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        val jsonObjectRequest = object : JsonObjectRequest(fcmApi, notification,
            Response.Listener { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }

        requestQueue.add(jsonObjectRequest)
    }
}