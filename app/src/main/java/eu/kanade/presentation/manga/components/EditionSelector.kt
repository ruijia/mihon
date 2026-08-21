package eu.kanade.presentation.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.domain.chapter.model.MangaEdition
import tachiyomi.presentation.core.components.material.padding

/**
 * Fork addition: edition switcher for self-hosted Acg-Hub manga (章节 / 单行本 / 番外).
 *
 * Renders nothing unless the manga actually has more than one edition, so every
 * other source — and self-hosted manga that only ever got raw chapters — looks
 * exactly like upstream. Switching only re-filters the chapter list that is
 * already in the db; see [MangaEdition] for why it must not refetch.
 */
@Composable
fun EditionSelector(
    editions: Set<MangaEdition>,
    selected: MangaEdition?,
    onSelect: (MangaEdition) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (editions.size < 2) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        editions.forEach { edition ->
            FilterChip(
                selected = edition == selected,
                onClick = { onSelect(edition) },
                label = { Text(edition.label) },
            )
        }
    }
}
