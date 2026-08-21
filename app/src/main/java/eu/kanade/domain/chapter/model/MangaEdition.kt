package eu.kanade.domain.chapter.model

import tachiyomi.domain.chapter.model.Chapter

/**
 * Fork addition: the three "editions" a chapter of a self-hosted Acg-Hub manga can
 * belong to — raw chapters, published volumes, and extras (番外/特典/附录).
 *
 * Why this exists: those three are separate reading tracks. Each numbers itself 1..N
 * on the server, so mixing them in one list means duplicate numbers, a bogus
 * "missing chapters" warning, and a continue-reading button that jumps between
 * tracks. The upstream app has no notion of editions, so the fork splits the
 * already-synced chapter list client-side.
 *
 * Why client-side and not "refetch the selected edition from the server":
 * [eu.kanade.domain.chapter.interactor.SyncChaptersWithSource] deletes every db
 * chapter whose url is absent from the fetched list. A server-side switch would
 * therefore wipe the other two editions' rows — read progress, bookmarks and
 * download bookkeeping with them — on every refresh. Splitting a full list keeps
 * one Chapter row per unit, so progress and downloads are per-edition for free.
 *
 * Only chapters from the self-hosted Komga-compat source are classified; every
 * other source returns null and behaves exactly as before (same fingerprint as the
 * whole-chapter CBZ download path: a Komga-shaped `/api/v1/books/` chapter url).
 */
enum class MangaEdition(val label: String) {
    // Order matters: it is the display order of the selector and the fallback order
    // when the previously selected edition is not present.
    RAW("章节"),
    VOLUME("单行本"),
    EXTRA("番外"),
}

/** Komga-shaped chapter url — the fingerprint of the self-hosted source. */
private const val SELF_HOSTED_MARKER = "/api/v1/books/"

/** Server-side label for volumes; extras carry their own (番外/特典/附录). */
private const val VOLUME_SCANLATOR = "单行本"

/**
 * The edition this chapter belongs to, or null when the chapter does not come from
 * the self-hosted source (in which case no edition UI is shown at all).
 *
 * The server ships the edition in `scanlator` because that is the only free-text
 * field the Komga extension maps onto [Chapter]; raw chapters carry none so they
 * stay out of the native scanlator-exclusion dialog.
 */
fun Chapter.editionOrNull(): MangaEdition? {
    if (!url.contains(SELF_HOSTED_MARKER)) return null
    return when {
        scanlator.isNullOrBlank() -> MangaEdition.RAW
        scanlator == VOLUME_SCANLATOR -> MangaEdition.VOLUME
        else -> MangaEdition.EXTRA
    }
}

/**
 * Editions present in this chapter list, in enum order. Empty for every source but
 * the self-hosted one; a single-element set means there is nothing to switch between.
 */
fun List<Chapter>.availableEditions(): Set<MangaEdition> =
    mapNotNullTo(sortedSetOf()) { it.editionOrNull() }

/** The edition actually in effect: the chosen one if still present, else the first. */
fun Set<MangaEdition>.effectiveEdition(selected: MangaEdition?): MangaEdition? =
    selected?.takeIf { it in this } ?: firstOrNull()
