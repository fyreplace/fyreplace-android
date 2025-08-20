# Next

-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keepnames class app.fyreplace.fyreplace.ui.views.navigation.**


# Legacy

-dontwarn org.conscrypt.**
-dontwarn javax.naming.**

-keep public class com.google.protobuf.**
-keep public class app.fyreplace.protos.**
-keep class app.fyreplace.fyreplace.legacy.grpc.** implements android.os.Parcelable
