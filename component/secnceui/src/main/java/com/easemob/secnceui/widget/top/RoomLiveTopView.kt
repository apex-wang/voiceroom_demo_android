package com.easemob.secnceui.widget.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.easemob.buddy.tool.ResourcesTools
import com.easemob.buddy.tool.dp
import com.easemob.buddy.tool.getDisplaySize
import com.easemob.buddy.tool.number2K
import com.easemob.config.ConfigConstants
import com.easemob.secnceui.R
import com.easemob.secnceui.constants.ScenesConstant
import com.easemob.secnceui.databinding.ViewChatroomLiveTopBinding

class RoomLiveTopView : ConstraintLayout, View.OnClickListener, IRoomLiveTopView {

    private lateinit var binding: ViewChatroomLiveTopBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private lateinit var roomInfo: com.easemob.secnceui.bean.RoomInfoBean

    private var onLiveTopClickListener: OnLiveTopClickListener? = null

    fun setOnLiveTopClickListener(onLiveTopClickListener: OnLiveTopClickListener) {
        this.onLiveTopClickListener = onLiveTopClickListener
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.view_chatroom_live_top, this)
        binding = ViewChatroomLiveTopBinding.bind(root)
        binding.ivChatroomBack.setOnClickListener(this)
        binding.llChatroomMemberRank.setOnClickListener(this)
//        binding.mtChatroomMembers.setOnClickListener(this)
        binding.mtChatroomNotice.setOnClickListener(this)
        binding.mtChatroomAgoraSound.setOnClickListener(this)

    }

    fun setTitleMaxWidth() {
        val layoutParams: ViewGroup.LayoutParams = binding.llTitle.layoutParams
        layoutParams.width = getDisplaySize().width - 220.dp.toInt()
        binding.llTitle.layoutParams = layoutParams
    }

    override fun onChatroomInfo(chatroomInfo: com.easemob.secnceui.bean.RoomInfoBean) {
        this.roomInfo = chatroomInfo
        binding.apply {
            mtChatroomOwnerName.text = chatroomInfo.owner?.username
            mtChatroomName.text = chatroomInfo.chatroomName
            val memberText = roomInfo.memberCount.number2K()
            mtChatroomMembers.text = memberText
            val giftText = roomInfo.giftCount.number2K()
            mtChatroomGifts.text = giftText
            val watchText = roomInfo.watchCount.number2K()
            mtChatroomWatch.text = watchText
            // 普通房间显示 最佳音效
            if (chatroomInfo.roomType == ConfigConstants.RoomType.Common_Chatroom) {
                mtChatroomAgoraSound.isVisible = true
                mtChatroomAgoraSound.text = when (chatroomInfo.soundSelection) {
                    ConfigConstants.SoundSelection.Karaoke -> root.context.getString(R.string.chatroom_karaoke)
                    ConfigConstants.SoundSelection.Gaming_Buddy -> root.context.getString(R.string.chatroom_gaming_buddy)
                    ConfigConstants.SoundSelection.Professional_Broadcaster -> root.context.getString(R.string.chatroom_professional_broadcaster)
                    else -> root.context.getString(R.string.chatroom_social_chat)
                }
            } else {
                mtChatroomAgoraSound.isVisible = false
            }

            // 房主头像
            binding.ivChatroomOwner.setImageResource(
                ResourcesTools.getDrawableId(
                    binding.ivChatroomOwner.context,
                    chatroomInfo.owner?.userAvatar ?: ScenesConstant.DefaultAvatar
                )
            )
            val topGifts = chatroomInfo.topRankUsers
            if (topGifts.isNullOrEmpty()) {
                llChatroomMemberRank.isVisible = false
            } else {
                llChatroomMemberRank.isVisible = true
                topGifts.forEachIndexed { index, audienceBean ->
                    val resId = ResourcesTools.getDrawableId(llChatroomMemberRank.context, audienceBean.userAvatar)
                    when (index) {
                        0 -> {
                            ivChatroomMember1.isVisible = true
                            binding.ivChatroomMember1.setImageResource(resId)
                        }
                        1 -> {
                            ivChatroomMember2.isVisible = true
                            binding.ivChatroomMember2.setImageResource(resId)
                        }
                        2 -> {
                            ivChatroomMember3.isVisible = true
                            binding.ivChatroomMember3.setImageResource(resId)
                        }
                        else -> {
                            return
                        }
                    }
                }
            }
        }
    }

    override fun onRankMember(topGifts: List<com.easemob.secnceui.bean.RoomRankUserBean>) {
        binding.apply {
            if (topGifts.isNullOrEmpty()) {
                llChatroomMemberRank.isVisible = false
            } else {
                llChatroomMemberRank.isVisible = true
                topGifts.forEachIndexed { index, audienceBean ->
                    val resId = ResourcesTools.getDrawableId(llChatroomMemberRank.context, audienceBean.userAvatar)
                    when (index) {
                        0 -> {
                            ivChatroomMember1.isVisible = true
                            binding.ivChatroomMember1.setImageResource(resId)
                        }
                        1 -> {
                            ivChatroomMember2.isVisible = true
                            binding.ivChatroomMember2.setImageResource(resId)
                        }
                        2 -> {
                            ivChatroomMember3.isVisible = true
                            binding.ivChatroomMember3.setImageResource(resId)
                        }
                        else -> {
                            return
                        }
                    }
                }
            }
        }
    }

    override fun subMemberCount() {
        if (this::roomInfo.isInitialized) {
            roomInfo.memberCount -= 1
            val text = roomInfo.memberCount.number2K()
            binding.mtChatroomMembers.text = text
        }
    }

    override fun onUpdateMemberCount(count: Int) {
        super.onUpdateMemberCount(count)
        if (count < 0) return
        if (this::roomInfo.isInitialized) {
            roomInfo.memberCount = count
            val text = roomInfo.memberCount.number2K()
            binding.mtChatroomMembers.text = text
        }
    }

    override fun onUpdateWatchCount(count: Int) {
        super.onUpdateWatchCount(count)
        if (count < 0) return
        if (this::roomInfo.isInitialized) {
            roomInfo.watchCount = count
            val text = roomInfo.watchCount.number2K()
            binding.mtChatroomWatch.text = text
        }
    }

    override fun onUpdateGiftCount(count: Int) {
        super.onUpdateGiftCount(count)
        if (count < 0) return
        if (this::roomInfo.isInitialized) {
            roomInfo.giftCount = count
            val text = roomInfo.giftCount.number2K()
            binding.mtChatroomGifts.text = text
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            // 返回
            binding.ivChatroomBack -> onLiveTopClickListener?.onClickBack(v)
            // 排行榜
            binding.llChatroomMemberRank,
            binding.mtChatroomMembers -> onLiveTopClickListener?.onClickRank(v)
            // 公告
            binding.mtChatroomNotice -> onLiveTopClickListener?.onClickNotice(v)
            //音效
            binding.mtChatroomAgoraSound -> onLiveTopClickListener?.onClickSoundSocial(v)
        }
    }
}