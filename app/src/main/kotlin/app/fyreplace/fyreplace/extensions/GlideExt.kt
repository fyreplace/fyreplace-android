package app.fyreplace.fyreplace.extensions

import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun RequestManager.loadAvatar(url: String?) = load(url)
    .placeholder(R.drawable.ic_baseline_account_circle)
    .circleCrop()
    .transition(DrawableTransitionOptions.withCrossFade())

fun RequestManager.loadAvatar(profile: Profile?) =
    loadAvatar(if (profile?.isBanned != true) profile?.avatar?.url else "")
