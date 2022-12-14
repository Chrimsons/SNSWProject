package routes

import RoleBasedAuthorization
import com.mongodb.client.MongoDatabase
import getEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.LearnerLicence
import model.LogEntry


import model.ProvisionalRequest
import org.litote.kmongo.*

fun Route.licenceRoute(db: MongoDatabase) {

    val licenceCollection = db.getCollection<LearnerLicence>("licences")


    route("/api/customer/licences") {
        install(RoleBasedAuthorization) { roles = listOf("customer") }

        post("{id}/logbook-entry") {
            val entry = call.receive<LogEntry>()
            val id = call.parameters["id"].toString()
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("id").toString().replace("\"", "")
            val licence = licenceCollection.findOne("{userId:'$userId',_id:ObjectId('$id')}")
            if (licence != null) {
                licence.logEntries.add(entry)
                licenceCollection.updateOne(licence)
                return@post call.respond(HttpStatusCode.Created, licence.dto())
            }
            return@post call.respond(HttpStatusCode.NotFound)
        }
        /*post("{id}/total"){
            val entry = call.receive<LogEntryDTO2>()
            val id = call.parameters["id"].toString()
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("id").toString().replace("\"","")
            val licence = licenceCollection.findOne("{userId:'$userId',_id:ObjectId('$id')}")
            if(licence != null){
                licence.logEntries.add(entry)
                licenceCollection.updateOne(licence)
                return@post call.respond(HttpStatusCode.Created,licence.dto())
            }
            return@post call.respond(HttpStatusCode.NotFound)
        }*/

        get {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("id").toString().replace("\"", "")
            val licences = licenceCollection.find("{userId:'$userId'}").toList().map { it.dto() }
            if (licences != null) {
                return@get call.respond(HttpStatusCode.OK, licences)
            }
            return@get call.respond(HttpStatusCode.NotFound)
        }

        get("{id}") {
            val id = call.parameters["id"].toString()
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("id").toString().replace("\"", "")
            val licence = licenceCollection.findOne("{userId:'$userId',_id:ObjectId('$id')}")
            if (licence != null) {
                val dto = licence.dto()
                return@get call.respond(dto)
            }
            return@get call.respond(HttpStatusCode.NotFound)
        }
    }

    route("/api/admin/licences") {

        install(RoleBasedAuthorization) { roles = listOf("admin") }
        post {
            val data = call.receive<LearnerLicence>()
            val email = call.principal<JWTPrincipal>()?.getEmail()
            val licence = data.copy(issuedBy = email!!)
            licenceCollection.insertOne(licence)
            call.respond(HttpStatusCode.Created, licence);
        }
        put("/p") {
            val data = call.receive<ProvisionalRequest>()
            val filter = "{_id:ObjectId('${data._id}')}"
            val result = licenceCollection.updateOne(filter, "{\$set:{p: true}}")
            if (result.modifiedCount.toInt() == 1) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/{userId}") {
            val userId = call.parameters["userId"].toString()
            val filter = "{userId:'$userId'}"
            val data = licenceCollection.findOne(filter)
            if (data != null) {
                return@get call.respond(data.dto())
            }
            return@get call.respond(HttpStatusCode.NotFound)
        }
    }
}