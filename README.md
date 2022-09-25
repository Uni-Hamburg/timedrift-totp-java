# timedrift-totp-java

Fork of [aerogear-otp-java](https://github.com/aerogear-attic/aerogear-otp-java)

This forks adds functionality to support time drifting Hardware TOTP tokens mentioned in [RFC 6238.](https://www.ietf.org/rfc/rfc6238.txt) Section 6.

[![circle-ci](https://img.shields.io/circleci/project/github/aerogear/aerogear-otp-java/master.svg)](https://circleci.com/gh/aerogear/aerogear-otp-java)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](https://opensource.org/s/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.jboss.aerogear/aerogear-otp-java.svg)](http://search.maven.org/#search%7Cga%7C1%7Caerogear-otp-java)
[![Javadocs](http://www.javadoc.io/badge/org.jboss.aerogear/aerogear-otp-java.svg?color=blue)](http://www.javadoc.io/doc/org.jboss.aerogear/aerogear-otp-java)

## Java Timed One Time Password API

A Java library for generating one time passwords according to [RFC 6238.](https://www.ietf.org/rfc/rfc6238.txt).

This API is compatible with Google Authenticator apps available for [Android](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=en) and [iPhone](https://itunes.apple.com/us/app/google-authenticator/id388497605?mt=8). You can follow the instructions [here](http://support.google.com/accounts/bin/answer.py?hl=en&answer=1066447) to install Google Authenticator. 

|                 | Project Info                |
| --------------- |-----------------------------|
| License:        | Apache License, Version 2.0 |
| Build:          | Maven                       |
| Documentation:  | tba                         |
| Issue tracker:  | tba                         |
| Mailing lists:  | tba                         |
|                 | tba                         |

## Usage

### Android Studio

Add to your application's `build.gradle` file

```groovy
dependencies {
  compile 'org.jboss.aerogear:aerogear-otp-java:1.0.0'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`

```xml
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-otp-java</artifactId>
  <version>1.0.0</version>
</dependency>
```