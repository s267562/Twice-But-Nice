package it.polito.mad.project.models.user

class UserList<T> (val adapter: T){
    //user interested to item
    val users: MutableList<User> = mutableListOf()
}