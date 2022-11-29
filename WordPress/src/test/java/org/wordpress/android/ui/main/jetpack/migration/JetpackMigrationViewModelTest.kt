package org.wordpress.android.ui.main.jetpack.migration

import kotlinx.coroutines.flow.first
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.R
import org.wordpress.android.localcontentmigration.MigrationEmailHelper
import org.wordpress.android.sharedlogin.resolver.LocalMigrationOrchestrator
import org.wordpress.android.test
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.DeletePrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.DeleteSecondaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.DonePrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.ErrorPrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.ErrorSecondaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.NotificationsPrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.WelcomePrimaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.ActionButton.WelcomeSecondaryButton
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.SiteListItemUiState
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.UiState.Content
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.UiState.Error
import org.wordpress.android.ui.main.jetpack.migration.JetpackMigrationViewModel.UiState.Loading
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.util.GravatarUtilsWrapper
import org.wordpress.android.util.SiteUtilsWrapper
import org.wordpress.android.util.config.PreventDuplicateNotifsFeatureConfig
import org.wordpress.android.viewmodel.ContextProvider

@RunWith(MockitoJUnitRunner::class)
class JetpackMigrationViewModelTest : BaseUnitTest() {
    private val siteUtilsWrapper: SiteUtilsWrapper = mock()
    private val gravatarUtilsWrapper: GravatarUtilsWrapper = mock()
    private val appPrefsWrapper: AppPrefsWrapper = mock()
    private val localMigrationOrchestrator: LocalMigrationOrchestrator = mock()
    private val migrationEmailHelper: MigrationEmailHelper = mock()
    private val preventDuplicateNotifsFeatureConfig: PreventDuplicateNotifsFeatureConfig = mock()
    private val contextProvider: ContextProvider = mock()
    private val classToTest = JetpackMigrationViewModel(
            siteUtilsWrapper = siteUtilsWrapper,
            gravatarUtilsWrapper = gravatarUtilsWrapper,
            contextProvider = contextProvider,
            preventDuplicateNotifsFeatureConfig = preventDuplicateNotifsFeatureConfig,
            appPrefsWrapper = appPrefsWrapper,
            localMigrationOrchestrator = localMigrationOrchestrator,
            migrationEmailHelper = migrationEmailHelper
    )

    // region ViewModel
    @Test
    fun `Should init Loading UiState as default`() = test {
        assertThat(classToTest.uiState.first()).isInstanceOf(Loading::class.java)
    }
    // endregion

