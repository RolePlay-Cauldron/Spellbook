# Spellbook

Libraries for common code between our Minecraft Paper Plugins

---

This project requires **Java 21+** as well as **Paper**, currently in the **1.21.1** version.

## Modules

__**Spellbook-Core:**__ Basic Utilities

- Advanced Logger
- Easy-to-use Scheduler

__**Spellbook-Database:**__ Database Utilities

- Coming soon

---

## Using this in your project

We release this project to **GitHub Packages** but also enable [jitpack.io](https://jitpack.io/) builds.

> Note that GitHub Packages require you to authenticate against the Repository!

### Adding the **GitHub Packages** Repository

1) Configure your Maven `~/.m2/settings.xml` with credentials. Create your Token (classic) [here](https://github.com/settings/tokens), you only require the `read:packages` scope
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
      <server>
        <id>github</id>
        <username>YOUR_GH_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
      </server>
  </servers>
</settings>
```

2) Add the repository to your plugins `pom.xml`
```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/RolePlay-Cauldron/Spellbook</url>
  </repository>
</repositories>
```

### Adding the **jitpack.io** Repository

1) Add the repository to your plugins `pom.xml`
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

### Add the Dependencies

Only choose the dependencies that you want
```xml
<dependencies>
  <dependency>
    <groupId>com.github.roleplaycauldron</groupId>
    <artifactId>spellbook-core</artifactId>
    <version>0.0.1</version>
  </dependency>
  <dependency>
    <groupId>com.github.roleplaycauldron</groupId>
    <artifactId>spellbook-database</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```

Since this Library is not a standalone Plugin, you need to ship it together with your plugins jar File.
