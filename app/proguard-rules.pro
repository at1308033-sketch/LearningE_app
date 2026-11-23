# Quy tắc R8 cho bản release.

# protobuf-lite đọc trường của message bằng tên.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }

# grpc-okhttp tham chiếu lớp tùy chọn không có trên Android.
-dontwarn com.squareup.okhttp.**
-dontwarn javax.naming.**
-dontwarn com.google.errorprone.annotations.**

# Tham số điều hướng kiểu enum được nạp theo tên lớp ghi trong nav_main.xml.
-keepnames enum com.learninge.app.ui.study.StudySource
-keepnames enum com.learninge.app.data.model.ActivityType
