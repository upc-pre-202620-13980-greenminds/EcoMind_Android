package pe.greenminds.ecomind.users.domain.model

// Rule of the personal commitment of a student (HU-006)
object PersonalCommitment {

    const val MAX_LENGTH = 150

    fun isValid(value: String): Boolean = value.trim().length in 1..MAX_LENGTH
}
