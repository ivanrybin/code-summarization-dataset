package reposanalyzer.methods.summarizers

import reposanalyzer.config.Language

interface MethodSummarizersFactory {

    companion object {
        fun getMethodSummarizer(
            language: Language,
            hideMethodName: Boolean,
            hiddenMethodName: String = MethodSummarizer.DEFAULT_HIDDEN_NAME
        ): MethodSummarizer {
            val methodSummarizer = when (language) {
                Language.JAVA -> JavaMethodSummarizer()
            }
            methodSummarizer.hideMethodName = hideMethodName
            methodSummarizer.hiddenMethodName = hiddenMethodName
            return methodSummarizer
        }
    }
}
