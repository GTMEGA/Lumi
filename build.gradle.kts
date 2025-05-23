plugins {
    id("com.falsepattern.fpgradle-mc") version ("0.15.1")
}

group = "com.falsepattern"
minecraft_fp {
    mod {
        modid = "lumi"
        name = "Lumi"
        rootPkg = "$group.lumi"
    }
    api {
        packages = listOf("api")
    }
    mixin {
        pkg = "internal.mixin.mixins"
        pluginClass = "internal.mixin.plugin.MixinPlugin"
    }
    core {
        accessTransformerFile = "lumi_at.cfg"
        coreModClass = "internal.asm.ASMLoadingPlugin"
    }
    tokens {
        tokenClass = "internal.Tags"
        modid = "MOD_ID"
        name = "MOD_NAME"
        version = "VERSION"
        rootPkg = "GROUPNAME"
    }
    publish {
        changelog = "https://github.com/GTMEGA/Lumi/releases/tag/$version"
        maven {
            repoUrl = "https://mvn.falsepattern.com/releases/"
            repoName = "mavenpattern"
        }
        curseforge {
            projectId = "1050470"
            dependencies {
                required("chunkapi")
                required("fplib")
            }
        }
        modrinth {
            projectId = "RIP6DWIB"
            dependencies {
                required("chunkapi")
                required("fplib")
            }
        }
    }
}

repositories {
    cursemavenEX()
    exclusive(mavenpattern(), "com.falsepattern")
}

dependencies {
    apiSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.6.0")

    implementation("it.unimi.dsi:fastutil:8.5.15")

    apiSplit("com.falsepattern:chunkapi-mc1.7.10:0.6.1")

    compileOnly("com.falsepattern:falsetweaks-mc1.7.10:3.9.6:api")

    compileOnly(deobfCurse("journeymap-32274:4500658"))
}
