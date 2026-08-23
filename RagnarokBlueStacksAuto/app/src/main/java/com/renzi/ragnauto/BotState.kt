package com.renzi.ragnauto

object BotState {
    @Volatile var running = false
    @Volatile var paused = false
    @Volatile var autoQuest = true
    @Volatile var autoCombat = true
    @Volatile var skipCutscenes = true
    @Volatile var autoRevive = true
    @Volatile var hpThreshold = 40
    @Volatile var spThreshold = 30
}
