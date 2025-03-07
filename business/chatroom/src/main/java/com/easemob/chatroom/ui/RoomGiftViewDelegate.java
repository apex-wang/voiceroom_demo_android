package com.easemob.chatroom.ui;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;

import com.easemob.chatroom.general.net.ChatroomHttpManager;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import bean.ChatMessageData;
import custorm.CustomMsgHelper;
import custorm.OnMsgCallBack;
import com.easemob.buddy.tool.ThreadManager;

import com.easemob.chatroom.general.repositories.ProfileManager;
import com.easemob.secnceui.bean.GiftBean;
import com.easemob.secnceui.widget.gift.ChatroomGiftView;
import com.easemob.secnceui.widget.gift.GiftBottomDialog;
import com.easemob.secnceui.widget.gift.OnSendClickListener;
import tools.ValueCallBack;

public class RoomGiftViewDelegate {
   private FragmentActivity activity;
   private GiftBottomDialog dialog;
   private int time = 2;
   private int Animation_time = 3;
   private TextView send;
   private ChatroomGiftView giftView;
   private String roomId;
   private String owner;
   private SVGAImageView svgaImageView;

   RoomGiftViewDelegate(FragmentActivity activity, ChatroomGiftView giftView,SVGAImageView svgaImageView){
      this.activity = activity;
      this.giftView = giftView;
      this.svgaImageView = svgaImageView;
   }

   public static RoomGiftViewDelegate getInstance(FragmentActivity activity, ChatroomGiftView giftView,SVGAImageView svgaImageView){
      return new RoomGiftViewDelegate(activity,giftView,svgaImageView);
   }

   public void onRoomDetails(String roomId,String owner){
      this.roomId = roomId;
      this.owner = owner;
      Log.e("onRoomDetails","owner: " + owner);
      Log.e("onRoomDetails","getUid: " + ProfileManager.getInstance().getProfile().getUid());
   }


   public void showGiftDialog(OnMsgCallBack msgCallBack) {
      if (activity != null){
         dialog = (GiftBottomDialog) activity.getSupportFragmentManager().findFragmentByTag("live_gift");
         if(dialog == null) {
            dialog = GiftBottomDialog.getNewInstance();
         }
         dialog.show(activity.getSupportFragmentManager(), "live_gift");
         dialog.setOnConfirmClickListener(new OnSendClickListener() {
            @Override
            public void SendGift(View view, Object bean) {
               dialog.setSendEnable(false);
               GiftBean giftBean = (GiftBean) bean;
               ChatroomHttpManager.getInstance().sendGift(roomId,
                       giftBean.getId(), giftBean.getNum(), 0, new ValueCallBack<Boolean>() {
                          @Override
                          public void onSuccess(Boolean var1) {
                             Log.e("sendGift","Successfully reported");
                             CustomMsgHelper.getInstance().sendGiftMsg(
                                     ProfileManager.getInstance().getProfile().getName(),
                                     ProfileManager.getInstance().getProfile().getPortrait(),
                                     giftBean.getId(), giftBean.getNum(),giftBean.getPrice(),giftBean.getName(),
                                     new OnMsgCallBack() {
                                        @Override
                                        public void onSuccess(ChatMessageData message) {
                                           Log.e("MenuItemClick",  "item_gift_onSuccess");
                                           ThreadManager.getInstance().runOnMainThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                 if (activity.isDestroyed()){
                                                    return;
                                                 }
                                                 giftView.refresh();
                                                 if (view instanceof TextView){
                                                    send = ((TextView) view);
                                                    send.setText(time +"s");
                                                    send.setEnabled(false);
                                                    startTask();
                                                 }
                                                 if (giftBean.getId().equals("VoiceRoomGift9")){
                                                    showGiftAction();
                                                    dialog.dismiss();
                                                 }
                                              }
                                           });
                                           if (msgCallBack != null) {
                                              msgCallBack.onSuccess(message);
                                           }
                                        }
                                        @Override
                                        public void onError(String messageId, int code, String error) {
                                           super.onError(messageId, code, error);
                                           dialog.dismiss();
                                           if (msgCallBack != null) {
                                              msgCallBack.onError(messageId, code, error);
                                           }
                                        }
                                     });
                          }

                          @Override
                          public void onError(int code, String desc) {
                             Log.e("sendGift","Reporting failed: " + code + " "+ desc);
                          }
                       });
            }
         });
      }
   }

   private Handler handler = new Handler();
   private Runnable task;
   private Runnable showTask;

   // 开启倒计时任务
   private void startTask() {
      handler.postDelayed(task = new Runnable() {
         @Override
         public void run() {
            // 在这里执行具体的任务
            time--;
            send.setText(time+"s");
            // 任务执行完后再次调用postDelayed开启下一次任务
            if (time==0){
               stopTask();
               send.setEnabled(true);
               send.setText("Send");
            }else {
               handler.postDelayed(this, 1000);
            }
         }
      }, 1000);
   }

   // 停止计时任务
   private void stopTask() {
      if (task != null) {
         handler.removeCallbacks(task);
         task = null;
         time = 2;
         dialog.setSendEnable(true);
      }
   }

   public void showGiftAction(){
      String name = "animation_of_rocket.svga";
      SVGAParser svgaParser = SVGAParser.Companion.shareParser();
//      svgaParser.setFrameSize(100, 100);
      svgaParser.decodeFromAssets(name, new SVGAParser.ParseCompletion() {
         @Override
         public void onComplete(@NotNull SVGAVideoEntity videoItem) {
            Log.e("zzzz", "onComplete: ");
            svgaImageView.setVideoItem(videoItem);
            svgaImageView.stepToFrame(0, true);
            startAnimationTask();
         }

         @Override
         public void onError() {
            Log.e("zzzz", "onComplete: ");
         }

      }, null);
   }

   public void startAnimationTask(){
      handler.postDelayed(showTask = new Runnable() {
         @Override
         public void run() {
            // 在这里执行具体的任务
            Animation_time--;
            Log.e("startActionTask","Animation_time: " + Animation_time);
            // 任务执行完后再次调用postDelayed开启下一次任务
            if (Animation_time==0){
               stopActionTask();
               Log.e("startActionTask","isAnimating: " + svgaImageView.isAnimating());
               if (svgaImageView.isAnimating()){
                  svgaImageView.stopAnimation(true);
               }
            }else {
               handler.postDelayed(this, 1000);
            }
         }
      }, 1000);
   }

   // 停止计时任务
   private void stopActionTask() {
      if (showTask != null) {
         handler.removeCallbacks(showTask);
         showTask = null;
         Animation_time = 3;
      }
   }

}
