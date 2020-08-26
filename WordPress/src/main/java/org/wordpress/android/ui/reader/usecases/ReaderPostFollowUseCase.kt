package org.wordpress.android.ui.reader.usecases

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.wordpress.android.datasets.wrappers.ReaderPostTableWrapper
import org.wordpress.android.models.ReaderPost
import org.wordpress.android.ui.reader.actions.ReaderActions.ActionListener
import org.wordpress.android.ui.reader.actions.ReaderBlogActionsWrapper
import org.wordpress.android.ui.reader.usecases.ReaderPostFollowUseCase.FollowSiteState.Failed.NoNetwork
import org.wordpress.android.ui.reader.usecases.ReaderPostFollowUseCase.FollowSiteState.Failed.RequestFailed
import org.wordpress.android.ui.reader.usecases.ReaderPostFollowUseCase.FollowSiteState.ReaderPostData
import org.wordpress.android.ui.reader.usecases.ReaderPostFollowUseCase.FollowSiteState.Success
import org.wordpress.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This class handles reader post follow click events.
 */
class ReaderPostFollowUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val readerBlogActionsWrapper: ReaderBlogActionsWrapper,
    private val readerPostTableWrapper: ReaderPostTableWrapper

) {
    private var continuation: Continuation<Boolean>? = null

    suspend fun toggleFollow(post: ReaderPost) = flow<FollowSiteState> {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            emit(NoNetwork)
        } else {
            val isAskingToFollow = !readerPostTableWrapper.isPostFollowed(post)
            val showEnableNotification = !post.isFollowedByCurrentUser
            emit(ReaderPostData(post.blogId, isAskingToFollow, showEnableNotification))

            performAction(post, isAskingToFollow)
        }
    }

    private suspend fun FlowCollector<FollowSiteState>.performAction(post: ReaderPost, isAskingToFollow: Boolean) {
        val succeeded = followSiteAndWaitForResult(post, isAskingToFollow)
        if (!succeeded) {
            emit(ReaderPostData(post.blogId, !isAskingToFollow))
            emit(RequestFailed)
        } else {
            emit(Success)
        }
    }

    private suspend fun followSiteAndWaitForResult(post: ReaderPost, isAskingToFollow: Boolean): Boolean {
        val actionListener = ActionListener { succeeded ->
            continuation?.resume(succeeded)
            continuation = null
        }

        return suspendCoroutine { cont ->
            continuation = cont
            readerBlogActionsWrapper.followBlogForPost(post, isAskingToFollow, actionListener)
        }
    }

    sealed class FollowSiteState {
        data class ReaderPostData(
            val blogId: Long,
            val following: Boolean,
            val showEnableNotification: Boolean = false
        ) : FollowSiteState()

        object Success : FollowSiteState()
        sealed class Failed : FollowSiteState() {
            object NoNetwork : Failed()
            object RequestFailed : Failed()
        }
    }
}
