package com.android.gradle.replicator.resgen

import com.google.common.truth.Truth
import org.junit.Test

class ResourceGenerationIntegrationTest: AbstractResourceGenerationTest() {
    @Test
    fun testFullResourceGeneration() {
        val modelFile = testFolder.newFile("resource-metadata.json")
        val propertiesFile = testFolder.newFile("generation.properties")
        val androidOutputFolder = testFolder.newFolder("res")
        val javaOutputFolder = testFolder.newFolder("resources")
        val assetOutputFolder = testFolder.newFolder("assets")

        modelFile.writeText(MODEL_FILE)
        propertiesFile.writeText(PROPERTIES_FILE)

        Main().process(arrayOf(
            "--resJson", modelFile.absolutePath,
            "--androidOutput", androidOutputFolder.absolutePath,
            "--javaOutput", javaOutputFolder.absolutePath,
            "--assetOutput", assetOutputFolder.absolutePath,
            "--generationProperties", propertiesFile.absolutePath
            // Use default seed
        ))
        val allResourceFiles = mutableSetOf<String>()
        androidOutputFolder.walkTopDown().forEach {
            if (it != androidOutputFolder) {
                allResourceFiles.add(it.toRelativeString(androidOutputFolder))
            }
        }

        Truth.assertThat(allResourceFiles).containsExactly(
            "mipmap-mdpi",
            "mipmap-mdpi/image_aaa.webp",
            "mipmap-mdpi/image_aab.webp",
            "drawable-v24",
            "drawable-v24/xml_aaa.xml",
            "mipmap-hdpi",
            "mipmap-hdpi/image_aaa.webp",
            "mipmap-hdpi/image_aab.webp",
            "drawable",
            "drawable/xml_aab.xml",
            "drawable/xml_aac.xml",
            "drawable/xml_aaa.xml",
            "drawable/xml_aad.xml",
            "drawable/xml_aae.xml",
            "mipmap-xxxhdpi",
            "mipmap-xxxhdpi/image_aaa.webp",
            "mipmap-xxxhdpi/image_aab.webp",
            "layout",
            "mipmap-xxhdpi",
            "mipmap-xxhdpi/image_aaa.webp",
            "mipmap-xxhdpi/image_aab.webp",
            "values-night",
            "values-night/values_aaa.xml",
            "values",
            "values/values_aaa.xml",
            "values/values_aab.xml",
            "values/values_aac.xml",
            "values/values_aad.xml",
            "values/values_aae.xml",
            "menu",
            "mipmap-xhdpi",
            "mipmap-xhdpi/image_aaa.webp",
            "mipmap-xhdpi/image_aab.webp",
            "mipmap-anydpi-v26",
            "mipmap-anydpi-v26/xml_aab.xml",
            "mipmap-anydpi-v26/xml_aaa.xml"
        )
    }
}

private const val MODEL_FILE = """
{
  "androidResources": {
    "animator": [],
    "anim": [],
    "color": [],
    "drawable": [
      {
        "qualifiers": "",
        "extension": "xml",
        "quantity": 5,
        "fileData": [
          320,
          1320,
          2293,
          5606,
          5606
        ]
      },
      {
        "qualifiers": "v24",
        "extension": "xml",
        "quantity": 1,
        "fileData": [
          1702
        ]
      }
    ],
    "font": [],
    "layout": [
      {
        "qualifiers": "",
        "extension": "xml",
        "quantity": 4,
        "fileData": [
          1234,
          2345,
          3456,
          4567
        ]
      }
    ],
    "menu": [
      {
        "qualifiers": "",
        "extension": "xml",
        "quantity": 1,
        "fileData": [
          9876
        ]
      }
    ],
    "mipmap": [
      {
        "qualifiers": "anydpi-v26",
        "extension": "xml",
        "quantity": 2,
        "fileData": [
          272,
          272
        ]
      },
      {
        "qualifiers": "hdpi",
        "extension": "webp",
        "quantity": 2,
        "fileData": [
          1404,
          2898
        ]
      },
      {
        "qualifiers": "hdpi",
        "extension": "nonstandard_format",
        "quantity": 1,
        "fileData": [
          28
        ]
      },
      {
        "qualifiers": "mdpi",
        "extension": "webp",
        "quantity": 2,
        "fileData": [
          982,
          1772
        ]
      },
      {
        "qualifiers": "xhdpi",
        "extension": "webp",
        "quantity": 2,
        "fileData": [
          1900,
          3918
        ]
      },
      {
        "qualifiers": "xxhdpi",
        "extension": "webp",
        "quantity": 2,
        "fileData": [
          2884,
          5914
        ]
      },
      {
        "qualifiers": "xxxhdpi",
        "extension": "webp",
        "quantity": 2,
        "fileData": [
          3844,
          7778
        ]
      }
    ],
    "raw": [],
    "transition": [],
    "values": [
      {
        "qualifiers": "",
        "extension": "xml",
        "quantity": 5,
        "valuesFileList": [
          {
            "stringCount": 0,
            "intCount": 0,
            "boolCount": 0,
            "colorCount": 7,
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
            "dimenCount": 1,
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
            "styleCount": [
              7,
              2,
              0,
              0
            ]
          },
          {
            "stringCount": 8,
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
            "styleCount": [
              7
            ]
          }
        ]
      }
    ],
    "xml": []
  },
  "javaResources": {
    "json": [
      2
    ],
    "txt": [
      51
    ]
  },
  "assets": {
    "jpg": [
      1837065,
      174265
    ],
    "obj": [
      1948
    ],
    "stl": [
      3884
    ]
  }
}
"""

private const val PROPERTIES_FILE = """
# Vector image generation
maxVectorImageSizeSmall=50
maxVectorImageSizeMedium=75
maxVectorImageSizeLarge=100
maxVectorImageLinesSmall=25
maxVectorImageLinesMedium=25
maxVectorImageLinesLarge=50

# Values generation
maxValues=10
maxArrayElements=5
maxStringWordCount=10
maxDimension=1024
"""