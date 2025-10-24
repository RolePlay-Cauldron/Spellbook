# Spellbook

Libraries for common code between our Minecraft Paper Plugins

---

This project requires **Java 21+** as well as **Paper**, currently in the **1.21.1** version.

The 

## Modules

__**Spellbook-Core:**__ Basic Utilities

- Advanced Logger
- Easy-to-use Scheduler

__**Spellbook-Database:**__ Database Utilities

- Coming soon

## Using this in your project

We release this project to **GitHub Packages** but also enable [jitpack.io](https://jitpack.io/) builds.

> Note that GitHub Packages require you to authenticate against the Repository!

### Adding the **GitHub Packages** Repository

1) Configure your Maven `settings.xml` with credentials:
```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GH_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

2) Add the repository to your plugins `pom.xml`:
```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/RolePlayCauldron/Spellbook</url>
  </repository>
</repositories>
```

### Adding the **jitpack.io** Repository

1) Add the repository to your plugins `pom.xml`:
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
