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

package com.android.gradle.replicator.generator

import com.android.gradle.replicator.generator.fixtures.BaseTest
import com.android.gradle.replicator.generator.fixtures.GradleRunner
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class BasicAndroidWithResourcesTest: BaseTest() {

    companion object {
        private const val TEST_STRUCTURE: String = """
{
  "gradle": "6.1.1",
  "agp": "4.0.1",
  "kotlin": "1.3.72",
  "properties": [],
  "rootModule": {
    "path": ":",
    "plugins": [],
    "dependencies": []
  },
  "modules": [
    {
      "path": ":module1",
      "plugins": [
        "kotlin-android",
        "com.android.application"
      ],
      "javaSources": {
        "fileCount": 1
      },
      "androidResources": {
        "animator": {},
        "anim": {},
        "color": {},
        "drawable": {
          "": {
            ".xml": 1,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0
          },
          "v24": {
            ".xml": 1,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0
          }
        },
        "font": {},
        "layout": {
          "": {
            ".xml": 4
          }
        },
        "menu": {
          "": {
            ".xml": 1
          }
        },
        "mipmap": {
          "anydpi-v26": {
            ".xml": 2,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 0
          },
          "hdpi": {
            ".xml": 0,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 2
          },
          "mdpi": {
            ".xml": 0,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 2
          },
          "xhdpi": {
            ".xml": 0,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 2
          },
          "xxhdpi": {
            ".xml": 0,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 2
          },
          "xxxhdpi": {
            ".xml": 0,
            ".png": 0,
            ".9.png": 0,
            ".jpg": 0,
            ".gif": 0,
            ".webp": 2
          }
        },
        "raw": {},
        "transition": {},
        "values": {
          "": {
            ".xml": 4
          },
          "night": {
            ".xml": 1
          }
        },
        "xml": {}
      },
      "javaResources": {
        "fileCount": 2
      },
      "dependencies": [],
      "android": {
        "compileSdkVersion": "android-30",
        "minSdkVersion": 21,
        "targetSdkVersion": 30,
        "buildFeatures": {}
      }
    }
  ]
}
"""
    }

    @Test
    fun testOutput() {
        val output = generateWithStructure(TEST_STRUCTURE)

        val moduleResourceMetadataFile = File(File(output, "module1"), "resource-metadata.json")

        Truth.assertWithMessage(moduleResourceMetadataFile.absolutePath)
                .that(moduleResourceMetadataFile.readText()).isEqualTo(
                        """
                                {
                                  "androidResources": {
                                    "animator": {},
                                    "anim": {},
                                    "color": {},
                                    "drawable": {
                                      "": {
                                        ".xml": 1,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0
                                      },
                                      "v24": {
                                        ".xml": 1,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0
                                      }
                                    },
                                    "font": {},
                                    "layout": {
                                      "": {
                                        ".xml": 4
                                      }
                                    },
                                    "menu": {
                                      "": {
                                        ".xml": 1
                                      }
                                    },
                                    "mipmap": {
                                      "anydpi-v26": {
                                        ".xml": 2,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 0
                                      },
                                      "hdpi": {
                                        ".xml": 0,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 2
                                      },
                                      "mdpi": {
                                        ".xml": 0,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 2
                                      },
                                      "xhdpi": {
                                        ".xml": 0,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 2
                                      },
                                      "xxhdpi": {
                                        ".xml": 0,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 2
                                      },
                                      "xxxhdpi": {
                                        ".xml": 0,
                                        ".png": 0,
                                        ".9.png": 0,
                                        ".jpg": 0,
                                        ".gif": 0,
                                        ".webp": 2
                                      }
                                    },
                                    "raw": {},
                                    "transition": {},
                                    "values": {
                                      "": {
                                        ".xml": 4
                                      },
                                      "night": {
                                        ".xml": 1
                                      }
                                    },
                                    "xml": {}
                                  },
                                  "javaResources": 2
                                }
                        """.trimIndent()
        )
    }

    @Test
    fun runOutput() {
        GradleRunner(generateWithStructure(TEST_STRUCTURE)).runTasks("projects")
    }
}