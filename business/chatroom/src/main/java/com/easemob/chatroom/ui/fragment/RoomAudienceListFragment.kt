package com.easemob.chatroom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.easemob.buddy.tool.*
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.easemob.baseui.BaseUiFragment
import com.easemob.baseui.adapter.BaseRecyclerViewAdapter
import com.easemob.baseui.adapter.OnItemChildClickListener
import com.easemob.baseui.general.callback.OnResourceParseCallback
import com.easemob.baseui.general.net.Resource
import com.easemob.chatroom.R
import com.easemob.chatroom.ui.adapter.RoomAudienceListViewHolder
import com.easemob.chatroom.bean.RoomKitBean
import com.easemob.chatroom.databinding.FragmentChatroomAudienceListBinding
import com.easemob.chatroom.databinding.ItemChatroomAudienceListBinding
import com.easemob.chatroom.general.net.ChatroomHttpManager
import com.easemob.chatroom.model.RoomRankViewModel
import tools.ValueCallBack
import tools.bean.VMemberBean
import tools.bean.VRoomUserBean

class RoomAudienceListFragment : BaseUiFragment<FragmentChatroomAudienceListBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    companion object {

        private const val KEY_ROOM_INFO = "room_info"

        fun getInstance(roomKitBean: RoomKitBean): RoomAudienceListFragment {
            return RoomAudienceListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomKitBean)
                }
            }
        }
    }

    private var roomKitBean: RoomKitBean? = null

    private lateinit var roomRankViewModel: RoomRankViewModel

    private var pageSize = 10
    private var cursor = ""
    private var total = 0
    private var isEnd = false
    private val members = mutableListOf<VMemberBean>()

    private var audienceAdapter: BaseRecyclerViewAdapter<ItemChatroomAudienceListBinding, VMemberBean, RoomAudienceListViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChatroomAudienceListBinding {
        return FragmentChatroomAudienceListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel =
            ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[RoomRankViewModel::class.java]
        arguments?.apply {
            roomKitBean = getSerializable(KEY_ROOM_INFO) as RoomKitBean?
            roomKitBean?.let {
                roomRankViewModel.getMembers(requireContext(), it.roomId, pageSize, cursor)
            }
        }
        binding?.apply {
            initAdapter(rvAudienceList)
            slAudienceList.setOnRefreshListener(this@RoomAudienceListFragment)
        }
        roomRankViewModel.membersObservable().observe(requireActivity()) { response: Resource<VRoomUserBean> ->
            parseResource(response, object : OnResourceParseCallback<VRoomUserBean>() {
                override fun onSuccess(data: VRoomUserBean?) {
                    binding?.slAudienceList?.isRefreshing = false
                    "getMembers cursor：${data?.cursor}，total：${data?.total}".logE()
                    if (data == null) return
                    cursor = data.cursor ?: ""
                    total = data.total
                    checkEmpty()
                    if (!data.members.isNullOrEmpty()) {
                        if (data.members.size < pageSize) {
                            isEnd = true
                        }
                        audienceAdapter?.addItems(data.members)
                    } else {
                        isEnd = true
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.slAudienceList?.isRefreshing = false
                }
            })
        }
    }

    private fun checkEmpty() {
        binding?.apply {
            if (total == 0) {
                ivContributionEmpty.isVisible = true
                mtContributionEmpty.isVisible = true
            } else {
                ivContributionEmpty.isVisible = false
                mtContributionEmpty.isVisible = false
            }
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        audienceAdapter =
            BaseRecyclerViewAdapter(
                members,
                null,
                object : OnItemChildClickListener<VMemberBean> {
                    override fun onItemChildClick(
                        data: VMemberBean?,
                        extData: Any?,
                        view: View,
                        position: Int,
                        itemViewType: Long
                    ) {
                        if (extData is Int) {
                            handleRequest(roomKitBean?.roomId, data?.uid, extData)
                        }
                    }
                },
                RoomAudienceListViewHolder::class.java
            )

        recyclerView.layoutManager = LinearLayoutManager(context)
        context?.let {
            recyclerView.addItemDecoration(
                MaterialDividerItemDecoration(it, MaterialDividerItemDecoration.VERTICAL).apply {
                    dividerThickness = 1.dp.toInt()
                    dividerInsetStart = 15.dp.toInt()
                    dividerInsetEnd = 15.dp.toInt()
                    dividerColor = ResourcesTools.getColor(it.resources, com.easemob.secnceui.R.color.divider_color_1F979797)
                }
            )
        }
        recyclerView.adapter = audienceAdapter
    }

    override fun onRefresh() {
        if (isEnd || cursor.isEmpty()) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                binding?.slAudienceList?.isRefreshing = false
            }, 1500)
        } else {
            roomKitBean?.let {
                roomRankViewModel.getMembers(requireContext(), it.roomId, pageSize, cursor)
            }
        }
    }

    private fun handleRequest(roomId: String?, uid: String?, @com.easemob.secnceui.annotation.MicClickAction action: Int) {
        if (roomId.isNullOrEmpty() || uid.isNullOrEmpty()) return
        context?.let { parentContext ->
            if (action == com.easemob.secnceui.annotation.MicClickAction.Invite) {
                ChatroomHttpManager.getInstance().invitationMic(roomId, uid, object : ValueCallBack<Boolean> {
                    override fun onSuccess(var1: Boolean?) {
                        if (var1 != true) return
                        CoroutineUtil.execMain {
                            activity?.let {
                                ToastTools.show(it, it.getString(R.string.chatroom_host_invitation_sent))
                            }
                        }
                    }

                    override fun onError(var1: Int, var2: String?) {

                    }
                })
            } else if (action == com.easemob.secnceui.annotation.MicClickAction.KickOff) {
                ChatroomHttpManager.getInstance()
                    .kickMic(roomId, uid, -1, object : ValueCallBack<Boolean> {
                        override fun onSuccess(var1: Boolean?) {
                            if (var1 != true) return
                            activity?.let {
                                ToastTools.show(it, "kickMic success")
                            }
                        }

                        override fun onError(var1: Int, var2: String?) {
                            activity?.let {
                                ToastTools.show(it, "kickMic onError $var1 $var2")
                            }
                        }
                    })
            }
        }


    }
}