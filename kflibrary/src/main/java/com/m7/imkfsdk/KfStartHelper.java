package com.m7.imkfsdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.m7.imkfsdk.chat.ChatActivity;
import com.m7.imkfsdk.chat.LoadingFragmentDialog;
import com.m7.imkfsdk.constant.Constants;
import com.m7.imkfsdk.utils.ToastUtils;
import com.moor.imkf.GetGlobleConfigListen;
import com.moor.imkf.GetPeersListener;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;
import com.moor.imkf.model.entity.CardInfo;
import com.moor.imkf.model.entity.Peer;
import com.moor.imkf.model.entity.ScheduleConfig;
import com.moor.imkf.utils.LogUtils;
import com.moor.imkf.utils.Utils;

import java.util.List;

/**
 * Created by pangw on 2018/7/9.
 */

public class KfStartHelper {
    private LoadingFragmentDialog loadingDialog;
    private CardInfo card;
    private Activity mActivity;
    private Context context;
    private String receiverAction;
    private String accessId;
    private String userName;
    private String userId;
    public static String avatar;
    public static String guestbookName;
    public static String guestbookMobile;

    public void setCard(CardInfo card) {
        this.card = card;
    }

    public KfStartHelper(Activity activity) {
        mActivity = activity;
        context = activity.getApplicationContext();
        loadingDialog = new LoadingFragmentDialog();
        loadingDialog.setMsg("客服程序正在启动...");
        initFaceUtils();
    }

    /**
     * 初始化表情
     */
    public void initFaceUtils() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.m7.imkfsdk.utils.FaceConversionUtil.getInstace().getFileText(
                        context);
            }
        }).start();
    }

    public void initSdkChat(String accessId, String userName,
                            String userId) {
        initSdkChat("com.mozi.smart.KEFU_NEW_MSG", accessId, userName, userId);
    }

    /**
     * 设置用户头像
     *
     * @param avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static String getGuestbookName() {
        return guestbookName;
    }

    /**
     * 设置留言名字
     *
     * @param guestbookName
     */
    public static void setGuestbookName(String guestbookName) {
        KfStartHelper.guestbookName = guestbookName;
    }

    public static String getGuestbookMobile() {
        return guestbookMobile;
    }

    /**
     * 设置留言电话
     *
     * @param guestbookMobile
     */
    public static void setGuestbookMobile(String guestbookMobile) {
        KfStartHelper.guestbookMobile = guestbookMobile;
    }

    private void initSdkChat(String receiverAction, String accessId, String userName,
                             String userId) {
        this.receiverAction = receiverAction;
        this.accessId = accessId;
        this.userName = userName;
        this.userId = userId;

        if (!Utils.isNetWorkConnected(context)) {
            Toast.makeText(context, R.string.notnetwork, Toast.LENGTH_SHORT).show();
            return;
        }
        loadingDialog.show(mActivity.getFragmentManager(), "");
        if (IMChatManager.isKFSDK) {
            getIsGoSchedule();
        } else {
            startKFService();
        }
    }


    private void getIsGoSchedule() {
        IMChatManager.getInstance().getWebchatScheduleConfig(new GetGlobleConfigListen() {
            @Override
            public void getSchedule(ScheduleConfig sc) {
                loadingDialog.dismiss();
                LogUtils.aTag("MainActivity", "日程");
                if (!sc.getScheduleId().equals("") && !sc.getProcessId().equals("") && sc.getEntranceNode() != null && sc.getLeavemsgNodes() != null) {
                    if (sc.getEntranceNode().getEntrances().size() > 0) {
                        if (sc.getEntranceNode().getEntrances().size() == 1) {
                            ScheduleConfig.EntranceNodeBean.EntrancesBean bean = sc.getEntranceNode().getEntrances().get(0);
                            ChatActivity.startActivity(context, Constants.TYPE_SCHEDULE, sc.getScheduleId(), sc.getProcessId(), bean.getProcessTo(), bean.getProcessType(), bean.get_id());
                        } else {
                            startScheduleDialog(sc.getEntranceNode().getEntrances(), sc.getScheduleId(), sc.getProcessId());
                        }

                    } else {
                        ToastUtils.showShort(R.string.sorryconfigurationiswrong);
                    }
                } else {
                    ToastUtils.showShort(R.string.sorryconfigurationiswrong);
                }
            }

            @Override
            public void getPeers() {
                LogUtils.aTag("start", "技能组");
                startChatActivityForPeer();
            }
        });
    }

    private void startScheduleDialog(final List<ScheduleConfig.EntranceNodeBean.EntrancesBean> entrances, final String scheduleId, final String processId) {
        final String[] items = new String[entrances.size()];
        for (int i = 0; i < entrances.size(); i++) {
            items[i] = entrances.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.Theme_AppCompat_Light_Dialog);
        builder.setTitle("选择日程");
        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ScheduleConfig.EntranceNodeBean.EntrancesBean bean = entrances.get(which);
                LogUtils.aTag("选择日程：", bean.getName());
                ChatActivity.startActivity(context, Constants.TYPE_SCHEDULE, scheduleId, processId, bean.getProcessTo(), bean.getProcessType(), bean.get_id());

            }
        });
        builder.create().show();
    }

    private void startChatActivityForPeer() {
        IMChatManager.getInstance().getPeers(new GetPeersListener() {
            @Override
            public void onSuccess(List<Peer> peers) {

                if (peers.size() > 1) {
                    startPeersDialog(peers, card);
                } else if (peers.size() == 1) {
                    ChatActivity.startActivity(context, Constants.TYPE_PEER, peers.get(0).getId(), card);
                } else {
                    ToastUtils.showShort(R.string.peer_no_number);
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailed() {
                loadingDialog.dismiss();
            }
        });
    }

    private void startKFService() {
        new Thread() {
            @Override
            public void run() {
                IMChatManager.getInstance().setOnInitListener(new InitListener() {
                    @Override
                    public void oninitSuccess() {
                        IMChatManager.isKFSDK = true;
                        getIsGoSchedule();
                        Log.d("MainActivity", "sdk初始化成功");
                    }

                    @Override
                    public void onInitFailed() {
                        IMChatManager.isKFSDK = false;
                        ToastUtils.showShort(R.string.sdkinitwrong);
                        Log.d("MainActivity", "sdk初始化失败, 请填写正确的accessid");
                    }
                });
                IMChatManager.getInstance().init(context, receiverAction, accessId, userName, userId);
            }
        }.start();
    }

    public void startPeersDialog(final List<Peer> peers, final CardInfo mCardInfo) {
        final String[] items = new String[peers.size()];
        for (int i = 0; i < peers.size(); i++) {
            items[i] = peers.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.Theme_AppCompat_Light_Dialog);
        builder.setTitle("选择技能组");
        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Peer peer = peers.get(which);
                LogUtils.aTag("选择技能组：", peer.getName());
                ChatActivity.startActivity(context, Constants.TYPE_PEER, peer.getId(), mCardInfo);
            }
        });
        builder.create().show();
    }
}
