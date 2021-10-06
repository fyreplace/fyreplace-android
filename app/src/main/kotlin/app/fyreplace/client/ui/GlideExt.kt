package app.fyreplace.client.ui

import app.fyreplace.client.R
import app.fyreplace.protos.Profile
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun RequestManager.loadAvatar(url: String?) = load(url)
    .placeholder(R.drawable.ic_baseline_account_circle)
    .circleCrop()
    .transition(DrawableTransitionOptions.withCrossFade())

fun RequestManager.loadAvatar(profile: Profile?) = loadAvatar(profile?.avatar?.url)
