Developing For PeripheralsPlusOne
============================

**Prerequisites**: Experience with ComputerCraft, Java and the Minecraft Forge Library

## The Very Basics (this *should* apply to any mod)

### 1. Forking the repo

First, make sure you have a Github account and have installed git and/or a Github client.
If you need help learning how to use git and Github, read [these articles](https://help.github.com/categories/bootcamp/)
.

### 2. Setting up the workspace

Refer to Minecraft Forge's documentation: https://mcforge.readthedocs.io/en/latest/gettingstarted/#from-zero-to-modding

### 3. Developing

You should know what to do

### 4. Building

Simply run `gradlew build` and navigate to the build/libs directory relative to the project directory

### 5. Making the pull request

So, you followed everything and you believe that your contribution is good enough to be included? If so, make the Pull Request!
Please refer to your preferred method of using git's documentation if you don't know how.

## PeripheralsPlusOne Specific Information

### 1. Coding Conventions

If you wish to have your pull request approved, you **must** follow these conventions.

#### Tabs or spaces?

Tabs.

#### Brackets

Please use [1tbs](http://en.wikipedia.org/wiki/Indent_style#Variant:_1TBS) with the exception that one line conditional
statements do not need brackets and hanging `if`s `else`s `do`s etc are on a new line.

```
if (something) {
    // stuff
}
else {
    // other stuff
}

// Not this
if (something) {
    // stuff
} else {
    // other stuff
}
```

#### Naming

PeripheralsPlusOne uses standard java naming conventions and try to follow Mojang's naming conventions for classes

### 2. Dependencies

All dependencies are managed by Gradle.

### 3. Anything else?

Submit a new issue.
