# Echo 工具包
## 使用方法
1. 在添加仓库地址依赖 [参考](https://github.com/EchoWorkGH/echoUtils/blob/master/build.gradle#L22)
```
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
        maven {
            url = "https://raw.githubusercontent.com/EchoWorkGH/echoUtils/master/echoUtils"
        }
    }
}
```
2. 添加工具包 [参考](https://github.com/EchoWorkGH/echoUtils/blob/master/app/build.gradle#L45)
```
    implementation 'com.echo:utils:0.0.1-beta3'
```
3. 由于第三方包中存在非AndroidX的包，需要gradle.properties中添加,转换第三方库到AndroidX  [参考](https://github.com/EchoWorkGH/echoUtils/blob/master/gradle.properties#L17)
```
# https://developer.android.com/topic/libraries/support-library/androidx-rn
android.useAndroidX=true   
# Automatically convert third-party libraries to use AndroidX
android.enableJetifier=true
```
4. 由于包中已经引入了rx，retrofit等，需要打多个包 [参考](https://github.com/EchoWorkGH/echoUtils/blob/master/app/build.gradle#L16)
```
    multiDexEnabled true
```
5. 默认使用了databinding [参考](https://github.com/EchoWorkGH/echoUtils/blob/master/app/build.gradle#L7)
```
    dataBinding {
        enabled true
    }
```
