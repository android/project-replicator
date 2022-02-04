package com.android.gradle.replicator.resgen.util

import com.android.gradle.replicator.resourceModel.ResourceModel
import com.android.gradle.replicator.resourceModel.ResourceTypes
import kotlin.random.Random

// Class for generating style items
class StyleItemGenerator(private val resourceModel: ResourceModel, private val constants: ResgenConstants) {
    enum class StyleItemType {
        COLOR,
        DIMEN,
        DRAWABLE,
        TEXT_APPEARANCE,
        SHAPE,
        BUTTON,
        FLOAT
    }

    data class StyleItem (val name: String, val type: StyleItemType, var value: String = "")

    // Candidates for style items
    private val styleList = mutableListOf(
        StyleItem("colorPrimary", StyleItemType.COLOR),
        StyleItem("colorSecondary", StyleItemType.COLOR),
        StyleItem("colorOnPrimary", StyleItemType.COLOR),
        StyleItem("colorOnSecondary", StyleItemType.COLOR),
        StyleItem("colorOnSurface", StyleItemType.COLOR),
        StyleItem("colorPrimaryVariant", StyleItemType.COLOR),
        StyleItem("colorSecondaryVariant", StyleItemType.COLOR),
        StyleItem("colorSurface", StyleItemType.COLOR),
        StyleItem("android:colorBackground", StyleItemType.COLOR),
        StyleItem("colorPrimarySurface", StyleItemType.COLOR),
        StyleItem("colorError", StyleItemType.COLOR),
        StyleItem("colorControlNormal", StyleItemType.COLOR),
        StyleItem("colorControlActivated", StyleItemType.COLOR),
        StyleItem("colorControlHighlight", StyleItemType.COLOR),
        StyleItem("android:textColorPrimary", StyleItemType.COLOR),
        StyleItem("android:textColorSecondary", StyleItemType.COLOR),

        StyleItem("listPreferredItemHeight", StyleItemType.DIMEN),
        StyleItem("actionBarSize", StyleItemType.DIMEN),

        StyleItem("selectableItemBackground", StyleItemType.DRAWABLE),
        StyleItem("selectableItemBackgroundBorderless", StyleItemType.DRAWABLE),
        StyleItem("dividerVertical", StyleItemType.DRAWABLE),
        StyleItem("dividerHorizontal", StyleItemType.DRAWABLE),

        /* not supported yet
        StyleItem("textAppearanceHeadline1", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceHeadline2", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceHeadline3", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceHeadline4", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceHeadline5", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceHeadline6", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceSubtitle1", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceSubtitle2", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceBody1", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceBody2", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceCaption", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceButton", StyleItemType.TEXT_APPEARANCE),
        StyleItem("textAppearanceOverline", StyleItemType.TEXT_APPEARANCE),

        StyleItem("shapeAppearanceSmallComponent", StyleItemType.SHAPE),
        StyleItem("shapeAppearanceMediumComponent", StyleItemType.SHAPE),
        StyleItem("shapeAppearanceLargeComponent", StyleItemType.SHAPE),

        StyleItem("materialButtonStyle", StyleItemType.BUTTON),
        StyleItem("borderlessButtonStyle", StyleItemType.BUTTON),
        StyleItem("materialButtonOutlinedStyle", StyleItemType.BUTTON),
         */

        StyleItem("android:disabledAlpha", StyleItemType.FLOAT),
        StyleItem("android:primaryContentAlpha", StyleItemType.FLOAT),
        StyleItem("android:secondaryContentAlpha", StyleItemType.FLOAT)
    )

    fun generateStyleItem(random: Random): StyleItem? {
        if (styleList.isEmpty()) return null // no more items to add

        // Choose item and remove it from candidates
        val choice = random.nextInt(styleList.size)
        val item = styleList[choice]
        styleList.removeAt(choice)

        val isReference = random.nextInt(10) in 0..4 // 50% chance to be a reference to another resource
        when (item.type) {
            StyleItemType.COLOR ->
                item.value = getReferenceOrGenerate(
                    random = random,
                    isReference = isReference,
                    resType = ResourceTypes.VALUES_COLOR,
                    generator = { genColor(constants, random) }
                )
            StyleItemType.DIMEN ->
                item.value = getReferenceOrGenerate(
                    random = random,
                    isReference = isReference,
                    resType = ResourceTypes.VALUES_DIMEN,
                    generator = { genDimen(constants, random) }
                )
            StyleItemType.DRAWABLE ->
                item.value = getReferenceOrGenerate(
                    random = random,
                    isReference = true, // always a reference
                    resType = ResourceTypes.VALUES_DIMEN,
                    generator = { "@android:drawable/btn_default" } // always a reference
                )
            StyleItemType.TEXT_APPEARANCE ->
                return null // not yet supported
            StyleItemType.SHAPE ->
                return null // not yet supported
            StyleItemType.BUTTON ->
                return null // not yet supported
            StyleItemType.FLOAT ->
                item.value = getReferenceOrGenerate(
                    random = random,
                    isReference = false, // never a reference
                    resType = ResourceTypes.VALUES_DIMEN,
                    generator = { genFloat(random) }
                )
        }
        return item
    }

    // Either get a reference or fall back to the generator to create a raw value
    private fun getReferenceOrGenerate(random: Random, isReference: Boolean, resType: ResourceTypes, generator: (Random) -> String): String {
        return if (isReference) {
            val res = genResourceOfType(random, resType, resourceModel)
            // res = null means no valid references
            res?.reference ?: generator(random)
        } else {
            generator(random)
        }
    }
}