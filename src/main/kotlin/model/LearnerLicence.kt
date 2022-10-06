package model

import ObjectIdAsStringSerializer
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Duration

@Serializable
data class LearnerLicence(
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<LearnerLicence> = newId(),
    val issued:Long = System.currentTimeMillis(),
    val issuedBy: String = "",
    val userId : String,
    val p:Boolean=false,
    val logEntries: MutableList<LogEntry> = mutableListOf()

){
    fun dto() : LearnerLicenceDTO= LearnerLicenceDTO(this)
}

@Serializable
data class LogEntry(
    val start: Long,
    val end: Long,
    val instructor:Boolean,
    val nightTime: Boolean,

){
    @get:JsonIgnore
    val duration : Duration
        get(){ if (!nightTime) {
            return Duration.ofMillis(end - start)
        } else {
            return Duration.ofMillis(0)
        }

        }
    @get:JsonIgnore
    val nightDuration : Duration
        get() {
            if (nightTime){
                return Duration.ofMillis(end - start)
            } else {
                return Duration.ofMillis(0)
            }
        }

    @get:JsonIgnore
    val bonus : Duration
    get(){
        if (nightTime && instructor){
            return Duration.ofMillis(nightDuration.toMillis() * 2)
        } else if (!nightTime && instructor) {
            return Duration.ofMillis(duration.toMillis() * 2)
        } else {
            return Duration.ofMillis(0)
        }
    }
    @get:JsonIgnore
    val totalNightHours : Duration
        get() {
            return Duration.ofMillis(nightDuration.toMillis())
        }
    @get:JsonIgnore
    val total : Duration
    get() {
        return Duration.ofMillis(duration.toMillis() + nightDuration.toMillis() + bonus.toMillis())
    }
}

@Serializable
class LearnerLicenceDTO{

    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<LearnerLicence>
    val issued:Long
    val issuedBy: String
    val userId : String
    val logEntries: MutableList<LogEntryDTO>
    val totalNightHours: TimeUnitDTO
    val total:TimeUnitDTO

    constructor(licence: LearnerLicence){
        _id = licence._id
        issued = licence.issued
        issuedBy = licence.issuedBy
        userId = licence.userId
        var licenceTotal : Long = 0
        var licenceNightTotal : Long = 0
        logEntries = licence.logEntries.map {
            val duration = TimeUnitDTO(it.duration)
            val nightDuration = TimeUnitDTO(it.nightDuration)
            val bonus = TimeUnitDTO(it.bonus)
            val totalNightHours = TimeUnitDTO(it.totalNightHours)
            val total = TimeUnitDTO(it.total)
            licenceTotal += it.total.toSeconds()
            licenceNightTotal += it.totalNightHours.toSeconds()
            LogEntryDTO(it.start,it.end,it.instructor,it.nightTime,duration,nightDuration,bonus,totalNightHours,total)
        }.toMutableList()
        val ltd = Duration.ofSeconds(licenceTotal)
        total = TimeUnitDTO(ltd)
        val lntd = Duration.ofSeconds(licenceNightTotal)
        totalNightHours = TimeUnitDTO(lntd)
    }
}
@Serializable
data class LogEntryDTO(
    val start: Long,
    val end: Long,
    val instructor:Boolean,
    val nightTime: Boolean,
    val duration : TimeUnitDTO,
    val nightDuration : TimeUnitDTO,
    val bonus : TimeUnitDTO,
    val totalNightHours: TimeUnitDTO,
    val total : TimeUnitDTO
)
data class LogEntryDTO2(

    val total : TimeUnitDTO
)

@Serializable
class TimeUnitDTO {
    val hours: Long
    val minutes: Long
    val seconds: Long
    constructor(d:Duration){
        hours = d.seconds / 3600;
        minutes = (d.seconds  % 3600) / 60;
        seconds = d.seconds  % 60;
    }
}

@Serializable
data class ProvisionalRequest(
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id:Id<LearnerLicence>
    )


