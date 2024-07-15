package com.phon.dialer.activities

import android.os.Bundle
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.phon.dialer.adapters.ConferenceCallsAdapter
import com.phon.dialer.databinding.ActivityConferenceBinding
import com.phon.dialer.helpers.CallManager

class ConferenceActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityConferenceBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            updateMaterialActivityViews(conferenceCoordinator, conferenceList, useTransparentNavigation = true, useTopSearchMenu = false)
            setupMaterialScrollListener(conferenceList, conferenceToolbar)
            conferenceList.adapter = ConferenceCallsAdapter(this@ConferenceActivity, conferenceList, ArrayList(CallManager.getConferenceCalls())) {}
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.conferenceToolbar, NavigationIcon.Arrow)
    }
}
