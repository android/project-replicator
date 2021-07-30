package com.android.gradle.replicator

import com.android.gradle.replicator.model.internal.resources.*
import com.android.utils.XmlUtils
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

// Method to create the correct data class. Need to make this so it scans the files appropriately
fun processResourceFiles(resourceType: String, qualifiers: String, extension: String, resourceFiles: Set<File>):
        AbstractAndroidResourceProperties {

    val ret =  when(resourceType) {
        "values" -> getValuesResourceData(qualifiers, extension, resourceFiles)
        "drawable",
        "mipmap",
        "raw" -> getSizeMattersResourceData(qualifiers, extension, resourceFiles)
        else -> getDefaultResourceData(qualifiers, extension, resourceFiles)
    }

    return ret
}

// Methods to parse specific resource types
// Parses values xml files and counts each type of value
fun getValuesResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>): AndroidValuesResourceProperties {
    return AndroidValuesResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size,
            valuesMapPerFile = mutableListOf<ValuesMap>().apply {
                resourceFiles.forEach {
                    this.add(parseValuesFile(it))
                }
            }
    )
}

fun getSizeMattersResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>):
        AndroidSizeMattersResourceProperties {
    return AndroidSizeMattersResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size,
            fileSizes = mutableListOf<Long>().apply {
                resourceFiles.forEach {
                    this.add(it.length())
                }
            }
    )
}

fun getDefaultResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>): AbstractAndroidResourceProperties {
    return AndroidDefaultResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size
    )
}


fun parseValuesFile(valuesFile: File): ValuesMap {

    val valuesMap = ValuesMap()

    val handler: DefaultHandler = object : DefaultHandler() {
        private var myDepth = 0
        private var myLocator: Locator? = null
        private var currentItemCount = 0
        override fun setDocumentLocator(locator: Locator) {
            myLocator = locator
        }
        override fun startElement(
            uri: String, localName: String, qName: String, attributes: Attributes
        ) {
            myDepth++

            if (myDepth == 3 && qName == "item") {
                currentItemCount++
            }

            if (myDepth == 2) {
                when (qName) {
                    "string" -> { valuesMap.stringCount++ }
                    "int" -> { valuesMap.intCount++ }
                    "bool" -> { valuesMap.boolCount++  }
                    "color" -> { valuesMap.colorCount++  }
                    "dimen" -> { valuesMap.dimenCount++  }
                    "item" -> { if (attributes.getValue("name") == "id") { valuesMap.idCount++ } }
                    "integer-array" -> {} // Only add on close
                    "array" -> {} // Only add on close
                    "style" -> {} // Only add on close
                    else -> {} // Ignore
                }
            }
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            if (myDepth == 2) {
                when (qName) {
                    "integer-array" -> {
                        valuesMap.integerArrayCount.add(currentItemCount)
                    }
                    "array" -> {
                        valuesMap.arrayCount.add(currentItemCount)
                    }
                    "style" -> {
                        valuesMap.styleCount.add(currentItemCount)
                    }
                    else -> { }
                }
                currentItemCount = 0
            }
            myDepth--
        }
    }

    val factory = SAXParserFactory.newInstance()
    // Parse the input
    XmlUtils.configureSaxFactory(factory, false, false)
    val saxParser = XmlUtils.createSaxParser(factory)
    saxParser.parse(InputSource(StringReader(valuesFile.readText())), handler)

    return valuesMap
}