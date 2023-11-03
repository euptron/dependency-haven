#Dependency Haven

<p align="center">
  [![](https://jitpack.io/v/etidoUP/Dependency-Haven.svg)](https://jitpack.io/#etidoUP/Dependency-Haven)
</p>

Dependency Haven is a powerful and easy-to-use dependency management tool for java and Android projects. Simplify the management of your project dependencies with this open-source library.

## Features

- [x] Resolve direct dependencies 
- [x] Resolve transitive dependencies
- [x] Cache resolved dependency POM and library
- [x] Read repositories from JSON
- [x] Handle parent POM
- [x] Handle POM properties
- [x] Skip unnecessary resolution
- [ ] Remote authentication
- [ ] CLI
- [ ] Auto update cached library and POM

## Installation

Getting started.
1. Add the following in your root build.gradle at the end of repositories
 ```gradle
dependencyResolutionManagement {
     repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
     repositories {
          mavenCentral()
          maven { url 'https://jitpack.io' }
     }
}
```
2. Add the following dependency to your project's `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.etidoUP:Dependency-Haven:v0.1.0-alpha'
}
```

## License

```
MIT License

Copyright (c) 2023 EUP

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
## Acknowledgements

Special thanks to [Tyron](https://github.com/tyron12233) for providing a free and open-source POM parsing class.
