package app.fyreplace.fyreplace.extensions

import android.widget.ImageView
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide

fun ImageView.setAvatar(profile: Profile?) {
    Glide.with(context).loadAvatar(profile).into(this)
}
