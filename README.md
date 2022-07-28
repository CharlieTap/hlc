# hybrid logical clock

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-ios]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-mac]

A Kotlin multiplatform implementation of a [hybrid logical clock](https://cse.buffalo.edu/tech-reports/2014-04.pdf)

# Usage

On each node that is a writer (i.e. can create events) create a clock

```kotlin
var local = HybridLogicalClock()
```

When receiving events update the clock

```kotlin
val remote = event.hlc
local = HybridLogicalClock.receive(local, remote)
```

When generating events increment the clock and update the local clock

```kotlin
HybridLogicalClock.increment(local).let { updated ->
    local = updated
    event.timestamp = updated 
}
```
Finally compare 
```kotlin
val winner = hlc1 > hlc2
```

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat
[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat