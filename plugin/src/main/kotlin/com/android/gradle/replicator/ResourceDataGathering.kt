package com.android.gradle.replicator

import com.android.gradle.replicator.model.internal.filedata.*
import com.android.utils.XmlUtils
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

// Method to create the correct data class. Need to make this so it scans the files appropriately
fun processResourceFiles(resourceType: String, qualifiers: String, extension: String, resourceFiles: Set<File>):
        AbstractAndroidResourceProperties {

    return when (resourceType) {
        "values" -> getValuesResourceData(qualifiers, extension, resourceFiles)
        else -> getDefaultResourceData(qualifiers, extension, resourceFiles)
    }
}

// Methods to parse specific resource types
// Parses values xml files and counts each type of value
fun getValuesResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>): ValuesAndroidResourceProperties {
    return ValuesAndroidResourceProperties(
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

fun getDefaultResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>):
        DefaultAndroidResourceProperties {
    return DefaultAndroidResourceProperties(
        qualifiers = qualifiers,
        extension = extension,
        quantity = resourceFiles.size,
        fileData = mutableListOf<Long>().apply {
            // XML files want lines instead of bytes
            if (extension == "xml") {
                resourceFiles.forEach {
                    this.add(it.readLines().size.toLong())
                }
            } else {
                resourceFiles.forEach {
                    this.add(it.length())
                }
            }
        }
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
    try {
        saxParser.parse(InputSource(StringReader(valuesFile.readText())), handler)
    } catch (e: SAXParseException) {
        println("e: Invalid xml file $valuesFile")
        println("   line ${e.lineNumber}; column ${e.columnNumber}: ${e.message}")
        println("   ${valuesFile.readLines()[e.lineNumber-1]}")
        println("   ${" ".repeat(e.columnNumber-1)}↑")
        println(    "Skipping file parsing")
        return ValuesMap() // Return empty map
    }

    return valuesMap
}