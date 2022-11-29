package org.wordpress.android.viewmodel.mlp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.SiteAction
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.site.GutenbergLayout
import org.wordpress.android.fluxc.network.rest.wpcom.site.GutenbergLayoutCategory
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnBlockLayoutsFetched
import org.wordpress.android.fluxc.store.SiteStore.SiteError
import org.wordpress.android.fluxc.store.SiteStore.SiteErrorType.GENERIC_ERROR
import org.wordpress.android.test
import org.wordpress.android.ui.layoutpicker.LayoutPickerUiState.Content
import org.wordpress.android.ui.layoutpicker.LayoutPickerUiState.Error
import org.wordpress.android.ui.mlp.ModalLayoutPickerDimensionProvider
import org.wordpress.android.ui.mlp.ModalLayoutPickerTracker
import org.wordpress.android.ui.mlp.SupportedBlocks
import org.wordpress.android.ui.mlp.SupportedBlocksProvider
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import org.wordpress.android.util.DisplayUtilsWrapper
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.util.NoDelayCoroutineDispatcher
import org.wordpress.android.util.SiteUtils.GB_EDITOR_NAME
import org.wordpress.android.viewmodel.mlp.ModalLayoutPickerViewModel.PageRequest.Blank
import org.wordpress.android.viewmodel.mlp.ModalLayoutPickerViewModel.PageRequest.Create

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ModalLayoutPickerViewModelTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: ModalLayoutPickerViewModel

    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var siteStore: SiteStore
    @Mock lateinit var selectedSiteRepository: SelectedSiteRepository
    @Mock lateinit var supportedBlocksProvider: SupportedBlocksProvider
    @Mock lateinit var thumbDimensionProvider: ModalLayoutPickerDimensionProvider
    @Mock lateinit var displayUtilsWrapper: DisplayUtilsWrapper
    @Mock lateinit var networkUtils: NetworkUtilsWrapper
    @Mock lateinit var analyticsTracker: ModalLayoutPickerTracker
    @Mock lateinit var onCreateNewPageRequestedObserver: Observer<Create>

    private val aboutCategory = GutenbergLayoutCategory(
            slug = "about",
            title = "About",
            description = "About pages",
            emoji = "👋"
    )

    private val aboutLayout = GutenbergLayout(
            slug = "about",
            title = "About",
            previewTablet = "https://headstartdata.files.wordpress.com/2020/01/about-2.png",
            previewMobile = "https://headstartdata.files.wordpress.com/2020/01/about-2.png",
            preview = "https://headstartdata.files.wordpress.com/2020/01/about-2.png",
            content = "",
            demoUrl = "",
            categories = listOf(aboutCategory)
    )

    private val defaultPageLayoutsEvent: OnBlockLayoutsFetched
        get() = OnBlockLayoutsFetched(listOf(aboutLayout), listOf(aboutCategory), null)

    @Before
    fun setUp() {
        viewModel = ModalLayoutPickerViewModel(
                dispatcher,
                siteStore,
                selectedSiteRepository,
                supportedBlocksProvider,
                thumbDimensionProvider,
                displayUtilsWrapper,
                networkUtils,
                analyticsTracker,
                NoDelayCoroutineDispatcher(),
                NoDelayCoroutineDispatcher()
        )
        viewModel.onCreateNewPageRequested.observeForever(
                onCreateNewPageRequestedObserver
        )
    }

    private fun <T> mockFetchingSelectedSite(
        isError: Boolean = false,
        isSiteUnavailable: Boolean = false,
        block: suspend CoroutineScope.() -> T
    ) = test {
            val site = SiteModel().apply {
                id = 1
                mobileEditor = GB_EDITOR_NAME
            }
            whenever(selectedSiteRepository.getSelectedSiteLocalId(true)).thenReturn(site.id)
            if (isSiteUnavailable) {
                whenever(siteStore.getSiteByLocalId(site.id)).thenReturn(null)
            } else {
                whenever(siteStore.getSiteByLocalId(site.id)).thenReturn(site)
            }
            whenever(siteStore.getBlockLayout(site, "about")).thenReturn(aboutLayout)
            whenever(supportedBlocksProvider.fromAssets()).thenReturn(SupportedBlocks())
            whenever(thumbDimensionProvider.previewWidth).thenReturn(136)
            whenever(thumbDimensionProvider.scale).thenReturn(1.0)
            whenever(networkUtils.isNetworkAvailable()).thenReturn(true)
            setupFetchLayoutsDispatcher(isError)
            block()
    }

    private fun setupFetchLayoutsDispatcher(isError: Boolean) {
        val event = if (isError) {
            OnBlockLayoutsFetched(null, null, SiteError(GENERIC_ERROR, "Error"))
        } else {
            defaultPageLayoutsEvent
        }
        whenever(dispatcher.dispatch(argWhere<Action<Void>> {
            it.type == SiteAction.FETCH_BLOCK_LAYOUTS
        })).then {
            viewModel.onBlockLayoutsFetched(event)
        }
    }

    @Test
    fun `when the user scroll beyond a threshold the title becomes visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onAppBarOffsetChanged(9, 10)
        assertThat(requireNotNull(viewModel.uiState.value as Content).isHeaderVisible).isEqualTo(true)
    }

    @Test
    fun `when the user scroll bellow a threshold the title remains hidden`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onAppBarOffsetChanged(11, 10)
        assertThat(requireNotNull(viewModel.uiState.value as Content).isHeaderVisible).isEqualTo(false)
    }

    @Test
    fun `when modal layout picker starts the categories are loaded`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).categories.size).isGreaterThan(0)
    }

    @Test
    fun `when modal layout picker starts the layouts are loaded`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).layoutCategories.size).isGreaterThan(0)
    }

    @Test
    fun `when modal layout picker starts fetch errors are handled`() = mockFetchingSelectedSite(true) {
        viewModel.createPageFlowTriggered()
        assertThat(viewModel.uiState.value is Error).isEqualTo(true)
    }

    @Test
    fun `when modal layout picker starts and the site is unavailable errors are handled`() =
            mockFetchingSelectedSite(isSiteUnavailable = true) {
                viewModel.createPageFlowTriggered()
                assertThat(viewModel.uiState.value is Error).isEqualTo(true)
            }

    @Test
    fun `modal layout picker is shown when triggered`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(viewModel.isModalLayoutPickerShowing.value!!.peekContent()).isEqualTo(true)
    }

    @Test
    fun `modal layout picker is dismissed when the user hits the back button`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.dismiss()
        assertThat(viewModel.isModalLayoutPickerShowing.value!!.peekContent()).isEqualTo(false)
    }

    @Test
    fun `when no layout is selected and the create page is triggered the blank page creation flow starts`() =
            mockFetchingSelectedSite {
                viewModel.createPageFlowTriggered()
                viewModel.onCreatePageClicked()
                val captor = ArgumentCaptor.forClass(Create::class.java)
                verify(onCreateNewPageRequestedObserver).onChanged(captor.capture())
                assertThat(captor.value).isEqualTo(Blank)
            }

    @Test
    fun `when a layout is selected and the create page is triggered the page creation flow starts with a template`() =
            mockFetchingSelectedSite {
                viewModel.createPageFlowTriggered()
                viewModel.onThumbnailReady("about")
                viewModel.onLayoutTapped("about")
                viewModel.onCreatePageClicked()
                val captor = ArgumentCaptor.forClass(Create::class.java)
                verify(onCreateNewPageRequestedObserver).onChanged(captor.capture())
                assertThat(captor.value.template).isEqualTo("about")
            }

    @Test
    fun `when modal layout picker starts no layout is selected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedLayoutSlug).isNull()
    }

    @Test
    fun `when the user taps on a layout the layout is selected if the thumbnail has loaded`() =
            mockFetchingSelectedSite {
                viewModel.createPageFlowTriggered()
                viewModel.onThumbnailReady("about-1")
                viewModel.onLayoutTapped("about-1")
                assertThat(requireNotNull(viewModel.uiState.value as Content).selectedLayoutSlug)
                        .isEqualTo("about-1")
            }

    @Test
    fun `when the user taps on a layout the layout is selected if the thumbnail has not loaded`() =
            mockFetchingSelectedSite {
                viewModel.createPageFlowTriggered()
                viewModel.onLayoutTapped("about-1")
                assertThat(requireNotNull(viewModel.uiState.value as Content).selectedLayoutSlug)
                        .isNotEqualTo("about-1")
            }

    @Test
    fun `when the user taps on a selected layout the layout is deselected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onLayoutTapped("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedLayoutSlug).isNull()
    }

    @Test
    fun `when the modal layout picker is dismissed the layout is deselected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onLayoutTapped("about-1")
        viewModel.dismiss()
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedLayoutSlug).isNull()
    }

    @Test
    fun `when modal layout picker starts no category is selected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedCategoriesSlugs).isEmpty()
    }

    @Test
    fun `when the user taps on a category the category is selected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onCategoryTapped("about")
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedCategoriesSlugs)
                .contains("about")
    }

    @Test
    fun `when the user taps on a selected category the category is deselected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onCategoryTapped("about")
        viewModel.onCategoryTapped("about")
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedCategoriesSlugs)
                .doesNotContain("about")
    }

    @Test
    fun `when the modal layout picker is dismissed the category is deselected`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onCategoryTapped("about")
        viewModel.dismiss()
        assertThat(requireNotNull(viewModel.uiState.value as Content).selectedCategoriesSlugs).isEmpty()
    }

    @Test
    fun `when no layout is selected the create blank page button is visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.createBlankPageVisible)
                .isEqualTo(true)
    }

    @Test
    fun `when a layout is selected the create blank page button is not visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.createBlankPageVisible)
                .isEqualTo(false)
    }

    @Test
    fun `when no layout is selected the create page button is not visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.createPageVisible)
                .isEqualTo(false)
    }

    @Test
    fun `when a layout is selected the create page button is visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.createPageVisible)
                .isEqualTo(true)
    }

    @Test
    fun `when no layout is selected the preview button is not visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.previewVisible)
                .isEqualTo(false)
    }

    @Test
    fun `when a layout is selected the preview button is visible`() = mockFetchingSelectedSite {
        viewModel.createPageFlowTriggered()
        viewModel.onThumbnailReady("about-1")
        viewModel.onLayoutTapped("about-1")
        assertThat(requireNotNull(viewModel.uiState.value as Content).buttonsUiState.previewVisible)
                .isEqualTo(true)
    }
}
