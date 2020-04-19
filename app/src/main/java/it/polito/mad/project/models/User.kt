package it.polito.mad.project.models

import java.io.Serializable

data class User(val name: String, val surname: String, val nickname: String, val email: String, val location: String, val photoProfilePath: String? = "") : Serializable {

}

