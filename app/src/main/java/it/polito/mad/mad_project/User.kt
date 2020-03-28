package it.polito.mad.mad_project

import java.io.Serializable

data class User(val _name: String) : Serializable {

    var name:String
        get() {
            return  name
        }

    var surname: String
        get() {
            return surname
        }

    var nickname: String
        get() {
            return nickname
        }

    var email: String
        get() {
            return email
        }

    var location: String
        get() {
            return location
        }

    init {
        name = _name
        surname = ""
        nickname = ""
        email = ""
        location = ""
    }
}

