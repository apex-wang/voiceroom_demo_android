package io.agora.secnceui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.buddy.tool.dp
import io.agora.secnceui.R
import io.agora.secnceui.bean.ChatroomWheatSeatType
import io.agora.secnceui.bean.ChatroomWheatUserRole
import io.agora.secnceui.bean.ChatroomWheatUserStatus
import io.agora.secnceui.bean.SeatInfoBean
import io.agora.secnceui.databinding.ViewChatroom2dSeatBinding

class Chatroom2DSeatView : ConstraintLayout {

    private lateinit var mBinding: ViewChatroom2dSeatBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.view_chatroom_2d_seat, this)
        mBinding = ViewChatroom2dSeatBinding.bind(root)
    }

    fun binding(seatInfo: SeatInfoBean) {
        when (seatInfo.wheatSeatType) {
            ChatroomWheatSeatType.Idle -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white30)
                    setImageResource(R.drawable.icon_seat_add)
                }
                mBinding.ivSeatMic.isVisible = false
                mBinding.mtSeatInfoName.apply {
                    text = seatInfo.index.toString()
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            ChatroomWheatSeatType.Mute -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white30)
                    setImageResource(R.drawable.icon_seat_mic)
                }
                mBinding.ivSeatMic.isVisible = false
                mBinding.mtSeatInfoName.apply {
                    text = seatInfo.index.toString()
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            ChatroomWheatSeatType.Lock -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white30)
                    setImageResource(R.drawable.icon_seat_close)
                }
                mBinding.ivSeatMic.isVisible = false
                mBinding.mtSeatInfoName.apply {
                    text = seatInfo.index.toString()
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            ChatroomWheatSeatType.Inactive -> {
                mBinding.mtSeatRotActive.isVisible = true
                mBinding.ivSeatBotFloat.isVisible = true
                setNormalWheatView(seatInfo)
            }
            else -> {
                setNormalWheatView(seatInfo)
            }
        }
    }

    private fun setNormalWheatView(seatInfo: SeatInfoBean) {
        mBinding.mtSeatInfoName.text = seatInfo.name
        // todo avatar
        when (seatInfo.userRole) {
            ChatroomWheatUserRole.Robot -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white)
                    setImageResource(seatInfo.rotImage)
                    val contentPadding = 10.dp.toInt()
                    setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding)
                }
                mBinding.mtSeatInfoName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_seat_robot_tag, 0, 0, 0
                )
                mBinding.mtSeatRotActive.isVisible = true
            }
            ChatroomWheatUserRole.Owner -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white30)
                    setImageResource(0)
                }
                mBinding.mtSeatInfoName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_seat_owner_tag, 0, 0, 0
                )
            }
            else -> {
                mBinding.ivSeatInfo.apply {
                    setBackgroundResource(R.drawable.bg_oval_white30)
                    setImageResource(0)
                }
                mBinding.mtSeatInfoName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
        mBinding.ivSeatMic.apply {
            when (seatInfo.userStatus) {
                ChatroomWheatUserStatus.None -> {
                    isVisible = false
                }
                ChatroomWheatUserStatus.Idle -> {
                    isVisible = true
                    setImageResource(R.drawable.icon_seat_on_mic0)
                }
                ChatroomWheatUserStatus.Mute -> {
                    isVisible = true
                    setImageResource(R.drawable.icon_seat_off_mic)
                }
                else -> {
                    // speaking
                    isVisible = true
                    setImageResource(R.drawable.icon_seat_on_mic1)
                }
            }
        }
    }
}