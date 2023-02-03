package com.example.contactpermission.models

class ContactType : java.io.Serializable {
    var id: Int? = null
    var name: String? = null
    var surname: String? = null
    var number: String? = null


    constructor(id: Int?, name: String?, surname: String?, number: String?) {
        this.id = id
        this.name = name
        this.surname = surname
        this.number = number
    }

    constructor(name: String?, surname: String?, number: String?) {
        this.name = name
        this.surname = surname
        this.number = number
    }

}