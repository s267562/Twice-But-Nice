package it.polito.mad.mad_project

import java.io.Serializable

data class User(val name: String, val surname: String, val nickname: String, val email: String, val location: String) : Serializable {
}

