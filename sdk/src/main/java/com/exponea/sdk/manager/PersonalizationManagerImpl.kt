package com.exponea.sdk.manager

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.exponea.sdk.models.Banner
import com.exponea.sdk.models.BannerResult
import com.exponea.sdk.models.Constants
import com.exponea.sdk.models.CustomerIds
import com.exponea.sdk.models.ExponeaProject
import com.exponea.sdk.models.FetchError
import com.exponea.sdk.models.Personalization
import com.exponea.sdk.models.Result
import com.exponea.sdk.util.Logger
import java.util.Date

/**
 * Personalization Manager is responsible to get the banner ids and fetch
 * the content for personalization of active banners.
 * When getPersonalization is called, the banners will be show in a web view instance.
 */

internal class PersonalizationManagerImpl(
    private val context: Context,
    private val fetchManager: FetchManager,
    private val fileManager: FileManager
) : PersonalizationManager {

    private var preferencesIds: MutableList<String> = mutableListOf()

    override fun showBanner(exponeaProject: ExponeaProject, customerIds: CustomerIds) {
        getBannersConfiguration(
                exponeaProject = exponeaProject,
                customerIds = customerIds,
                onSuccess = {
                    if (canShowBanner(it.results)) {
                        getPersonalization(exponeaProject, customerIds)
                    }
                },
                onFailure = {
                    Logger.e(this, "Check the error log for more information.")
                }
        )
    }

    /**
     * Call the fetch manager and get the banner configuration.
     */

    override fun getBannersConfiguration(
        exponeaProject: ExponeaProject,
        customerIds: CustomerIds,
        onSuccess: (Result<ArrayList<Personalization>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ) {

        fetchManager.fetchBannerConfiguration(
                exponeaProject = exponeaProject,
                customerIds = customerIds,
                onSuccess = onSuccess,
                onFailure = onFailure
        )
    }

    /**
     * Call the fetch manager and get the banner.
     */

    override fun getPersonalization(exponeaProject: ExponeaProject, customerIds: CustomerIds) {

        val banner = Banner(customerIds = customerIds, personalizationIds = preferencesIds)

        fetchManager.fetchBanner(
                exponeaProject = exponeaProject,
                bannerConfig = banner,
                onSuccess = {
                    val data = it.results.first()
                    if (saveHtml(data)) showWebView(data.script, data.style)
                },
                onFailure = {
                    Logger.e(this, "Check the error log for more information.")
                }
        )
    }

    /**
     * Fetches personalized banners
     */
    override fun getWebLayer(
        exponeaProject: ExponeaProject,
        customerIds: CustomerIds,
        onSuccess: (Result<ArrayList<BannerResult>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ) {
        getBannersConfiguration(
            exponeaProject = exponeaProject,
            customerIds = customerIds,
            onSuccess = {
                if (canShowBanner(it.results)) {
                    val banner = Banner(customerIds = customerIds, personalizationIds = preferencesIds)
                    fetchManager.fetchBanner(exponeaProject, banner, onSuccess, onFailure)
                }
            },
            onFailure = {
                Logger.e(this, "Check the error log for more information.")
            }
        )
    }

    private fun canShowBanner(personalization: ArrayList<Personalization>): Boolean {

        var isExpirationValid = true
        var isMobileAvailable = false

        for (data in personalization) {
            // Check if the banner has expiration date and if it's valid.
            // If doesn't have any expiration set, then is allowed.
            data.dateFilter?.let {
                if (it.enabled) {
                    isExpirationValid = checkExpirationDate(
                            it.fromDate?.toLong(),
                            it.toDate?.toLong()
                    )
                }
            }
            data.deviceTarget?.let {
                if ((it.type == "mobile") || (it.type == "any")) {
                    isMobileAvailable = true
                }
            }

            // Only shows the valid banners.
            if (isExpirationValid && isMobileAvailable) {
                data.id?.let {
                    preferencesIds.add(it)
                }
            } else {
                Logger.d(this, "Banner ${data.id} is not valid")
            }
        }
        return preferencesIds.isNotEmpty()
    }

    /**
     * Save the html content into a temporary file.
     */

    private fun saveHtml(data: BannerResult): Boolean {
        data.html?.let {
            fileManager.createFile(
                    filename = Constants.General.bannerFilename,
                    type = Constants.General.bannerFilenameExt
            )

            fileManager.writeToFile(
                    filename = Constants.General.bannerFullFilename,
                    text = it
            )
            return true
        }
        return false
    }

    /**
     * Receive the script and css from the exponea api and prepare
     * the web view to be presented
     */

    private fun showWebView(script: String?, css: String?) {
        val webView = WebView(context)
        // Inject the javascript into web view.
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject the JavaScript into the WebView if exists.
                script?.let { webView.loadUrl(it) }
                // Inject the CSS into the WebView if exits.
                css?.let { webView.loadUrl(it) }
            }
        }
        // Load web view with all the received data.
        webView.loadUrl(ClassLoader.getSystemResource(Constants.General.bannerFullFilename).file)
    }

    // Helper Methods

    private fun checkExpirationDate(start: Long?, end: Long?): Boolean {

        val fromDate = start?.let { it } ?: run { return false }
        val toDate = end?.let { it } ?: run { return false }

        val startDate = Date(fromDate)
        val endDate = Date(toDate)
        val currDate = Date()

        return (currDate >= startDate) && (currDate <= endDate)
    }
}
