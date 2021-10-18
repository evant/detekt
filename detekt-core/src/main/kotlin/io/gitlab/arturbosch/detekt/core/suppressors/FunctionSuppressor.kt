package io.gitlab.arturbosch.detekt.core.suppressors

import io.gitlab.arturbosch.detekt.api.ConfigAware
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/*
 * Possible improvement: don't only check name but also check for parameters.
 * ```yaml
 * ignoreFunctions:
 *   - 'toString()' # only functions called toString without parameter
 *   - 'compare(String)' # only functions called compare with one parameter of type String
 *   - 'equals' # every function called function (it doesn't matter it's parameters is ignored)
 * ```
 * This would not be a breaking change.
 */
internal fun functionSuppressorFactory(rule: ConfigAware): Suppressor? {
    val names = rule.valueOrDefault("ignoreFunction", emptyList<String>())
    return if (names.isNotEmpty()) {
        { finding ->
            val element = finding.entity.ktElement
            element != null && functionSuppressor(element, names)
        }
    } else {
        null
    }
}

private fun functionSuppressor(element: KtElement, names: List<String>): Boolean {
    return element.isInFunctionNamed(names)
}

private fun KtElement.isInFunctionNamed(names: List<String>): Boolean {
    return if (this is KtNamedFunction && name in names) {
        true
    } else {
        getStrictParentOfType<KtNamedFunction>()?.isInFunctionNamed(names) ?: false
    }
}
