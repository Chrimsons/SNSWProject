package model

import ObjectIdAsStringSerializer
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class User(
    val firstname:String,
    val lastname:String,
    val mobile:String,
    val email:String,
    val password:String,
    val roles : List<String> = listOf(),
    val gender:String,
    val dob : Long ,
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<User> = newId(),


)
@Serializable
data class LoginDTO(

    val email:String,
    val password:String,



    )