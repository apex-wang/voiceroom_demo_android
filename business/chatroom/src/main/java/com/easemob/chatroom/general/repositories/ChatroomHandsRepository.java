package com.easemob.chatroom.general.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.easemob.chatroom.general.net.ChatroomHttpManager;

import com.easemob.baseui.general.callback.ResultCallBack;
import com.easemob.baseui.general.net.Resource;
import tools.ValueCallBack;
import tools.bean.VRGiftBean;
import tools.bean.VRMicListBean;
import tools.bean.VRoomUserBean;

public class ChatroomHandsRepository extends BaseRepository{

    public LiveData<Resource<VRMicListBean>> getRaisedList(Context context, String roomId, int pageSize, String cursor) {
        return new NetworkOnlyResource<VRMicListBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRMicListBean>> callBack) {
                ChatroomHttpManager.getInstance().getApplyMicList(roomId, pageSize, cursor, new ValueCallBack<VRMicListBean>() {
                    @Override
                    public void onSuccess(VRMicListBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<VRoomUserBean>> getInvitedList(Context context, String roomId, int pageSize, String cursor) {
        return new NetworkOnlyResource<VRoomUserBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRoomUserBean>> callBack) {
                ChatroomHttpManager.getInstance().getRoomMembers(roomId, pageSize, cursor, new ValueCallBack<VRoomUserBean>() {
                    @Override
                    public void onSuccess(VRoomUserBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 礼物榜单
     */
    public LiveData<Resource<VRGiftBean>> getGifts(Context context, String roomId) {
        return new NetworkOnlyResource<VRGiftBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRGiftBean>> callBack) {
                ChatroomHttpManager.getInstance().getGiftList(roomId, new ValueCallBack<VRGiftBean>() {
                    @Override
                    public void onSuccess(VRGiftBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }
}
