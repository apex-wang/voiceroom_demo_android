package com.easemob.chatroom.ui.activity

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import bean.ChatMessageData
import com.alibaba.android.arouter.facade.annotation.Route
import custorm.CustomMsgHelper
import custorm.OnMsgCallBack
import com.easemob.baseui.BaseUiActivity
import com.easemob.baseui.adapter.OnItemClickListener
import com.easemob.baseui.general.callback.OnResourceParseCallback
import com.easemob.baseui.general.net.Resource
import com.easemob.baseui.utils.StatusBarCompat
import com.easemob.buddy.tool.ThreadManager
import com.easemob.buddy.tool.ToastTools
import com.easemob.buddy.tool.logE
import com.easemob.chatroom.R
import com.easemob.chatroom.bean.RoomKitBean
import com.easemob.chatroom.controller.RtcRoomController
import com.easemob.chatroom.databinding.ActivityChatroomBinding
import com.easemob.chatroom.general.constructor.RoomInfoConstructor
import com.easemob.chatroom.general.constructor.RoomInfoConstructor.convertByRoomDetailInfo
import com.easemob.chatroom.general.constructor.RoomInfoConstructor.convertByRoomInfo
import com.easemob.chatroom.general.net.ChatroomHttpManager
import com.easemob.chatroom.general.repositories.ProfileManager
import com.easemob.chatroom.model.ChatroomViewModel
import com.easemob.chatroom.ui.RoomGiftViewDelegate
import com.easemob.chatroom.ui.RoomHandsViewDelegate
import com.easemob.chatroom.ui.RoomObservableViewDelegate
import com.easemob.config.ConfigConstants
import com.easemob.config.RouterParams
import com.easemob.config.RouterPath
import com.easemob.secnceui.widget.top.OnLiveTopClickListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener
import manager.ChatroomConfigManager
import manager.ChatroomHelper
import manager.ChatroomListener
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import tools.ValueCallBack
import tools.bean.VRUserBean
import tools.bean.VRoomBean
import tools.bean.VRoomInfoBean

