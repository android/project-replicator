/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.gradle.replicator

import com.google.common.truth.Truth
import java.io.File
import kotlin.test.Test

class BuildFeaturesTests {

    @Test
    fun testAidl() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              aidl = false
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "aidl": false
              }
            }
          },
          "modules": []
        }
        """.trimIndent())
    }

    @Test
    fun testBuildConfig() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              buildConfig = false
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "buildConfig": false
              }
            }
          },
          "modules": []
        }
        """.trimIndent())
    }

    @Test
    fun testCompose() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              compose = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "compose": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testPrefab() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              prefab = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "prefab": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testRenderScript() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              renderScript = false
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "renderScript": false
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testResValues() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              resValues = false
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "resValues": false
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testShaders() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              shaders = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "shaders": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testViewBinding() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              viewBinding = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [
              {
                "library": "com.android.databinding:viewbinding:$AGP_VERSION",
                "method": "api"
              }
            ],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "viewBinding": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testDataBinding() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              dataBinding = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [
              {
                "library": "androidx.databinding:databinding-compiler:$AGP_VERSION",
                "method": "annotationProcessor"
              },
              {
                "library": "com.android.databinding:adapters:$AGP_VERSION",
                "method": "api"
              },
              {
                "library": "com.android.databinding:baseLibrary:$AGP_VERSION",
                "method": "api"
              },
              {
                "library": "com.android.databinding:library:$AGP_VERSION",
                "method": "api"
              }
            ],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "dataBinding": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testMlModelBinding() {
        val projectSetup = setup()

        projectSetup.buildFile.appendText("""
        android {
            buildFeatures {
              mlModelBinding = true
            }
        }
        """.trimIndent())

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [],
              "font": [],
              "layout": [],
              "menu": [],
              "mipmap": [],
              "navigation": [],
              "raw": [],
              "transition": [],
              "values": [],
              "xml": []
            },
            "javaResources": {},
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {
                "mlModelBinding": true
              }
            }
          },
          "modules": []
        }
        """.trimIndent())

    }

    @Test
    fun testResources() {
        val projectSetup = setup()

        //setup resources

        val androidResourceFolder = File(projectSetup.projectDir, "src/main/res")
        val javaResourceFolder = File(projectSetup.projectDir, "src/main/resources")

        val androidResources = listOf(
                "drawable/ic_launcher_background.xml",
                "drawable-v24/ic_launcher_background.xml",
                "layout/activity_main.xml",
                "layout/content_main.xml",
                "layout/fragment_first.xml",
                "layout/fragment_second.xml",
                "menu/menu_main.xml",
                "mipmap-anydpi-v26/ic_launcher_round.xml",
                "mipmap-anydpi-v26/ic_launcher.xml",
                "mipmap-hdpi/ic_launcher.webp",
                "mipmap-hdpi/ic_launcher_round.webp",
                "mipmap-mdpi/ic_launcher.webp",
                "mipmap-mdpi/ic_launcher_round.webp",
                "mipmap-xhdpi/ic_launcher.webp",
                "mipmap-xhdpi/ic_launcher_round.webp",
                "mipmap-xxhdpi/ic_launcher.webp",
                "mipmap-xxhdpi/ic_launcher_round.webp",
                "mipmap-xxxhdpi/ic_launcher.webp",
                "mipmap-xxxhdpi/ic_launcher_round.webp",
                "navigation/nav_graph.xml"
        )

        val valuesResources = listOf(
            "values/colors.xml",
            "values/dimens.xml",
            "values/strings.xml",
            "values/themes.xml",
            "values-night/themes.xml"
        )

        val javaResources = listOf(
                "foo.txt",
                "bar.json"
        )

        androidResources.forEach {
            val resourceFile = File(androidResourceFolder, it)
            resourceFile.parentFile.mkdirs()
            resourceFile.createNewFile()
        }

        valuesResources.forEach {
            val resourceFile = File(androidResourceFolder, it)
            resourceFile.parentFile.mkdirs()
            resourceFile.createNewFile()
            resourceFile.writeText("<resources>\n</resources>")
        }

        javaResources.forEach {
            val resourceFile = File(javaResourceFolder, it)
            resourceFile.parentFile.mkdirs()
            resourceFile.createNewFile()
        }

        projectSetup.runner.build()

        // Verify the result
        Truth.assertThat(projectSetup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
        {
          "gradle": "$GRADLE_VERSION",
          "agp": "$AGP_VERSION",
          "kotlin": "n/a",
          "properties": [],
          "rootModule": {
            "path": ":",
            "plugins": [
              "com.android.application"
            ],
            "javaSources": {
              "fileCount": 0
            },
            "androidResources": {
              "animator": [],
              "anim": [],
              "color": [],
              "drawable": [
                {
                  "qualifiers": "",
                  "extension": "xml",
                  "quantity": 1,
                  "fileSizes": [
                    0
                  ]
                },
                {
                  "qualifiers": "v24",
                  "extension": "xml",
                  "quantity": 1,
                  "fileSizes": [
                    0
                  ]
                }
              ],
              "font": [],
              "layout": [
                {
                  "qualifiers": "",
                  "extension": "xml",
                  "quantity": 4
                }
              ],
              "menu": [
                {
                  "qualifiers": "",
                  "extension": "xml",
                  "quantity": 1
                }
              ],
              "mipmap": [
                {
                  "qualifiers": "anydpi-v26",
                  "extension": "xml",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                },
                {
                  "qualifiers": "hdpi",
                  "extension": "webp",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                },
                {
                  "qualifiers": "mdpi",
                  "extension": "webp",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                },
                {
                  "qualifiers": "xhdpi",
                  "extension": "webp",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                },
                {
                  "qualifiers": "xxhdpi",
                  "extension": "webp",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                },
                {
                  "qualifiers": "xxxhdpi",
                  "extension": "webp",
                  "quantity": 2,
                  "fileSizes": [
                    0,
                    0
                  ]
                }
              ],
              "navigation": [
                {
                  "qualifiers": "",
                  "extension": "xml",
                  "quantity": 1
                }
              ],
              "raw": [],
              "transition": [],
              "values": [
                {
                  "qualifiers": "",
                  "extension": "xml",
                  "quantity": 4,
                  "valuesFileList": [
                    {
                      "stringCount": 0,
                      "intCount": 0,
                      "boolCount": 0,
                      "colorCount": 0,
                      "dimenCount": 0,
                      "idCount": 0,
                      "integerArrayCount": [],
                      "arrayCount": [],
                      "styleCount": []
                    },
                    {
                      "stringCount": 0,
                      "intCount": 0,
                      "boolCount": 0,
                      "colorCount": 0,
                      "dimenCount": 0,
                      "idCount": 0,
                      "integerArrayCount": [],
                      "arrayCount": [],
                      "styleCount": []
                    },
                    {
                      "stringCount": 0,
                      "intCount": 0,
                      "boolCount": 0,
                      "colorCount": 0,
                      "dimenCount": 0,
                      "idCount": 0,
                      "integerArrayCount": [],
                      "arrayCount": [],
                      "styleCount": []
                    },
                    {
                      "stringCount": 0,
                      "intCount": 0,
                      "boolCount": 0,
                      "colorCount": 0,
                      "dimenCount": 0,
                      "idCount": 0,
                      "integerArrayCount": [],
                      "arrayCount": [],
                      "styleCount": []
                    }
                  ]
                },
                {
                  "qualifiers": "night",
                  "extension": "xml",
                  "quantity": 1,
                  "valuesFileList": [
                    {
                      "stringCount": 0,
                      "intCount": 0,
                      "boolCount": 0,
                      "colorCount": 0,
                      "dimenCount": 0,
                      "idCount": 0,
                      "integerArrayCount": [],
                      "arrayCount": [],
                      "styleCount": []
                    }
                  ]
                }
              ],
              "xml": []
            },
            "javaResources": {
              "json": [
                0
              ],
              "txt": [
                0
              ]
            },
            "assets": {},
            "dependencies": [],
            "android": {
              "compileSdkVersion": "android-30",
              "minSdkVersion": 24,
              "targetSdkVersion": 30,
              "buildFeatures": {}
            }
          },
          "modules": []
        }
        """.trimIndent())
    }

    private fun setup(): ProjectSetup {
        val setup = setupProject(type = BuildFileType.GROOVY, traceOffset = 2) {
            """
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath "com.android.tools.build:gradle:$AGP_VERSION"
                    }
                """.trimIndent()
        }

        setup.buildFile.appendText("""
            apply plugin: "com.android.application"
            
            android {
                compileSdkVersion = 30
                defaultConfig {
                    minSdkVersion = 24
                    targetSdkVersion = 30
                }
            }

            """.trimIndent())

        return setup
    }

}
