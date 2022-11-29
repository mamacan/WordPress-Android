package org.wordpress.android.localcontentmigration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.store.mobile.JetpackMigrationStore
import org.wordpress.android.fluxc.store.mobile.MigrationCompleteFetchedPayload.Error
import org.wordpress.android.fluxc.store.mobile.MigrationCompleteFetchedPayload.Success
import org.wordpress.android.localcontentmigration.ContentMigrationAnalyticsTracker.ErrorType.EmailError
import org.wordpress.android.modules.BG_THREAD
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class MigrationEmailHelper @Inject constructor(
    private val jetpackMigrationStore: JetpackMigrationStore,
    private val migrationAnalyticsTracker: ContentMigrationAnalyticsTracker,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job

    fun notifyMigrationComplete() = launch(bgDispatcher) {
        when (val result = jetpackMigrationStore.migrationComplete()) {
            is Success -> migrationAnalyticsTracker.trackMigrationEmailSuccess()
            is Error -> migrationAnalyticsTracker.trackMigrationEmailFailed(EmailError(result.error?.message))
        }
    }
}
