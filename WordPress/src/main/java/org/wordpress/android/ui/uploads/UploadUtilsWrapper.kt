package org.wordpress.android.ui.uploads

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import dagger.Reusable
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.MediaModel
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.post.PostStatus
import org.wordpress.android.fluxc.store.PostStore.PostError
import org.wordpress.android.ui.uploads.UploadActionUseCase.UploadAction
import org.wordpress.android.ui.uploads.UploadUtils.OnPublishingCallback
import org.wordpress.android.util.SnackbarSequencer
import javax.inject.Inject

/**
 * Injectable wrapper around UploadUtils.
 *
 * UploadUtils interface is consisted of static methods, which makes the client code difficult to test/mock.
 * Main purpose of this wrapper is to make testing easier.
 */
@Reusable
class UploadUtilsWrapper @Inject constructor(
    private val sequencer: SnackbarSequencer,
    private val dispatcher: Dispatcher
) {
    fun userCanPublish(site: SiteModel): Boolean {
        return UploadUtils.userCanPublish(site)
    }

    @Suppress("LongParameterList")
    fun onMediaUploadedSnackbarHandler(
        activity: Activity?,
        snackbarAttachView: View?,
        isError: Boolean,
        mediaList: List<MediaModel?>?,
        site: SiteModel?,
        messageForUser: String?
    ) = UploadUtils.onMediaUploadedSnackbarHandler(
            activity,
            snackbarAttachView,
            isError,
            mediaList,
            site,
            messageForUser,
            sequencer
    )

    @JvmOverloads
    @Suppress("LongParameterList")
    fun onPostUploadedSnackbarHandler(
        activity: Activity?,
        snackbarAttachView: View?,
        isError: Boolean,
        isFirstTimePublish: Boolean,
        post: PostModel?,
        errorMessage: String?,
        site: SiteModel?,
        onPublishingCallback: OnPublishingCallback? = null
    ) = UploadUtils.onPostUploadedSnackbarHandler(
            activity,
            snackbarAttachView,
            isError,
            isFirstTimePublish,
            post,
            errorMessage,
            site,
            dispatcher,
            sequencer,
            onPublishingCallback
    )

    @JvmOverloads
    @Suppress("LongParameterList")
    fun handleEditPostResultSnackbars(
        activity: Activity,
        snackbarAttachView: View,
        data: Intent,
        post: PostModel,
        site: SiteModel,
        uploadAction: UploadAction,
        publishPostListener: OnClickListener?,
        onPublishingCallback: OnPublishingCallback? = null
    ) = UploadUtils.handleEditPostModelResultSnackbars(
            activity,
            dispatcher,
            snackbarAttachView,
            data,
            post,
            site,
            uploadAction,
            sequencer,
            publishPostListener,
            onPublishingCallback
    )

    fun showSnackbarError(
        view: View?,
        message: String?,
        buttonTitleRes: Int,
        buttonListener: OnClickListener?
    ) = UploadUtils.showSnackbarError(view, message, buttonTitleRes, buttonListener, sequencer)

    fun showSnackbarError(
        view: View?,
        message: String?
    ) = UploadUtils.showSnackbarError(view, message, sequencer)

    fun showSnackbar(
        view: View?,
        messageRes: Int
    ) = UploadUtils.showSnackbar(view, messageRes, sequencer)

    fun showSnackbar(
        view: View?,
        messageText: String
    ) = UploadUtils.showSnackbar(view, messageText, sequencer)

    fun getErrorMessageResIdFromPostError(
        postStatus: PostStatus,
        isPage: Boolean,
        postError: PostError,
        isEligibleForAutoUpload: Boolean
    ) = UploadUtils.getErrorMessageResIdFromPostError(
            postStatus,
            isPage,
            postError,
            isEligibleForAutoUpload
    )

    fun publishPost(
        activity: Activity,
        post: PostModel,
        site: SiteModel,
        onPublishingCallback: OnPublishingCallback? = null
    ) = UploadUtils.publishPost(activity, post, site, dispatcher, onPublishingCallback)
}
