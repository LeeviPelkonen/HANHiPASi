package com.example.arkki

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.arkki.stickers.StickerPackDetailsActivity
import kotlinx.android.synthetic.main.activity_questionnaire_end.*
import org.tensorflow.lite.examples.classification.BuildConfig
import org.tensorflow.lite.examples.classification.R

class QuestionnaireEndActivity : AppCompatActivity() {

    val AUTHORITY_QUERY_PARAM = "authority"
    val IDENTIFIER_QUERY_PARAM = "identifier"
    val STICKER_APP_AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY
    val CONSUMER_WHATSAPP_PACKAGE_NAME = "com.whatsapp"
    val CONTENT_PROVIDER = ".provider.sticker_whitelist_check"
    val QUERY_PATH = "is_whitelisted"
    val QUERY_RESULT_COLUMN_NAME = "result"
    val identifier = "1"
    val stickerPackName = "Luontoarkki"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire_end)

        val maxScore = intent.getIntExtra("MaxScore", 0)
        val score = intent.getIntExtra("Score",0)
        val percentage = (((score).toDouble() / (maxScore).toDouble()) * 100).toInt()


        if (percentage < 50 ) {
            congratulationsLabel.text = getString(R.string.pahus, percentage)
        } else {
            redeemReward.visibility = View.VISIBLE
            congratulationsLabel.text = getString(R.string.jippii, percentage)
        }

        redeemReward.setOnClickListener { addStickerPackToWhatsApp() }

        playAgainQuiz.setOnClickListener {
            val i = Intent(this, QuestionnaireActivity::class.java)
            startActivity(i)
        }

    }

    private fun addStickerPackToWhatsApp() {
        try {
            if (!isWhitelistedFromProvider(applicationContext)) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToChooser()
            } else if (!isWhitelistedFromProvider(applicationContext)) {
                launchIntentToAddPackToSpecificPackage()
            }
        } catch (e: Exception) {
            Log.e("dbg", "error adding sticker pack to WhatsApp", e)
        }

    }

    private fun isWhitelistedFromProvider(context: Context): Boolean {
        val packageManager = context.packageManager

        if (isPackageInstalled(packageManager)) {
            val whatsappProviderAuthority = CONSUMER_WHATSAPP_PACKAGE_NAME + CONTENT_PROVIDER
            val providerInfo = packageManager.resolveContentProvider(whatsappProviderAuthority, PackageManager.GET_META_DATA)
                ?: return false
            // provider is not there. The WhatsApp app may be an old version.
            val queryUri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(whatsappProviderAuthority).appendPath(QUERY_PATH).appendQueryParameter(AUTHORITY_QUERY_PARAM, STICKER_APP_AUTHORITY).appendQueryParameter(IDENTIFIER_QUERY_PARAM, identifier).build()
            context.contentResolver.query(queryUri, null, null, null, null)!!.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val whiteListResult = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_RESULT_COLUMN_NAME))
                    return whiteListResult == 1
                }
            }
        } else {
            //if app is not installed, then don't need to take into its whitelist info into account.
            return true
        }
        return false
    }

    private fun isPackageInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun launchIntentToAddPackToSpecificPackage() {
        val intent = createIntentToAddStickerPack()
        intent.setPackage("com.whatsapp")
        try {
            startActivityForResult(intent, 200)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }

    }

    //Handle cases either of WhatsApp are set as default app to handle this intent. We still want users to see both options.
    private fun launchIntentToAddPackToChooser() {
        val intent = createIntentToAddStickerPack()
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), 200)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }

    }

    private fun createIntentToAddStickerPack(): Intent {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }

    override fun onBackPressed() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