@Route(path = RouterPath.ChatroomPath)
class ChatroomLiveActivity : BaseUiActivity<ActivityChatroomBinding>(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, ChatroomListener,
    RoomObservableViewDelegate.OnRoomViewDelegateListener {

    companion object {
        const val RC_PERMISSIONS = 101
    }

    /**room viewModel*/
    private lateinit var roomViewModel: ChatroomViewModel
    private lateinit var giftViewDelegate: RoomGiftViewDelegate
    private lateinit var handsDelegate: RoomHandsViewDelegate

    /**
     * 代理头部view以及麦位view
     */
    private lateinit var roomObservableDelegate: RoomObservableViewDelegate

    /**创建房间or房间内请求详情数据*/
    private var roomInfoBean: VRoomInfoBean? = null

    /**房间基础*/
    private val roomKitBean = RoomKitBean()
    private var password: String? = ""
    private var isOwner: Boolean = false

    override fun getViewBinding(inflater: LayoutInflater): ActivityChatroomBinding {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        return ActivityChatroomBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarCompat.setLightStatusBar(this, false)
        roomViewModel = ViewModelProvider(this)[ChatroomViewModel::class.java]
        giftViewDelegate = RoomGiftViewDelegate.getInstance(this, binding.chatroomGiftView, binding.svgaView)
        handsDelegate = RoomHandsViewDelegate.getInstance(this, binding.chatBottom)
        initListeners()
        initData()
        initView()
        requestAudioPermission()
    }

    private fun initData() {
        val roomBean = intent.getSerializableExtra(RouterParams.KEY_CHATROOM_INFO) as VRoomBean.RoomsBean?
        roomInfoBean = intent.getSerializableExtra(RouterParams.KEY_CHATROOM_DETAILS_INFO) as VRoomInfoBean?
        password = intent.getStringExtra(RouterParams.KEY_CHATROOM_JOIN_PASSWORD).toString()
        if (roomBean == null && roomInfoBean == null) {
            ToastTools.show(this, "roomInfo is null!")
            finish()
        } else if (roomInfoBean != null) {
            // 详情进入数据全
            roomInfoBean?.room?.let { roomDetail ->
                roomKitBean.convertByRoomDetailInfo(roomDetail)
                handsDelegate.onRoomDetails(roomDetail.room_id, roomDetail.owner.uid)
                giftViewDelegate.onRoomDetails(roomDetail.room_id, roomDetail.owner.uid)
                isOwner = (roomDetail.owner.uid == ProfileManager.getInstance().profile.uid)
            }
        } else {
            // 房间列表进入，需请求详情
            roomBean?.let { roomInfo ->
                roomKitBean.convertByRoomInfo(roomInfo)
                handsDelegate.onRoomDetails(roomBean.room_id, roomBean.ownerUid)
                roomViewModel.getDetails(this, roomKitBean.roomId)
                giftViewDelegate.onRoomDetails(roomBean.room_id, roomBean.owner.uid)
                isOwner = (roomBean.ownerUid == ProfileManager.getInstance().profile.uid)
            }
        }
        ChatroomHelper.getInstance().init(roomKitBean.chatroomId)
        ChatroomHelper.getInstance().saveWelcomeMsg(getString(R.string.chatroom_welcome),
            ProfileManager.getInstance().profile.name)
        binding.messageView.refreshSelectLast()
        ChatroomConfigManager.getInstance().setChatRoomListener(this)
    }

    private fun initListeners() {
        // 房间详情
        roomViewModel.roomDetailObservable.observe(this) { response: Resource<VRoomInfoBean> ->
            parseResource(response, object : OnResourceParseCallback<VRoomInfoBean>() {

                override fun onLoading(data: VRoomInfoBean?) {
                    super.onLoading(data)
                    showLoading(false)
                }

                override fun onSuccess(data: VRoomInfoBean?) {
                    roomInfoBean = data
                    data?.let {
                        roomObservableDelegate.onRoomDetails(it)
                        binding.chatBottom.showMicVisible(
                            RtcRoomController.get().isLocalAudioMute,
                            roomObservableDelegate.isOnMic()
                        )
                    }
                }
            })
        }
        roomViewModel.joinObservable.observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {

                override fun onSuccess(data: Boolean?) {
                  ToastTools.show(this@ChatroomLiveActivity,getString(R.string.chatroom_join_room_success))
                }

                override fun onHideLoading() {
                    super.onHideLoading()
                    dismissLoading()
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(this@ChatroomLiveActivity, message ?: getString(R.string.chatroom_join_room_failed))
                    ThreadManager.getInstance().runOnMainThreadDelay({
                        finish()
                    }, 1000)
                }
            })
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View?, insets: WindowInsetsCompat ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            "systemInset:left:${systemInset.left},top:${systemInset.top},right:${systemInset.right},bottom:${systemInset.bottom}".logE(
                "insets=="
            )
            "paddingInset:left:${binding.clMain.paddingLeft},top:${binding.clMain.paddingTop},right:${binding.clMain.paddingRight},bottom:${binding.clMain.paddingBottom}".logE(
                "insets=="
            )

            binding.clMain.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.clMain.setOnTouchListener(OnTouchListener { v, event ->
            reset()
            false
        })
        binding.messageView.setMessageViewListener(object : com.easemob.secnceui.widget.barrage.ChatroomMessagesView.MessageViewListener {
            override fun onItemClickListener(message: ChatMessageData?) {
            }

            override fun onListClickListener() {
                reset()
            }
        })
    }

    private fun initView() {
        binding.chatBottom.initMenu(roomKitBean.roomType)
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) { // 普通房间
            binding.likeView.likeView.setOnClickListener { binding.likeView.addFavor() }
            binding.chatroomGiftView.init(roomKitBean.chatroomId)
            binding.messageView.init(roomKitBean.chatroomId)
            binding.rvChatroom2dMicLayout.isVisible = true
            binding.rvChatroom3dMicLayout.isVisible = false
            roomObservableDelegate =
                RoomObservableViewDelegate(this, roomKitBean, binding.cTopView, binding.rvChatroom2dMicLayout)
            binding.rvChatroom2dMicLayout.setMyRtcUid(ProfileManager.getInstance().rtcUid())
            binding.rvChatroom2dMicLayout.onItemClickListener(
                object : OnItemClickListener<com.easemob.secnceui.bean.MicInfoBean> {
                    override fun onItemClick(data: com.easemob.secnceui.bean.MicInfoBean, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onUserMicClick(data)
                    }
                },
                object : OnItemClickListener<com.easemob.secnceui.bean.MicInfoBean> {
                    override fun onItemClick(data: com.easemob.secnceui.bean.MicInfoBean, view: View, position: Int, viewType: Long) {
                        if (roomKitBean.isOwner) {
                            roomObservableDelegate.onBotMicClick(
                                RtcRoomController.get().isUseBot,
                                getString(R.string.chatroom_open_bot_prompt)
                            )
                        }else{
                            if (!RtcRoomController.get().isUseBot) {
                                ToastTools.showTips(
                                    this@ChatroomLiveActivity,
                                    getString(com.easemob.secnceui.R.string.chatroom_only_host_can_change_robot)
                                )
                            }
                        }
                    }
                }
            ).setUpAdapter(RtcRoomController.get().isUseBot)
        } else { // 空间音效房间
            binding.likeView.isVisible = false
            binding.rvChatroom2dMicLayout.isVisible = false
            binding.rvChatroom3dMicLayout.isVisible = true
            roomObservableDelegate =
                RoomObservableViewDelegate(this, roomKitBean, binding.cTopView, binding.rvChatroom3dMicLayout)
            binding.rvChatroom3dMicLayout.setMyRtcUid(ProfileManager.getInstance().rtcUid())
            binding.rvChatroom3dMicLayout.onItemClickListener(
                object : OnItemClickListener<com.easemob.secnceui.bean.MicInfoBean> {
                    override fun onItemClick(data: com.easemob.secnceui.bean.MicInfoBean, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onUserMicClick(data)
                    }
                },
                object : OnItemClickListener<com.easemob.secnceui.bean.MicInfoBean> {
                    override fun onItemClick(data: com.easemob.secnceui.bean.MicInfoBean, view: View, position: Int, viewType: Long) {
                        if (roomKitBean.isOwner) {
                            roomObservableDelegate.onBotMicClick(
                                RtcRoomController.get().isUseBot,
                                getString(R.string.chatroom_open_bot_prompt)
                            )
                        }else{
                            if (!RtcRoomController.get().isUseBot) {
                                ToastTools.showTips(
                                    this@ChatroomLiveActivity,
                                    getString(com.easemob.secnceui.R.string.chatroom_only_host_can_change_robot)
                                )
                            }
                        }
                    }
                },
            ).setUpMicInfoMap(RtcRoomController.get().isUseBot)
        }
        binding.cTopView.setTitleMaxWidth()
        // 头部 如果是创建房间进来有详情
        roomInfoBean?.let {
            roomObservableDelegate.onRoomDetails(it)
            binding.chatBottom.showMicVisible(
                RtcRoomController.get().isLocalAudioMute,
                roomObservableDelegate.isOnMic()
            )
        }
        roomObservableDelegate.onRoomViewDelegateListener = this
        binding.cTopView.setOnLiveTopClickListener(object : OnLiveTopClickListener {
            override fun onClickBack(view: View) {

                if (roomKitBean.isOwner){
                    roomObservableDelegate.onExitRoom(
                        getString(R.string.chatroom_end_live),
                        getString(R.string.chatroom_end_live_tips), finishBack = {
                        finish()
                    })
                }else{
                    finish()
                }
            }

            override fun onClickRank(view: View) {
                roomObservableDelegate.onClickRank()
            }

            override fun onClickNotice(view: View) {
                roomObservableDelegate.onClickNotice(
                    roomInfoBean?.room?.announcement
                        ?: getString(R.string.chatroom_first_enter_room_notice_tips)
                )
            }

            override fun onClickSoundSocial(view: View) {
                roomObservableDelegate.onClickSoundSocial(
                    roomInfoBean?.room?.soundSelection ?: ConfigConstants.SoundSelection.Social_Chat, finishBack = {
                        finish()
                    })
            }
        })
        binding.chatBottom.setMenuItemOnClickListener(object :
            com.easemob.secnceui.widget.primary.MenuItemClickListener {
            override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                when (itemId) {
                    com.easemob.secnceui.R.id.extend_item_eq -> {
                        roomObservableDelegate.onAudioSettingsDialog(finishBack = {
                            finish()
                        })
                    }
                    com.easemob.secnceui.R.id.extend_item_mic -> {
                        if (roomObservableDelegate.mySelfMicStatus() == com.easemob.secnceui.annotation.MicStatus.ForceMute){
                            // 被禁言
                            ToastTools.show(this@ChatroomLiveActivity,getString(R.string.chatroom_mic_muted_by_host))
                            return
                        }
                        if (RtcRoomController.get().isLocalAudioMute) {
                            binding.chatBottom.setEnableMic(true)
                            roomObservableDelegate.muteLocalAudio(false)
                        } else {
                            binding.chatBottom.setEnableMic(false)
                            roomObservableDelegate.muteLocalAudio(true)
                        }
                    }
                    com.easemob.secnceui.R.id.extend_item_hand_up -> {
                        "extend_item_hand_up isOwner:${handsDelegate.isOwner}".logE("onChatExtendMenuItemClick")
                        if (handsDelegate.isOwner) {
                            if (this@ChatroomLiveActivity::handsDelegate.isInitialized) {
                                handsDelegate.showOwnerHandsDialog()
                                binding.chatBottom.setShowHandStatus(true, false)
                            }
                        } else {
                            if (this@ChatroomLiveActivity::handsDelegate.isInitialized) {
                                handsDelegate.showMemberHandsDialog(-1)
                            }
                        }
                    }
                    com.easemob.secnceui.R.id.extend_item_gift -> {
                        giftViewDelegate.showGiftDialog(object : OnMsgCallBack() {
                            override fun onSuccess(message: ChatMessageData?) {
                                if (this@ChatroomLiveActivity::roomObservableDelegate.isInitialized) {
                                    roomObservableDelegate.receiveGift(roomKitBean.roomId)
                                }
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                ToastTools.show(this@ChatroomLiveActivity,getString(R.string.chatroom_send_gift_fail))
                            }
                        })
                    }
                }
            }

            override fun onInputViewFocusChange(focus: Boolean) {

            }

            override fun onInputLayoutClick() {
                checkFocus(false)
            }

            override fun onEmojiClick(isShow: Boolean) {

            }

            override fun onSendMessage(content: String?) {
                if (!content.isNullOrEmpty()){
                    ChatroomHelper.getInstance().sendTxtMsg(content,
                        ProfileManager.getInstance().profile.name, object : OnMsgCallBack() {
                            override fun onSuccess(message: ChatMessageData?) {
                                ThreadManager.getInstance().runOnMainThread {
                                    binding.messageView.refreshSelectLast()
                                    binding.likeView.isVisible = true
                                }
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                Log.e("send error", " $code $error")
                            }
                        })
                }
            }
        })
    }

    override fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    currentFocus?.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        EMClient.getInstance().chatroomManager().leaveChatRoom(roomKitBean.chatroomId)
        binding.chatroomGiftView.clear()
        RtcRoomController.get().destroy()
        ChatroomConfigManager.getInstance().removeChatRoomListener(this)
        roomViewModel.leaveRoom(this, roomKitBean.roomId)
        super.finish()
    }

    @AfterPermissionGranted(RC_PERMISSIONS)
    private fun requestAudioPermission() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            onPermissionGrant()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_PERMISSIONS, *perms).build())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun onPermissionGrant() {
        "onPermissionGrant initSdkJoin".logE()
        roomViewModel.initSdkJoin(roomKitBean, password)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        "onPermissionsGranted requestCode$requestCode $perms".logE()
        onPermissionGrant()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        "onPermissionsDenied $perms ".logE()
    }

    override fun onRationaleAccepted(requestCode: Int) {
        "onRationaleAccepted requestCode$requestCode ".logE()
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onRationaleDenied(requestCode: Int) {
        "onRationaleDenied requestCode$requestCode ".logE()
    }


    private fun reset() {
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom){
            binding.chatBottom.hideExpressionView(false)
            hideKeyboard()
            binding.chatBottom.showInput()
            binding.likeView.isVisible = true
            binding.chatBottom.hindViewChangeIcon()
        }
    }

    override fun receiveTextMessage(roomId: String?, message: ChatMessageData?) {
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        binding.messageView.refreshSelectLast()
    }

    override fun receiveGift(roomId: String?, message: ChatMessageData?) {
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        binding.chatroomGiftView.refresh()
        if (CustomMsgHelper.getInstance().getMsgGiftId(message).equals("VoiceRoomGift9")) {
            giftViewDelegate.showGiftAction()
        }
        if (this@ChatroomLiveActivity::roomObservableDelegate.isInitialized) {
            roomObservableDelegate.receiveGift(roomKitBean.roomId)
        }
    }

    override fun receiveApplySite(roomId: String?, message: ChatMessageData?) {
        Log.e("liveActivity", "receiveApplySite $isOwner")
        binding.chatBottom.setShowHandStatus(isOwner, true)
    }

    override fun announcementChanged(roomId: String?, announcement: String?) {
        super.announcementChanged(roomId, announcement)
        "announcementChanged roomId:$roomId  announcement:$announcement".logE("announcementChanged")
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        roomInfoBean?.room?.announcement = announcement
    }

    override fun roomAttributesDidUpdated(roomId: String?, attributeMap: MutableMap<String, String>?, fromId: String?) {
        super.roomAttributesDidUpdated(roomId, attributeMap, fromId)
        "roomAttributesDidUpdated currentThread:${Thread.currentThread()} roomId:$roomId  fromId:$fromId attributeMap:$attributeMap".logE(
            "roomAttributesDid"
        )
        if (isFinishing) return
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        attributeMap?.let {
            val micInfoMap = RoomInfoConstructor.convertAttr2MicInfoMap(it)
            val newMicMap = RoomInfoConstructor.convertMicInfoMap2UiBean(micInfoMap, roomKitBean.ownerId)
            val handsCheckMap = mutableMapOf<String,String>()
            micInfoMap.forEach { (t, u) ->
                handsCheckMap[t] = u.member?.uid ?: ""
            }
            ThreadManager.getInstance().runOnMainThread {
                if (isOwner){
                    handsDelegate.check(handsCheckMap)
                }
                roomObservableDelegate.onUpdateMicMap(newMicMap)
                if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) { // 普通房间
                    binding.rvChatroom2dMicLayout.receiverAttributeMap(newMicMap)
                } else {
                    binding.rvChatroom3dMicLayout.receiverAttributeMap(newMicMap)
                }
                binding.chatBottom.showMicVisible(
                    RtcRoomController.get().isLocalAudioMute,
                    roomObservableDelegate.isOnMic()
                )
                if (!isOwner) {
                    Log.e("liveActivity", "roomAttributesDidUpdated:  ${roomObservableDelegate.isOnMic()}")
                    binding.chatBottom.setEnableHand(roomObservableDelegate.isOnMic())
                    handsDelegate.resetRequest()
                }
            }
        }
    }

    //接收取消申请上麦
    override fun receiveCancelApplySite(roomId: String?, message: ChatMessageData?) {
        Log.e("ChatroomLiveActivity","receiveCancelApplySite" + message.toString())
        ThreadManager.getInstance().runOnMainThread{
            //刷新 owner 申请列表
            handsDelegate.update(0)
        }
    }

    //接收拒绝邀请消息
    override fun receiveInviteRefusedSite(roomId: String?, message: ChatMessageData?) {
        Log.e("ChatroomLiveActivity","receiveInviteRefusedSite" + message.toString())
        ToastTools.show(this, getString(R.string.chatroom_mic_audience_rejected_invitation, ""))
    }

    private fun checkFocus(focus:Boolean){
        binding.likeView.isVisible = focus
    }

    override fun roomAttributesDidRemoved(roomId: String?, keyList: List<String>?, fromId: String?) {
        super.roomAttributesDidRemoved(roomId, keyList, fromId)
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        "roomAttributesDidRemoved roomId:$roomId  fromId:$fromId keyList:$keyList".logE("roomAttributesDid")
    }

    override fun onTokenWillExpire() {
        ChatroomHttpManager.getInstance().loginWithToken(
            EMClient.getInstance().deviceInfo.getString("deviceid"),
            ProfileManager.getInstance().profile.portrait, object : ValueCallBack<VRUserBean> {
                override fun onSuccess(bean: VRUserBean?) {
                    "onSuccess: chat_uid: ${bean?.chat_uid} im_token: ${bean?.im_token}".logE("onTokenWillExpire")
                    ChatroomHelper.getInstance().renewToken(bean?.im_token)
                }

                override fun onError(code: Int, desc: String?) {
                    "onError: $code  desc: $desc".logE("onTokenWillExpire")
                }

            })
    }

    //接收拒绝申请消息(目前暂无拒绝申请)
    override fun receiveDeclineApply(roomId: String?, message: ChatMessageData?) {
        super.receiveDeclineApply(roomId, message)
        Log.e("ChatroomLiveActivity","receiveDeclineApply" + message.toString())
        ToastTools.show(this, getString(R.string.chatroom_mic_audience_rejected_invitation, ""))
    }

    //接收邀请消息
    override fun receiveInviteSite(roomId: String?, message: ChatMessageData?) {
        super.receiveInviteSite(roomId, message)
        roomObservableDelegate.receiveInviteSite(roomKitBean.roomId, -1)
    }

    override fun receiveSystem(roomId: String?, message: ChatMessageData?) {
        super.receiveSystem(roomId, message)
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        val ext: MutableMap<String, String>? = CustomMsgHelper.getInstance().getCustomMsgParams(message)
        "ext: $ext ${Thread.currentThread()}".logE("receiveSystem")
        ext?.let {
            roomObservableDelegate.receiveSystem(ext)
        }
        binding.messageView.refreshSelectLast()
    }

    override fun voiceRoomUpdateRobotVolume(roomId: String?, volume: String?) {
        super.voiceRoomUpdateRobotVolume(roomId, volume)
        "voiceRoomUpdateRobotVolume roomId:$roomId,volume:$volume".logE()
        if (TextUtils.equals(roomId,roomKitBean.chatroomId)){
            RtcRoomController.get().botVolume = volume?.toInt() ?: ConfigConstants.RotDefaultVolume
        }
    }

    override fun onInvitation(micIndex: Int) {
        if (this@ChatroomLiveActivity::handsDelegate.isInitialized) {
            handsDelegate.showOwnerHandsDialog()
            binding.chatBottom.setShowHandStatus(true, false)
        }
    }

    override fun onUserClickOnStage(micIndex: Int) {
        if (this@ChatroomLiveActivity::handsDelegate.isInitialized) {
            handsDelegate.onUserClickOnStage(micIndex)
        }
    }

    override fun onMemberExited(roomId: String?, s1: String?, s2: String?) {
        super.onMemberExited(roomId, s1, s2)
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        if (this@ChatroomLiveActivity::roomObservableDelegate.isInitialized) {
            roomObservableDelegate.subMemberCount()
        }
    }

    override fun userBeKicked(roomId: String?, reason: Int) {
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        Log.e("ChatroomLiveActivity", "userBeKicked: $reason")
        if(reason == EMAChatRoomManagerListener.DESTROYED) {
            ToastTools.show(this,getString(R.string.chatroom_close), Toast.LENGTH_SHORT)
            finish()
        }else if(reason == EMAChatRoomManagerListener.BE_KICKED){
            ToastTools.show(this,getString(R.string.chatroom_kick_member), Toast.LENGTH_SHORT)
            finish()
        }
    }

    override fun onRoomDestroyed(roomId: String?) {
        if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
        Log.e("ChatroomLiveActivity","onRoomDestroyed: ")
        ToastTools.show(this,getString(R.string.chatroom_close), Toast.LENGTH_SHORT)
        finish()
    }
}