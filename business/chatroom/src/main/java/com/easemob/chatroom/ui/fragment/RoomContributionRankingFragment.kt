package com.easemob.chatroom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.easemob.baseui.BaseUiFragment
import com.easemob.baseui.adapter.BaseRecyclerViewAdapter
import com.easemob.baseui.general.callback.OnResourceParseCallback
import com.easemob.baseui.general.net.Resource
import com.easemob.buddy.tool.ResourcesTools
import com.easemob.buddy.tool.ThreadManager
import com.easemob.buddy.tool.dp
import com.easemob.buddy.tool.logE
import com.easemob.chatroom.R
import com.easemob.chatroom.ui.adapter.RoomContributionRankingViewHolder
import com.easemob.chatroom.bean.RoomKitBean
import com.easemob.chatroom.databinding.FragmentChatroomContributionRankingBinding
import com.easemob.chatroom.databinding.ItemChatroomContributionRankingBinding
import com.easemob.chatroom.model.RoomRankViewModel
import com.easemob.chatroom.model.RoomRankViewModelFactory
import tools.bean.VRGiftBean
import tools.bean.VRankingMemberBean

class RoomContributionRankingFragment : BaseUiFragment<FragmentChatroomContributionRankingBinding>(),
    OnRefreshListener {

    companion object {
        private const val KEY_ROOM_INFO = "room_info"

        fun getInstance(roomKitBean: RoomKitBean): RoomContributionRankingFragment {
            return RoomContributionRankingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomKitBean)
                }
            }
        }
    }

    private var roomKitBean: RoomKitBean? = null
    private var total = 0
    private var isEnd = false

    private lateinit var roomRankViewModel: RoomRankViewModel

    private var contributionAdapter: BaseRecyclerViewAdapter<ItemChatroomContributionRankingBinding, VRankingMemberBean, RoomContributionRankingViewHolder>? =
        null


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatroomContributionRankingBinding {
        return FragmentChatroomContributionRankingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomRankViewModel = ViewModelProvider(this, RoomRankViewModelFactory())[RoomRankViewModel::class.java]

        arguments?.apply {
            roomKitBean = getSerializable(KEY_ROOM_INFO) as RoomKitBean?
            roomKitBean?.let {
                roomRankViewModel.getGifts(requireContext(), it.roomId)
            }
        }
        binding?.apply {
            initAdapter(rvContributionRanking)
            slContributionRanking.setOnRefreshListener(this@RoomContributionRankingFragment)
        }
        roomRankViewModel.giftsObservable().observe(requireActivity()) { response: Resource<VRGiftBean> ->
            parseResource(response, object : OnResourceParseCallback<VRGiftBean>() {
                override fun onSuccess(data: VRGiftBean?) {
                    binding?.slContributionRanking?.isRefreshing = false
                    total = data?.ranking_list?.size ?: 0
                    "getGifts total：${total}".logE()
                    if (data == null) return
                    isEnd = true
                    checkEmpty()
                    if (!data.ranking_list.isNullOrEmpty()) {
                        contributionAdapter?.submitListAndPurge(data.ranking_list)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding?.slContributionRanking?.isRefreshing = false
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
        contributionAdapter =
            BaseRecyclerViewAdapter(null, null, RoomContributionRankingViewHolder::class.java)

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
        recyclerView.adapter = contributionAdapter
    }

    override fun onRefresh() {
        if (isEnd) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                binding?.slContributionRanking?.isRefreshing = false
            }, 1500)
        } else {
            roomKitBean?.let {
                roomRankViewModel.getGifts(requireContext(), it.roomId)
            }
        }
    }
}