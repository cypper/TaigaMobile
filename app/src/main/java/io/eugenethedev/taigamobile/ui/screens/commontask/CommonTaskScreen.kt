package io.eugenethedev.taigamobile.ui.screens.commontask

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.vanpra.composematerialdialogs.color.ColorPalette
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.*
import io.eugenethedev.taigamobile.ui.commons.ResultStatus
import io.eugenethedev.taigamobile.ui.components.*
import io.eugenethedev.taigamobile.ui.components.appbars.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.components.buttons.AddButton
import io.eugenethedev.taigamobile.ui.components.editors.TaskEditor
import io.eugenethedev.taigamobile.ui.components.editors.TextFieldWithHint
import io.eugenethedev.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import io.eugenethedev.taigamobile.ui.components.lists.UserItem
import io.eugenethedev.taigamobile.ui.components.loaders.CircularLoader
import io.eugenethedev.taigamobile.ui.components.loaders.DotsLoader
import io.eugenethedev.taigamobile.ui.components.loaders.LoadingDialog
import io.eugenethedev.taigamobile.ui.components.pickers.ColorPicker
import io.eugenethedev.taigamobile.ui.components.texts.MarkdownText
import io.eugenethedev.taigamobile.ui.components.texts.NothingToSeeHereText
import io.eugenethedev.taigamobile.ui.components.texts.SectionTitle
import io.eugenethedev.taigamobile.ui.components.texts.TitleWithIndicators
import io.eugenethedev.taigamobile.ui.screens.main.FilePicker
import io.eugenethedev.taigamobile.ui.screens.main.LocalFilePicker
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.*
import java.time.LocalDateTime

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun CommonTaskScreen(
    navController: NavController,
    commonTaskId: Long,
    commonTaskType: CommonTaskType,
    ref: Int,
    onError: @Composable (message: Int) -> Unit = {},
) {
    val viewModel: CommonTaskViewModel = viewModel()
    remember {
        viewModel.start(commonTaskId, commonTaskType)
        null
    }

    val commonTask by viewModel.commonTask.observeAsState()
    commonTask?.subscribeOnError(onError)

    val creator by viewModel.creator.observeAsState()
    creator?.subscribeOnError(onError)

    val assignees by viewModel.assignees.observeAsState()
    assignees?.subscribeOnError(onError)

    val watchers by viewModel.watchers.observeAsState()
    watchers?.subscribeOnError(onError)

    val userStories by viewModel.userStories.observeAsState()
    userStories?.subscribeOnError(onError)

    val tasks by viewModel.tasks.observeAsState()
    tasks?.subscribeOnError(onError)

    val comments by viewModel.comments.observeAsState()
    comments?.subscribeOnError(onError)

    val statuses by viewModel.statuses.observeAsState()
    statuses?.subscribeOnError(onError)
    val statusSelectResult by viewModel.statusSelectResult.observeAsState()
    statusSelectResult?.subscribeOnError(onError)

    val sprints by viewModel.sprints.observeAsState()
    sprints?.subscribeOnError(onError)

    val epics by viewModel.epics.observeAsState()
    epics?.subscribeOnError(onError)

    val team by viewModel.team.observeAsState()
    team?.subscribeOnError(onError)

    val customFields by viewModel.customFields.observeAsState()
    customFields?.subscribeOnError(onError)

    val attachments by viewModel.attachments.observeAsState()
    attachments?.subscribeOnError(onError)

    val tags by viewModel.tags.observeAsState()
    tags?.subscribeOnError(onError)

    val editResult by viewModel.editResult.observeAsState()
    editResult?.subscribeOnError(onError)

    val deleteResult by viewModel.deleteResult.observeAsState()
    deleteResult?.subscribeOnError(onError)
    deleteResult?.takeIf { it.resultStatus == ResultStatus.Success }?.let {
        navController.popBackStack()
    }

    val promoteResult by viewModel.promoteResult.observeAsState()
    promoteResult?.subscribeOnError(onError)
    promoteResult?.takeIf { it.resultStatus == ResultStatus.Success }?.data?.let {
        navController.popBackStack()
        navController.navigateToTaskScreen(it.id, CommonTaskType.UserStory, it.ref)
    }
    
    fun makeEditStatusAction(statusType: StatusType) = EditAction(
        items = statuses?.data.orEmpty(),
        isItemsLoading = statuses?.resultStatus == ResultStatus.Loading,
        selectItem = viewModel::selectStatus,
        isResultLoading = statusSelectResult?.let { it.data == statusType && it.resultStatus == ResultStatus.Loading } ?: false
    )

    commonTask?.data.let {
        CommonTaskScreenContent(
            commonTaskType = commonTaskType,
            toolbarTitle = stringResource(
                when (commonTaskType) {
                    CommonTaskType.UserStory -> R.string.userstory_slug
                    CommonTaskType.Task -> R.string.task_slug
                    CommonTaskType.Epic -> R.string.epic_slug
                    CommonTaskType.Issue -> R.string.issue_slug
                }
            ).format(ref),
            epicColor = it?.color ?: "#000000",
            status = it?.status,
            type = it?.type,
            severity = it?.severity,
            priority = it?.priority,
            sprintName = it?.sprint?.name,
            title = it?.title ?: "",
            isClosed = it?.isClosed ?: false,
            story = it?.userStoryShortInfo,
            epics = it?.epicsShortInfo.orEmpty(),
            description = it?.description ?: "",
            creationDateTime = it?.createdDateTime ?: LocalDateTime.now(),
            creator = creator?.data,
            tags = it?.tags.orEmpty(),
            customFields = customFields?.data?.fields.orEmpty(),
            attachments = attachments?.data.orEmpty(),
            assignees = assignees?.data.orEmpty(),
            watchers = watchers?.data.orEmpty(),
            userStories = userStories?.data.orEmpty(),
            tasks = tasks?.data.orEmpty(),
            comments = comments?.data.orEmpty(),
            isLoading = commonTask?.resultStatus == ResultStatus.Loading,
            navigateBack = navController::popBackStack,
            navigateToCreateTask = { navController.navigateToCreateTaskScreen(CommonTaskType.Task, commonTaskId) },
            navigateToTask = navController::navigateToTaskScreen,
            editStatus = makeEditStatusAction(StatusType.Status),
            editType = makeEditStatusAction(StatusType.Type),
            editSeverity = makeEditStatusAction(StatusType.Severity),
            editPriority = makeEditStatusAction(StatusType.Priority),
            loadStatuses = { viewModel.loadStatuses(it) },
            editSprint = EditAction(
                items = sprints?.data.orEmpty(),
                loadItems = viewModel::loadSprints,
                isItemsLoading = sprints?.resultStatus == ResultStatus.Loading,
                selectItem = viewModel::selectSprint,
                isResultLoading = sprints?.resultStatus == ResultStatus.Loading
            ),
            editEpics = EditAction(
                items = epics?.data.orEmpty(),
                loadItems = viewModel::loadEpics,
                isItemsLoading = epics?.resultStatus == ResultStatus.Loading,
                selectItem = viewModel::linkToEpic,
                isResultLoading = epics?.resultStatus == ResultStatus.Loading,
                removeItem = {
                    // Since epic structure in CommonTaskExtended differs from what is used in edit there is separate lambda
                }
            ),
            unlinkFromEpic = viewModel::unlinkFromEpic,
            editAttachments = EditAttachmentsAction(
                deleteAttachment = viewModel::deleteAttachment,
                addAttachment = viewModel::addAttachment,
                isResultLoading = attachments?.resultStatus == ResultStatus.Loading
            ),
            editAssignees = EditAction(
                items = team?.data.orEmpty(),
                loadItems = viewModel::loadTeam,
                isItemsLoading = team?.resultStatus == ResultStatus.Loading,
                selectItem = viewModel::addAssignee,
                isResultLoading = assignees?.resultStatus == ResultStatus.Loading,
                removeItem = viewModel::removeAssignee
            ),
            editWatchers = EditAction(
                items = team?.data.orEmpty(),
                loadItems = viewModel::loadTeam,
                isItemsLoading = team?.resultStatus == ResultStatus.Loading,
                selectItem = viewModel::addWatcher,
                isResultLoading = watchers?.resultStatus == ResultStatus.Loading,
                removeItem = viewModel::removeWatcher
            ),
            editComments = EditCommentsAction(
                createComment = viewModel::createComment,
                deleteComment = viewModel::deleteComment,
                isResultLoading = comments?.resultStatus == ResultStatus.Loading
            ),
            editTask = viewModel::editTask,
            deleteTask = viewModel::deleteTask,
            isEditLoading = editResult?.resultStatus == ResultStatus.Loading,
            isDeleteLoading = deleteResult?.resultStatus == ResultStatus.Loading,
            promoteTask = viewModel::promoteToUserStory,
            isPromoteLoading = promoteResult?.resultStatus == ResultStatus.Loading,
            isCustomFieldsLoading = customFields?.resultStatus == ResultStatus.Loading,
            editCustomField = viewModel::editCustomField,
            editTags = EditAction(
                items = tags?.data.orEmpty(),
                loadItems = viewModel::loadTags,
                selectItem = viewModel::addTag,
                removeItem = viewModel::deleteTag,
                isResultLoading = tags?.resultStatus == ResultStatus.Loading
            )
        )
    }

}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun CommonTaskScreenContent(
    commonTaskType: CommonTaskType,
    toolbarTitle: String,
    epicColor: String,
    status: Status?,
    sprintName: String?,
    title: String,
    isClosed: Boolean,
    type: Status? = null,
    severity: Status? = null,
    priority: Status? = null,
    epics: List<EpicShortInfo> = emptyList(),
    story: UserStoryShortInfo?,
    description: String,
    creationDateTime: LocalDateTime,
    creator: User?,
    tags: List<Tag> = emptyList(),
    customFields: List<CustomField> = emptyList(),
    attachments: List<Attachment> = emptyList(),
    assignees: List<User> = emptyList(),
    watchers: List<User> = emptyList(),
    userStories: List<CommonTask> = emptyList(),
    tasks: List<CommonTask> = emptyList(),
    comments: List<Comment> = emptyList(),
    isLoading: Boolean = false,
    navigateBack: () -> Unit = {},
    navigateToCreateTask: () -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> },
    loadStatuses: (StatusType) -> Unit = { _ -> },
    editStatus: EditAction<Status> = EditAction(),
    editType: EditAction<Status> = EditAction(),
    editSeverity: EditAction<Status> = EditAction(),
    editPriority: EditAction<Status> = EditAction(),
    editSprint: EditAction<Sprint?> = EditAction(),
    editEpics: EditAction<CommonTask> = EditAction(),
    unlinkFromEpic: (EpicShortInfo) -> Unit = {},
    editAttachments: EditAttachmentsAction = EditAttachmentsAction(),
    editAssignees: EditAction<User> = EditAction(),
    editWatchers: EditAction<User> = EditAction(),
    editComments: EditCommentsAction = EditCommentsAction(),
    editTask: (title: String, description: String) -> Unit = { _, _ -> },
    deleteTask: () -> Unit = {},
    isEditLoading: Boolean = false,
    isDeleteLoading: Boolean = false,
    promoteTask: () -> Unit = {},
    isPromoteLoading: Boolean = false,
    editCustomField: (CustomField, CustomFieldValue?) -> Unit = { _, _ -> },
    isCustomFieldsLoading: Boolean = false,
    editTags: EditAction<Tag> = EditAction()
) = Box(Modifier.fillMaxSize()) {
    var isTaskEditorVisible by remember { mutableStateOf(false) }

    var isStatusSelectorVisible by remember { mutableStateOf(false) }
    var isTypeSelectorVisible by remember { mutableStateOf(false) }
    var isSeveritySelectorVisible by remember { mutableStateOf(false) }
    var isPrioritySelectorVisible by remember { mutableStateOf(false) }
    var isSprintSelectorVisible by remember { mutableStateOf(false) }
    var isAssigneesSelectorVisible by remember { mutableStateOf(false) }
    var isWatchersSelectorVisible by remember { mutableStateOf(false) }
    var isEpicsSelectorVisible by remember { mutableStateOf(false) }


    var customFieldsValues by remember { mutableStateOf(emptyMap<Long, CustomFieldValue?>()) }
    customFieldsValues = customFields.map { it.id to (if (it.id in customFieldsValues) customFieldsValues[it.id] else it.value) }.toMap()

    Column(Modifier.fillMaxSize()) {
        var isMenuExpanded by remember { mutableStateOf(false) }
        AppBarWithBackButton(
            title = {
                Text(
                    text = toolbarTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = {
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_options),
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    // delete alert dialog
                    var isDeleteAlertVisible by remember { mutableStateOf(false) }
                    if (isDeleteAlertVisible) {
                        ConfirmActionAlert(
                            title = stringResource(R.string.delete_task_title),
                            text = stringResource(R.string.delete_task_text),
                            onConfirm = {
                                isDeleteAlertVisible = false
                                deleteTask()
                            },
                            onDismiss = { isDeleteAlertVisible = false }
                        )
                    }

                    // promote alert dialog
                    var isPromoteAlertVisible by remember { mutableStateOf(false) }
                    if (isPromoteAlertVisible) {
                        ConfirmActionAlert(
                            title = stringResource(R.string.promote_title),
                            text = stringResource(R.string.promote_text),
                            onConfirm = {
                                isPromoteAlertVisible = false
                                promoteTask()
                            },
                            onDismiss = { isPromoteAlertVisible = false }
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        // edit
                        DropdownMenuItem(
                            onClick = {
                                isMenuExpanded = false
                                isTaskEditorVisible = true
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.edit),
                                style = MaterialTheme.typography.body1
                            )
                        }

                        // delete
                        DropdownMenuItem(
                            onClick = {
                                isMenuExpanded = false
                                isDeleteAlertVisible = true
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                style = MaterialTheme.typography.body1
                            )
                        }

                        // promote
                        if (commonTaskType == CommonTaskType.Task || commonTaskType == CommonTaskType.Issue) {
                            DropdownMenuItem(
                                onClick = {
                                    isMenuExpanded = false
                                    isPromoteAlertVisible = true
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.promote_to_user_story),
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                }
            },
            navigateBack = navigateBack
        )

        if (isLoading || creator == null || status == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularLoader()
            }
        } else {
            val sectionsPadding = 16.dp
            val badgesPadding = 8.dp

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = mainHorizontalScreenPadding)
                ) {

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // epic color
                            if (commonTaskType == CommonTaskType.Epic) {
                                Spacer(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = safeParseHexColor(epicColor),
                                            shape = MaterialTheme.shapes.small
                                        )
                                )

                                Spacer(Modifier.width(badgesPadding))
                            }

                            // status
                            ClickableBadge(
                                text = status.name,
                                colorHex = status.color,
                                onClick = {
                                    isStatusSelectorVisible = true
                                    loadStatuses(StatusType.Status)
                                },
                                isLoading = editStatus.isResultLoading
                            )

                            Spacer(Modifier.width(badgesPadding))

                            // sprint
                            if (commonTaskType != CommonTaskType.Epic) {
                                ClickableBadge(
                                    text = sprintName ?: stringResource(R.string.no_sprint),
                                    color = sprintName?.let { MaterialTheme.colors.primary }
                                        ?: Color.Gray,
                                    onClick = {
                                        isSprintSelectorVisible = true
                                        editSprint.loadItems(null)
                                    },
                                    isLoading = editSprint.isResultLoading,
                                    isClickable = commonTaskType != CommonTaskType.Task
                                )
                            }
                        }

                        if (commonTaskType == CommonTaskType.Issue) {
                            Spacer(Modifier.height(badgesPadding))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // type
                                ClickableBadge(
                                    text = type!!.name,
                                    colorHex = type.color,
                                    onClick = {
                                        isTypeSelectorVisible = true
                                        loadStatuses(StatusType.Type)
                                    },
                                    isLoading = editType.isResultLoading
                                )

                                Spacer(Modifier.width(badgesPadding))

                                // severity
                                ClickableBadge(
                                    text = severity!!.name,
                                    colorHex = severity.color,
                                    onClick = {
                                        isSeveritySelectorVisible = true
                                        loadStatuses(StatusType.Severity)
                                    },
                                    isLoading = editSeverity.isResultLoading
                                )

                                Spacer(Modifier.width(badgesPadding))

                                // priority
                                ClickableBadge(
                                    text = priority!!.name,
                                    colorHex = priority.color,
                                    onClick = {
                                        isPrioritySelectorVisible = true
                                        loadStatuses(StatusType.Priority)
                                    },
                                    isLoading = editPriority.isResultLoading
                                )
                            }
                        }


                        // title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.h5.let {
                                if (isClosed) {
                                    it.merge(
                                        SpanStyle(
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    )
                                } else {
                                    it
                                }
                            }
                        )

                        Spacer(Modifier.height(4.dp))
                    }

                    // belongs to (epics)
                    if (commonTaskType == CommonTaskType.UserStory) {
                        item {
                            Text(
                                text = stringResource(R.string.belongs_to_epics),
                                style = MaterialTheme.typography.subtitle1
                            )
                        }

                        items(epics) {
                            EpicItemWithAction(
                                epic = it,
                                onClick = { navigateToTask(it.id, CommonTaskType.Epic, it.ref) },
                                onRemoveClick = { unlinkFromEpic(it) }
                            )

                            Spacer(Modifier.height(2.dp))
                        }

                        item {
                            if (editEpics.isResultLoading) {
                                DotsLoader()
                            }

                            AddButton(
                                text = stringResource(R.string.link_to_epic),
                                onClick = {
                                    isEpicsSelectorVisible = true
                                    editEpics.loadItems(null)
                                }
                            )
                        }
                    }

                    // belongs to (story)
                    if (commonTaskType == CommonTaskType.Task) {
                        story?.let {
                            item {
                                Text(
                                    text = stringResource(R.string.belongs_to_story),
                                    style = MaterialTheme.typography.subtitle1
                                )

                                UserStoryItem(
                                    story = story,
                                    onClick = {
                                        navigateToTask(it.id, CommonTaskType.UserStory, it.ref)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(sectionsPadding))

                        // description
                        if (description.isNotEmpty()) {
                            MarkdownText(
                                text = description,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            NothingToSeeHereText()
                        }

                        Spacer(Modifier.height(sectionsPadding))

                        // tags
                        FlowRow(
                            crossAxisAlignment = FlowCrossAxisAlignment.Center,
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp
                        ) {
                            var isAddTagFieldVisible by remember { mutableStateOf(false) }

                            tags.forEach {
                                TagItem(
                                    tag = it,
                                    onRemoveClick = { editTags.removeItem(it) }
                                )
                            }

                            when {
                                editTags.isResultLoading -> CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                                isAddTagFieldVisible -> AddTagField(
                                    tags = editTags.items,
                                    onInputChange = editTags.loadItems,
                                    onSaveClick = {
                                        editTags.loadItems(null)
                                        editTags.selectItem(it)
                                    }
                                )
                                else -> AddButton(
                                    text = stringResource(R.string.add_tag),
                                    onClick = { isAddTagFieldVisible = true }
                                )
                            }
                        }

                    }

                    item {
                        Spacer(Modifier.height(sectionsPadding))

                        // created by
                        Text(
                            text = stringResource(R.string.created_by),
                            style = MaterialTheme.typography.subtitle1
                        )

                        UserItem(
                            user = creator,
                            dateTime = creationDateTime
                        )

                        Spacer(Modifier.height(sectionsPadding))

                        // assigned to
                        Text(
                            text = stringResource(R.string.assigned_to),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    itemsIndexed(assignees) { index, item ->
                        UserItemWithAction(
                            user = item,
                            onRemoveClick = { editAssignees.removeItem(item) }
                        )

                        if (index < assignees.lastIndex) {
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    // add assignee & loader
                    item {
                        if (editAssignees.isResultLoading) {
                            DotsLoader()
                        }
                        AddButton(
                            text = stringResource(R.string.add_user),
                            onClick = {
                                isAssigneesSelectorVisible = true
                                editAssignees.loadItems(null)
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(sectionsPadding))

                        // watchers
                        Text(
                            text = stringResource(R.string.watchers),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    itemsIndexed(watchers) { index, item ->
                        UserItemWithAction(
                            user = item,
                            onRemoveClick = { editWatchers.removeItem(item) }
                        )

                        if (index < watchers.lastIndex) {
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    // add watcher & loader
                    item {
                        if (editWatchers.isResultLoading) {
                            DotsLoader()
                        }

                        AddButton(
                            text = stringResource(R.string.add_user),
                            onClick = {
                                isWatchersSelectorVisible = true
                                editWatchers.loadItems(null)
                            }
                        )

                        Spacer(Modifier.height(sectionsPadding * 2))
                    }

                    if (customFields.isNotEmpty()) {

                        item {
                            SectionTitle(text = stringResource(R.string.custom_fields))
                        }

                        itemsIndexed(customFields) { index, item ->
                            CustomField(
                                customField = item,
                                value = customFieldsValues[item.id],
                                onValueChange = { customFieldsValues = customFieldsValues - item.id + Pair(item.id, it) },
                                onSaveClick = { editCustomField(item, customFieldsValues[item.id]) }
                            )

                            if (index < customFields.lastIndex) {
                                Divider(
                                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                                    color = Color.LightGray
                                )
                            }
                        }

                        item {
                            if (isCustomFieldsLoading) {
                                Spacer(Modifier.height(8.dp))
                                DotsLoader()
                            }

                            Spacer(Modifier.height(sectionsPadding * 3))
                        }
                    }

                    item {

                        // attachments
                        val filePicker = LocalFilePicker.current
                        SectionTitle(
                            text = stringResource(R.string.attachments_template).format(attachments.size),
                            onAddClick = {
                                filePicker.requestFile(editAttachments.addAttachment)
                            }
                        )
                    }

                    items(attachments) {
                        AttachmentItem(
                            attachment = it,
                            onRemoveClick = { editAttachments.deleteAttachment(it) }
                        )
                    }

                    item {
                        if (editAttachments.isResultLoading) {
                            DotsLoader()
                        }
                    }

                    // user stories
                    if (commonTaskType == CommonTaskType.Epic) {
                        SimpleTasksListWithTitle(
                            titleText = R.string.userstories,
                            topPadding = sectionsPadding * 2,
                            commonTasks = userStories,
                            navigateToTask = navigateToTask
                        )
                    }

                    // tasks
                    if (commonTaskType == CommonTaskType.UserStory) {
                        SimpleTasksListWithTitle(
                            titleText = R.string.tasks,
                            topPadding = sectionsPadding * 2,
                            commonTasks = tasks,
                            navigateToTask = navigateToTask,
                            navigateToCreateCommonTask = navigateToCreateTask
                        )
                    }

                    item {
                        // comments
                        Spacer(Modifier.height(sectionsPadding * 3))
                        SectionTitle(stringResource(R.string.comments_template).format(comments.size))
                    }

                    itemsIndexed(comments) { index, item ->
                        CommentItem(
                            comment = item,
                            onDeleteClick = { editComments.deleteComment(item) }
                        )

                        if (index < comments.lastIndex) {
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.LightGray
                            )
                        }
                    }

                    item {
                        if (editComments.isResultLoading) {
                            DotsLoader()
                        }

                        Spacer(Modifier.navigationBarsWithImePadding().height(72.dp))
                    }
                }

                CreateCommentBar(editComments.createComment)
            }
        }
    }


    // Bunch of list selectors
    Selectors(
        statusEntry = SelectorEntry(
            edit = editStatus,
            isVisible = isStatusSelectorVisible,
            hide = { isStatusSelectorVisible = false }
        ),
        typeEntry = SelectorEntry(
            edit = editType,
            isVisible = isTypeSelectorVisible,
            hide = { isTypeSelectorVisible = false }
        ),
        severityEntry = SelectorEntry(
            edit = editSeverity,
            isVisible = isSeveritySelectorVisible,
            hide = { isSeveritySelectorVisible = false }
        ),
        priorityEntry = SelectorEntry(
            edit = editPriority,
            isVisible = isPrioritySelectorVisible,
            hide = { isPrioritySelectorVisible = false }
        ),
        sprintEntry = SelectorEntry(
            edit = editSprint,
            isVisible = isSprintSelectorVisible,
            hide = { isSprintSelectorVisible = false }
        ),
        epicsEntry = SelectorEntry(
            edit = editEpics,
            isVisible = isEpicsSelectorVisible,
            hide = { isEpicsSelectorVisible = false }
        ),
        assigneesEntry = SelectorEntry(
            edit = editAssignees,
            isVisible = isAssigneesSelectorVisible,
            hide = { isAssigneesSelectorVisible = false }
        ),
        watchersEntry = SelectorEntry(
            edit = editWatchers,
            isVisible = isWatchersSelectorVisible,
            hide = { isWatchersSelectorVisible = false }
        )
    )

    // Editor
    if (isTaskEditorVisible || isEditLoading) {
        TaskEditor(
            toolbarText = stringResource(R.string.edit),
            title = title,
            description = description,
            onSaveClick = { title, description ->
                isTaskEditorVisible = false
                editTask(title, description)
            },
            navigateBack = { isTaskEditorVisible = false }
        )
    }

    if (isEditLoading || isDeleteLoading || isPromoteLoading) {
        LoadingDialog()
    }
}

@Composable
private fun EpicItemWithAction(
    epic: EpicShortInfo,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionAlert(
            title = stringResource(R.string.unlink_epic_title),
            text = stringResource(R.string.unlink_epic_text),
            onConfirm = {
                isAlertVisible = false
                onRemoveClick()
            },
            onDismiss = { isAlertVisible = false }
        )
    }

    TitleWithIndicators(
        ref = epic.ref,
        title = epic.title,
        textColor = MaterialTheme.colors.primary,
        indicatorColorsHex = listOf(epic.color),
        modifier = Modifier
            .weight(1f)
            .padding(end = 4.dp)
            .clickableUnindicated(onClick = onClick)
    )

    IconButton(
        onClick = { isAlertVisible = true },
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_remove),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
private fun TagItem(
    tag: Tag,
    onRemoveClick: () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .background(color = safeParseHexColor(tag.color), shape = MaterialTheme.shapes.small)
        .padding(horizontal = 4.dp, vertical = 2.dp)
) {
    Text(
        text = tag.name,
        color = Color.White
    )

    Spacer(Modifier.width(2.dp))

    IconButton(
        onClick = onRemoveClick,
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_remove),
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun AddTagField(
    tags: List<Tag>,
    onInputChange: (String) -> Unit,
    onSaveClick: (Tag) -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically
) {
    var value by remember { mutableStateOf(TextFieldValue()) }
    var color by remember { mutableStateOf(ColorPalette.Primary.first()) }

    Column {
        TextFieldWithHint(
            hintId = R.string.tag,
            value = value,
            onValueChange = {
                value = it
                onInputChange(it.text)
            },
            width = 180.dp,
            hasBorder = true,
            singleLine = true
        )

        DropdownMenu(
            expanded = tags.isNotEmpty(),
            onDismissRequest = {},
            properties = PopupProperties(clippingEnabled = false),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            tags.forEach {
                DropdownMenuItem(onClick = { onSaveClick(it) }) {
                    Spacer(
                        Modifier.size(22.dp)
                            .background(
                                color = safeParseHexColor(it.color),
                                shape = MaterialTheme.shapes.small
                            )
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }

    Spacer(Modifier.width(4.dp))

    ColorPicker(
        size = 32.dp,
        color = color,
        onColorPicked = { color = it }
    )

    Spacer(Modifier.width(2.dp))

    IconButton(
        onClick = {
            value.text.takeIf { it.isNotEmpty() }?.let {
                onSaveClick(Tag(it, "#%08X".format(color.toArgb()).replace("#FF", "#")))
                value = TextFieldValue()
            }
        },
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_save),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}


@Composable
private fun UserStoryItem(
    story: UserStoryShortInfo,
    onClick: () -> Unit
) = TitleWithIndicators(
    ref = story.ref,
    title = story.title,
    textColor = MaterialTheme.colors.primary,
    indicatorColorsHex = story.epicColors,
    modifier = Modifier.clickableUnindicated(onClick = onClick)
)

@Composable
private fun UserItemWithAction(
    user: User,
    onRemoveClick: () -> Unit
) {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionAlert(
            title = stringResource(R.string.remove_user_title),
            text = stringResource(R.string.remove_user_text),
            onConfirm = {
                isAlertVisible = false
                onRemoveClick()
            },
            onDismiss = { isAlertVisible = false }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        UserItem(user)

        IconButton(onClick = { isAlertVisible = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: Attachment,
    onRemoveClick: () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
) {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionAlert(
            title = stringResource(R.string.remove_attachment_title),
            text = stringResource(R.string.remove_attachment_text),
            onConfirm = {
                isAlertVisible = false
                onRemoveClick()
            },
            onDismiss = { isAlertVisible = false }
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1f, fill = false)
            .padding(end = 4.dp)
    ) {
        val activity = LocalContext.current as Activity
        Icon(
            painter = painterResource(R.drawable.ic_attachment),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.padding(end = 2.dp)
        )

        Text(
            text = attachment.name,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.clickable {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attachment.url)))
            }
        )
    }

    IconButton(
        onClick = { isAlertVisible = true },
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = null,
            tint = Color.Red
        )
    }

}

@Composable
private fun CommentItem(
    comment: Comment,
    onDeleteClick: () -> Unit
) = Column {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionAlert(
            title = stringResource(R.string.delete_comment_title),
            text = stringResource(R.string.delete_comment_text),
            onConfirm = {
                isAlertVisible = false
                onDeleteClick()
            },
            onDismiss = { isAlertVisible = false }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        UserItem(
            user = comment.author,
            dateTime = comment.postDateTime
        )

        if (comment.canDelete == true) {
            IconButton(onClick = { isAlertVisible = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }

    MarkdownText(
        text = comment.text,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CommonTaskScreenPreview() = TaigaMobileTheme {
    CompositionLocalProvider(
        LocalFilePicker provides object : FilePicker() {}
    ) {
        CommonTaskScreenContent(
            commonTaskType = CommonTaskType.UserStory,
            toolbarTitle = "Userstory #99",
            epicColor = "#000000",
            status = Status(
                id = 0,
                name = "In progress",
                color = "#729fcf",
                type = StatusType.Status
            ),
            sprintName = "Very very very long sprint name",
            title = "Very cool and important story. Need to do this quickly",
            isClosed = false,
            story = null,
            epics = List(1) {
                EpicShortInfo(
                    id = 1L,
                    title = "Important epic",
                    ref = 1,
                    color = "#F2C94C"
                )
            },
            description = "Some description about this wonderful task",
            creationDateTime = LocalDateTime.now(),
            creator = User(
                _id = 0L,
                fullName = "Full Name",
                photo = null,
                bigPhoto = null,
                username = "username"
            ),
            assignees = List(1) {
                User(
                    _id = 0L,
                    fullName = "Full Name",
                    photo = null,
                    bigPhoto = null,
                    username = "username"
                )
            },
            watchers = List(2) {
                User(
                    _id = 0L,
                    fullName = "Full Name",
                    photo = null,
                    bigPhoto = null,
                    username = "username"
                )
            },
            tasks = List(1) {
                CommonTask(
                    id = it.toLong(),
                    createdDate = LocalDateTime.now(),
                    title = "Very cool story",
                    ref = 100,
                    status = Status(
                        id = (0..2).random().toLong(),
                        name = "In progress",
                        color = "#729fcf",
                        type = StatusType.Status
                    ),
                    assignee = null,
                    projectInfo = Project(0, "", ""),
                    taskType = CommonTaskType.UserStory,
                    isClosed = false
                )
            },
            comments = List(1) {
                Comment(
                    id = "",
                    author = User(
                        _id = 0L,
                        fullName = "Full Name",
                        photo = null,
                        bigPhoto = null,
                        username = "username"
                    ),
                    text = "This is comment text",
                    postDateTime = LocalDateTime.now(),
                    deleteDate = null
                )
            }
        )
    }
}