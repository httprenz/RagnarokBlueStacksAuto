package com.renzi.ragnauto

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.renzi.ragnauto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.swQuest.setOnCheckedChangeListener { _, checked -> BotState.autoQuest = checked }
        binding.swCombat.setOnCheckedChangeListener { _, checked -> BotState.autoCombat = checked }
        binding.swSkip.setOnCheckedChangeListener { _, checked -> BotState.skipCutscenes = checked }
        binding.swRevive.setOnCheckedChangeListener { _, checked -> BotState.autoRevive = checked }

        binding.hpSeek.setOnSeekBarChangeListener(simpleSeek {
            BotState.hpThreshold = it
            binding.hpValue.text = "$it%"
        })

        binding.spSeek.setOnSeekBarChangeListener(simpleSeek {
            BotState.spThreshold = it
            binding.spValue.text = "$it%"
        })

        binding.btnStart.setOnClickListener {
            BotState.running = true
            BotState.paused = false
            BotAccessibilityService.instance?.showControlOverlay()
            binding.statusText.text = "Status: running"
        }

        binding.btnPause.setOnClickListener {
            if (BotState.running) {
                BotState.paused = !BotState.paused
                binding.statusText.text = if (BotState.paused) "Status: paused" else "Status: running"
            }
        }

        binding.btnStop.setOnClickListener {
            BotState.running = false
            BotState.paused = false
            BotAccessibilityService.instance?.hideControlOverlay()
            binding.statusText.text = "Status: stopped"
        }
    }

    private fun simpleSeek(onChange: (Int) -> Unit) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = onChange(progress)
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
}
