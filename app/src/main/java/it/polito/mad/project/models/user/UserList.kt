package it.polito.mad.project.models.user

import it.polito.mad.project.adapters.UserAdapter

class UserList<T> (val adapter: T){
    //user interested to item
    val users: MutableList<User> = mutableListOf()
}