package io.eugenethedev.taigamobile.ui.components.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.*
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox
import io.eugenethedev.taigamobile.ui.components.texts.TitleWithIndicators
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.NavigateToTask
import io.eugenethedev.taigamobile.ui.utils.safeParseHexColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Single task item
 */
@Composable
fun CommonTaskItem(
    commonTask: CommonTask,
    horizontalPadding: Dp = mainHorizontalScreenPadding,
    verticalPadding: Dp = 8.dp,
    showExtendedInfo: Boolean = false,
    navigateToTask: NavigateToTask = { _, _, _ -> }
) = ContainerBox(
    horizontalPadding, verticalPadding,
    onClick = { navigateToTask(commonTask.id, commonTask.taskType, commonTask.ref) }
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showExtendedInfo) {
            Text(commonTask.projectInfo.name)

            Text(
                text = stringResource(
                    when (commonTask.taskType) {
                        CommonTaskType.UserStory -> R.string.userstory
                        CommonTaskType.Task -> R.string.task
                        CommonTaskType.Epic -> R.string.epic
                        CommonTaskType.Issue -> R.string.issue
                    }
                ).uppercase(),
                color = MaterialTheme.colors.primaryVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = commonTask.status.name,
                color = safeParseHexColor(commonTask.status.color),
                style = MaterialTheme.typography.body2
            )

            Text(
                text = commonTask.createdDate.format(dateTimeFormatter),
                color = Color.Gray,
                style = MaterialTheme.typography.body2
            )
        }

        TitleWithIndicators(
            ref = commonTask.ref,
            title = commonTask.title,
            indicatorColorsHex = commonTask.colors,
            isInactive = commonTask.isClosed,
            tags = commonTask.tags
        )

        Text(
            text = commonTask.assignee?.fullName?.let { stringResource(R.string.assignee_pattern)
                .format(it) } ?: stringResource(R.string.unassigned),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTaskItemPreview() = TaigaMobileTheme {
    CommonTaskItem(
        CommonTask(
            id = 0L,
            createdDate = LocalDateTime.now(),
            title = "Very cool story",
            ref = 100,
            status = Status(
                id = 0L,
                name = "In progress",
                color = "#729fcf",
                type = StatusType.Status
            ),
            assignee = null,
            projectInfo = Project(0, "Name", "slug"),
            taskType = CommonTaskType.UserStory,
            isClosed = false
        ),
        showExtendedInfo = true
    )
}
