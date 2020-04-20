package it.polito.mad.project.models

import java.io.Serializable

data class User(val name: String) : Serializable {
    var surname: String = ""
    var nickname: String = ""
    var email: String = ""
    var location: String = ""
    var photoProfilePath: String = ""

    constructor(name: String, surname: String, nickname: String, email: String, location: String, photoProfilePath: String? = "") : this(name) {
        this.surname = surname
        this.nickname = nickname
        this.email = email
        this.location = location
        this.photoProfilePath = photoProfilePath?: ""
    }

}

