package com.example.arkki

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.arkki.stickers.AddStickerPackActivity
import com.example.arkki.stickers.StickerPackDetailsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import maes.tech.intentanim.CustomIntent.customType
import org.tensorflow.lite.examples.classification.BuildConfig
import org.tensorflow.lite.examples.classification.R

class MainActivity : AppCompatActivity() {

    private lateinit var navigationBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationBar = findViewById(R.id.bottomNavigationView)
        navigationBar.setOnNavigationItemSelectedListener {
            when (it.toString()) {
                "Koti" -> {
                    Log.d("dbg", "koti")
                }
                "Linnut" -> {
                    val intent = Intent(this, ClassifierActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
                "Peli" -> {
                    Log.d("dbg", "peli")
                }
                "Trivia" -> {
                    Log.d("dbg", "trivia")
                    val intent = Intent(this, QuestionnaireActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
                "Kamera" -> {
                    Log.d("dbg", "kamera123")
                    val intent = Intent(this, InstaCameraActivity::class.java)
                    startActivity(intent)
                    customType(this, "bottom-to-up")
                }
            }

            Log.d("dbg", "$it")

            return@setOnNavigationItemSelectedListener true
        }
        //button_add.setOnClickListener { addStickerPackToWhatsApp("1", "Luontoarkki") }

    }

    override fun onResume() {
        super.onResume()
        navigationBar.selectedItemId = R.id.action_home
    }


    /*
    fun addStickerPackToWhatsApp(identifier: String, stickerPackName: String) {
        try {
            //if neither WhatsApp Consumer or WhatsApp Business is installed, then tell user to install the apps.

            /*
            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(getActivity().getPackageManager()) && !WhitelistCheck.isWhatsAppSmbAppInstalled(getPackageManager())) {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
                return;
            }

             */
            val stickerPackWhitelistedInWhatsAppConsumer = WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(this, identifier)
            val stickerPackWhitelistedInWhatsAppSmb = WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(this, identifier)
            if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToChooser(identifier, stickerPackName)
            } else if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME)
            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME)
            } else {
                //Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
            }
        } catch (e: Exception) {
            Log.e("dbg", "error adding sticker pack to WhatsApp", e)
            //Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
        }

    }

    private fun launchIntentToAddPackToSpecificPackage(identifier: String, stickerPackName: String, whatsappPackageName: String) {
        val intent = createIntentToAddStickerPack(identifier, stickerPackName)
        intent.setPackage(whatsappPackageName)
        try {
            startActivityForResult(intent, ADD_PACK)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }

    }

    //Handle cases either of WhatsApp are set as default app to handle this intent. We still want users to see both options.
    private fun launchIntentToAddPackToChooser(identifier: String, stickerPackName: String) {
        val intent = createIntentToAddStickerPack(identifier, stickerPackName)
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), ADD_PACK)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }

    }

    private fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }
     */

}
