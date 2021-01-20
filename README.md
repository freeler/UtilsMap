### 高德地图工具类
https://github.com/freeler/UtilsMap

#### 使用
```kotlin
Step 1. Add the JitPack repository to your build file

allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

Step 2. Add the dependency

dependencies {
    implementation 'com.github.freeler:UtilsMap:Tag'
}
```

#### 集成高德地图版本
```kotlin
api 'com.amap.api:3dmap:6.9.2'
```

#### 当前项目配置
- sdkVersion: 29
- gradle: 4.1.1
- kotlin_version: 1.4.21

#### 工具类说明
##### AMapHelper 谷歌地图瓦片

- useTileOverlay 加载在线瓦片数据

- deleteAllFiles 删除文件

##### MapLayUtil

- getCenterLatLng 获取坐标中心点

- getZoom 获取地图层级

##### 坐标转换
PositionUtil