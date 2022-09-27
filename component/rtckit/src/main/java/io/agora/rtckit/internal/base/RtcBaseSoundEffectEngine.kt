package io.agora.rtckit.internal.base

import io.agora.config.ConfigConstants

/**
 * @author create by zhangwei03
 *
 * 音效管理引擎
 */
internal abstract class RtcBaseSoundEffectEngine<T> : RtcBaseEngine<T>() {

    /**
     * @param soundSpeakerType  机器人播放类型
     * @param soundId 唯一标识
     * @param filePath String 文件路径
     * @param loopCount 循环次数
     * @param publish 是否将音效传到远端  true 传送 false 本地播放
     */
    abstract fun playEffect(
        soundId: Int,
        filePath: String,
        loopCount: Int,
        publish: Boolean,
        soundSpeakerType: Int = ConfigConstants.BotSpeaker.BotBlue,
    ): Boolean

    abstract fun stopEffect(soundId: Int): Boolean

    abstract fun pauseEffect(soundId: Int): Boolean

    abstract fun resumeEffect(soundId: Int): Boolean

    abstract fun stopAllEffect(): Boolean
}