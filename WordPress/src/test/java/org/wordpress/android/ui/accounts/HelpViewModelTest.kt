package org.wordpress.android.ui.accounts

import androidx.lifecycle.Observer
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.TEST_DISPATCHER
import org.wordpress.android.WordPress
import org.wordpress.android.viewmodel.Event

@InternalCoroutinesApi
class HelpViewModelTest : BaseUnitTest() {
    @Mock lateinit var wordPress: WordPress
    @Mock private lateinit var onSignoutCompletedObserver: Observer<Unit>

    private lateinit var viewModel: HelpViewModel

    @Before
    fun setUp() {
        viewModel = HelpViewModel(
                mainDispatcher = TEST_DISPATCHER,
                bgDispatcher = TEST_DISPATCHER,
        )
    }

    @Test
    fun `when log out is clicked, dialog is shown and sign out is invoked`() {
        val showSigningOutDialogEvents = mutableListOf<Event<Boolean>>()
        viewModel.showSigningOutDialog.observeForever { showSigningOutDialogEvents.add(it) }
        val onSignoutCompletedCaptor = ArgumentCaptor.forClass(Unit::class.java)
        viewModel.onSignOutCompleted.observeForever(onSignoutCompletedObserver)

        viewModel.signOutWordPress(wordPress)

        verify(wordPress).wordPressComSignOut()
        assertThat(showSigningOutDialogEvents[0].getContentIfNotHandled()).isTrue
        assertThat(showSigningOutDialogEvents[1].getContentIfNotHandled()).isFalse
        verify(onSignoutCompletedObserver, times(1)).onChanged(onSignoutCompletedCaptor.capture())
    }
}
