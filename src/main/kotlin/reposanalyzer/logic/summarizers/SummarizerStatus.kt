package reposanalyzer.logic.summarizers

enum class SummarizerStatus {
    NOT_INITIALIZED,
    LOADED,
    RUNNING,
    DONE,
    REPO_NOT_PRESENT,
    INTERRUPTED,
    EMPTY_HISTORY,
    SMALL_COMMITS_NUMBER,
    SMALL_MERGES_PART,
    INIT_ERROR,
    INIT_BAD_DEF_BRANCH_ERROR,
    WORK_ERROR,
    BAD_DIR,
    NO_PATHS_TO_ANALYSE
}
