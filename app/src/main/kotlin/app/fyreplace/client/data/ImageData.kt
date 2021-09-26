package app.fyreplace.client.data

data class ImageData(val name: String, val mime: String, val data: ByteArray) {
    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        else -> {
            other as ImageData

            when {
                name != other.name -> false
                mime != other.mime -> false
                !data.contentEquals(other.data) -> false
                else -> true
            }
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
