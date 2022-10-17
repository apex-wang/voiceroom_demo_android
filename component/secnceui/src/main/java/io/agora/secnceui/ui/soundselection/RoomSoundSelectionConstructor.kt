package io.agora.secnceui.ui.soundselection

import android.content.Context
import io.agora.config.ConfigConstants
import io.agora.secnceui.R
import io.agora.secnceui.bean.CustomerUsageBean
import io.agora.secnceui.bean.SoundSelectionBean

object RoomSoundSelectionConstructor {

    fun builderSoundSelectionList(
        context: Context,
        curSoundSelectionType: Int
    ): MutableList<SoundSelectionBean> {

        val soundSelectionList = mutableListOf(
            SoundSelectionBean(
                soundSelectionType = ConfigConstants.SoundSelection.Social_Chat,
                index = 0,
                soundName = context.getString(R.string.chatroom_social_chat),
                soundIntroduce = context.getString(R.string.chatroom_social_chat_introduce),
                isCurrentUsing = curSoundSelectionType == ConfigConstants.SoundSelection.Social_Chat,
                customer = mutableListOf(
                    CustomerUsageBean("netease", R.drawable.icon_chatroom_netease),
                    CustomerUsageBean("momo", R.drawable.icon_chatroom_momo),
                    CustomerUsageBean("yinyu", R.drawable.icon_chatroom_yinyu),
                    CustomerUsageBean("pipi", R.drawable.icon_chatroom_pipi),
                )
            ),
            SoundSelectionBean(
                soundSelectionType =  ConfigConstants.SoundSelection.Karaoke,
                index = 1,
                soundName = context.getString(R.string.chatroom_karaoke),
                soundIntroduce = context.getString(R.string.chatroom_karaoke_introduce),
                isCurrentUsing = curSoundSelectionType ==  ConfigConstants.SoundSelection.Karaoke,
                customer = mutableListOf(
                    CustomerUsageBean("netease", R.drawable.icon_chatroom_netease),
                    CustomerUsageBean("jiamian", R.drawable.icon_chatroom_jiamian),
                    CustomerUsageBean("yinyu", R.drawable.icon_chatroom_yinyu),
                    CustomerUsageBean("poaipai", R.drawable.icon_chatroom_poaipai),
                    CustomerUsageBean("lets_play", R.drawable.icon_chatroom_lets_play),
                    CustomerUsageBean("qingtian", R.drawable.icon_chatroom_qingtian),
                    CustomerUsageBean("skr", R.drawable.icon_chatroom_skr),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            ),
            SoundSelectionBean(
                soundSelectionType = ConfigConstants.SoundSelection.Gaming_Buddy,
                index = 2,
                soundName = context.getString(R.string.chatroom_gaming_buddy),
                soundIntroduce = context.getString(R.string.chatroom_gaming_buddy_introduce),
                isCurrentUsing = curSoundSelectionType == ConfigConstants.SoundSelection.Gaming_Buddy,
                customer = mutableListOf(
                    CustomerUsageBean("yalla_ludo", R.drawable.icon_chatroom_yalla_ludo),
                    CustomerUsageBean("jiamian", R.drawable.icon_chatroom_jiamian),
                )
            ),
            SoundSelectionBean(
                soundSelectionType = ConfigConstants.SoundSelection.Professional_Broadcaster,
                index = 2,
                soundName = context.getString(R.string.chatroom_professional_broadcaster),
                soundIntroduce = context.getString(R.string.chatroom_professional_broadcaster_introduce),
                isCurrentUsing = curSoundSelectionType == ConfigConstants.SoundSelection.Professional_Broadcaster,
                customer = mutableListOf(
                    CustomerUsageBean("qingmang", R.drawable.icon_chatroom_qingmang),
                    CustomerUsageBean("calf", R.drawable.icon_chatroom_calf),
                    CustomerUsageBean("yuwanxingqiu", R.drawable.icon_chatroom_yuwanxingqiu),
                    CustomerUsageBean("yizhibo", R.drawable.icon_chatroom_yizhibo),
                )
            )
        )
        val comparator: Comparator<SoundSelectionBean> = Comparator { o1, o2 ->
            o2.isCurrentUsing.compareTo(o1.isCurrentUsing)
        }
        soundSelectionList.sortWith(comparator)
        return soundSelectionList
    }

    fun builderCurSoundSelection(context: Context, soundSelectionType: Int): SoundSelectionBean {
        return SoundSelectionBean(
            soundSelectionType = soundSelectionType,
            index = 0,
            soundName = soundNameBySoundSelectionType(context, soundSelectionType),
            soundIntroduce = soundIntroduceBySoundSelectionType(context, soundSelectionType),
            isCurrentUsing = true,
            customer = soundSelectionCustomer(soundSelectionType),
        )
    }

    private fun soundNameBySoundSelectionType(context: Context, soundSelectionType: Int): String {
        return when (soundSelectionType) {
            ConfigConstants.SoundSelection.Karaoke -> {
                context.getString(R.string.chatroom_karaoke)
            }
            ConfigConstants.SoundSelection.Gaming_Buddy -> {
                context.getString(R.string.chatroom_gaming_buddy)
            }
            ConfigConstants.SoundSelection.Professional_Broadcaster -> {
                context.getString(R.string.chatroom_professional_broadcaster)
            }
            else -> {
                context.getString(R.string.chatroom_social_chat)
            }
        }
    }

    private fun soundIntroduceBySoundSelectionType(context: Context, soundSelectionType: Int): String {
        return when (soundSelectionType) {
            ConfigConstants.SoundSelection.Karaoke -> {
                context.getString(R.string.chatroom_karaoke_introduce)
            }
            ConfigConstants.SoundSelection.Gaming_Buddy -> {
                context.getString(R.string.chatroom_gaming_buddy_introduce)
            }
            ConfigConstants.SoundSelection.Professional_Broadcaster -> {
                context.getString(R.string.chatroom_professional_broadcaster_introduce)
            }
            else -> {
                context.getString(R.string.chatroom_social_chat_introduce)
            }
        }
    }

    private fun soundSelectionCustomer(soundSelectionType: Int): List<CustomerUsageBean> {
        return  when (soundSelectionType) {
            ConfigConstants.SoundSelection.Karaoke -> {
                mutableListOf(
                    CustomerUsageBean("netease", R.drawable.icon_chatroom_netease),
                    CustomerUsageBean("jiamian", R.drawable.icon_chatroom_jiamian),
                    CustomerUsageBean("yinyu", R.drawable.icon_chatroom_yinyu),
                    CustomerUsageBean("poaipai", R.drawable.icon_chatroom_poaipai),
                    CustomerUsageBean("lets_play", R.drawable.icon_chatroom_lets_play),
                    CustomerUsageBean("qingtian", R.drawable.icon_chatroom_qingtian),
                    CustomerUsageBean("skr", R.drawable.icon_chatroom_skr),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            }
            ConfigConstants.SoundSelection.Gaming_Buddy -> {
                mutableListOf(
                    CustomerUsageBean("yalla_ludo", R.drawable.icon_chatroom_yalla_ludo),
                    CustomerUsageBean("jiamian", R.drawable.icon_chatroom_jiamian),
                )
            }
            ConfigConstants.SoundSelection.Professional_Broadcaster -> {
                mutableListOf(
                    CustomerUsageBean("qingmang", R.drawable.icon_chatroom_qingmang),
                    CustomerUsageBean("calf", R.drawable.icon_chatroom_calf),
                    CustomerUsageBean("yuwanxingqiu", R.drawable.icon_chatroom_yuwanxingqiu),
                    CustomerUsageBean("yizhibo", R.drawable.icon_chatroom_yizhibo),
                )
            }
            else -> {
                mutableListOf(
                    CustomerUsageBean("netease", R.drawable.icon_chatroom_netease),
                    CustomerUsageBean("momo", R.drawable.icon_chatroom_momo),
                    CustomerUsageBean("yinyu", R.drawable.icon_chatroom_yinyu),
                    CustomerUsageBean("pipi", R.drawable.icon_chatroom_pipi),
                )
            }
        }
    }
}