package dev.ragnarok.fenrir.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.VideosFragment
import dev.ragnarok.fenrir.fragment.VideosTabsFragment
import dev.ragnarok.fenrir.fragment.search.SingleTabSearchFragment
import dev.ragnarok.fenrir.mvp.view.IVideosListView
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.util.Utils

class VideoSelectActivity : NoMainActivity(), PlaceProvider {

    companion object {
        private const val VIDEO_TABS = "video-tabs"
        private const val VIDEO_ALBUM = "video-album"
        private const val VIDEO_SEARCH = "video-search"
        /**
         * @param accountId От чьего имени получать
         * @param ownerId   Чьи получать
         */
        fun createIntent(context: Context?, accountId: Int, ownerId: Int): Intent =
            Intent(context, VideoSelectActivity::class.java).apply {
                putExtra(Extra.ACCOUNT_ID, accountId)
                putExtra(Extra.OWNER_ID, ownerId)
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            intent.extras?.let { extras ->
                val accountId: Int
                val ownerId: Int
                extras.apply {
                    accountId = getInt(Extra.ACCOUNT_ID)
                    ownerId = getInt(Extra.OWNER_ID)
                    attachInitialFragment(accountId, ownerId)
                }
            }
        }
    }

    private fun attachInitialFragment(accountId: Int, ownerId: Int) {
        VideosTabsFragment
            .newInstance(accountId, ownerId, IVideosListView.ACTION_SELECT)
            .beginTransaction(VIDEO_TABS)
            .commit()

    }

    private fun Fragment.beginTransaction(name: String): FragmentTransaction =
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
            replace(mainContainerViewId, this@beginTransaction)
            addToBackStack(name)
        }

    override fun openPlace(placeOrNull: Place?) {
        placeOrNull?.let { place ->
            when (place.type) {
                Place.VIDEO_ALBUM -> {
                    VideosFragment.newInstance(place.args)
                        .beginTransaction(VIDEO_ALBUM)
                        .commit()
                }
                Place.SINGLE_SEARCH -> {
                    SingleTabSearchFragment
                        .newInstance(place.args)
                        .beginTransaction(VIDEO_SEARCH)
                        .commit()
                }
                Place.VIDEO_PREVIEW -> {
                    val intent = Intent().apply {
                        putParcelableArrayListExtra(
                            Extra.ATTACHMENTS,
                            Utils.singletonArrayList(place.args.getParcelable(Extra.VIDEO))
                        )
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
                else -> return@let
            }
        }
    }
}