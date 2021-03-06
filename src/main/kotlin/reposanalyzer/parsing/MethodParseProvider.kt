package reposanalyzer.parsing

import astminer.cli.normalizeParseResult
import astminer.common.model.Node
import astminer.common.model.Parser
import astminer.common.preOrder
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import org.eclipse.jgit.revwalk.RevCommit
import reposanalyzer.config.AnalysisConfig
import reposanalyzer.config.Granularity
import reposanalyzer.config.Language
import reposanalyzer.logic.AnalysisRepository
import reposanalyzer.logic.calculateLinesStarts
import reposanalyzer.logic.getFileLinesLength
import reposanalyzer.logic.splitToParents
import reposanalyzer.methods.MethodIdentity
import reposanalyzer.methods.MethodSummary
import reposanalyzer.methods.MethodSummaryStorage
import reposanalyzer.methods.extractors.getMethodFullName
import reposanalyzer.methods.filter.MethodSummaryFilter
import reposanalyzer.methods.normalizeAstLabel
import reposanalyzer.methods.summarizers.MethodSummarizersFactory
import reposanalyzer.utils.readFileToString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class MethodParseProvider(
    private val parsers: ConcurrentHashMap<Language, Parser<out Node>>,
    private val summaryStorage: MethodSummaryStorage,
    private val config: AnalysisConfig,
    private val analysisRepo: AnalysisRepository? = null
) {

    private val methodsFilter = MethodSummaryFilter(config.summaryFilterConfig)
    private val pathMiner = PathMiner(PathRetrievalSettings(config.maxPathLength, config.maxPathWidth))

    fun parse(
        files: List<File>,
        language: Language,
        rootPath: String,
        currCommit: RevCommit? = null
    ): Boolean {
        val parser = parsers[language] ?: return false
        val labelExtractor = getLabelExtractor()
        val summarizer = getMethodSummarizer(language)

        parser.parseFiles(files) { parseResult ->
            val fileContent = parseResult.filePath.readFileToString()
            val fileLinesStarts = parseResult.filePath.getFileLinesLength().calculateLinesStarts()
            val relativePath = parseResult.filePath
                .removePrefix(rootPath + File.separator)
                .splitToParents()
                .joinToString("/")

            normalizeParseResult(parseResult, true)
            val labeledParseResults = labelExtractor.toLabeledData(parseResult)
            labeledParseResults.forEach { (root, label) ->
                val methodName = normalizeAstLabel(label)
                val methodFullName = root.getMethodFullName(label)
                val (returnType, argsTypes) = summarizer.extractReturnTypeAndArgs(root)
                val realIdentity = MethodIdentity(methodName, methodFullName, returnType, argsTypes, relativePath)
                if (summaryStorage.contains(realIdentity)) {
                    return@forEach
                }
                val methodSummary = summarizer.summarize(
                    root,
                    label,
                    fileContent,
                    relativePath,
                    fileLinesStarts
                )
                root.preOrder().forEach { node ->
                    config.excludeNodes.forEach {
                        node.removeChildrenOfType(it)
                    }
                }
                methodSummary.paths = retrievePaths(root)
                addCommonInfo(methodSummary, currCommit)

                // methods filtering
                if (methodsFilter.isSummaryGood(methodSummary)) {
                    summaryStorage.add(methodSummary)
                }
            }
        }

        return true
    }

    private fun <T : Node> retrievePaths(root: T): List<String> {
        if (!config.isPathMining) return emptyList()
        root.preOrder().forEach { node ->
            config.excludeNodes.forEach {
                node.removeChildrenOfType(it)
            }
        }
        val paths = pathMiner.retrievePaths(root)
        val pathContexts = paths.map { toPathContextNormalizedToken(it) }
            .shuffled()
            .filter { pathContext -> pathContext.startToken.isNotEmpty() && pathContext.endToken.isNotEmpty() }
            .take(config.maxPaths)
        return pathContexts.map { pathContext ->
            val nodePath = pathContext.orientedNodeTypes.map { node -> node.typeLabel }
            "${pathContext.startToken},${nodePath.joinToString("|")},${pathContext.endToken}"
        }
    }

    private fun addCommonInfo(methodSummary: MethodSummary, currCommit: RevCommit? = null) {
        currCommit?.let {
            methodSummary.commit = it
        }
        analysisRepo?.let {
            methodSummary.repoOwner = analysisRepo.owner
            methodSummary.repoName = analysisRepo.name
            methodSummary.repoLicense = analysisRepo.licence
        }
    }

    private fun getLabelExtractor() = LabelExtractorFactory.getLabelExtractor(
        config.task, Granularity.METHOD, config.hideMethodName, config.filterPredicates
    )

    private fun getMethodSummarizer(lang: Language) =
        MethodSummarizersFactory.getMethodSummarizer(lang, config.hideMethodName)
}
