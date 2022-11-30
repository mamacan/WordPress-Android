package org.wordpress.android.localcontentmigration

import org.wordpress.android.fluxc.store.AccountStore
import org.wordpress.android.localcontentmigration.LocalContentEntityData.AccessTokenData
import javax.inject.Inject

class LocalAccessTokenProviderHelper @Inject constructor(
    private val accountStore: AccountStore,
): LocalDataProviderHelper {
    override fun getData(localEntityId: Int?) = AccessTokenData(
            token = accountStore.accessToken.orEmpty(),
            accountStore.account?.avatarUrl.orEmpty(),
    )
}
