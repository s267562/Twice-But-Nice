package it.polito.mad.project.commons.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.Volley
import org.json.JSONObject

open class NotificationFragment : Fragment() {

   private lateinit var notificator: Notificator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificator = Notificator(Volley.newRequestQueue(requireActivity().applicationContext))
    }

    fun sendNotification(topic: String, title: String, message: String, notificationBody: JSONObject = JSONObject()) {
        notificator.sendNotification(topic, title, message, notificationBody)
    }
}
