package app.fyreplace.fyreplace.grpc

import android.os.Parcel
import android.os.Parcelable
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostOrBuilder
import app.fyreplace.protos.Profile
import app.fyreplace.protos.ProfileOrBuilder

abstract class MessageParcelable : Parcelable {
    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val bytes = getItemBytes()
        parcel.writeInt(bytes.size)
        parcel.writeByteArray(bytes)
    }

    protected abstract fun getItemBytes(): ByteArray
}

fun Parcel.readOnlyByteArray() = ByteArray(readInt()).apply { readByteArray(this) }

class ParcelableProfile(private val profile: Profile) : MessageParcelable(),
    ProfileOrBuilder by profile {
    val v get() = profile

    constructor(parcel: Parcel) : this(Profile.parseFrom(parcel.readOnlyByteArray()))

    override fun getItemBytes(): ByteArray = profile.toByteArray()

    companion object CREATOR : Parcelable.Creator<ParcelableProfile> {
        override fun createFromParcel(parcel: Parcel) = ParcelableProfile(parcel)

        override fun newArray(size: Int) = arrayOfNulls<ParcelableProfile?>(size)
    }
}

val Profile.p get() = ParcelableProfile(this)

class ParcelablePost(private val post: Post) : MessageParcelable(),
    PostOrBuilder by post {
    val v get() = post

    constructor(parcel: Parcel) : this(Post.parseFrom(parcel.readOnlyByteArray()))

    override fun getItemBytes(): ByteArray = post.toByteArray()

    companion object CREATOR : Parcelable.Creator<ParcelablePost> {
        override fun createFromParcel(parcel: Parcel) = ParcelablePost(parcel)

        override fun newArray(size: Int) = arrayOfNulls<ParcelablePost?>(size)
    }
}

val Post.p get() = ParcelablePost(this)
