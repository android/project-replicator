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
        "animator": [],
        "anim": [],
        "color": [],
        "drawable": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 1,
            "fileSizes": [
              64
            ]
          },
          {
            "qualifiers": "v24",
            "extension": ".xml",
            "quantity": 1,
            "fileSizes": [
              128
            ]
          }
        ],
        "font": [],
        "layout": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 4
          }
        ],
        "menu": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 1
          }
        ],
        "mipmap": [
          {
            "qualifiers": "anydpi-v26",
            "extension": ".xml",
            "quantity": 2,
            "fileSizes": [
              256,
              512
            ]
          },
          {
            "qualifiers": "hdpi",
            "extension": ".webp",
            "quantity": 2,
            "fileSizes": [
              1024,
              2048
            ]
          },
          {
            "qualifiers": "mdpi",
            "extension": ".webp",
            "quantity": 2,
            "fileSizes": [
              4096,
              8192
            ]
          },
          {
            "qualifiers": "xhdpi",
            "extension": ".webp",
            "quantity": 2,
            "fileSizes": [
              16384,
              32768
            ]
          },
          {
            "qualifiers": "xxhdpi",
            "extension": ".webp",
            "quantity": 2,
            "fileSizes": [
              65536,
              131072
            ]
          },
          {
            "qualifiers": "xxxhdpi",
            "extension": ".webp",
            "quantity": 2,
            "fileSizes": [
              262144,
              524288
            ]
          }
        ],
        "raw": [],
        "transition": [],
        "values": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 4,
            "valuesFileList": [
              {
                "stringCount": 5,
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
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 0,
                "dimenCount": 0,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 1,
                "dimenCount": 0,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 2,
                "dimenCount": 1,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              }
            ]
          },
          {
            "qualifiers": "night",
            "extension": ".xml",
            "quantity": 4,
            "valuesFileList": [
              {
                "stringCount": 5,
                "intCount": 0,
                "boolCount": 0,
                "colorCount": 0,
                "dimenCount": 0,
                "idCount": 2,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 0,
                "dimenCount": 0,
                "idCount": 2,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 1,
                "dimenCount": 0,
                "idCount": 2,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 2,
                "dimenCount": 1,
                "idCount": 2,
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
          200,
          300
        ]
      },
      "assets": {
        "png": [
          400,
          500
        ],
        "stl": [
          600,
          700
        ]
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
                                    "animator": [],
                                    "anim": [],
                                    "color": [],
                                    "drawable": [
                                      {
                                        "qualifiers": "",
                                        "extension": ".xml",
                                        "quantity": 1,
                                        "fileSizes": [
                                          64
                                        ]
                                      },
                                      {
                                        "qualifiers": "v24",
                                        "extension": ".xml",
                                        "quantity": 1,
                                        "fileSizes": [
                                          128
                                        ]
                                      }
                                    ],
                                    "font": [],
                                    "layout": [
                                      {
                                        "qualifiers": "",
                                        "extension": ".xml",
                                        "quantity": 4
                                      }
                                    ],
                                    "menu": [
                                      {
                                        "qualifiers": "",
                                        "extension": ".xml",
                                        "quantity": 1
                                      }
                                    ],
                                    "mipmap": [
                                      {
                                        "qualifiers": "anydpi-v26",
                                        "extension": ".xml",
                                        "quantity": 2,
                                        "fileSizes": [
                                          256,
                                          512
                                        ]
                                      },
                                      {
                                        "qualifiers": "hdpi",
                                        "extension": ".webp",
                                        "quantity": 2,
                                        "fileSizes": [
                                          1024,
                                          2048
                                        ]
                                      },
                                      {
                                        "qualifiers": "mdpi",
                                        "extension": ".webp",
                                        "quantity": 2,
                                        "fileSizes": [
                                          4096,
                                          8192
                                        ]
                                      },
                                      {
                                        "qualifiers": "xhdpi",
                                        "extension": ".webp",
                                        "quantity": 2,
                                        "fileSizes": [
                                          16384,
                                          32768
                                        ]
                                      },
                                      {
                                        "qualifiers": "xxhdpi",
                                        "extension": ".webp",
                                        "quantity": 2,
                                        "fileSizes": [
                                          65536,
                                          131072
                                        ]
                                      },
                                      {
                                        "qualifiers": "xxxhdpi",
                                        "extension": ".webp",
                                        "quantity": 2,
                                        "fileSizes": [
                                          262144,
                                          524288
                                        ]
                                      }
                                    ],
                                    "raw": [],
                                    "transition": [],
                                    "values": [
                                      {
                                        "qualifiers": "",
                                        "extension": ".xml",
                                        "quantity": 4,
                                        "valuesFileList": [
                                          {
                                            "stringCount": 5,
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
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 0,
                                            "dimenCount": 0,
                                            "idCount": 0,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          },
                                          {
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 1,
                                            "dimenCount": 0,
                                            "idCount": 0,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          },
                                          {
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 2,
                                            "dimenCount": 1,
                                            "idCount": 0,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          }
                                        ]
                                      },
                                      {
                                        "qualifiers": "night",
                                        "extension": ".xml",
                                        "quantity": 4,
                                        "valuesFileList": [
                                          {
                                            "stringCount": 5,
                                            "intCount": 0,
                                            "boolCount": 0,
                                            "colorCount": 0,
                                            "dimenCount": 0,
                                            "idCount": 2,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          },
                                          {
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 0,
                                            "dimenCount": 0,
                                            "idCount": 2,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          },
                                          {
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 1,
                                            "dimenCount": 0,
                                            "idCount": 2,
                                            "integerArrayCount": [],
                                            "arrayCount": [],
                                            "styleCount": []
                                          },
                                          {
                                            "stringCount": 2,
                                            "intCount": 6,
                                            "boolCount": 0,
                                            "colorCount": 2,
                                            "dimenCount": 1,
                                            "idCount": 2,
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
                                      200,
                                      300
                                    ]
                                  },
                                  "assets": {
                                    "png": [
                                      400,
                                      500
                                    ],
                                    "stl": [
                                      600,
                                      700
                                    ]
                                  }
                                }
                        """.trimIndent()
        )
    }

    @Test
    fun runOutput() {
        GradleRunner(generateWithStructure(TEST_STRUCTURE)).runTasks("projects")
    }
}