    // region UiState Content
    @Test
    fun `Should have correct default userAvatarUrl for Welcome Content`() {
        val welcomeContent = Content.Welcome(
                sites = emptyList(),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.userAvatarUrl
        val expected = ""
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct default isProcessing for Welcome Content`() {
        val welcomeContent = Content.Welcome(
                sites = emptyList(),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.isProcessing
        val expected = false
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct screenIconRes for Welcome Content`() {
        val welcomeContent = Content.Welcome(
                sites = emptyList(),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.screenIconRes
        val expected = R.drawable.ic_wordpress_jetpack_logo
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Welcome Content`() {
        val welcomeContent = Content.Welcome(
                sites = emptyList(),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.title
        val expected = UiStringRes(R.string.jp_migration_welcome_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Welcome Content`() {
        val welcomeContent = Content.Welcome(
                sites = emptyList(),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.subtitle
        val expected = UiStringRes(R.string.jp_migration_welcome_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Welcome Content when sites size IS GREATER than 1`() {
        val welcomeContent = Content.Welcome(
                sites = listOf(
                        SiteListItemUiState(123L, "name", "url", "iconUrl"),
                        SiteListItemUiState(456L, "name", "url", "iconUrl")
                ),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.message
        val expected = UiStringRes(R.string.jp_migration_welcome_sites_found_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Welcome Content when sites size IS NOT GREATER than 1`() {
        val welcomeContent = Content.Welcome(
                sites = listOf(SiteListItemUiState(123L, "name", "url", "iconUrl")),
                primaryActionButton = WelcomePrimaryButton {},
                secondaryActionButton = WelcomeSecondaryButton {},
        )
        val actual = welcomeContent.message
        val expected = UiStringRes(R.string.jp_migration_welcome_site_found_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct screenIconRes for Notifications Content`() {
        val notificationsContent = Content.Notifications(NotificationsPrimaryButton {})
        val actual = notificationsContent.screenIconRes
        val expected = R.drawable.ic_jetpack_migration_notifications
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Notifications Content`() {
        val notificationsContent = Content.Notifications(NotificationsPrimaryButton {})
        val actual = notificationsContent.title
        val expected = UiStringRes(R.string.jp_migration_notifications_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Notifications Content`() {
        val notificationsContent = Content.Notifications(NotificationsPrimaryButton {})
        val actual = notificationsContent.subtitle
        val expected = UiStringRes(R.string.jp_migration_notifications_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Notifications Content`() {
        val notificationsContent = Content.Notifications(NotificationsPrimaryButton {})
        val actual = notificationsContent.message
        val expected = UiStringRes(R.string.jp_migration_notifications_disabled_in_wp_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct screenIconRes for Done Content`() {
        val doneContent = Content.Done(DonePrimaryButton {})
        val actual = doneContent.screenIconRes
        val expected = R.drawable.ic_jetpack_migration_success
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Done Content`() {
        val doneContent = Content.Done(DonePrimaryButton {})
        val actual = doneContent.title
        val expected = UiStringRes(R.string.jp_migration_done_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Done Content`() {
        val doneContent = Content.Done(DonePrimaryButton {})
        val actual = doneContent.subtitle
        val expected = UiStringRes(R.string.jp_migration_done_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Done Content`() {
        val doneContent = Content.Done(DonePrimaryButton {})
        val actual = doneContent.message
        val expected = UiStringRes(R.string.jp_migration_done_delete_wp_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct deleteWpIcon for Done Content`() {
        val doneContent = Content.Done(DonePrimaryButton {})
        val actual = doneContent.deleteWpIcon
        val expected = R.drawable.ic_jetpack_migration_delete_wp
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct screenIconRes for Delete Content`() {
        val deleteContent = Content.Delete(DeletePrimaryButton {}, DeleteSecondaryButton {})
        val actual = deleteContent.screenIconRes
        val expected = R.drawable.ic_jetpack_migration_delete
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Delete Content`() {
        val deleteContent = Content.Delete(DeletePrimaryButton {}, DeleteSecondaryButton {})
        val actual = deleteContent.title
        val expected = UiStringRes(R.string.jp_migration_delete_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Delete Content`() {
        val deleteContent = Content.Delete(DeletePrimaryButton {}, DeleteSecondaryButton {})
        val actual = deleteContent.subtitle
        val expected = UiStringRes(R.string.jp_migration_delete_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Delete Content`() {
        val deleteContent = Content.Delete(DeletePrimaryButton {}, DeleteSecondaryButton {})
        val actual = deleteContent.message
        val expected = UiStringRes(R.string.jp_migration_delete_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct deleteWpIcon for Delete Content`() {
        val deleteContent = Content.Delete(DeletePrimaryButton {}, DeleteSecondaryButton {})
        val actual = deleteContent.deleteWpIcon
        val expected = R.drawable.ic_jetpack_migration_delete_wp
        assertThat(actual).isEqualTo(expected)
    }
    // endregion

    // region UiState Error
    @Test
    fun `Should have correct default isProcessing for Error UiState`() {
        val uiStateError = Error(
                primaryActionButton = ErrorPrimaryButton {},
                secondaryActionButton = ErrorSecondaryButton {},
                type = Error.Networking,
        )
        val actual = uiStateError.isProcessing
        val expected = false
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct screenIconRes for Error UiState`() {
        val uiStateError = Error(
                primaryActionButton = ErrorPrimaryButton {},
                secondaryActionButton = ErrorSecondaryButton {},
                type = Error.Networking,
        )
        val actual = uiStateError.screenIconRes
        val expected = R.drawable.ic_jetpack_migration_error
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Generic ErrorType`() {
        val genericErrorType = Error.Generic
        val actual = genericErrorType.title
        val expected = UiStringRes(R.string.jp_migration_generic_error_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Generic ErrorType`() {
        val genericErrorType = Error.Generic
        val actual = genericErrorType.subtitle
        val expected = UiStringRes(R.string.jp_migration_generic_error_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Generic ErrorType`() {
        val genericErrorType = Error.Generic
        val actual = genericErrorType.message
        val expected = UiStringRes(R.string.jp_migration_generic_error_message)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct title for Networking ErrorType`() {
        val networkingErrorType = Error.Networking
        val actual = networkingErrorType.title
        val expected = UiStringRes(R.string.jp_migration_network_error_title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct subtitle for Networking ErrorType`() {
        val networkingErrorType = Error.Networking
        val actual = networkingErrorType.subtitle
        val expected = UiStringRes(R.string.jp_migration_network_error_subtitle)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct message for Networking ErrorType`() {
        val networkingErrorType = Error.Networking
        val actual = networkingErrorType.message
        val expected = UiStringRes(R.string.jp_migration_network_error_message)
        assertThat(actual).isEqualTo(expected)
    }
    // endregion

    // region ActionButton
    @Test
    fun `Should have correct text for WelcomePrimaryButton`() {
        val button = WelcomePrimaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_continue_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for WelcomeSecondaryButton`() {
        val button = WelcomeSecondaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_help_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for NotificationsPrimaryButton`() {
        val button = NotificationsPrimaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_continue_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for DonePrimaryButton`() {
        val button = DonePrimaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_finish_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for ErrorPrimaryButton`() {
        val button = ErrorPrimaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_try_again_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for ErrorSecondaryButton`() {
        val button = ErrorSecondaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_help_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for DeletePrimaryButton`() {
        val button = DeletePrimaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_got_it_button)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should have correct text for DeleteSecondaryButton`() {
        val button = DeleteSecondaryButton {}
        val actual = button.text
        val expected = UiStringRes(R.string.jp_migration_need_help_button)
        assertThat(actual).isEqualTo(expected)
    }
    // endregion
}
