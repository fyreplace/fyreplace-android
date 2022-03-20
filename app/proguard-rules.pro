# Conscrypt
-dontwarn org.conscrypt.**

# gRPC/javax
-dontwarn javax.naming.**
-keep public class app.fyreplace.protos.**

# Glide

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
