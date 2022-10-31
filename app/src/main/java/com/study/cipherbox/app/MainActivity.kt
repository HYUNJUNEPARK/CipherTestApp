package com.study.cipherbox.app

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.study.cipherbox.R
import com.study.cipherbox.databinding.ActivityMainBinding
import com.study.cipherbox.sdk.CipherBox
import com.study.cipherbox.vm.KeyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cipherBox: CipherBox
    private lateinit var keyId: String
    private val viewModel: KeyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        TODO 옵저빙이 안됨
         */

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
                cipherBox = CipherBox.getInstance(this)!!
                binding.mainActivity = this

                isECKeyPair()

                viewModel.getPublicKey()
                viewModel.getESPKeyIdList(this)

                viewModel.publicKey.observe(this) { publicKey ->
                    binding.publicKeyTextView.text = publicKey
                }

                viewModel.espKeyList.observe(this) { keyIdList ->
                    binding.publicKeyIdTextView.text = keyIdList.toString()
                }

            } else {
                Toast.makeText(this, "API 31 이상 사용 가능", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //ECKeyPair 가 키스토어에 있다면 Agreement 버튼 활성화
    private fun isECKeyPair() {
        try {
            binding.keyAgreementButton.isEnabled = cipherBox.isECKeyPair()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onGenerateECKeyPair() {
        try {
            cipherBox.generateECKeyPair()
            binding.keyAgreementButton.isEnabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAgreementKey() {
        try {
            keyId = cipherBox.generateRandom(32)

            cipherBox.generateSharedSecretKey(
                publicKey = cipherBox.getECPublicKey(),
                nonce = keyId
            )

            binding.keyIdTextView.text = keyId
            binding.sendButton.isEnabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onReset() {
        try {
            cipherBox.reset()
            viewModel.reset()
            binding.keyAgreementButton.isEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSend() {
        try {
            val message = binding.messageEditText.text.toString()
            val encryptedMsg = cipherBox.encrypt(message, keyId)
            val decryptedMsg = cipherBox.decrypt(encryptedMsg, keyId)

            binding.userMessageTextView.text = message
            binding.encryptionCBCTextView.text = encryptedMsg
            binding.decryptionCBCTextView.text = decryptedMsg

            binding.messageEditText.text = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}