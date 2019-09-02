package de.otto.edison.togglz

import org.togglz.core.annotation.EnabledByDefault
import org.togglz.core.annotation.Label

object EnumAnnotations {

    fun getLabel(featureEnum: Enum<*>) = getAnnotation(featureEnum, Label::class.java)?.value ?: featureEnum.name

    fun isEnabledByDefault(featureEnum: Enum<*>) = getAnnotation(featureEnum, EnabledByDefault::class.java) != null

    fun <A : Annotation> getAnnotation(featureEnum: Enum<*>, annotationClass: Class<A>): A? =
            featureEnum.javaClass.getField(featureEnum.name).getAnnotation(annotationClass)
                    ?: featureEnum.javaClass.getAnnotation(annotationClass)

    fun getAnnotations(featureEnum: Enum<*>) = featureEnum::name.annotations.union(featureEnum::class.annotations)
}