package com.jfcardenas.musicwall.ui.viewmodel

import com.jfcardenas.musicwall.auth.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSession: UserSessionRepository,
) : ViewModel() {

    fun completeOnboarding() {
        userSession.markOnboardingComplete()
    }
}
